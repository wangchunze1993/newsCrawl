package com.neo.sk.arachne2.utils


import java.io.{File, FileFilter, IOException}

import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/**
 * User: Huangshanqi
 * Date: 2015/10/12
 * Time: 17:16
 */
object UrlUtil {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val BASE_PATH = System.getProperty("user.dir") + "/seeds/"
  //  private val base = new File(URL_PATH)
  //实际地址是 /seeds/jobname_jobid/

  def createJobFile(jobId: Long, jobName: String, path: String): File = {
    //    val path = BASE_PATH + jobName + "_" + jobId + "/"
    val file = new File(path)
    if (!file.exists()) {
      file.mkdir()
      file
    } else file
  }

  def getJobFile(jobId: Long, jobName: String): Option[File] = {
    val path = BASE_PATH + jobName + "_" + jobId + "/"
    val file = new File(path)
    if (!file.exists()) {
      None
    } else Some(file)
  }

  def getSeedsFile = {
    val file = new File(BASE_PATH)
    if (!file.exists()) {
      file.mkdir()
      file
    } else file
  }

  def saveJobURL(seed: List[String], jobId: Long, jobName: String) {
    try {
      val path = BASE_PATH + jobName + "_" + jobId + "/"
      createJobFile(jobId, jobName, path)
      //      FileUtils.writeLines(new File(path +jobName+"-"+System.currentTimeMillis()), seed)
      FileUtils.writeLines(new File(path + jobName + "-" + System.currentTimeMillis()), seed.asJava)
    } catch {
      case e: IOException => logger.error(e.getMessage, e)
    }
  }

  def loadJobURL(jobId: Long, jobName: String) = {
    val file = getJobFile(jobId, jobName)
    var seeds = List[String]()
    if (file.isDefined) {
      val files = file.get.listFiles(new SeedFileFilter(jobName))
      if (files != null && files.nonEmpty) {
        val f = files(0)
        logger.info("Loading Seed File: " + f.getName)
        try {
          seeds = FileUtils.readLines(f).asScala.toList
          f.delete()
          logger.info("Loading Seed File: " + f.getName + " Finish")
        } catch {
          case e: IOException => logger.error(e.getMessage, e)
        }
      }
    }
    seeds
  }

  def clearJobURL(jobId: Long, jobName: String) {
    val file = getJobFile(jobId, jobName)
    if (file.isDefined) {
      val files = file.get.listFiles(new SeedFileFilter(jobName))
      for (f <- files) {
        f.delete()
      }
    }
  }

  def clearAllURL(): Unit = {
    val base = getSeedsFile
    val files = base.listFiles()
    for (f <- files) {
      f.delete()
    }
  }

  private class SeedFileFilter(jobName: String) extends FileFilter {
    override def accept(pathname: File): Boolean = {
      if (pathname.isFile && pathname.getName.startsWith(this.jobName + "-")) true else false
    }
  }

/*  def main(args: Array[String]) = {
    val seed = List("1111111111", "2222222222222222", "333333333333333333", "44444444444444444444")
    saveJobURL(seed, 38, "jiamen")
    clearAllURL()
  }*/
}

