package com.neo.sk.arachne2.jobs.wangyi

import java.io.{PrintWriter, FileWriter, File}

import com.github.nscala_time.time.Imports._
import com.neo.sk.arachne2.ActorManager
import com.neo.sk.arachne2.core.HttpFetchResult
import com.neo.sk.arachne2.core.statistic.MaxFileInfo
import com.neo.sk.arachne2.utils.TimeUtil
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
/**
 * Created by 王春泽 on 2016/3/4.
 */
object WangyiParseUtil {
  private final val logger = LoggerFactory.getLogger(getClass)

  /**处理文章列表
   * */
  def listParser(
                fetchResult:HttpFetchResult,
                jobName:String,
                jobId:Long,
                maxFileInfo:MaxFileInfo,
                prefix:String
                  )={
    logger.debug("parsing page: " + fetchResult.url + prefix+"> >>>>> >>>>> >>>>>>>>>>>>>>>")
    val content=fetchResult.content
    val doc = Jsoup.parse(content)

    var needSaveMaxId=false
    var isContinue=true

    val newsListElement=doc.select("div.area-left").get(0).select("> div.list-item")

    if (newsListElement != null && newsListElement.size() > 0) {
      for (newsElement <- newsListElement) {
        val infoUrl = newsElement.select("div").get(0).select("h2").get(0).select("a").get(0).attr("href") //新闻详情的url
        val formatTime = newsElement.select("div").get(0).select("p").get(0).select("span").get(0).text()
        val postTime = TimeUtil.parseToTimeStamp(formatTime) //发布时间
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

    if(isContinue){
      val nextPageItem = doc.select("div.list-page").get(0).select(".pre").get(0)
      val nextPageUrl = nextPageItem.attr("href")
      if(nextPageUrl=="#"){
        isContinue=false
        logger.info("the news crawl finished! lalala~~")
      }else {
//        val nextPageUrl = nextPageItem.attr("href")
        ActorManager.addUrl(nextPageUrl, jobName, jobId) //添加url
        logger.info(s"====next page is $nextPageUrl")
      }
    }

    if(needSaveMaxId){
      logger.info("save the maxId, lalala~~~")
      maxFileInfo.saveMaxList()
    }
  }


  /**
   * 新闻内容解析
   */
  def newsParse(fetchResult: HttpFetchResult,jobName:String,jobId:Long)={
    val wangyiArticle=new WangyiArticle()
    val content=fetchResult.content

    val doc = Jsoup.parse(content)
    if (doc.select(".post_content_main").size() > 0) {
      wangyiArticle.title = doc.title() //标题
      wangyiArticle.description = doc.select("meta[name=description]").get(0).attr("content") //描述

      val sourceEle=doc.select(".ep-source, .cDGray")
      wangyiArticle.author = sourceEle.select(".ep-editor").get(0).text().split("：").last.split("_").head//作者
      wangyiArticle.source=sourceEle.select(".left").get(0).html().split("：").last //来源

      val contentEles = doc.getElementById("endText").select("p")
      var content=""
      contentEles.filterNot(x=>x.hasClass("f_center")).foreach{p=>
        content+=p.text()+"<p>"
      }
      wangyiArticle.content=content //正文

      val postTimeEle=doc.select(".post_time_source").get(0)
      val timeParttern="(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2})".r
      val postTime=timeParttern findFirstIn postTimeEle.text() //用正则匹配得到时间
      wangyiArticle.time = TimeUtil.parseToTimeStamp(postTime.getOrElse("")) //发布时间

      val pics=contentEles.select("img").map(_.attr("src")).mkString("#") //新闻图片
      wangyiArticle.picUrls = pics
      wangyiArticle.thumbnail=pics.split("#").headOption.getOrElse("") //略缩图
      wangyiArticle.url = fetchResult.url //新闻url

      Some(wangyiArticle)
    } else {
      None
    }
  }


}
