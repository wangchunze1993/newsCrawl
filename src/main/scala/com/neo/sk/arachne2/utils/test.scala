package com.neo.sk.arachne2.utils

import com.neo.sk.arachne2.ActorManager
import com.neo.sk.arachne2.jobs.dongman1.Dongman1Article
import org.jsoup.Jsoup
import scala.collection.JavaConversions._
import scala.io.Source

/**
 * Created by springlustre on 2016/8/1.
 */

object test {

  def main(args: Array[String]): Unit = {
    val Dongman1Article=new Dongman1Article()
    val content = Source.fromFile("seed/html.txt").mkString
    val doc = Jsoup.parse(content)

    val data =
    try {
      if (doc.select(".container").size() > 0) {
        val contaner = doc.select(".container").get(0)
        val title = doc.select("title").get(0).text().split("-").head
        val description = doc.select("meta[name=Description]").get(0).attr("content") //描述

        if (contaner.select(".artical-info").select(".author").nonEmpty) {
          val timeFormat = contaner.select(".author").select(".time").get(0).text() + " 0:0:0"
          val time = TimeUtil.parseToTimeStamp(timeFormat) // time
          val source = "" //sourceEle.select(".f12_yellow").get(0).text()
          val author = contaner.select(".author").text().split(" ").head.split("：").last

          val content = contaner.select(".artical-content").get(0)

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
          Dongman1Article.cateId = 1
          Some(Dongman1Article)
        } else {
          None
        }
      } else {
        None
      }
    }catch{
      case ex:Exception=>
        println(ex)
        None
    }

    println(data)

    Thread.sleep(3000)

  }
}
