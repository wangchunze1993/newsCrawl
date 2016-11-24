package com.neo.sk.arachne2.utils

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource
import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory

/**
 * User: Taoz
 * Date: 2/9/2015
 * Time: 4:33 PM
 */
object DBUtil {
  import com.neo.sk.arachne2.common.Config._
  val log = LoggerFactory.getLogger(this.getClass)

  private val dataSource = createDataSource()

  private def createDataSource() = {

    val mysqlDS = new MysqlDataSource()
    mysqlDS.setURL(slickUrl)
    mysqlDS.setUser(slickUser)
    mysqlDS.setPassword(slickPassword)
    val hikariDS = new HikariDataSource()
    hikariDS.setDataSource(mysqlDS)
    hikariDS.setMaximumPoolSize(slickMaximumPoolSize)
    hikariDS.setConnectionTimeout(slickConnectTimeout)
    hikariDS.setIdleTimeout(slickIdleTimeout)
    hikariDS.setMaxLifetime(slickMaxLifetime)
    hikariDS.setAutoCommit(true)
    hikariDS
  }


  import slick.driver.MySQLDriver.api._
  val db = Database.forDataSource(dataSource)


}