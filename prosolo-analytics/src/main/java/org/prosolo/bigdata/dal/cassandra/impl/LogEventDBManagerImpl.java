package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.HashMap;

import org.prosolo.bigdata.dal.cassandra.LogEventDBManager;
import org.prosolo.bigdata.events.pojo.LogEvent;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author Zoran Jeremic Apr 6, 2015
 *
 */

public class LogEventDBManagerImpl extends SimpleCassandraClientImpl implements
		LogEventDBManager {
	HashMap<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();

	public LogEventDBManagerImpl() {
		super();
		this.prepareStatements();
	}

	private void prepareStatements() {
		String insertLogEvent = "INSERT INTO logevents (id,"
				+ "topic,"
				+ "eventtype,timestamp, actorid, actorfullname, "
				+ "objecttype, objectid, objecttitle,  "
				+ "targettype, targetid, reasontype, reasonid, link, parameters "

				+ ") VALUES (now(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? );";
		this.preparedStatements.put("insertDefaultEvent", this.getSession()
				.prepare(insertLogEvent));

	}

	@Override
	public void insertLogEvent(LogEvent event) {
		BoundStatement boundStatement = new BoundStatement(
				this.preparedStatements.get("insertDefaultEvent"));

		try {
			boundStatement.setString(0, event.getTopic().name());
			boundStatement.setString(1, event.getEventType().toString());
			boundStatement.setLong(2, event.getTimestamp());
			boundStatement.setLong(3, event.getActorId());
			boundStatement.setString(4, event.getActorFullname());
			boundStatement.setString(5, event.getObjectType());
			boundStatement.setLong(6, event.getObjectId());
			boundStatement.setString(7, event.getObjectTitle());
			boundStatement.setString(8, event.getTargetType());
			boundStatement.setLong(9, event.getTargetId());
			boundStatement.setString(10, event.getReasonType());
			boundStatement.setLong(11, event.getReasonId());
			boundStatement.setString(12, event.getLink());
			boundStatement.setString(13, event.getParameters().toString());
			this.getSession().execute(boundStatement);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
