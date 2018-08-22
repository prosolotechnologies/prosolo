package org.prosolo.bigdata.dal.cassandra.impl

import org.slf4j.LoggerFactory

/**
  * Created by zoran on 08/08/17.
  */
object LogType extends Enumeration{
  val INFO=Value("INFO")
  val ERROR=Value("ERROR")
  val REPORT=Value("REPORT")
  val STAGE=Value("STAGE")
}
object LogSeverity extends Enumeration{
  //info
  val GENERAL=Value("GENERAL")
  val WARNING=Value("WARNING")
  //error
  val CRITICAL=Value("CRITICAL")
  val MAJOR=Value("MAJOR")
  val MINOR=Value("MINOR")
  val TRIVIAL=Value("TRIVIAL")

  //report

  //stage
  val START=Value("START")
  val END=Value("END")
}

class JobLoggerDAO (val dbName:String) extends Entity with Serializable {
  val logger = LoggerFactory.getLogger(getClass)
  override val keyspace=dbName

  def insertJobLog(jobid:String, logtype:LogType.Value, loglevel:LogSeverity.Value, message:String): Unit ={
    val query= s"INSERT INTO $keyspace." + TablesNames.JOBS_LOGS + "(jobid, logtype, loglevel, timestamp, message) VALUES(?,?,?,?); "
    DBManager.connector.withSessionDo { session ⇒
      //val currentTime:java.lang.Long=System.currentTimeMillis()
      session.execute(query,jobid, logtype.toString,loglevel.toString,System.currentTimeMillis().asInstanceOf[java.lang.Long],message.toString )
      logger.debug("INSERT JOB LOG:"+jobid+" logtype:"+logtype.toString+" loglevel:"+loglevel.toString+" message:"+message)
    }
  }

}
