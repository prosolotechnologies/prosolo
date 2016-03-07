package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.prosolo.bigdata.dal.cassandra.LogEventDBManager;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.streaming.Topic;
import org.prosolo.common.domainmodel.activities.events.EventType;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.google.gson.JsonParser;

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
		String getLogEventsInPeriod = "SELECT * FROM logevents where actorid = ?  and timestamp >= ? and timestamp <= ? ALLOW FILTERING;";
		this.preparedStatements.put("insertDefaultEvent", this.getSession()
				.prepare(insertLogEvent));
		this.preparedStatements.put("getLogEventsInPeriod", this.getSession()
				.prepare(getLogEventsInPeriod));

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


	@Override
	public List<LogEvent> getLogEventsBetweenTimestamps(long actorId, long fromTimestamp,
			long toTimestamp) {
		BoundStatement boundStatement = new BoundStatement(
				this.preparedStatements.get("getLogEventsInPeriod"));
		boundStatement.setLong(0, actorId);
		boundStatement.setLong(1, fromTimestamp);
		boundStatement.setLong(2, toTimestamp);
		final JsonParser parser = new JsonParser();
		return map(query(boundStatement), (row) -> {
			LogEvent logEvent = new LogEvent();
			logEvent.setActorId(row.getLong(0));
			logEvent.setTimestamp(row.getLong(1));
			logEvent.setActorFullname(row.getString(2));
			logEvent.setEventType(EventType.valueOf(row.getString(3)));
			if(StringUtils.isNotBlank(row.getString(5))){
				logEvent.setLearningContext(parser.parse(row.getString(5)).getAsJsonObject());
			}
			logEvent.setLink(row.getString(6));
			logEvent.setObjectId(row.getLong(7));
			logEvent.setObjectTitle(row.getString(8));
			logEvent.setObjectType(row.getString(9));
			if(StringUtils.isNotBlank(row.getString(10))){
				logEvent.setParameters(parser.parse(row.getString(10)).getAsJsonObject());
			}
			logEvent.setReasonId(row.getLong(11));
			logEvent.setReasonType(row.getString(12));
			logEvent.setTargetId(row.getLong(13));
			logEvent.setTargetType(row.getString(14));
			logEvent.setTopic(Topic.valueOf(row.getString(15)));
			return logEvent;
		});
		
	}
	
	private List<Row> query(BoundStatement statement) {
		return getSession().execute(statement).all();
	}
	
	private <T> List<T> map(List<Row> rows, Function<Row, T> function) {
		return rows.stream().map(function).collect(Collectors.toList());
	}
	
}
