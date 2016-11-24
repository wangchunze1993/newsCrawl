package com.neo.sk.arachne2.cache

import akka.actor.{Props, Actor}
import com.neo.sk.arachne2.common.{Cache2DB, Save2Cache}
import org.slf4j.LoggerFactory

/**
 * Created by 王春泽 on 2016/4/11.
 */

class CacheManager extends Actor{

  private final val logger = LoggerFactory.getLogger(getClass)

  private val jobNames=List("wangyi","leifeng")

  @throws[Exception](classOf[Exception])
  override def preStart():Unit={
    logger.info("CacheManeger-"+self.path.name+"is starting..............")
  }

  @throws[Exception](classOf[Exception])
  override def postStop():Unit={
    logger.info("CacheManeger-"+self.path.name+"is stopping..............")
  }

  override def receive:Receive={
    //保存到cache
    case Save2Cache(jobName:String,jobId:Long,dataType:String,content:String)=>{
      context.child(s"CacheActor-$jobName-$jobId").getOrElse{
        context.actorOf(Props[CacheActor](new CacheActor(jobName:String,jobId:Long)),name=s"CacheActor-$jobName-$jobId")
      } ! Save2Cache(jobName,jobId,dataType,content)
    }

    //从cache写入数据库
    case Cache2DB(jobName:String,jobId:Long) =>
      context.child(s"CacheActor-$jobName-$jobId").getOrElse{
        context.actorOf(Props[CacheActor](new CacheActor(jobName:String,jobId:Long)),name=s"CacheActor-$jobName-$jobId")
      } ! Cache2DB(jobName,jobId)



    case unknow@_ => logger.info("unknow message: "+unknow + "in"+context.self.path.name+"from "+context.sender().path.name)
  }
}
