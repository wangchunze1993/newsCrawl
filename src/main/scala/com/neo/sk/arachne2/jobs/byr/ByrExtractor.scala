package com.neo.sk.arachne2.jobs.byr

import com.neo.sk.arachne2.ActorManager
import com.neo.sk.arachne2.core.{AbstractWebExecutor, HttpFetchResult}
import org.apache.http.client.CookieStore
import org.slf4j.LoggerFactory

/**
 * Created by 王春泽 on 2016/4/11.
 */
class ByrExtractor extends AbstractWebExecutor{

  private final val logger = LoggerFactory.getLogger(getClass)

  override def getCookieStore: CookieStore = null

  /**
   * 网页操作接口
   */
  override def process(fetchResult: HttpFetchResult, jobName: String, jobId: Long,dataType:String): Unit = {
    //todo
    //http://m.byr.cn/board/Java/0?p=1
    //http://m.byr.cn/article/Java/single/44814/0
    //http://m.byr.cn/article/OracleClub/single/305/0

    val url = fetchResult.url
    val boardRegex  = "http://m.byr.cn/board/(.*)/0\\?p=(\\d+)".r
    val detailRegex = "http://m.byr.cn/article/(.*)/single/(\\d+)/0".r
    url match {
      case boardRegex(boardName,page) =>{
        val result = ByrParseUtil.getUrls(fetchResult,jobName,jobId,getMaxInfoClient,boardName,page.toInt)
      }
      case detailRegex(boardName,id) =>{
        val articleOpt = ByrParseUtil.getArticle(fetchResult,jobName,jobId,boardName,id)
        if(articleOpt.isDefined){
//          logger.info(s"author==="+articleOpt.get.authorId)
          getStatisticClient.addItem(1,1)
//          ActorManager.save2EoleTask(jobName,jobId,dataType,articleOpt.get.toString,1)
          logger.info(articleOpt.get.toString)
        }else{
          getStatisticClient.addItem(1,0)
          logger.info(s"can not get article detail ............$jobName-$jobId in $url")
        }
      }
      case u@_=> logger.info(s"unknow url regex : $u")
    }
  }
}
