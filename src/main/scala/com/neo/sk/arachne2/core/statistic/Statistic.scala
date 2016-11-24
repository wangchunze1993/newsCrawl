package com.neo.sk.arachne2.core.statistic

import org.slf4j.LoggerFactory

/**
 * Created by 王春泽 on 2016/4/11.
 */
class Statistic{

  private  val logger = LoggerFactory.getLogger(this.getClass)
  private var allDownNumber:Long = 0L
  private var allParseNumber:Long = 0L


  private var startTime:Long = 0L
  private var endTime:Long = 0L

  private var newLoop = true


  /**
   * 用户调用统计接口
   * @param downNum
   * @param parseNum
   */
  def addItem(downNum:Long,parseNum:Long): Unit ={

    if(newLoop){
      //new loop
      logger.info("Statistic start....................")
      startTime = System.currentTimeMillis()
      newLoop = false
    }
    allDownNumber += downNum
    allParseNumber += parseNum
  }


  /**
   * jobActor调用保存统计统计数据时调用返回统计数据
   * @return
   */
  def saveStatistic() = {

    val result = (allDownNumber,allParseNumber)
    allDownNumber = 0L
    allParseNumber = 0L
    newLoop = true

    result
  }

}
