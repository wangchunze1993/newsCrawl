package com.neo.sk.arachne2.core.proxy

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{ConcurrentLinkedQueue, Executors, TimeUnit}

import com.github.nscala_time.time.Imports._
import com.neo.sk.arachne2.common.{UrlTask, GetProxyResult, AppSettings}
import com.neo.sk.arachne2.utils.http.HttpClientUtil
import net.liftweb.json._
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * User: Huangshanqi
 * Date: 2015/3/20
 * Time: 14:16
 */
class ProxyManager {

  private final val logger = LoggerFactory.getLogger(getClass)
  private val httpClientUtil = new HttpClientUtil()
  private val proxyQueue = new ConcurrentLinkedQueue[String]() //并发队列 原子操作下线程安全

  private final val fetchProxyUrl = AppSettings.fetchProxyUrl + AppSettings.fetchProxyNum + "&retry=1"
  private final val fetchProxyMaxRetryTime = 5
  private final val minUsableProxyNum = 30
  private val isFetchingProxy = new AtomicBoolean(false) //在这个Boolean值的变化的时候不允许在之间插入，保持操作的原子性
  private val executor = Executors.newScheduledThreadPool(5) //Executors：线程管理 创建延迟连接池
  private val fetchProxyRunnable = new FetchProxyRunnable(fetchProxyUrl)

  def startTask(): Unit = {
    //executor.scheduleAtFixedRate(fetchProxyRunnable, AppSettings.fetchProxyDelay, AppSettings.fetchProxyInterval, TimeUnit.SECONDS)
    executor.scheduleAtFixedRate(fetchProxyRunnable, 20, 10, TimeUnit.SECONDS)//在 initialDelay后开始执行，然后在initialDelay+period 后执行
  }

  def getProxy(retryTime:Int):GetProxyResult={
    val proxy =  proxyQueue.poll()
    if(proxy == null){
      GetProxyResult(retryTime,None)
    }else{
      proxyQueue.offer(proxy)
      GetProxyResult(retryTime,Some(proxy))
    }
  }

  def removeProxy(proxy:String)={
    proxyQueue.remove(proxy)
    if(proxyQueue.size()<minUsableProxyNum){
      executor.schedule(fetchProxyRunnable, 200, TimeUnit.MILLISECONDS)
    }
  }

  /**
   * 解析json
   * @param content
   * @return
   */
  private def json2List(content: String) = {
    implicit val formats = DefaultFormats
    val status = (parse(content) \ "status").extract[Int]
    var list = List[String]()
    if (status == 0) {
      val proxyList = parse(content) \ "proxy"
      list = list ::: proxyList.extract[List[String]]
    }
    list
  }

  private def getProxyFromApate(url:String) = {
    logger.info("geting proxy from apate..............................")
    val segs = url.split("&")
    val retryTime = segs(1).split("=")(1).toInt
    val baseUrl = segs(0)
    if(retryTime>fetchProxyMaxRetryTime){
      //警告 todo
      logger.info("can not get proxy from proxy server .............." )
    }else{
      if(isFetchingProxy.compareAndSet(false,true)){
        try {
          val result = httpClientUtil.getResponseContent(fetchProxyUrl,"utf-8")
          logger.info("get proxy success from server .............")
          isFetchingProxy.set(false)
          if(result!=null && result.nonEmpty){
            var list = json2List(result)
            val all = mutable.HashSet[String]()
            all ++= proxyQueue.asScala.toSet
            all ++= list
            proxyQueue.clear()
            proxyQueue.addAll(all.asJava)
          }else{
            logger.info("get nothing from proxy server ..............")
          }
        }catch {
          case e:Exception =>{
            isFetchingProxy.set(false)
            logger.info("get proxy fail with " + retryTime +" times,",e)
            val retryUrl = baseUrl + "&retry="+(retryTime+1)
            executor.schedule(new FetchProxyRunnable(retryUrl), 200, TimeUnit.MILLISECONDS)
          }
        }
      }else{
        logger.info("already fetching proxy from proxy server .............")
      }
    }

  }


  class FetchProxyRunnable(url:String) extends Runnable {  //实现Runnable接口 以实现多线程
    override def run(): Unit = {
     getProxyFromApate(url)
    }
  }


  class ReportProxyRunnable extends Runnable{
    override def run(): Unit = {
      logger.info("Proxy queue report:"+DateTime.now.toString("yyyy-MM-dd HH:mm:ss") + " size = " + proxyQueue.size())
    }
  }
}
