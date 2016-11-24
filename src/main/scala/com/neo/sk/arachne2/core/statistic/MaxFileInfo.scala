package com.neo.sk.arachne2.core.statistic

import java.io.{BufferedWriter, File, FileWriter}

import com.neo.sk.arachne2.core.job.JobConfFileName
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.io.Source

/**
 * Created by 王春泽 on 2016/4/11.
 */
class MaxFileInfo {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val oldMaxList = new mutable.HashMap[String, String]()
  private val newMaxList = new mutable.HashMap[String, String]()
  private val curMaxList = new mutable.HashMap[String, String]()
  //用户容器
  private val container = new mutable.HashMap[String, String]()
  private var jobName: String = "default"
  private var jobId: Long = 0L
  private val jobsPath = System.getProperty("user.dir") + "/jobs"

  //for user
  def getOldMaxByKey(key:String)={
   oldMaxList.get(key)
  }

  //for user
  def getNewMaxByKey(key:String)={
    newMaxList.get(key)
  }

  //for user
  def setOldMaxItem(key:String,value:String): Unit ={
    oldMaxList.put(key,value)
  }
  //for user
  def setNewMaxItem(key:String,value:String): Unit ={
    newMaxList.put(key,value)
  }

  //for user
  def setCurMaxItem(key:String,value:String): Unit ={
    curMaxList.put(key,value)
  }
  //for user
  def getCurMaxItem(key:String)={
    curMaxList.get(key)
  }


  //for user
  def putIntoContainer(key:String,value:String): Unit ={
    container.put(key,value)
  }

  //for user
  def getFromContainer(key:String)={
    container.get(key)
  }

  //for user
  def removeFromContainer(key:String)={
    container.remove(key)
  }
  def setNameId(name: String, id: Long) {
    jobName = name
    jobId = id
  }

  //for jobActor
  def loadOldMaxListFromFile(fileName: String) = {
    try {
      val file = new File(fileName) //获取保存各类最大文章id的文件
      if (file.exists()) {
        val lines = Source.fromFile(file).getLines().toList
        for(line <- lines){
          val segments = line.split("##")
          oldMaxList.put(segments(0), segments(1))
          newMaxList.put(segments(0), segments(1))
//          curMaxList.put(segments(0), segments(1))
        }
        logger.info(s"从$fileName 读取 ${lines.length} 行增量记录 。。。。")
      } else {
        logger.error(fileName + "is not exists,and will create new maxInfo.txt")
        file.createNewFile()
      }
    } catch {
      case e: Exception => logger.error("error on loadOldMaxListFromFile :" , e)
    }
  }

  //for user
  def saveMaxList() {
    val fileName = jobsPath + "/" + jobName + "/" + JobConfFileName.maxInfoFileName
    writeNewMaxListToFile(jobName, jobId, fileName, newMaxList)
  }

  //for jobActor
  private def writeNewMaxListToFile(jobName: String, jobId: Long, fileName: String, hashmap: mutable.HashMap[String, String]): Unit = {
    try {
      val file = new File(fileName)
      if (file.exists()) {
        val writer = new BufferedWriter(new FileWriter(file))
        hashmap.foreach(e => {
          val (key, value) = e
          writer.write(key + "##" + value + "\n")
        })
        writer.close()
      }
    } catch {
      case e: Exception => logger.error("Exception " + "in MaxFileInfo about " + jobName + "-" + jobId + " :" + e.getMessage)
    }
  }

}
