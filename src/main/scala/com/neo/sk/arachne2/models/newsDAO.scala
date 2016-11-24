package com.neo.sk.arachne2.models

import javax.print.attribute.standard.JobName

import com.neo.sk.arachne2.utils.DBUtil._
import slick.driver.MySQLDriver.api._

import scala.collection.mutable

/**
 * Created by 王春泽 on 2016/3/30.
 */

object newsDAO {
  private[this] val news=tables.SlickTables.tNews


  def insertData(list:List[(String,String,String,String,String,Long,String,String,Int,String,String,String)])={
    val q=news.map(w=>(w.title,w.author,w.source,w.thumbnail,w.description,w.createTime,w.content,w.picUrls,
                         w.cateId,w.category,w.url,w.tags))++=list
    db.run(q.transactionally)
  }


}
