package com.neo.sk.arachne2.core

import com.neo.sk.arachne2.core.statistic.{MaxFileInfo, Statistic}
import org.apache.http.client.CookieStore

/**
 * User: Huangshanqi
 * Date: 2015/10/15
 * Time: 10:58
 */
abstract class AbstractWebExecutor extends WebExecutor{
/**
 * job actor 抽象类
 */

  //统计相关
  private val statistic = new Statistic
  //增量相关
  private val maxInfo = new MaxFileInfo

  def getCookieStore: CookieStore

  def getStatisticClient = statistic
  def getMaxInfoClient = maxInfo
}
