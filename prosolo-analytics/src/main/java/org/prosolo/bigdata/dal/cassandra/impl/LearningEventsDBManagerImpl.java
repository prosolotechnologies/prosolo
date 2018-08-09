package org.prosolo.bigdata.dal.cassandra.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.dal.pojo.SessionRecord;
import org.prosolo.bigdata.dal.cassandra.LearningEventsDBManager;
import org.prosolo.bigdata.session.impl.LearningEventSummary;
import org.prosolo.common.util.date.DateEpochUtil;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BatchStatement.Type;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.google.gson.Gson;

public class LearningEventsDBManagerImpl extends SimpleCassandraClientImpl implements LearningEventsDBManager {

	private final static Logger logger = Logger
			.getLogger(LearningEventsDBManager.class);

	HashMap<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();
	private static final String UPDATE_LEARNING_EVENTS_COUNTER = "updateLearningEventsCounter";
	private static final String UPDATE_LEARNING_MILESTONES = "updateLearningEventsMilestones";
	private static final String GET_LEARNING_STATISTICS = "getLearningEventsData";
	private static final String GET_LEARNING_MILESTONES = "getLearningMilestonesData";
	private String summaryDateFormat = "yyyy-MM-dd";

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
		String updateLearningEventsCounter = "UPDATE learningevents SET number = number + ? WHERE actorid=? AND epochday=?;";
		this.preparedStatements.put(UPDATE_LEARNING_EVENTS_COUNTER,
				this.getSession().prepare(updateLearningEventsCounter));

		String updateLearningEventsMilestones = "UPDATE learningmilestones SET milestones = ? + milestones WHERE actorid = ? and epochday = ?;";
		this.preparedStatements.put(UPDATE_LEARNING_MILESTONES,
				this.getSession().prepare(updateLearningEventsMilestones));

		String getLearningEventsData = "SELECT * FROM learningevents WHERE actorid=? and epochday >= ? AND epochday <= ?;";
		this.preparedStatements.put(GET_LEARNING_STATISTICS, this.getSession().prepare(getLearningEventsData));

		String getLearningMilestonesData = "SELECT * FROM learningmilestones WHERE actorid=? and epochday >= ? AND epochday <= ?;";
		this.preparedStatements.put(GET_LEARNING_MILESTONES, this.getSession().prepare(getLearningMilestonesData));
	}

	@Override
	public void saveLearningEventsData(SessionRecord sessionRecord, Map<Long, Integer> learningEventsPerDay,
			Map<Long, List<String>> milestonesPerDay) {
		// one is counter based, the other is not, so we canno't batch them
		// together, but we can batch statements within them
		createEventNumberStatement(learningEventsPerDay, sessionRecord)
				.ifPresent(statement -> this.getSession().execute(statement));
		createEventMilestonesStatement(milestonesPerDay, sessionRecord)
				.ifPresent(statement -> this.getSession().execute(statement));

	}

	private Optional<Statement> createEventNumberStatement(Map<Long, Integer> learningEventsPerDay,
			SessionRecord sessionRecord) {
		if (MapUtils.isEmpty(learningEventsPerDay)) {
			return Optional.empty();
		}
		// if only one entry is present, create just a BoundStatement
		else if (learningEventsPerDay.size() == 1) {
			return learningEventsPerDay.entrySet().stream().findFirst()
					.map(entry -> new BoundStatement(this.preparedStatements.get(UPDATE_LEARNING_EVENTS_COUNTER))
							.bind(Long.valueOf(entry.getValue()), sessionRecord.getUserId(), entry.getKey()));
		}
		// if more than one day-in-year/year is involved, wrap them in a counter
		// batch
		else {
			BatchStatement statement = new BatchStatement(Type.COUNTER);
			for (Map.Entry<Long, Integer> entry : learningEventsPerDay.entrySet()) {
				BoundStatement boundStatement = new BoundStatement(
						this.preparedStatements.get(UPDATE_LEARNING_EVENTS_COUNTER));
				statement.add(
						boundStatement.bind(Long.valueOf(entry.getValue()), sessionRecord.getUserId(), entry.getKey()));
			}
			return Optional.of(statement);
		}

	}

	private Optional<Statement> createEventMilestonesStatement(Map<Long, List<String>> milestonesPerDay,
			SessionRecord sessionRecord) {
		if (MapUtils.isEmpty(milestonesPerDay)) {
			return Optional.empty();
		}
		// if only one entry is present, create just a BoundStatement
		else if (milestonesPerDay.size() == 1) {
			return milestonesPerDay.entrySet().stream().findFirst()
					.map(entry -> new BoundStatement(this.preparedStatements.get(UPDATE_LEARNING_MILESTONES))
							.bind(entry.getValue(), sessionRecord.getUserId(), entry.getKey()));
		}
		// if more than one day-in-year/year is involved, wrap them in a batch
		else {
			BatchStatement statement = new BatchStatement();
			for (Map.Entry<Long, List<String>> entry : milestonesPerDay.entrySet()) {
				BoundStatement boundStatement = new BoundStatement(
						this.preparedStatements.get(UPDATE_LEARNING_MILESTONES));
				statement.add(boundStatement.bind(entry.getValue(), sessionRecord.getUserId(), entry.getKey()));
			}
			return Optional.of(statement);
		}

	}

	@Override
	public List<LearningEventSummary> getLearningEventsData(long actorId, long epochDayFrom, long epochDayTo) {
		logger.debug("getLearningEventsData for student:"+actorId+" epochDayFrom:"+epochDayFrom+" epochDayTo:"+epochDayTo);
		// get learning event counters
		BoundStatement eventCounterStatement = StatementUtil.statement(preparedStatements.get(GET_LEARNING_STATISTICS),
				actorId, epochDayFrom, epochDayTo);
		List<Row> eventCountersRows = getSession().execute(eventCounterStatement).all();
		// get learning milestones
		BoundStatement milestoneStatement = StatementUtil.statement(preparedStatements.get(GET_LEARNING_MILESTONES),
				actorId, epochDayFrom, epochDayTo);
		List<Row> milestoneRows = getSession().execute(milestoneStatement).all();
		logger.debug("getLearningEventsData found results:"+milestoneRows.size());
		List<LearningEventSummary> summaries = addMissingValues(combineRows(eventCountersRows, milestoneRows),
				epochDayFrom, epochDayTo);
		return summaries;
	}

	private List<LearningEventSummary> combineRows(List<Row> eventCountersRows, List<Row> milestoneRows) {
		List<LearningEventSummary> summaries = new ArrayList<>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for (Row eventCounterRow : eventCountersRows) {
			LearningEventSummary summary = new LearningEventSummary();
			summary.setDaysSinceEpoch(eventCounterRow.getLong(1));
			long milliseconds = DateEpochUtil.getTimeSinceEpoch(Math.toIntExact(summary.getDaysSinceEpoch()));
			summary.setDate(sdf.format(new Date(milliseconds)));
			summary.setValue(eventCounterRow.getLong(2));
			Row milestoneRow = findMilestoneRowForSameDay(eventCounterRow, milestoneRows);
			if (milestoneRow != null) {
				List<String> list = milestoneRow.getList(2, String.class);
				summary.setMilestones(new ArrayList<>());
				for (String milestone : list) {
					summary.getMilestones().add(new Gson().fromJson(milestone, LearningEventSummary.Milestone.class));
				}
			}
			summaries.add(summary);
		}
		return summaries;
	}

	private Row findMilestoneRowForSameDay(Row eventCounterRow, List<Row> milestoneRows) {
		long epochday = eventCounterRow.getLong(1);
		for (Row milestoneRow : milestoneRows) {
			if (milestoneRow.getLong(1) == epochday) {
				return milestoneRow;
			}
		}
		return null;
	}
	

	private List<LearningEventSummary> addMissingValues(List<LearningEventSummary> values, long epochDayFrom,
			long epochDayTo) {
		long timespan = epochDayTo - epochDayFrom;
		List<LearningEventSummary> summaries = new ArrayList<>();
		for(int i = 0; i < timespan; i++) {
			long currentEpochDay = epochDayFrom + i;
			LearningEventSummary existingSummary = findSummaryByEpochDay(currentEpochDay,values);
			if(existingSummary==null) {
				summaries.add(createBlankSummary(currentEpochDay));
			}
			else {
				summaries.add(existingSummary);
			}
		}
		return summaries;
	}

	private LearningEventSummary findSummaryByEpochDay(long currentEpochDay, List<LearningEventSummary> values) {
		LearningEventSummary targetSummary = null;
		for(LearningEventSummary summary : values) {
			if(summary.getDaysSinceEpoch() == currentEpochDay) {
				targetSummary = summary;
				break;
			}
		}
		return targetSummary;
	}
	
	private LearningEventSummary createBlankSummary(long daysSinceEpoch) {
		LearningEventSummary summary = new LearningEventSummary();
		Date date = DateEpochUtil.getDateFromDaysSinceEpoch(daysSinceEpoch);
		summary.setDate(new SimpleDateFormat(summaryDateFormat).format(date));
		summary.setValue(0);
		return summary;
	}


}
