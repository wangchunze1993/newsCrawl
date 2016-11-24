package com.neo.sk.arachne2.file

import java.io._

import akka.actor.Actor
import com.neo.sk.arachne2.common.Save2File
import com.neo.sk.arachne2.utils.TimeUtil._
import org.slf4j.LoggerFactory


/**
 * Created by 王春泽 on 2016/3/11.
 */
class Save2FileActor (jobName:String,jobId:Long) extends Actor{
  private val logger=LoggerFactory.getLogger(getClass)

  @throws[Exception](classOf[Exception])
  override def preStart()={
    logger.info("Save2FileActor "+self.path.name+"is starting.........")
  }

  override def postStop()={
    logger.info("Save2FileActor "+self.path.name+"is stoping..........")
  }

  override def receive:Receive={
    case Save2File(jobName:String,jobId:Long,dataType:String,content:String)=>{

        val curTime=System.currentTimeMillis()
        val fileName=formatTimeStamp(curTime,"yyyy-MM-dd-HH-mm-ss")
        val filesDir = new File(s"tempTxt/$jobName")
        (filesDir.exists,filesDir.isDirectory) match {
          case (true, true) => {
            val targetFile = filesDir.listFiles().find(f=>
              (curTime-parseToTimeStamp(f.getName.dropRight(4),"yyyy-MM-dd-HH-mm-ss"))<30*60*1000L)
              .getOrElse(new File(s"$filesDir/$fileName.txt" ))

            val fileWriter= new FileWriter(targetFile,true)
            val printWriter= new PrintWriter(fileWriter)
            printWriter.println(content)
            printWriter.close()
            fileWriter.close()
          }

          case (false, _) => {
            logger.info(s"jobs dir $filesDir is not exist !!!")
          }

          case (_, false) => {
            logger.info(s"$filesDir is not a directory  !!!")
          }
        }
    }

    case unknow@_ => logger.info("unknow massage "+unknow +"in"+context.self.path.name+"from"+context.sender().path.name)
  }
}
