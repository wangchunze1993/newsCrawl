package com.neo.sk.arachne2.jobs.youxi1

import com.neo.sk.arachne2.ActorManager
import com.neo.sk.arachne2.core.{AbstractWebExecutor, HttpFetchResult}
import org.apache.http.client.CookieStore
import org.slf4j.LoggerFactory

/**
 * Created by springlustre on 2016/7/27.
 */

class Dongman1Extractor extends AbstractWebExecutor {
  private final val logger = LoggerFactory.getLogger(getClass)

  override def getCookieStore: CookieStore = {
    null
  }

  /** 网页操作部分
    * */
  override def process(fetchResult: HttpFetchResult, jobName: String, jobId: Long, dataType: String) = {
    val url = fetchResult.url
    val listRegex = "(http://acg.178.com/list/donghua/)(\\w{3,}).*".r
    val newsRegex = "(http://acg.178.com/)(\\d{6}).*".r
    url match {
      case listRegex(prefix, category) => //当前页面是文章列表
        Dongman1ParseUtil.listParser(fetchResult, jobName, jobId, getMaxInfoClient, prefix)
        logger.info("文章列表" + prefix)
      case newsRegex(prefix, year) =>
        logger.info("新闻详情" + prefix)
        val articleOpt = Dongman1ParseUtil.newsParse(fetchResult, jobName, jobId)
        articleOpt match {
          case Some(article) =>
            getStatisticClient.addItem(1, 0)
            ActorManager.save2Cache(jobName, jobId, dataType, articleOpt.get.toString())
//            ActorManager.save2File(jobName, jobId, dataType, articleOpt.get.toString())
            println("==============" + article.toString())

          case None =>
            getStatisticClient.addItem(1, 0)
        }
      case u@_ => logger.info(s"unknow url regex : $u")
    }
  }
}
