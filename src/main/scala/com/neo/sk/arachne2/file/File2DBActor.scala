package com.neo.sk.arachne2.file

import java.io.File

import akka.actor.Actor
import com.neo.sk.arachne2.common.{File2DBJob}
import com.neo.sk.arachne2.models.newsDAO
import com.neo.sk.arachne2.utils.TimeUtil._
import org.slf4j.LoggerFactory

import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global
/**
 * Created by 王春泽 on 2016/3/30.
 */

class File2DBActor extends Actor{
  private val logger=LoggerFactory.getLogger(getClass)
  @throws[Exception](classOf[Exception])
  override def preStart()={
    logger.info("Save2FileActor "+self.path.name+"is starting.........")
  }

  override def postStop()={
    logger.info("Save2FileActor "+self.path.name+"is stoping..........")
  }

  override def receive:Receive={
    case File2DBJob(jobName)=>
      val curTime=System.currentTimeMillis()
      val filesDir = new File(s"tempTxt/$jobName")
      (filesDir.exists,filesDir.isDirectory) match {
        case (true, true) => {
          val targetFile = filesDir.listFiles().filter(f=>
            (curTime-parseToTimeStamp(f.getName.dropRight(4),"yyyy-MM-dd-HH-mm-ss"))>10*60*1000L)

          val contentList=
            targetFile.flatMap { file =>
              val source = Source.fromFile(file, "GBK")
              val content = source.getLines().filter(_.split("\u0001").length==12)
              source.close()
              content.map { line =>
                val arr = line.split("\u0001")
                (arr(0),arr(1),arr(2).split(" ").head,arr(3),arr(4),arr(5).toLong,arr(6),arr(7),arr(8).toInt,arr(9),arr(10),arr(11))
              }
            }.toList
//          newsDAO.insertWangyi(contentList).onComplete{
//            case scala.util.Success(_) =>
//              println("--successful!!--")
//              targetFile.foreach(f=>f.delete())
//              logger.info("------delete file---")
//            case scala.util.Failure(_) =>
//              println("database operation failed >>>>>>>>>>>>>>>>>")
//          }

        }

        case (false, _) => {
          logger.info(s"jobs dir $filesDir is not exist !!!")
        }
        case (_, false) => {
          logger.info(s"$filesDir is not a directory  !!!")
        }
      }


    case unknow@_ => logger.info("unknow massage "+unknow +"in"+context.self.path.name+"from"+context.sender().path.name)
  }
}


