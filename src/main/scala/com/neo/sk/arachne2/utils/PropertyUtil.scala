package com.neo.sk.arachne2.utils

import java.io.{FileInputStream, File}
import java.util.Properties

import com.neo.sk.arachne2.core.job.{JobPropertiesKey, JobConfig}
import org.slf4j.LoggerFactory

/**
 * User: Huangshanqi
 * Date: 2015/10/15
 * Time: 13:20
 */
object PropertyUtil {

  private final val logger = LoggerFactory.getLogger(getClass)
  final val pathSeparator = System.getProperty("file.separator")

  def loadPropertyFromFile(filePath:String):Option[Properties] = {
    val file = new File(filePath)
//    logger.info("file isExist === " + file.exists()+","+file.getAbsolutePath)
    val properties = new Properties()
    try {
      properties.load(new FileInputStream(file))
      Some(properties)
    }catch {
      case e:Exception =>
        logger.info("load property file error:" ,e)
        None
    }
  }

  def isPropertiesFileUsable(filePath:String):(Boolean,Option[JobConfig]) = {
//    logger.info("properties file name = " + filePath)
    val propertiesOpt = loadPropertyFromFile(filePath)
//    logger.info("propertiesOpt.isDefined:"+propertiesOpt.isDefined)
    if(propertiesOpt.isDefined){
      val properties = propertiesOpt.get
      val jobId = properties.getProperty(JobPropertiesKey.jobId)
      val jobName = properties.getProperty(JobPropertiesKey.jobName)
      val loadClassPath = properties.getProperty(JobPropertiesKey.loadClassPath)
//      logger.info(s"jobId=$jobId,jobName=$jobName,loadClassPath=$loadClassPath")
      if(jobId!=null&&jobName!=null&&loadClassPath!=null){
        val dataType = properties.getProperty(JobPropertiesKey.dataType,"news")
        val crawlerIntervalSecond = properties.getProperty("crawlerIntervalSecond",(24*60*30).toString).toLong
        val isNeedProxy = properties.getProperty(JobPropertiesKey.isNeedProxy,"true").toBoolean
        val charset = properties.getProperty(JobPropertiesKey.charset,"utf-8")
        val threadNum = properties.getProperty(JobPropertiesKey.threadNum,"5").toInt
        val isDelete = properties.getProperty(JobPropertiesKey.isDelete,"true").toBoolean
//        val jobConfigOpt = new
        val jobConfig = new JobConfig(jobId.toLong,jobName,dataType,crawlerIntervalSecond,isNeedProxy,charset,threadNum,loadClassPath,isDelete)
        val usable = (!isDelete) && jobConfig.getExtractor.isDefined
        (usable,Some(jobConfig))
      }else{
        (false,None)
      }
    }else{
      (false,None)
    }
  }

  def contailsAllPropertiesKey(properties:Properties):Boolean = {
    properties.containsKey(JobPropertiesKey.jobId) &&
      properties.containsKey(JobPropertiesKey.jobName) &&
      properties.containsKey(JobPropertiesKey.isNeedProxy) &&
    properties.containsKey()
  }


/*  def main (args: Array[String]) {
    val pOpt = loadPropertyFromFile(System.getProperty("user.dir") + "/jobs/cnblog/config.properties")
    pOpt.foreach{
      pro =>
        val elements = pro.propertyNames()
        while (elements.hasMoreElements){
          val key = elements.nextElement().toString
          val value = pro.getProperty(key)
          println(key+":"+value)
        }
    }
  }*/

}
