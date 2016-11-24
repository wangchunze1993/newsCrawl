package com.neo.sk.arachne2.jobs.wangyi

import javax.print.attribute.standard.JobName

import com.neo.sk.arachne2.ActorManager
import com.neo.sk.arachne2.core.{HttpFetchResult, AbstractWebExecutor}
import org.apache.http.client.CookieStore
import org.slf4j.LoggerFactory

/**
 * Created by 王春泽 on 2016/3/4.
 */
class WangyiExtractor extends AbstractWebExecutor {
  private final val logger = LoggerFactory.getLogger(getClass)
  override def getCookieStore:CookieStore = {
    null
  }

  /**网页操作部分
   * */
  override def process(fetchResult:HttpFetchResult,jobName:String,jobId:Long,dataType:String)={
    //http://news.163.com/domestic/
    //http://news.163.com/special/0001124J/guoneinews_03.html#headList
    //详情页  http://news.163.com/16/0304/09/BHA9P6T90001124J.html#f=dlist
    //http://news.163.com/16/0304/08/BHA63KMG00014JB5.html#f=dlist
    val url =fetchResult.url
    val listRegex = "(http://news.163.com/)(\\w{3,}).*".r
    val newsRegex = "(http://news.163.com/)(\\d{2}).*".r
    url match{
      case listRegex(prefix,category)=> //当前页面是文章列表
        WangyiParseUtil.listParser(fetchResult, jobName, jobId, getMaxInfoClient, prefix)
        logger.info("文章列表"+prefix)
      case newsRegex(prefix,year)=>
        logger.info("新闻详情"+prefix)
        val articleOpt = WangyiParseUtil.newsParse(fetchResult, jobName, jobId)
        articleOpt match {
          case Some(article) =>
            getStatisticClient.addItem(1, 0)
            ActorManager.save2Cache(jobName,jobId,dataType,articleOpt.get.toString())
            ActorManager.save2File(jobName,jobId,dataType,articleOpt.get.toString())
            println("=============="+article.toString())

          case None =>
            getStatisticClient.addItem(1, 0)
        }
      case u@_ => logger.info(s"unknow url regex : $u")
    }
  }
}
