package com.neo.sk.arachne2.core.job

import java.io.File

import akka.actor.{Props, Actor}
import com.neo.sk.arachne2.common._
import com.neo.sk.arachne2.utils.PropertyUtil
import org.slf4j.LoggerFactory
import scala.StringBuilder
import scala.concurrent.duration._
import scala.collection.mutable
/**
 * Created by 王春泽 on 2016/4/11.
 */

class CrawlerEngine extends Actor{
  /**
   * 爬虫引擎，job扫描管理，./jobs/目录的扫描、加载
   */

  import com.neo.sk.arachne2.ActorManager.myExecutionContext

  private final val logger = LoggerFactory.getLogger(getClass)
  private val jobConfFileModifyTimeMap = mutable.HashMap[String,mutable.HashMap[String,Long]]()
  private final val jobsDirPath = System.getProperty("user.dir")+"/jobs"
  private val jobController = context.actorOf(Props[JobController],name = "JobController")


  private val scanJobDirSchedule = context.system.scheduler.schedule(10.seconds,30.seconds,self,ScanJobDirTick) //定时扫描


  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    logger.info(self.path.name +" starting " + "."*15)
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    scanJobDirSchedule.cancel()
    logger.info(self.path.name +" stopping " + "."*15)
  }

  override def receive: Receive = {

    case ScanJobDirTick =>{
      scanJobDir(jobsDirPath)
    }

    case AddUrl(url,jobName,jobId)=>{
      jobController ! AddUrl(url,jobName,jobId)
    }
    case AddUrlList(urls,jobName,jobId)=>{
      jobController ! AddUrlList(urls,jobName,jobId)
    }

    case unknow@_ => logger.info("unknow message: " + unknow + "  in " + context.self.path.name + " from " + context.sender().path.name)
  }

  def isJobConfigFileNotModify(jarName:String,oldMap:mutable.HashMap[String,Long],newMap:mutable.HashMap[String,Long]):Boolean ={
    oldMap.get(JobConfFileName.propertiesFileName).get==newMap.get(JobConfFileName.propertiesFileName).get &&
    oldMap.get(JobConfFileName.seedFileName).get==newMap.get(JobConfFileName.seedFileName).get &&
    oldMap.get(jarName).get.equals(newMap.get(jarName).get)
  }

  def scanJobDir(jobsDirPath:String)={ //扫描jobs文件夹 获取相关信息
    logger.info(" start scanJobDir ...............")
    val jobsDir = new File(jobsDirPath)
    (jobsDir.exists,jobsDir.isDirectory) match {
      case(true,true) =>{
        for(jobDir <- jobsDir.listFiles() if jobDir.isDirectory){
          //seed.txt,conf.properties,jobName.jar
          val jobFiles = jobDir.listFiles()
          val jobName = jobDir.getName
          val jarName = jobName+".jar"
          if(jobFiles.length >= 3){
            val lastModifyTimeMap = mutable.HashMap[String,Long]()
            for(file <- jobFiles){
//              logger.info(s"find config file at /jobs/$jobName: " + file.getName)
              file.getName match {
                case seedFileName if seedFileName.equals(JobConfFileName.seedFileName) =>{
                  lastModifyTimeMap.put(JobConfFileName.seedFileName,file.lastModified())
                }
                case propertiesFileName if propertiesFileName.equals(JobConfFileName.propertiesFileName) =>{
                  lastModifyTimeMap.put(JobConfFileName.propertiesFileName,file.lastModified())
                }
                case jarFileName if jarFileName.equals(jarName) =>{
                  lastModifyTimeMap.put(jarName,file.lastModified())
                }
                case unknowconfigFile@__ =>
//                  logger.info(s"unknow job config file $unknowconfigFile")
              }
            }
            if(lastModifyTimeMap.keySet.size==3){
              val oldJobConfFileInfoOpt = jobConfFileModifyTimeMap.get(jobName)
              val (usable,jobConfOpt) = PropertyUtil.isPropertiesFileUsable(jobsDirPath + """/""" + jobName + """/""" + JobConfFileName.propertiesFileName)
              logger.info(s"$jobName oldJobConfFileInfoOpt.isDefined:"+oldJobConfFileInfoOpt.isDefined+",usable:"+usable+",jobConfOpt.isDefined:"+jobConfOpt.isDefined)
              (oldJobConfFileInfoOpt,usable,jobConfOpt) match {
                case (None,true,Some(jobConf)) =>{
                  //todo start the job
                  lastModifyTimeMap.put("jobId",jobConf.jobId)
                  jobConfFileModifyTimeMap.put(jobName,lastModifyTimeMap)
                  logger.info("start new job " + jobConf.jobName +"-"+jobConf.jobId)
                  jobController ! StartJob(jobConf)
                }
                case (Some(oldJobConfFileInfo),false,_) =>{
                  //todo kill the job
                  val jobId = oldJobConfFileInfo.getOrElse("jobId","00000").toString
                  logger.info("some thing change in config file ,and will kill the job " + jobName+"-"+jobId)
                  jobController ! StopJob(jobName,jobId.toLong)
                  jobConfFileModifyTimeMap.remove(jobName)
                }
                case (Some(oldJobConfFileInfo),true,Some(jobConf)) =>{
                  if(!isJobConfigFileNotModify(jarName,oldJobConfFileInfo,lastModifyTimeMap)){
                    //配置文件有所改动并可用，hashMap中移除,并停止job,下次scan周期会启动（重启）
                    //todo kill the job
                    logger.info("some thing change in config file ,and will kill the job " + jobConf.jobName +"-"+jobConf.jobId)
                    jobController ! StopJob(jobConf.jobName,jobConf.jobId)
                    jobConfFileModifyTimeMap.remove(jobName)
                  }else{
                    logger.info(s"the job $jobName still running and nothing change in the config files.............")
                  }
                }
                case (_,false,Some(jobConf)) =>{
                  logger.info(s"the job /jobs/$jobName will not starting ......................")
                }
                case (_,_,_) => logger.info("nothing change and do nothing ....................")
              }
            }
          }else{
            logger.info("Missing some job file in " + jobDir.getPath)
          }
        }
      }
      case (false,_) =>{
        logger.info(s"jobs dir $jobsDirPath is not exist !!!")
      }
      case (_,false) =>{
        logger.info(s"$jobsDirPath is not a directory  !!!")
      }
    }

  }
}
