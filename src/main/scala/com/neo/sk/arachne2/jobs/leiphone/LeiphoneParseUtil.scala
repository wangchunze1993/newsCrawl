package com.neo.sk.arachne2.jobs.leiphone

import com.neo.sk.arachne2.core.HttpFetchResult
import com.neo.sk.arachne2.core.statistic.MaxFileInfo
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
import com.github.nscala_time.time.Imports.DateTimeFormat
import com.neo.sk.arachne2.ActorManager

/**
 * User: zhaorui
 * Date: 2015/10/20
 * Time: 17:10
 */
object LeiphoneParseUtil {
  private final val logger = LoggerFactory.getLogger(getClass)

  def pageParser(
                  fetchResult: HttpFetchResult,
                  jobName: String,
                  jobId: Long,
                  maxFileInfo: MaxFileInfo,
                  prefix: String,
                  page: Int
                  ) = {
    logger.debug("parsing page: " + fetchResult.url + s" page=$page >>>>>>>>>>>>>>>>>>>>>>>>>")

    val content = fetchResult.content
    val doc = Jsoup.parse(content)

    var needSaveMaxId = false
    var isContinue = true

      val pageListElements = doc.select("div.lph-pageList").get(0).select("div.wrap")
    if(pageListElements != null && pageListElements.size() > 0){
      val pageListElement = pageListElements.get(0)
      val newsItems = pageListElement.select("div.word")

      for(newsItem <- newsItems if isContinue){
        val timeItem = newsItem.select("div.time").get(0)
        val date = timeItem.select("span")(0).text()
        val time = timeItem.select("span")(1).text()
        logger.info("**********"+s"the date= $date the time=$time")
        val dateRegex = "(\\d+)[^\\d]*(\\d+)[^\\d]*(\\d+).*".r
        val timeRegex = "(\\d+):(\\d+)".r

        val newUrl = newsItem.select("a").get(0).attr("href")

        (date, time) match {
          case (dateRegex(year, month, day), timeRegex(hour, minute)) =>
            val postTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(s"$year-$month-$day $hour:$minute").getMillis//发布时间
            maxFileInfo.getOldMaxByKey(prefix) match {
              case Some(oldTime) =>
                  if (oldTime.toLong > postTime) {
                    isContinue = true
                  } else {
                    ActorManager.addUrl(newUrl, jobName, jobId)
                    maxFileInfo.getNewMaxByKey(prefix) match {
                      case Some(newTime) =>
                        if (newTime.toLong < postTime) { //将记录的时间为最新的新闻发布时间
                          maxFileInfo.setNewMaxItem(prefix, postTime.toString)
                          needSaveMaxId = true
                        }
                      case None =>
                        maxFileInfo.setNewMaxItem(prefix, postTime.toString)
                        needSaveMaxId = true
                    }
                  }

              case None =>
                ActorManager.addUrl(newUrl, jobName, jobId)
                maxFileInfo.setNewMaxItem(prefix, postTime.toString)
                needSaveMaxId = true
            }

          case t@_ =>
            logger.info(s"can not regex match article time $t" )
        }
      }
    }

    if(isContinue){
      val nextPageItem = doc.select("div.lph-pageList").get(0).select("div.lph-paging1").get(0).getElementsByTag("a").get(0)
      val nextPageUrl = nextPageItem.attr("href")
      if(nextPageUrl=="http://www.leiphone.com/page/1#lph-pageList"){
        isContinue=false
        logger.info("the news crawl finished! lalala~~")
      }else {
        val pageRegex = "(http://www.leiphone.com/page/)(\\d+).*".r
        nextPageUrl match {
          case pageRegex(prefix, page) =>
            ActorManager.addUrl(prefix + page, jobName, jobId)
          case _ =>
        }
      }
    }

    if(needSaveMaxId){
      logger.info("save the maxId, lalala~~~")
      maxFileInfo.saveMaxList()
    }
  }


  def newsParser(fetchResult: HttpFetchResult, jobName: String, jobId: Long) = {
    val leiphoneArticle = new LeiphoneArticle()
    val doc = Jsoup.parse(fetchResult.content)
    if(doc.select("div.pageCont").size() > 0){
      leiphoneArticle.title = doc.title()
      leiphoneArticle.description = doc.select("meta[name=description]").get(0).attr("content")
      val authorEle = doc.select("div.pi-author").get(0)
      leiphoneArticle.author = authorEle.select("a[href]").get(0).text()
      val contentEle = doc.select("div.pageCont").get(0)
      leiphoneArticle.content = contentEle.text()
      leiphoneArticle.time = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(authorEle.getElementsByTag("span").map(_.text()).mkString(" ")).getMillis
      leiphoneArticle.picUrls = contentEle.select("img").map(_.attr("src")).mkString("|")
      if(contentEle.select("img").size() > 0){
        leiphoneArticle.thumbnail = contentEle.select("img").get(0).attr("src")
      }

      val newsRegex = "http://www.leiphone.com/([^/]*)/.*".r
      fetchResult.url match {
        case newsRegex(cate) =>
          leiphoneArticle.category = cate
        case _ =>
          logger.warn("no match category>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
      }
      leiphoneArticle.url = fetchResult.url

      if(doc.select("div.pageTag").size() > 0){
        leiphoneArticle.tags = doc.select("div.pageTag").get(0).getElementsByTag("li").map(_.text()).mkString("|")
      }

      Some(leiphoneArticle)
    }else{
      None
    }

  }
}