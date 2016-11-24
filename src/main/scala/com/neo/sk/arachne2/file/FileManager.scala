package com.neo.sk.arachne2.file

import akka.actor.{Props, Actor}
import com.neo.sk.arachne2.common.{File2DBJob, File2DB, Save2File}
import org.slf4j.LoggerFactory
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
/**
 * Created by 王春泽 on 2016/3/11.
 */
class FileManager extends Actor{
  private final val logger = LoggerFactory.getLogger(getClass)
//  private val file2DbSchedule = context.system.scheduler.schedule(1.second,5 minutes,self,File2DB)

  private val jobNames=List("wangyi","leifeng")

  @throws[Exception](classOf[Exception])
  override def preStart():Unit={
    logger.info("FileManeger-"+self.path.name+"is starting..............")
  }

  @throws[Exception](classOf[Exception])
  override def postStop():Unit={
    logger.info("FileManager-"+self.path.name+"is stopping..............")
  }

  override def receive:Receive={
    case Save2File(jobName:String,jobId:Long,dataType:String,content:String)=>{
      context.child(s"Save2FileActor-$jobName-$jobId").getOrElse{
        context.actorOf(Props[Save2FileActor](new Save2FileActor(jobName,jobId)),name=s"Save2FileActor-$jobName-$jobId")
      } ! Save2File(jobName,jobId,dataType,content)
    }

//    case File2DB =>
//      jobNames.foreach{jobName=>
//        context.child(s"File2DBActor-$jobName").getOrElse{
//          context.actorOf(Props[File2DBActor],name=s"File2DBActor-$jobName")
//        } ! File2DBJob(jobName)
//      }

    case unknow@_ => logger.info("unknow message: "+unknow + "in"+context.self.path.name+"from "+context.sender().path.name)
  }
}
