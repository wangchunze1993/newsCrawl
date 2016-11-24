package com.neo.sk.arachne2.test

import java.io.{FileWriter, PrintWriter, File}
import java.util.regex.Pattern

import com.github.nscala_time.time.Imports._
import com.neo.sk.arachne2.core.HttpFetchResult
import com.neo.sk.arachne2.jobs.byr.ByrParseUtil
import com.neo.sk.arachne2.jobs.wangyi.WangyiArticle
import com.neo.sk.arachne2.models.newsDAO
import com.neo.sk.arachne2.utils.TimeUtil
import com.neo.sk.arachne2.utils.TimeUtil._
import com.neo.sk.arachne2.utils.http.HttpClientUtil
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global
/**
 * Created by 王春泽 on 2016/3/4.
 * */
object Test {
  private final val logger=LoggerFactory.getLogger(getClass)
  def regexTest()={
    //http://news.163.com/domestic/
    //http://news.163.com/special/0001124J/guoneinews_03.html#headList
    //详情页  http://news.163.com/16/0304/09/BHA9P6T90001124J.html#f=dlist
    val url="http://news.163.com/16/0304/08/BHA63KMG00014JB5.html#f=dlist"
//    val url = "http://news.163.com/special/0001124J/guoneinews_03.html#headList"
    val listRegex = "(http://news.163.com/)(\\w{3,}).*".r
    val newsRegex = "(http://news.163.com/)(\\d{2}).*".r
    println(listRegex findFirstIn(url))
    url match {
      case listRegex(prefix,page) =>
        println("list"+prefix,page)
      case newsRegex(prefix,year) =>
        println("news"+prefix,year)
      case _ =>
        println(url)
    }
  }

  def listParse()={
    val content=Source.fromFile("seed/html.txt").mkString
    val doc=Jsoup.parse(content)
    val newsListElement=doc.select("div.area-left").get(0).select("> div.list-item")
    if(newsListElement != null && newsListElement.size() > 0){
      for(newsElement <- newsListElement){
        val infoUrl=newsElement.select("div").get(0).select("h2").get(0).select("a").get(0).attr("href")
        val formatTime=newsElement.select("div").get(0).select("p").get(0).select("span").get(0).text()
        val postTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime(formatTime).getMillis//发布时间
        println(infoUrl,postTime)
      }
    }
  }

  def newsParse= {
    val wangyiArticle = new WangyiArticle()
    val content = Source.fromFile("seed/html.txt").mkString
    val doc = Jsoup.parse(content)
    if (doc.select(".post_content_main").size() > 0) {
      wangyiArticle.title = doc.title() //标题
      wangyiArticle.description = doc.select("meta[name=description]").get(0).attr("content") //描述

      val sourceEle=doc.select(".ep-source, .cDGray")
      println(sourceEle.size())
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
      wangyiArticle.thumbnail=pics.split("#").headOption.getOrElse("")

      Some(wangyiArticle)
    } else {
      None
    }
  }

  def save2File(jobName:String,jobId:Long,dataType:String,content:String)={
    val curTime=System.currentTimeMillis()
    val fileName=formatTimeStamp(curTime,"yyyy-MM-dd-HH-mm-ss")
    val filesDir = new File(s"tempTxt/$jobName")
    (filesDir.exists,filesDir.isDirectory) match {
      case (true, true) => {
        val aa=filesDir.listFiles().exists(f=>(curTime-parseToTimeStamp(f.getName.dropRight(4),"yyyy-MM-dd-HH-mm-ss"))<5*60*1000L)
        val targetFile = filesDir.listFiles().find(f=>
            (curTime-parseToTimeStamp(f.getName.dropRight(4),"yyyy-MM-dd-HH-mm-ss"))<5*60*1000L)
              .getOrElse(new File(s"$filesDir/$fileName.txt" ))
        val fileWriter= new FileWriter(targetFile,true)
        val printWriter= new PrintWriter(fileWriter)
        printWriter.println(content)
        printWriter.close()
        fileWriter.close()
      }

      case (false, _) => {
        logger.info(s"jobs dir $filesDir is not exist !!!")
      }

      case (_, false) => {
        logger.info(s"$filesDir is not a directory  !!!")
      }
    }
  }


  def file2DB(jobName:String)={
    val curTime=System.currentTimeMillis()
    val fileName=formatTimeStamp(curTime,"yyyy-MM-dd-HH-mm-ss")
    val filesDir = new File(s"tempTxt/$jobName")
    (filesDir.exists,filesDir.isDirectory) match {
      case (true, true) => {
        val targetFile = filesDir.listFiles().filter(f=>
          (curTime-parseToTimeStamp(f.getName.dropRight(4),"yyyy-MM-dd-HH-mm-ss"))>1*60*1000L)

        val contentList=
          targetFile.flatMap { file =>
            val source = Source.fromFile(file, "GBK")
            val content = source.getLines().filter(_.split("\u0001").length==12)
            content.map { line =>
              val arr = line.split("\u0001")
              (arr(0),arr(1),arr(2).split(" ").head,arr(3),arr(4),arr(5).toLong,arr(6),arr(7),arr(8).toInt,arr(9),arr(10),arr(11))
            }
          }.toList

        newsDAO.insertData(contentList).onComplete{
          case scala.util.Success(_) =>
            println("--successful!!--")
            targetFile.foreach(f=>f.deleteOnExit())
          case scala.util.Failure(_) =>
            println("database operation failed >>>>>>>>>>>>>>>>>")

          case _ => println("-------------")
        }

      }


      case (false, _) => {
        logger.info(s"jobs dir $filesDir is not exist !!!")
      }
      case (_, false) => {
        logger.info(s"$filesDir is not a directory  !!!")
      }
    }

  }

  def deleteFile()={
    val filesDir = new File("tempTxt/test")
    val targetFile = filesDir.listFiles()
    targetFile.foreach{f=>f.delete()}
  }

  def main(args: Array[String]) {
//    save2File("wangyi",10021L,"news","aaaaaaaaaaaaaaaaaaaaaaaa sssss ")
//    file2DB("wangyi")
    deleteFile()
    Thread.sleep(1000*5)
//    println(newsParse(new HttpFetchResult("","",1,1),"",1L))
  }

  def testByrArticleParse() = {
    val content = new HttpClientUtil().getResponseContent("http://m.byr.cn/article/SICE/single/10066","utf-8")
    val result = HttpFetchResult(
      url= "http://m.byr.cn/article/SICE/single/10095/0",
      content,
      httpCode = 200,
      retryTime=1
    )

   val opt =  ByrParseUtil.getArticle(result,"byr",1111L,"life","1212221")
    println(opt)
  }
}
