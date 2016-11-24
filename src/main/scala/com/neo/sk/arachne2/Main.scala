package com.neo.sk.arachne2

import com.neo.sk.arachne2.core.statistic.RemoteActorManager
import org.slf4j.LoggerFactory

/**
 * Created by Õı¥∫‘Û on 2016/4/11.
 */
object Main {
  private final val logger = LoggerFactory.getLogger(getClass)
  def  main (args: Array[String]) {
    logger.info("System starting.................1")
//    RemoteActorManager
    ActorManager.start()
  }

}
