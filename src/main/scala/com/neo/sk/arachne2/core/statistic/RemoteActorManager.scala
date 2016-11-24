package com.neo.sk.arachne2.core.statistic

import java.util.concurrent.Executors

import akka.actor.{Props, Actor, ActorSystem}
import akka.util.Timeout
import com.neo.sk.arachne2.ActorManager
import com.neo.sk.arachne2.common.{JobInfo, HeartBeatTickInfo, JobStatistic}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
 * User: Huangshanqi
 * Date: 2015/10/12
 * Time: 17:29
 */

class RemoteActorManager extends Actor{
  /**
   * 远程actor，负责给监控网站提供爬虫状态
   */
  import ActorManager.myExecutionContext

  private final val logger = LoggerFactory.getLogger(this.getClass)

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    logger.info("ActorManager- "+ self.path.name +" starting.............................")
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    logger.info("ActorManager- "+ self.path.name +" stopping.............................")
  }

  //job信息actor
  val remoteConfig = ConfigFactory.load().getConfig("remoteConf")
  val remoteSystem = ActorSystem("arachne2WebActorSystem", remoteConfig.getConfig("Sys"))
  //远程通信actor
  val remoteActor = remoteSystem.actorSelection(remoteConfig.getString("remoteActorPath"))

  override def receive: Receive = {

    case JobInfo(jobName,jobId,runningStatus,sendTime) =>{
      Future {
        remoteActor ! new JobInfo(jobName,jobId,runningStatus,sendTime)
      }
    }

    case HeartBeatTickInfo(jobName,jobId,sendTime) =>{
      Future {
        remoteActor ! new HeartBeatTickInfo(jobName,jobId,System.currentTimeMillis())
      }
    }

    case JobStatistic(jobName,jobId,downNum,parseNum,jobStartTime,jobEndTime) =>{
      logger.info("doing JobFlusData in JobInfoActor`````````````````````````")
      Future {
        remoteActor ! new JobStatistic(jobName,jobId,downNum,parseNum,jobStartTime,jobEndTime)
      }
    }

    case unknow@_ => logger.error(s"unkown message $unknow in RemoteManager from" + context.sender().path.name)
  }
}

object RemoteActorManager {

  private val remoteActorManage = ActorManager.system.actorOf(Props[RemoteActorManager],name = "RemoteActorManager")
  def remoteActor = remoteActorManage
}

