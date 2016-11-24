package com.neo.sk.arachne2.common

import java.io.File
import com.neo.sk.arachne2.core.job.JobConfig

/**
 * User: Huangshanqi
 * Date: 2015/10/12
 * Time: 17:46
 */
sealed trait Message
//url
case object IntervalTick extends Message

//proxy
case class UrlTask(url:String) extends Message
case class GetProxy(retryTime:Int) extends Message
case class GetProxyResult(retryTime:Int,proxy: Option[String]) extends Message
case class RemoveProxy(proxy:String) extends Message
case object ProxyNumReportTick extends Message

//jobInfo
case class JobInfo(jobName:String,jobId:Long,jobStatus:Int,sendTime:Long) extends Message
case class JobStatistic(jobName:String,jobId:Long,downNum:Long,parseNUm:Long,startTime:Long,endTime:Long) extends Message
case object HeartBeatTick extends Message
case class HeartBeatTickInfo(jobName:String,jobId:Long,sendTime:Long) extends Message
//jobInfo

case object ScanJobDirTick extends Message

//job Manage
case class StartJob(jobConf:JobConfig) extends Message
case class StopJob(jobName:String,jobId:Long) extends Message
case class JobActorDeadInfo(jobName:String,jobId:Long) extends Message
case class AddUrl(url:String,jobName:String,jobId:Long) extends Message
case class AddUrlList(urls:Traversable[String],jobName:String,jobId:Long) extends Message
case class GetUrl(retryTime:Int) extends Message
case class GetUrlResult(url:Option[String],retryTime:Int) extends Message
case class HttpFetchTask(url:String,httpRetryTime:Int,proxyRetry:Int) extends Message
case object JobTick extends Message
case class JobTickFinish(start:Long,end:Long) extends Message
case object JobInitialTick extends Message
case object HeartBeat extends Message
//job Manage

//eole
case class UploadBuffer2EoleTask(jobName:String,jobId:Long,content:String,retryTime:Int) extends Message
case class UploadEoleBuffer(jobName:String,jobId:Long) extends Message

case class Save2EoleTask(jobName:String,jobId:Long,dataType:String,content:String,retryTime:Int) extends Message
case class File2HDFS(jobName:String,jobId:Long,dataType:String,file:File,retryTime:Int) extends Message

//file
case class Save2File(jobName:String,jobId:Long,dataType:String,content:String) extends Message
case object File2DB extends Message
case class File2DBJob(jobName:String) extends Message

//data cache
case class Save2Cache(jobName:String,jobId:Long,dataType:String,content:String) extends Message
case class Cache2DB(jobName:String,jobId:Long) extends Message
case class Cache2DBJob(jobName:String) extends Message
