package com.neo.sk.arachne2.utils

import java.sql.Date
import java.text.SimpleDateFormat

import com.github.nscala_time.time.Imports._

/**
 * Created by 王春泽 on 2016/3/7.
 */
object TimeUtil {
  /**
   * 格式化时间转化成时间戳
   * @param formatTime
   * @return
   */
  def parseToTimeStamp(formatTime:String,format:String = "yyyy-MM-dd HH:mm:ss")={
    DateTimeFormat.forPattern(format).parseDateTime(formatTime).getMillis
  }


  /**
   * 时间戳格式化 yyyy-MM-dd HH:mm:ss
   * @param timeMs
   * @param format
   * @return
   */
  def formatTimeStamp(timeMs:Long,format:String = "yyyy-MM-dd HH:mm:ss") ={
    val data  = new Date(timeMs)
    val simpleDateFormat = new SimpleDateFormat(format)
    simpleDateFormat.format(data)
  }



}
