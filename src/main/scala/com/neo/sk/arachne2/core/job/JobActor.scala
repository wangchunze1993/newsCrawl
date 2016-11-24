package com.neo.sk.arachne2.core.job

import java.io.File

import akka.actor.{Actor, Props, Terminated}
import com.github.nscala_time.time.Imports.DateTime
import com.neo.sk.arachne2.ActorManager
import com.neo.sk.arachne2.common._
import com.neo.sk.arachne2.core.HttpFetchResult
import com.neo.sk.arachne2.core.statistic.RemoteActorManager
import com.neo.sk.arachne2.core.url.UrlPoolManager
import com.neo.sk.arachne2.utils.http.{HttpClientUtil, InputStreamUtils}
import org.apache.http.HttpStatus
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.io.Source
/**
 * Created by 王春泽 on 2016/4/11.
 */

class JobActor(jobConf:JobConfig) extends Actor{

  /**
   * 具体job Actor
   */
  import com.neo.sk.arachne2.ActorManager.myExecutionContext

  private final val logger = LoggerFactory.getLogger(getClass)
  private val jobName = jobConf.jobName
  private val jobId = jobConf.jobId
  private val dataType = jobConf.dataType
  private val extractor = jobConf.getExtractor.get
  private val urlPoolActor = context.actorOf(Props[UrlPoolManager](new UrlPoolManager(jobConf)),s"UrlPoolManager-$jobName-$jobId")
  private final val jobPath = System.getProperty("user.dir") + "/jobs/" + jobName + "/"
  private final val jobDir = new File(jobPath)
  if(!jobDir.exists()){
    jobDir.mkdirs()
  }
  private var userClient: CloseableHttpClient = null

  private var finishedUrlFetchActorNum = 0
  private var jobStartTimeMs = 0L

//  private val heartbeatSchedule = context.system.scheduler.schedule(2.seconds,30.seconds,self,HeartBeat)
  private val jobSchedule = context.system.scheduler.schedule(1.second,jobConf.crawlerIntervalSecond.seconds,self,JobTick)
  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    logger.info(self.path.name +" starting.............................")
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
//    heartbeatSchedule.cancel()
    jobSchedule.cancel()
    logger.info(self.path.name +" stopping.............................")
  }

  override def receive: Receive = {

    case HeartBeat =>{
      RemoteActorManager.remoteActor ! HeartBeatTickInfo(jobName,jobId,DateTime.now.getMillis)
    }
    case JobTick =>{
      logger.info(s"${context.self.path.name} JobTick ..............................")
      jobTickInitail()
//      RemoteActorManager.remoteActor ! JobInfo(jobName,jobId,JobStatus.running,DateTime.now.getMillis)
//      dataDirInit()
//      saveFile2Hdfs()
    }
    case Terminated(actor) =>{
      logger.info("Terminated of ................ " + actor.path.name)
      finishedUrlFetchActorNum += 1
      context.unwatch(actor)
      if(finishedUrlFetchActorNum>=jobConf.threadNum){
        //子urlFetchActor 全部完成,想远程actor发送统计
        //todo
        self ! JobTickFinish(jobStartTimeMs,DateTime.now.getMillis)
      }
    }
    case JobTickFinish(start,end) =>{
//      ActorManager.actorManager !UploadEoleBuffer(jobName,jobId)
      val (allDownNumber,allParseNumber)= extractor.getStatisticClient.saveStatistic()
      logger.info(s"add jobActors finished and will save data with $allParseNumber/$allDownNumber.......")
//      RemoteActorManager.remoteActor ! JobStatistic(jobName, jobId, allDownNumber, allParseNumber, start, end)
//      RemoteActorManager.remoteActor ! JobInfo(jobName,jobId,JobStatus.sleeping,end)
    }

    case AddUrl(url,jobName,jobId)=>{
      urlPoolActor ! AddUrl(url,jobName,jobId)
    }
    case AddUrlList(urls,jobName,jobId)=>{
      urlPoolActor ! AddUrlList(urls,jobName,jobId)
    }
    case unknow@_ => logger.info("unknow message: " + unknow + "  in " + context.self.path.name + " from " + context.sender().path.name)
  }

/*  def saveFile2Hdfs() = {
    val file = new File(s"${System.getProperty("user.dir") }/data/$jobName-old")
    if(file.exists()){
      ActorManager.saveFile2Hdfs(jobName, jobId,dataType, file, 1)
    }
  }

  def dataDirInit() = {
    val dir = new File(s"${System.getProperty("user.dir") }/data")
    if(!dir.exists()){
      dir.mkdir()
    }
    val oldFile = new File(s"${System.getProperty("user.dir") }/data/$jobName-old")
    if(oldFile.exists()){
      oldFile.delete()
    }
    val file = new File(s"${System.getProperty("user.dir") }/data/$jobName")
    if(file.exists()){
      file.renameTo(new File(s"${System.getProperty("user.dir") }/data/$jobName-old"))
    }
  }*/

  def jobTickInitail() ={
    initailHttpClient()
    initialMaxInfo()
    initialSeedFile()
    initialFetchActors()
  }

  def initailHttpClient() = {
    userClient = HttpClientUtil.getHttpClient(jobConf.getExtractor.get.getCookieStore,10000)
  }

  def initialMaxInfo() ={
    extractor.getMaxInfoClient.setNameId(jobName,jobId)
    extractor.getMaxInfoClient.loadOldMaxListFromFile(jobPath + JobConfFileName.maxInfoFileName)
  }
  def initialSeedFile() = {
    val seedFile = new File(jobPath + JobConfFileName.seedFileName)
    if(seedFile.exists()){
        val seedUrls = Source.fromFile(seedFile).getLines().toList
        urlPoolActor ! AddUrlList(seedUrls,jobName,jobId)
    }else{
      logger.info("The seed file " + seedFile.getAbsolutePath + " is not exist!")
      //todo tell JobController to kill himself

    }
  }

def initialFetchActors() ={

  if(finishedUrlFetchActorNum<jobConf.threadNum){
    //上一个周期还没完成
    self ! JobTickFinish(jobStartTimeMs,DateTime.now.getMillis)
    Thread.sleep(5000)
  }
  jobStartTimeMs = DateTime.now.getMillis
  finishedUrlFetchActorNum = 0
  for(index <- 1 to jobConf.threadNum){  //创建threadNum个UrlFetchActor
    val urlFetchActor = context.child(s"UrlFetchActor-$jobName-$jobId-$index").getOrElse {
      context.actorOf(Props[UrlFetchActor](new UrlFetchActor()),name = s"UrlFetchActor-$jobName-$jobId-$index")
    }
    context.watch(urlFetchActor)
    urlFetchActor ! GetUrl(0)
  }
}


  class UrlFetchActor extends Actor{
    /**
     * 主要用于从url池中获取url 之后向HttpFetchActor发送消息 开始抓取页面信息
     */
    private final val logger = LoggerFactory.getLogger(getClass)
    private final val MaxEmptyUrlTimes = 500
    private final val FetchUrlIntervalMs = 100
    private val httpFetchActor = context.actorOf(Props[HttpFetchActor](new HttpFetchActor()),self.path.name+"-HttpFetchActor")
    @throws[Exception](classOf[Exception])
    override def preStart(): Unit = {
      logger.info(self.path.name +" starting.............................")
    }

    @throws[Exception](classOf[Exception])
    override def postStop(): Unit = {
      logger.info(self.path.name +" stopping.............................")
    }

    override def receive: Receive = {
      case GetUrl(retryTime)=>{   //向urlPoolActor发送消息获取url
        urlPoolActor ! GetUrl(retryTime)
      }
      case GetUrlResult(urlOpt,retryTime)=>{ //接收从urlPoolActor返回的消息
        if(urlOpt.isDefined){
          httpFetchActor ! HttpFetchTask(urlOpt.get,0,0)
        }else{
          if(retryTime>=MaxEmptyUrlTimes){
            //todo  kill himeself
            context.stop(self)
          }else{
            context.system.scheduler.scheduleOnce(FetchUrlIntervalMs.millisecond,self,GetUrl(retryTime+1))
          }
        }
      }
      case unknow@_ => logger.info("unknow message: " + unknow + "  in " + context.self.path.name + " from " + context.sender().path.name)
    }
  }


  class HttpFetchActor extends Actor{
    /**
     * 主要用于通过url获取http页面内容并解析
     */
    private final val logger = LoggerFactory.getLogger(getClass)
    private final val MaxUrlRetryTimes = 20
    private final val MaxProxyRetryTimes = 20
    private final val HttpFetchRetryDelayMs = 100
    @throws[Exception](classOf[Exception])
    override def preStart(): Unit = {
      logger.info(self.path.name +" starting.............................")
    }

    @throws[Exception](classOf[Exception])
    override def postStop(): Unit = {
      logger.info(self.path.name +" stopping.............................")
    }

    override def receive: Actor.Receive = {
      case HttpFetchTask(url,urlRetryTimes,proxyRetryTimes)=>{
        if(urlRetryTimes<=MaxUrlRetryTimes){
          //todo
          val send = sender()
          val getProxyResult = ActorManager.getProxy(proxyRetryTimes)
          if(jobConf.isNeedProxy){  //需要代理
            (getProxyResult.proxy.isDefined,proxyRetryTimes<=MaxProxyRetryTimes) match {
              case(true,_) =>{
                try{
                  val response = HttpClientUtil.executeHttpGet(url,userClient,getProxyResult.proxy.get)
                  val httpContent = EntityUtils.toString(InputStreamUtils.getRealEntity(response.getEntity),jobConf.charset)
                  val httpCode = response.getStatusLine.getStatusCode
                  EntityUtils.consume(response.getEntity)
                  response.close()
                  if(httpCode == HttpStatus.SC_OK && httpContent!=null && httpContent.trim.length>0){
                    extractor.process(HttpFetchResult(url,httpContent,httpCode,urlRetryTimes),jobName,jobId,dataType)
                  }else{
                    logger.info(s"$url fetch http data with HttpCode=$httpCode and httpCoontent=null=="+ (httpContent==null))
                    context.system.scheduler.scheduleOnce(HttpFetchRetryDelayMs*urlRetryTimes.millisecond,self,HttpFetchTask(url,urlRetryTimes+1,proxyRetryTimes))
                  }
//                  context.parent ! GetUrl(0)
                }catch {
                  case e:Exception =>{
                    logger.info(s"$url fetch http failed:",e.getMessage)
                    context.system.scheduler.scheduleOnce(HttpFetchRetryDelayMs*urlRetryTimes.millisecond,self,HttpFetchTask(url,urlRetryTimes+1,proxyRetryTimes))
//                    context.parent ! GetUrl(0)
                  }
                }
              }
              case (false,true) =>{
                context.system.scheduler.scheduleOnce(HttpFetchRetryDelayMs.millis,self,HttpFetchTask(url,urlRetryTimes,proxyRetryTimes+1))
              }
              case (false,false)=>{
                logger.info("can not get proxy from proxyPool for a long time !")
                //todo 警告
                context.parent ! GetUrl(0)
              }
            }

          }else{
            try{
//              val response = HttpClientUtil.executeHttpGet(url,userClient,getProxyResult.proxy.get)
              val response = HttpClientUtil.executeHttpGet(url,userClient,null)
              val httpContent = EntityUtils.toString(InputStreamUtils.getRealEntity(response.getEntity),jobConf.charset)
              val httpCode = response.getStatusLine.getStatusCode
              EntityUtils.consume(response.getEntity)
              response.close()
              if(httpCode == HttpStatus.SC_OK && httpContent!=null && httpContent.trim.length>0){
                extractor.process(HttpFetchResult(url,httpContent,httpCode,urlRetryTimes),jobName,jobId,dataType)
              }else{
                logger.info(s"$url fetch http data with HttpCode=$httpCode and httpCoontent=null=="+ (httpContent==null))
                context.system.scheduler.scheduleOnce(HttpFetchRetryDelayMs*urlRetryTimes.millisecond,self,HttpFetchTask(url,urlRetryTimes+1,proxyRetryTimes))
              }
//              context.parent ! GetUrl(0)
            }catch {
              case e:Exception =>{
                logger.info(s"$url fetch http failed:",e)
                context.system.scheduler.scheduleOnce(HttpFetchRetryDelayMs*urlRetryTimes.millisecond,self,HttpFetchTask(url,urlRetryTimes+1,proxyRetryTimes))
//                context.parent ! GetUrl(0)
              }
            }
          }
        }else{
          //todo 多次无法获取该url,保存文件？
          logger.info(s"can not fetch http data from $url for $urlRetryTimes times !")
          context.parent ! GetUrl(0)
        }
      }
      case unknow@_ => logger.info("unknow message: " + unknow + "  in " + context.self.path.name + " from " + context.sender().path.name)
    }
  }
}
