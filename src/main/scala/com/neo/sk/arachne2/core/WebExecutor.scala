package com.neo.sk.arachne2.core

/**
 * User: Huangshanqi
 * Date: 2015/10/15
 * Time: 10:55
 */
trait WebExecutor {
  /**
   * 网页操作接口
   */

  def process(fetchResult: HttpFetchResult,jobName:String,jobId:Long,dataType:String)
}
