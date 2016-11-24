package com.neo.sk.arachne2.jobs.youxi1

import com.neo.sk.arachne2.ActorManager
import com.neo.sk.arachne2.core.HttpFetchResult
import com.neo.sk.arachne2.core.statistic.MaxFileInfo
import com.neo.sk.arachne2.utils.TimeUtil
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

/**
 * Created by springlustre on 2016/7/27.
 */


object Dongman1ParseUtil {
  private final val logger = LoggerFactory.getLogger(getClass)

  /** 处理文章列表
    * */
  def listParser(
    fetchResult: HttpFetchResult,
    jobName: String,
    jobId: Long,
    maxFileInfo: MaxFileInfo,
    prefix: String
    ) = {
    logger.debug("parsing page: " + fetchResult.url + prefix + "> >>>>> >>>>> >>>>>>>>>>>>>>>")
    val content = fetchResult.content
    val doc = Jsoup.parse(content)

    var needSaveMaxId = false
    var isContinue = true

    val newsListElement = doc.select("#j-imgtextlist").get(0).select("li")

    if (newsListElement != null && newsListElement.size() > 0) {
      for (newsElement <- newsListElement) {
        val infoUrl = newsElement.select("div.imgbox").select("a").get(0).attr("href")
        val formatTime = newsElement.select("p.labelbox").get(0).select("span").get(1).attr("data-time") //.split(".").toList//.head
        val timeParttern = "(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2})".r
        val postTimeParttern = timeParttern findFirstIn formatTime //用正则匹配得到时间
        val postTime = TimeUtil.parseToTimeStamp(postTimeParttern.getOrElse("")) //发布时间
        maxFileInfo.getOldMaxByKey(prefix) match {
          case Some(oldTime) =>
            if (postTime > oldTime.toLong) {
              //更新的新闻
              logger.info(s"the post time $postTime:{${TimeUtil.formatTimeStamp(postTime)}}" +
                          s"and oldTime is{${TimeUtil.formatTimeStamp(oldTime.toLong)}}")
              ActorManager.addUrl(infoUrl, jobName, jobId) //添加url
              maxFileInfo.getNewMaxByKey(prefix) match {
                //更新maxFileInfo中的键
                case Some(newTime) =>
                  if (newTime.toLong < postTime) {
                    //将记录的时间为最新的新闻发布时间
                    maxFileInfo.setNewMaxItem(prefix, postTime.toString)
                    needSaveMaxId = true
                  }
                case None =>
                  maxFileInfo.setNewMaxItem(prefix, postTime.toString)
                  needSaveMaxId = true
              }
            } else {
              isContinue = true
              needSaveMaxId = true
            }
          case None =>
            ActorManager.addUrl(infoUrl, jobName, jobId) //添加url
            maxFileInfo.setNewMaxItem(prefix, postTime.toString)
            needSaveMaxId = true
        }
      }
    }

    if (isContinue) {
      val curPage = doc.select(".splitpage").get(0).select("a.selected").text().toInt
      if (curPage != 1) {
        // not reach the first page , to continue
        val nextPageUrl = doc.select(".splitpage").get(0).select(".cms_pages").
          filter(_.text() == (curPage - 1).toString).get(0).attr("href")
        ActorManager.addUrl(nextPageUrl, jobName, jobId) //添加url
        logger.info(s"====next page is $nextPageUrl")
      } else {
        //the first page
        isContinue = false
        logger.info("the news crawl finished! lalala~~")
      }
    }

    if (needSaveMaxId) {
      logger.info("save the maxId, lalala~~~")
      maxFileInfo.saveMaxList()
    }
  }


  /**
   * 新闻内容解析
   */
  def newsParse(fetchResult: HttpFetchResult, jobName: String, jobId: Long) = {
    val Dongman1Article = new Dongman1Article()
    val content = fetchResult.content
    //    val content = Source.fromFile("seed/html.txt").mkString
    val doc = Jsoup.parse(content)
    if (doc.select(".container").size() > 0) {
      val contaner = doc.select(".container").get(0)
      val title = contaner.select(".title").get(0).select(".title >h1").get(0).text() //title
      val description = doc.select("meta[name=Description]").get(0).attr("content") //描述

      val sourceEle = contaner.select("#source_text")
      val timeFormat = sourceEle.text().split(" ")(0) + " " + sourceEle.text().split(" ")(1)
      val time = TimeUtil.parseToTimeStamp(timeFormat) // time
      val source = sourceEle.select(".f12_yellow").get(0).text()
      val author = sourceEle.select(".f12_yellow").get(1).text()

      val content = contaner.select("#text").get(0)

      val pics = content.select("img").map(_.attr("src")).mkString("#") //新闻图片
      val thumbnail = pics.split("#").headOption.getOrElse("")

      Dongman1Article.title = title
      Dongman1Article.description = description
      Dongman1Article.picUrls = pics
      Dongman1Article.time = time
      Dongman1Article.author = author
      Dongman1Article.source = source
      Dongman1Article.thumbnail = thumbnail
      Dongman1Article.content = content.text()
      Dongman1Article.url = fetchResult.url
      Dongman1Article.cateId = 1
      Some(Dongman1Article)
    } else {
      None
    }
  }


}
