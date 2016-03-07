package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.MapUtils;
import org.prosolo.bigdata.common.dal.pojo.SessionRecord;
import org.prosolo.bigdata.dal.cassandra.LearningEventsDBManager;
import org.prosolo.bigdata.session.impl.LearningEventsMatchSummary.DayInYear;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BatchStatement.Type;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Statement;

public class LearningEventsDBManagerImpl extends SimpleCassandraClientImpl implements LearningEventsDBManager {
	
	HashMap<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();
	private static final String UPDATE_LEARNING_EVENTS_COUNTER = "updateLearningEventsCounter";
	private static final String UPDATE_LEARNING_MILESTONES = "updateLearningEventsMilestones";
	
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
		String updateLearningEventsCounter = "UPDATE learningevents SET number = number + ? WHERE actorid=? AND year=? and dayofyear=?;";
		this.preparedStatements.put(UPDATE_LEARNING_EVENTS_COUNTER, this.getSession()
				.prepare(updateLearningEventsCounter));
		
		String updateLearningEventsMilestones = "UPDATE learningmilestones SET milestones = ? + milestones WHERE actorid = ? and year = ? and dayofyear = ?;";
		this.preparedStatements.put(UPDATE_LEARNING_MILESTONES, this.getSession()
				.prepare(updateLearningEventsMilestones));
	}

	@Override
	public void saveLearningEventsData(SessionRecord sessionRecord, Map<DayInYear, Integer> learningEventsPerDay, 
			Map<DayInYear, List<String>> milestonesPerDay) {
		//one is counter based, the other is not, so we canno't batch them together, but we can batch statements within them
		createEventNumberStatement(learningEventsPerDay, sessionRecord)
			.ifPresent(statement -> this.getSession().execute(statement));
		createEventMilestonesStatement(milestonesPerDay, sessionRecord)
			.ifPresent(statement -> this.getSession().execute(statement));
		
	}
	
	private Optional<Statement> createEventNumberStatement(Map<DayInYear, Integer> learningEventsPerDay, SessionRecord sessionRecord) {
		if(MapUtils.isEmpty(learningEventsPerDay)) {
			return Optional.empty();
		}
		//if only one entry is present, create just a BoundStatement
		else if(learningEventsPerDay.size() == 1) {
			return learningEventsPerDay.entrySet().stream().findFirst()
			.map(entry ->  new BoundStatement(
					this.preparedStatements.get(UPDATE_LEARNING_EVENTS_COUNTER))
					.bind(entry.getValue(),sessionRecord.getUserId(),entry.getKey().getYear(),
							entry.getKey().getDayOfYear()));
		}
		//if more than one day-in-year/year is involved, wrap them in a counter batch
		else {
			BatchStatement statement = new BatchStatement(Type.COUNTER);
			for(Map.Entry<DayInYear, Integer> entry : learningEventsPerDay.entrySet()) {
				BoundStatement boundStatement = new BoundStatement(
						this.preparedStatements.get(UPDATE_LEARNING_EVENTS_COUNTER));
				statement.add(boundStatement.bind(entry.getValue(),sessionRecord.getUserId(),
						entry.getKey().getYear(),entry.getKey().getDayOfYear()));
			}
			return Optional.of(statement);
		}
		
	}
	
	private Optional<Statement> createEventMilestonesStatement(Map<DayInYear, List<String>> milestonesPerDay, SessionRecord sessionRecord) {
		if(MapUtils.isEmpty(milestonesPerDay)) {
			return Optional.empty();
		}
		//if only one entry is present, create just a BoundStatement
		else if(milestonesPerDay.size() == 1) {
			return milestonesPerDay.entrySet().stream().findFirst()
			.map(entry ->  new BoundStatement(
					this.preparedStatements.get(UPDATE_LEARNING_MILESTONES))
					.bind(entry.getValue(),sessionRecord.getUserId(),entry.getKey().getYear(),
							entry.getKey().getDayOfYear()));
		}
		//if more than one day-in-year/year is involved, wrap them in a batch
		else {
			BatchStatement statement = new BatchStatement();
			for(Map.Entry<DayInYear, List<String>> entry : milestonesPerDay.entrySet()) {
				BoundStatement boundStatement = new BoundStatement(
						this.preparedStatements.get(UPDATE_LEARNING_MILESTONES));
				statement.add(boundStatement.bind(entry.getValue(),sessionRecord.getUserId(),
						entry.getKey().getYear(),entry.getKey().getDayOfYear()));
			}
			return Optional.of(statement);
		}
		
	}

}
