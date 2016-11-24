package com.neo.sk.arachne2.core

import org.apache.http.HttpStatus

/**
 * User: Huangshanqi
 * Date: 2015/10/16
 * Time: 15:59
 */
case class HttpFetchResult(url:String,content:String,httpCode:Int,retryTime:Int)
