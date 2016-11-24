package com.neo.sk.arachne2.jobs.byrboard

import java.io.{FileOutputStream, OutputStreamWriter, BufferedWriter}

import com.neo.sk.arachne2.ActorManager
import com.neo.sk.arachne2.core.{HttpFetchResult, AbstractWebExecutor}
import org.apache.http.client.CookieStore
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
/**
 * Created by 王春泽 on 2016/4/11.
 */
class ByrboardExtractor extends AbstractWebExecutor{
  private final val logger = LoggerFactory.getLogger(getClass)

  override def getCookieStore: CookieStore = null

  /**
   * 网页操作接口
   */
  override def process(fetchResult: HttpFetchResult, jobName: String, jobId: Long,dataType:String): Unit = {
    val url = fetchResult.url
    val content = fetchResult.content
    val doc = Jsoup.parse(content)
    val uls = doc.getElementsByTag("ul")
    if(uls!=null&&uls.size()>0){
      val lis = uls.get(0).getElementsByTag("li")
      if(lis!=null && lis.size()>0){
        for(li <- lis){
          val text = li.text.trim
          val a = li.getElementsByTag("a")
          if(a!=null && a.size()>0){
            val nextUrl = "http://m.byr.cn"+a.get(0).attr("href") + """/0?p=1"""
            if(text.contains("目录")){
              ActorManager.addUrl(nextUrl,jobName,jobId)
            }else if (text.contains("版面")){
              add2File(nextUrl)
            }else{
              logger.info(s"unknow li text with $url")
            }
          }else{
            logger.info("can not get a from li ......")
          }
        }
      }else{
        logger.info("can not ge li tag from ul .............")
      }
    }else{
      logger.info("can not gel ul tag ..................")
    }
  }


  def add2File(content:String) = {
    val fileName = System.getProperty("user.dir") + "/jobs/output/byrboard.txt"

    try {
      val out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName,true)))
      out.append(content).append("\n")
      out.close()
    } catch {
      case e:Exception => logger.info("error in add2File:",e)
    }
  }

}
