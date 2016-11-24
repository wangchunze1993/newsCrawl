package com.neo.sk.arachne2.jobs.leiphone

import com.neo.sk.arachne2.ActorManager
import com.neo.sk.arachne2.core.{HttpFetchResult, AbstractWebExecutor}
import org.apache.http.client.CookieStore
import org.slf4j.LoggerFactory
import java.io._

/**
 * User: zhaorui
 * Date: 2015/10/20
 * Time: 16:26
 */
class LeiphoneExtractor extends AbstractWebExecutor{
  private final val logger = LoggerFactory.getLogger(getClass)
  override def getCookieStore: CookieStore = {
    null
  }

  /**
   * 网页操作接口
   */
  override def process(fetchResult: HttpFetchResult, jobName: String, jobId: Long,dataType:String) = {
    //http://www.leiphone.com/news/201510/536A5YvgEIQJNrlU.html
    //http://www.leiphone.com/page/1
    val url = fetchResult.url
    val pageRegex = "(http://www.leiphone.com/page/)(\\d+).*".r
    val newsRegex = "(http://www.leiphone.com/news/).*".r

    url match {
      case pageRegex(prefix, page) =>
        LeiphoneParseUtil.pageParser(fetchResult, jobName, jobId, getMaxInfoClient, prefix, page.toInt)
      case newsRegex(prefix) =>
        val articleOpt = LeiphoneParseUtil.newsParser(fetchResult, jobName, jobId)
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
