package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.HashMap;
import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.SessionRecord;
import org.prosolo.bigdata.dal.cassandra.LearningEventsDBManager;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;

public class LearningEventsDBManagerImpl extends SimpleCassandraClientImpl implements LearningEventsDBManager {
	
	HashMap<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();
	private static final String INSERT_LEARNING_EVENTS_INFO_QUERY = "insertLearningEventInformation";
	
	public static class LearningEventsDBManagerImplHolder {
		public static final LearningEventsDBManagerImpl INSTANCE = new LearningEventsDBManagerImpl();
	}
	public static LearningEventsDBManagerImpl getInstance() {
		return LearningEventsDBManagerImplHolder.INSTANCE;
	}
	
	private LearningEventsDBManagerImpl() {
		super();
		this.prepareStatements();
	}

	private void prepareStatements() {
		String insertLearningEvents = "INSERT INTO learningevents(actorid,sessionstart, sessionend, number, milestones) VALUES (?,?,?,?,?);";
		this.preparedStatements.put(INSERT_LEARNING_EVENTS_INFO_QUERY, this.getSession()
				.prepare(insertLearningEvents));
	}

	@Override
	public void saveLearningEventsData(SessionRecord sessionRecord, int hits, List<String> milestones) {
		BoundStatement boundStatement = new BoundStatement(
				this.preparedStatements.get(INSERT_LEARNING_EVENTS_INFO_QUERY));
		boundStatement.setLong(0, sessionRecord.getUserId());
		boundStatement.setLong(1, sessionRecord.getSessionStart());
		boundStatement.setLong(2, sessionRecord.getSessionEnd());
		boundStatement.setInt(3, hits);
		boundStatement.setList(4, milestones);
		this.getSession().execute(boundStatement);
	}

}
