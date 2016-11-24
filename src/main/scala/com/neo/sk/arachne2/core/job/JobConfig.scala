package com.neo.sk.arachne2.core.job

import java.io.File
import java.net.URL

import com.neo.sk.arachne2.core.AbstractWebExecutor
import org.slf4j.LoggerFactory

import scala.reflect.internal.util.ScalaClassLoader.URLClassLoader


/**
 * Created by Íõ´ºÔó on 2016/4/11.
 */
class JobConfig(var jobId:Long,
                 var jobName:String,
                 var dataType:String,
                 var crawlerIntervalSecond:Long,
                 var isNeedProxy:Boolean,
                 var charset:String,
                 var threadNum:Int,
                 var loadClassPath:String,
                 var isDelete:Boolean) {

  private final val logger = LoggerFactory.getLogger(getClass)
  private val jobDirParh = System.getProperty("user.dir") + "/jobs/" + jobName + "/"
  private val webExtractor = loadWebExtractor(jobDirParh)
  def getExtractor = webExtractor

  def loadWebExtractor(jobDirPath:String):Option[AbstractWebExecutor]= {
    val jobDir = new File(jobDirPath)
    if(jobDir.exists() && jobDir.isDirectory){
      try{
        val jarFile = new File(jobDir,jobName+".jar")
        if(jarFile.exists()){
          val jarPath = "file:"+jarFile.getAbsolutePath
          val classLoader = new URLClassLoader(Array(new URL(jarPath)),Thread.currentThread().getContextClassLoader)
          val loadClass = classLoader.loadClass(loadClassPath)
//          logger.info(s"load class:$loadClassPath success from jar ...............")
          Some(loadClass.newInstance().asInstanceOf[AbstractWebExecutor])
        }else{
          logger.info(s"There is no $jobName.jar exist in $jobDirParh")
          None
        }
      }catch {
        case e:Exception =>
          logger.info("load class webExtractor error:",e)
          None
      }
    }else{
      logger.info(s"jobDir with $jobDirParh not exist !")
      None
    }
  }


}
