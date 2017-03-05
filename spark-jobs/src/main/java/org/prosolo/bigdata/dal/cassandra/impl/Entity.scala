package org.prosolo.bigdata.dal.cassandra.impl

import com.datastax.driver.core.ProtocolVersion
import com.datastax.spark.connector.cql.TableDef

/**
  * Created by zoran on 04/03/17.
  */
/**
  * zoran 04/03/17
  */
trait Entity {
    val keyspace: String
    //val table: String

 // lazy val tableDef: TableDef = DBManager.schema.keyspaceByName(keyspace).tableByName(table)

  implicit val protocolVersion: ProtocolVersion = ProtocolVersion.NEWEST_SUPPORTED
}
