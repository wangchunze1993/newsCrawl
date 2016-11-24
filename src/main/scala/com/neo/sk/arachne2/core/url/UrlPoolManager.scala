package com.neo.sk.arachne2.core.url

import java.io.File

import akka.actor.Actor
import com.github.nscala_time.time.Imports.DateTime
import com.neo.sk.arachne2.common._
import com.neo.sk.arachne2.core.job.JobConfig
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
/**
 * Created by 王春泽 on 2016/4/11.
 */
class UrlPoolManager(jobConfig:JobConfig) extends Actor{
  /**
   * 每个jobActor的url管理actor
   */

  import com.neo.sk.arachne2.ActorManager.myExecutionContext

  private final val logger = LoggerFactory.getLogger(getClass)

  private val urlQueue = new mutable.Queue[String]()
  private final val MaxQueueLength = 100000
  private val jobName = jobConfig.jobName
  private final val seedPath = System.getProperty("user.dir") + "/seed/" + jobName + "/"

  private val urlSizeSchedule = context.system.scheduler.schedule(10.seconds,1.minute,self,IntervalTick)

  private val seedDir = new File(seedPath)
  if(!seedDir.exists()){
    seedDir.mkdirs()
  }

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    logger.info(self.path.name +" starting and will load urls from file.......")
    readAllUrlsFromFile()
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    urlSizeSchedule.cancel()
    logger.info(self.path.name +" stopping and will save urls to file.......")
    saveUrls2File(urlQueue)
  }

  override def receive: Receive = {

    case IntervalTick => {
      logger.info(s"${context.self.path.name} url size = ${urlQueue.length}")
    }
    case AddUrl(url,jobname,jobId) =>{
      logger.info("add url : " + url)
      urlQueue.enqueue(url)
      checkQueueLength
    }
    case AddUrlList(urls,jobname,jobId) =>{
      urls foreach(urlQueue.enqueue(_))
      checkQueueLength
    }
    case GetUrl(retryTime) =>{
      if(urlQueue.isEmpty){
        readUrlsFromFile()
      }
      try {
        val url = urlQueue.dequeue()
        sender() ! GetUrlResult(Some(url),retryTime)
      }catch {
        case e:NoSuchElementException =>{
          sender() ! GetUrlResult(None,retryTime)
        }
        case unknowException: Exception=>{
          logger.info("unknow error on GetUrl ",unknowException)
          sender() ! GetUrlResult(None,retryTime)
        }
      }
    }
    case unknow@_ => logger.info("unknow message: " + unknow + "  in " + context.self.path.name + " from " + context.sender().path.name)
  }

  def checkQueueLength = {
    if(urlQueue.size > MaxQueueLength){
      //to many url,must save to file
      val saveUrlList = mutable.ListBuffer[String]()
      for(index <- 1 to MaxQueueLength/2){
        saveUrlList += urlQueue.dequeue()
      }
      Future{
        saveUrls2File(saveUrlList)
      }
    }
  }

  def saveUrls2File(urls:Traversable[String]): Unit ={
   //todo
    try{
      FileUtils.writeLines(new File(seedPath+DateTime.now.toString("yyyyMMddHHmmssSSS")+".txt"),urls.toList.asJava)
    }catch {
      case e:Exception => logger.info(s"saveUrls2File error on job=$jobName:" ,e)
    }
  }

  def readUrlsFromFile()={
    try{
      val seedFiles = seedDir.listFiles()
      if(seedFiles.nonEmpty){
        val file = seedFiles(0)
        val urls = FileUtils.readLines(file).asScala
        urls foreach(urlQueue.enqueue(_))
        file.delete()
      }
    }catch {
      case e:Exception => logger.info(s"readUrlsFromFile error on job=$jobName:" ,e)
    }
  }
  def readAllUrlsFromFile()={
    try{
      val seedFiles = seedDir.listFiles()
      if(seedFiles.nonEmpty){
        for(file <- seedFiles){
          val urls = FileUtils.readLines(file).asScala
          urls foreach(urlQueue.enqueue(_))
          file.delete()
        }
      }
    }catch {
      case e:Exception => logger.info(s"readUrlsFromFile error on job=$jobName:" ,e)
    }
  }
}
