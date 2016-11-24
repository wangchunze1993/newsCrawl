package com.neo.sk.arachne2.common

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory


/**
 * User: Huangshanqi
 * Date: 2015/6/2
 * Time: 15:22
 */
object AppSettings {
  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  val config =  ConfigFactory.load()

  //slick db
//  val slickConfig =config.getConfig("slick.db")
//  val slickUrl = slickConfig.getString("url")
//  val slickUser = slickConfig.getString("user")
//  val slickPassword = slickConfig.getString("password")
//  val slickMaximumPoolSize = slickConfig.getInt("maximumPoolSize")
//  val slickConnectTimeout = slickConfig.getInt("connectTimeout")
//  val slickIdleTimeout = slickConfig.getInt("idleTimeout")
//  val slickMaxLifetime = slickConfig.getInt("maxLifetime")

  //proxy
  val proxyConfig = config.getConfig("proxy")
  //代理获取延时(second)
  val fetchProxyDelay = proxyConfig.getInt("fetchProxyDelay")
  //代理获取周期(second)
  val fetchProxyInterval = proxyConfig.getInt("fetchProxyInterval")
  //代理获取接口
  val fetchProxyUrl = proxyConfig.getString("fetchProxyUrl")
  //代理获取数量
  val fetchProxyNum = proxyConfig.getInt("fetchProxyNum")

  //triton
  val tritonConfig = config.getConfig("triton")
  val tritonUsername = tritonConfig.getString("username")
  val tritonPassword = tritonConfig.getString("password")
  val tritonHost = tritonConfig.getString("host")
  val tritonPort =tritonConfig.getInt("port")

  //eole
  val eoleConfig = config.getConfig("eole")
  val eoleUsername = eoleConfig.getString("username")
  val eoleKey = eoleConfig.getString("key")
  val eoleTritonHost = eoleConfig.getString("tritonHost")
  val eoleSaveDir = eoleConfig.getString("saveDir")
  val maxSave2EoleActorNum = eoleConfig.getInt("maxSave2EoleActorNum")
  val bufferLength = eoleConfig.getInt("bufferLength")

}

