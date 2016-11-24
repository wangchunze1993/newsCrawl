package com.neo.sk.arachne2

import java.io.{FileOutputStream, OutputStreamWriter, BufferedWriter, File}
import java.util.concurrent.{SynchronousQueue, TimeUnit, ThreadPoolExecutor}

import akka.actor.{Props, ActorSystem, Actor}
import com.neo.sk.arachne2.cache.CacheManager
import com.neo.sk.arachne2.common._
import com.neo.sk.arachne2.core.job.CrawlerEngine
import com.neo.sk.arachne2.core.proxy.ProxyManager
import com.neo.sk.arachne2.file.FileManager


import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext

/**
 * Created by 王春泽 on 2016/4/11.
 */
class ActorManager extends Actor{
  /**
   * actor 总控制器
   */

  private final val logger = LoggerFactory.getLogger(getClass)

  private val crawlerEngine = context.actorOf(Props[CrawlerEngine],name = "CrawlerEngine")
  private val cacheManager  = context.actorOf(Props[CacheManager],name = "CacheManager")
  private val fileManager = context.actorOf(Props[FileManager],name = "FileManager") // FileManager的actor
//  private val eoleManager = context.actorOf(Props[EoleManager](new EoleManager()),name = "EoleManager")
//  private val save2HdfsManager = context.actorOf(Props[Save2HdfsManager], "Save2HdfsManager") //actor的创建

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    logger.info("ActorManager- "+ self.path.name +" starting.............................")
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    logger.info("ActorManager- "+ self.path.name +" stopping.............................")
  }

  override def receive: Receive = {

    case AddUrl(url,jobName,jobId)=>{
      crawlerEngine ! AddUrl(url,jobName,jobId)
    }

    case AddUrlList(urls,jobName,jobId)=>{
      crawlerEngine ! AddUrlList(urls,jobName,jobId)
    }

    case Save2Cache(jobName,jobId,dataType,content)=>{
      cacheManager  ! Save2Cache(jobName,jobId,dataType,content)
    }
    case Save2File(jobName,jobId,dataType,content)=>{
      fileManager  !  Save2File(jobName,jobId,dataType,content)
    }

    case unknow@_ => logger.info("unknow message: " + unknow + "  in " + context.self.path.name + " from " + context.sender().path.name)
  }

}


object ActorManager {

  val pool = new ThreadPoolExecutor(0, 4096,60L, TimeUnit.SECONDS, new SynchronousQueue[Runnable]())
  implicit val myExecutionContext: ExecutionContext = ExecutionContext.fromExecutorService(pool)
  implicit val system = ActorSystem("arachne2ActorSystem")

  val actorManager = system.actorOf(Props[ActorManager],"ActorManager")
  val actorSystem = system
  private val proxyPool = new ProxyManager()
  def start()={
//    proxyPool.startTask()
    println("ActorManger object start.......")
  }

  def getProxy(retryTime:Int) = proxyPool.getProxy(retryTime)
  def removeProxy(proxy:String) = proxyPool.removeProxy(proxy)
  def addUrl(url:String,jobName:String,jobId:Long) = {
    actorManager ! AddUrl(url,jobName,jobId)
  }
  def addUrlList(urls:Traversable[String],jobName:String,jobId:Long) = {
    actorManager ! AddUrlList(urls,jobName,jobId)
  }

  def save2File(jobName:String,jobId:Long,dataType:String,content:String) = {
    val replaceContent = content.replaceAll("\n","\u0002").replaceAll("\r","\u0002")+"\n"
    actorManager ! Save2File(jobName,jobId,dataType:String,replaceContent)
  }

  def save2Cache(jobName:String,jobId:Long,dataType:String,content:String)={
    val replaceContent = content.replaceAll("\n","\u0002").replaceAll("\r","\u0002")
    actorManager ! Save2Cache(jobName,jobId,dataType,replaceContent)
  }

}