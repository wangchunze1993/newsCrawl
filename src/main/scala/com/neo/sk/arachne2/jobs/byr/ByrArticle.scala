package com.neo.sk.arachne2.jobs.byr

/**
 * Created by 王春泽 on 2016/4/11.
 */
class ByrArticle(
                  var  url:String,
                  var  id: String,
                  var  reId: String,
                  var  firstId: String,
                  var  boardId: String,
                  var  boardName: String,
                  var  authorId: String,
                  var  postTimeMs: String,//ms
                  var  fromIp: String,
                  var  title: String,
                  var  allContent: String, //带引文回复
                  var  pureContent: String //回复内容
)  {

  def this(){
    this("","","","","","","","0","","","","")
  }


  override def toString = {
    val split: String = "\u0001"
    val builder = new StringBuilder
    builder.append(url).append(split)
    builder.append(id).append(split)
    builder.append(reId).append(split)
    builder.append(firstId).append(split)
    builder.append(boardId).append(split)
    builder.append(boardName).append(split)
    builder.append(authorId).append(split)
    builder.append(postTimeMs).append(split)
    builder.append(fromIp).append(split)
    builder.append(title).append(split)
    builder.append(allContent).append(split)
    builder.append(pureContent)
    builder.toString()
  }
}
