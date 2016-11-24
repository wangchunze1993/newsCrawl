package com.neo.sk.arachne2.core.job

import akka.actor.{PoisonPill, Terminated, Props, Actor}
import com.github.nscala_time.time.Imports._
import com.neo.sk.arachne2.ActorManager
import com.neo.sk.arachne2.common._
import com.neo.sk.arachne2.core.statistic.RemoteActorManager
import org.slf4j.LoggerFactory

/**
 * Created by 王春泽 on 2016/4/11.
 */
class JobController extends Actor{

  /**
   * 爬虫Job控制，用于创建、管理具体的jobActor
   */

  private final val logger = LoggerFactory.getLogger(getClass)

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    logger.info(self.path.name +" starting.............................")
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    logger.info(self.path.name +" stopping.............................")
  }

  override def receive: Receive = {

    case StartJob(jobConf) =>{
      //启动爬虫job

      val jobActor = context.child("JobActor-" + jobConf.jobName+"-"+jobConf.jobId).getOrElse(
        context.actorOf(Props[JobActor](new JobActor(jobConf)),name ="JobActor-" + jobConf.jobName+"-"+jobConf.jobId)
      )
      context.watch(jobActor)
    }
    case StopJob(jobName,jobId) =>{
      context.child("JobActor-" + jobName+"-"+jobId) foreach { child =>
        context.stop(child)
        context.unwatch(child)
        RemoteActorManager.remoteActor ! JobInfo(jobName,jobId,JobStatus.unStart,DateTime.now.getMillis)
      }
    }
    case Terminated(deadActor) =>{
      context.unwatch(deadActor)
      logger.info("the JobActor " + deadActor.path.name + " dead !!!")
    }
    case AddUrl(url,jobName,jobId)=>{
      context.child("JobActor-" + jobName+"-"+jobId) foreach { child =>
        child !  AddUrl(url,jobName,jobId)
      }
    }
    case AddUrlList(urls,jobName,jobId)=>{
      context.child("JobActor-" + jobName+"-"+jobId) foreach { child =>
        child !  AddUrlList(urls,jobName,jobId)
      }
    }
    case unknow@_ => logger.info("unknow message: " + unknow + "  in " + context.self.path.name + " from " + context.sender().path.name)
  }
}
