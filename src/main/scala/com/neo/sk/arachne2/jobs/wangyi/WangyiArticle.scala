package com.neo.sk.arachne2.jobs.wangyi

/**
 * Created by 王春泽 on 2016/3/4.
 */
class WangyiArticle(
                     var title: String,//标题
                     var author: String,//作者
                     var source:String,//来源
                     var thumbnail: String,//略缩图
                     var description: String,//描述
                     var time: Long,//时间戳
                     var content: String,//内容
                     var picUrls: String,//文章图片
                     var cateId: Int, //类别id
                     var category: String,//类别
                     var url: String, //新闻地址
                     var tags: String //标签
                  ) {
    def this() = {
      this("", "", "", "", "", 0l, "","", 0, "", "", "网易")
    }

    override def toString = {
      val split: String = "\u0001"
      val builder = new StringBuilder
      builder.append(title).append(split)
      builder.append(author).append(split)
      builder.append(source).append(split)
      builder.append(thumbnail).append(split)
      builder.append(description).append(split)
      builder.append(time).append(split)
      builder.append(content).append(split)
      builder.append(picUrls).append(split)
      builder.append(cateId).append(split)
      builder.append(category).append(split)
      builder.append(url).append(split)
      builder.append(tags).append(split)
      builder.toString()
    }
}
