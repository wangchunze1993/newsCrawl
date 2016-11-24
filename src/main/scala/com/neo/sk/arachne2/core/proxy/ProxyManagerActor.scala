package com.neo.sk.arachne2.core.proxy

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.{Actor, Props}
import com.github.nscala_time.time.Imports.DateTime
import com.neo.sk.arachne2.ActorManager
import com.neo.sk.arachne2.common._
import com.neo.sk.arachne2.utils.http.HttpClientUtil
import org.slf4j.LoggerFactory
import net.liftweb.json._
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}
/**
 * User: Huangshanqi
 * Date: 2015/10/12
 * Time: 17:13
 */
class ProxyManagerActor extends Actor{
  /**
   * 代理模块
   */
  import ActorManager.myExecutionContext


  private final val logger = LoggerFactory.getLogger(getClass)
  private val httpClientUtil = new HttpClientUtil()
  private val proxyQueue = new ConcurrentLinkedQueue[String]()

  private final val fetchProxyUrl = AppSettings.fetchProxyUrl + AppSettings.fetchProxyNum + "&retry=1"
  private final val fetchProxyMaxRetryTime = 5
  private final val minUsableProxyNum = 30

  private val isFetchingProxy = new AtomicBoolean(false)

  val fetchProxySchedule = context.system.scheduler.schedule(AppSettings.fetchProxyDelay.seconds,AppSettings.fetchProxyInterval.seconds,self,UrlTask(fetchProxyUrl))
  val proxyNumReportSchedule = context.system.scheduler.schedule(10.seconds,30.seconds,self,ProxyNumReportTick)

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    logger.info("ProxyManager- "+ self.path.name +" starting.............................")
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    logger.info("ProxyManager- "+ self.path.name +" stopping.............................")
  }

  override def receive: Receive = {

    case UrlTask(url) =>{
      val segs = url.split("&")
      val retryTime = segs(1).split("=")(1).toInt
      val baseUrl = segs(0)
      if(retryTime>fetchProxyMaxRetryTime){
        //警告 todo
        logger.info("can not get proxy from proxy server .............." )
      }else{
        if(isFetchingProxy.compareAndSet(false,true)){
          Future{
            httpClientUtil.getResponseContent(fetchProxyUrl,"utf-8")
          }.onComplete{
            case Success(result) =>{
              logger.info("get proxy success from server .............")
              isFetchingProxy.set(false)
              if(result!=null && result.nonEmpty){
                var list = json2List(result)
                val all = mutable.HashSet[String]()
                all ++= proxyQueue.asScala.toSet
                all ++= list
                proxyQueue.clear()
                proxyQueue.addAll(all.asJava)
              }else{
                logger.info("get nothing from proxy server ..............")
              }
            }
            case Failure(error) =>{
              isFetchingProxy.set(false)
              logger.info("get proxy fail with " + retryTime +" times,",error.getCause)
              val retryUrl = baseUrl + "&retry="+(retryTime+1)
              self ! UrlTask(retryUrl)
            }
          }
        }else{
          logger.info("already fetching proxy from proxy server .............")
        }
      }
    }
    case GetProxy(retryTime) =>{
     val proxy =  proxyQueue.poll()
      if(proxy == null){
        sender() ! GetProxyResult(retryTime,None)
      }else{
        proxyQueue.offer(proxy)
        sender() ! GetProxyResult(retryTime,Some(proxy))
      }
    }

    case RemoveProxy(proxy) =>{
      proxyQueue.remove(proxy)
      if(proxyQueue.size()<minUsableProxyNum){
        self ! UrlTask(fetchProxyUrl)
      }
    }

    case ProxyNumReportTick =>{
      logger.info("Proxy queue report:"+DateTime.now.toString("yyyy-MM-dd HH:mm:ss") + " size = " + proxyQueue.size())
//      logger.info("Proxy queue report:"+ proxyQueue.asScala.mkString("\n"))
    }
    case unknow@_ => logger.info("unknow message: " + unknow + "  in " + context.self.path.name + " from " + context.sender().path.name)
  }

   def getProxy(retryTime:Int):GetProxyResult={
    val proxy =  proxyQueue.poll()
    if(proxy == null){
      GetProxyResult(retryTime,None)
    }else{
      proxyQueue.offer(proxy)
      GetProxyResult(retryTime,Some(proxy))
    }
  }

  def removeProxy(proxy:String)={
    proxyQueue.remove(proxy)
    if(proxyQueue.size()<minUsableProxyNum){
      self ! UrlTask(fetchProxyUrl)
    }
  }

  /**
   * 解析json
   * @param content
   * @return
   */
  private def json2List(content: String) = {
    implicit val formats = DefaultFormats
    val status = (parse(content) \ "status").extract[Int]
    var list = List[String]()
    if (status == 0) {
      val proxyList = parse(content) \ "proxy"
      list = list ::: proxyList.extract[List[String]]
    }
    list
  }
}

object ProxyManagerActor {
  val proxyManagerObj = ActorManager.actorSystem.actorOf(Props[ProxyManagerActor](new ProxyManagerActor), "ProxyManagerActor")
  def start() ={
    proxyManagerObj
  }
}
