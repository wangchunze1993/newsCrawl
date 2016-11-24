package com.neo.sk.arachne2.utils

import org.apache.commons.codec.digest.DigestUtils

import scala.util.Random

/**
 * User: Taoz
 * Date: 7/8/2015
 * Time: 8:42 PM
 */
object SecureUtil {

  val random = new Random(System.currentTimeMillis())

  val chars = Array(
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
  )


  def getSecurePassword(password: String, ip: String, timestamp: Long): String = {
    DigestUtils.md5Hex(DigestUtils.md5Hex(timestamp + password) + ip + timestamp)
  }

  def checkSignature(parameters: List[String], signature: String, secureKey: String) = {
    generateSignature(parameters, secureKey) == signature
  }

  def generateSignature(parameters: List[String], secureKey: String) = {
    val strSeq = ( secureKey :: parameters ).sorted.mkString("")
    DigestUtils.md5Hex(strSeq)
  }

  def generateSignatureParameters(parameters: List[String], secureKey: String) = {
    val timestamp = System.currentTimeMillis().toString
    val nonce = nonceStr(6)
    val pList = nonce :: timestamp :: parameters
    val signature = generateSignature(pList, secureKey)
    (timestamp, nonce, signature)
  }

  def nonceStr(length: Int) = {
    val range = chars.length
    (0 until length).map { _ =>
      chars(random.nextInt(range))
    }.mkString("")
  }




}
