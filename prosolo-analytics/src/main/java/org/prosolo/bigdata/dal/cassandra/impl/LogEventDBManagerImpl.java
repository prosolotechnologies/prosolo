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

	private LogEventDBManagerImpl() {
		super();
		this.prepareStatements();
	}

	public static class LogEventDBManagerImplHolder {
		public static final LogEventDBManagerImpl INSTANCE = new LogEventDBManagerImpl();
	}
	public static LogEventDBManagerImpl getInstance() {
		return LogEventDBManagerImplHolder.INSTANCE;
	}

	private void prepareStatements() {
		String insertLogEvent = "INSERT INTO logevents (id,"
				+ "topic,"
				+ "eventtype,timestamp, actorid, actorfullname, "
				+ "objecttype, objectid, objecttitle,  "
				+ "targettype, targetid, reasontype, reasonid, link, parameters, learningcontext "

				+ ") VALUES (now(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,? );";
		this.preparedStatements.put("insertDefaultEvent", this.getSession()
				.prepare(insertLogEvent));

	}

	@Override
	public void insertLogEvent(LogEvent event) {
		BoundStatement boundStatement = new BoundStatement(
				this.preparedStatements.get("insertDefaultEvent"));

		try {
			boundStatement.setString(0, event.getTopic().name());
			if(event.getEventType()!=null){
				boundStatement.setString(1, event.getEventType().toString());
			}else{
				boundStatement.setString(1, "");
			}

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
			if(event.getParameters()!=null){
				boundStatement.setString(13, event.getParameters().toString());
			}else{
				boundStatement.setString(13, "");
			}
			if(event.getLearningContext()!=null){
				boundStatement.setString(14, event.getLearningContext().toString());
			}else{
				boundStatement.setString(14, "");
			}

			this.getSession().execute(boundStatement);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
