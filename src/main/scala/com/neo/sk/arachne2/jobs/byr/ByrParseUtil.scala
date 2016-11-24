package com.neo.sk.arachne2.jobs.byr

import java.util.regex.Pattern

import com.neo.sk.arachne2.ActorManager
import com.neo.sk.arachne2.core.HttpFetchResult
import com.neo.sk.arachne2.core.statistic.MaxFileInfo
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import com.github.nscala_time.time.Imports.DateTimeFormat
/**
 * Created by 王春泽 on 2016/4/11.
 */
object ByrParseUtil {
  private final val logger = LoggerFactory.getLogger(getClass)

  def getUrls(fetchResult: HttpFetchResult, jobName: String, jobId: Long, maxFileInfo: MaxFileInfo, boardName: String, curPage: Int): List[String] = {
    val result = ListBuffer[String]()
    var needSaveMaxId = false
    var isContinue = true
    val content = fetchResult.content

    val doc = Jsoup.parse(content)

    val uls = doc.getElementsByTag("ul")
    if(uls!=null && uls.size()>0){
      val lis = uls.get(0).getElementsByTag("li")
      if(lis!=null && lis.size()>0){
        for(li <- lis  if isContinue){
          val a = li.select("a.top")
          if(a!=null&&a.size()>0){
            //提示
          }else{
            val divs = li.getElementsByTag("div")
            if(divs!=null && divs.size()==2){
              val as = divs.get(0).getElementsByTag("a")
              if(as!=null && as.size()>0){
                ///article/Java/single/44829/0
                val postIdRegex = "(.*)/single/(\\d+)/(.*)".r
                val postIdText = as.get(0).attr("href")
                postIdText match {
                  case postIdRegex(prefix,postId,postfix) =>{
                    if(maxFileInfo.getOldMaxByKey(boardName).isEmpty){
                      maxFileInfo.setOldMaxItem(boardName,0.toString)
                      maxFileInfo.setNewMaxItem(boardName,0.toString)
                    }
                    if(postId.toLong>maxFileInfo.getOldMaxByKey(boardName).get.toLong){
                      val a = divs.get(0).getElementsByTag("a")
                      if(a!=null&&a.size()>0){
                        val nextUrl = "http://m.byr.cn" + a.get(0).attr("href")
                        ActorManager.addUrl(nextUrl,jobName,jobId)
                        if(postId.toLong > maxFileInfo.getNewMaxByKey(boardName).get.toLong){
                          maxFileInfo.setNewMaxItem(boardName,postId)
                          needSaveMaxId = true
                        }
                      }
                    }else{
                      //已经爬取过了
                      isContinue = false
                    }
                  }
                  case unknow@_ => logger.info(s"unknow postIdRegex .. $postIdText")
                }
              }
            }else{
              logger.info(s"can not get div in li in job $jobName-$jobId........")
            }
          }
        }
        //next page
        val plants = doc.select("a.plant")
        if(plants!=null && plants.size()>0){
          val page = plants.get(0).text.trim
          val pageRegex = "(\\d+)/(\\d+)".r
          page match {
            case pageRegex(cur,last) =>{
              if(!cur.equals(last) && isContinue){
                ////http://m.byr.cn/board/Java/0?p=1
                val nextPageUrl = s"http://m.byr.cn/board/$boardName/0?p=" + (curPage+1)
                ActorManager.addUrl(nextPageUrl,jobName,jobId)
              }
            }
            case unknow@_ => logger.info(s"can not match page info ...$unknow")
          }
        }
      }else{
        logger.info(s"can not get li in ul in job $jobName-$jobId........")
      }

      if(needSaveMaxId){
        maxFileInfo.saveMaxList()
      }
    }
    result.toList
  }

  def getArticle(fetchResult: HttpFetchResult, jobName: String, jobId: Long,boardName:String, id: String): Option[ByrArticle] = {

    val html = fetchResult.content
    var isSuccess = false
    val byrArticle = new ByrArticle()
    byrArticle.url = fetchResult.url
    byrArticle.boardName = boardName
    byrArticle.boardId = boardName
    byrArticle.id = id

    val doc = Jsoup.parse(html)
    val mainDiv = doc.getElementById("m_main")
    if(mainDiv!=null){
      isSuccess = true
      val secDivs = mainDiv.select("div.sec")
      if(secDivs!=null && secDivs.size()>0){
        val as = secDivs.get(0).getElementsByTag("a")
        if(as!=null && as.size()>2){
          //<a href="/article/Qinghai/82732">同主题展开</a>
          for(a <- as){
            val aText = a.text
            aText match {
                //展开|楼主|同主题展开|溯源|返回
              case lz if lz.contains("楼主") =>{
                // <a href="/article/Qinghai/single/82732">楼主</a>
                val url = a.attr("href")
                val idRegex = "(.*)/single/(\\d+)(.*)".r
                url match {
                  case idRegex(prefix,firstId,postfix) => byrArticle.firstId = firstId
                  case unknow@_ => logger.info(s"sunknow idRegex:$url at 楼主url")
                }
              }
              case original if original.contains("溯源") =>{
                //<a href="/article/Qinghai/single/82754">溯源</a>
                val url = a.attr("href")
                val idRegex = "(.*)/single/(\\d+)(.*)".r
                url match {
                  case idRegex(prefix,reId,postfix) => byrArticle.reId = reId
                  case unknow@_ => logger.info(s"sunknow idRegex:$url at 溯源url")
                }
              }
              case other@_  =>
            }
          }
        }
        if(byrArticle.reId=="") byrArticle.reId = byrArticle.id
        val uls = mainDiv.getElementsByTag("ul")
        if(uls!=null&&uls.size()>0){
          val lis = uls.get(0).getElementsByTag("li")
          if(lis!=null&&lis.size()>1){
            byrArticle.title = lis.get(0).text.trim
            val div1 = lis.get(1).select("div.nav")
            val div2 = lis.get(1).select("div.sp")
            if(div1!=null && div1.size()>0){
              val as = div1.get(0).getElementsByTag("a")
              if(as!=null&&as.size()>0){
                byrArticle.authorId = as.get(0).text.trim
                val postTimeText = as.get(1).text.trim
                //2015-10-14 16:05:51
                byrArticle.postTimeMs = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime(postTimeText).getMillis.toString
              }
            }
            if(div2!=null&&div2.size()>0){
             /*
                【 在 crazy0602 的大作中提到: 】
                : 靠。才看到..十一的时候刚好在青海。错过了带你飞得机会
                : 发自「贵邮」
                O(∩_∩)O谢谢啊
                --
                FROM 114.241.14.* [北京市 联通ADSL]
              */
              var content = div2.get(0).html().
               replaceAll("<br\\s*/>","\n").
               replaceAll("<.+>","").
               replaceAll("<.+/>","").
               replaceAll("\n(\n)+","\n")
              byrArticle.allContent = content

              //FROM 114.241.14.*
              val ipRegex = "FROM ((\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d*))(.*)"
              val pattern =Pattern.compile(ipRegex,Pattern.MULTILINE)
              val ipMatch = pattern.matcher(content)
              if(ipMatch.find()){
                byrArticle.fromIp = ipMatch.group(1)
                content = ipMatch.replaceAll("")
              }

              val startRegex = "^:(.*)\n"
              val startRegexPattern = Pattern.compile(startRegex, Pattern.MULTILINE)
              val startRegexMatch = startRegexPattern.matcher(content)
              if (startRegexMatch.find) {
                content = startRegexMatch.replaceAll("")
              }

              val sayRegex = "^【(.*)\n"
              val sayMatch = Pattern.compile(sayRegex, Pattern.MULTILINE).matcher(content)
              if (sayMatch.find) {
                content = sayMatch.replaceAll("")
              }

              val lineRegex = "^--(.*)\n(.*)"
              val linePattern = Pattern.compile(lineRegex, Pattern.MULTILINE)
              val lineMatch = linePattern.matcher(content)
              if (lineMatch.find()){
                content = lineMatch.replaceAll("")
              }

              val pureContent = content
              byrArticle.pureContent = pureContent
            }
          }
        }
      }
    }
    if(isSuccess) Some(byrArticle) else None
  }
}