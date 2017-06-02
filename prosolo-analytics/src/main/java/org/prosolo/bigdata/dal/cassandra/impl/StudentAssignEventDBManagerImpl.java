package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.prosolo.bigdata.dal.cassandra.StudentAssignEventDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.data.StudentAssign;
import org.prosolo.bigdata.dal.cassandra.impl.data.StudentAssignEventData;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;

public class StudentAssignEventDBManagerImpl extends SimpleCassandraClientImpl implements StudentAssignEventDBManager {
	

	//private static final String TIMESTAMPS_TABLE_NAME = "currenttimestamps";
	
	HashMap<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();
	private static final String UPDATE_STUDENT_ASSINGED_ASSIGNED = "updateStudentAssignEventsAssigned";
	private static final String UPDATE_STUDENT_ASSINGED_UNASSIGNED = "updateStudentAssignEventsUnassigned";
    private static final String GET_CURRENT_TIMESTAMP = "getCurrentTimestamp";
	private static final String UPDATE_CURRENT_TIMESTAMP = "updateCurrentTimestamp";
	
	private long bucket;
	
	public static class StudentAssignEventDBManagerImplHolder {
		public static final StudentAssignEventDBManagerImpl INSTANCE = new StudentAssignEventDBManagerImpl();
	}
	public static StudentAssignEventDBManagerImpl getInstance() {
		return StudentAssignEventDBManagerImplHolder.INSTANCE;
	}
	
	private StudentAssignEventDBManagerImpl() {
		super();
		this.prepareStatements();
		this.bucket = getOrCreateCurrentTimestamp();
	}

	private void prepareStatements() {
		String updateStudentsAssignedAssigned = "UPDATE " + TablesNames.STUDENT_ASSIGN_EVENTS + " " +
				"USING TTL 97200 " +
				"SET assigned = assigned + ? " +
				"WHERE courseId=? AND timestamp=? and instructorId=?;";
		this.preparedStatements.put(UPDATE_STUDENT_ASSINGED_ASSIGNED, this.getSession()
				.prepare(updateStudentsAssignedAssigned));
		
		String updateStudentsAssignedUnassigned = "UPDATE " + TablesNames.STUDENT_ASSIGN_EVENTS + " " +
				"USING TTL 97200 " +
				"SET unassigned = unassigned + ? " +
				"WHERE courseId=? AND timestamp=? and instructorId=?;";
		this.preparedStatements.put(UPDATE_STUDENT_ASSINGED_UNASSIGNED, this.getSession()
				.prepare(updateStudentsAssignedUnassigned));
		
		String getCurrentTimestamp = "SELECT timestamp FROM " + TablesNames.CURRENT_TIMESTAMPS + " " +
				"WHERE tablename=?;";
		this.preparedStatements.put(GET_CURRENT_TIMESTAMP, this.getSession()
				.prepare(getCurrentTimestamp));
		
		String updateCurrentTimestamp = "UPDATE " + TablesNames.CURRENT_TIMESTAMPS + " " +
				"SET timestamp=? " +
				"WHERE tablename=?;";
		this.preparedStatements.put(UPDATE_CURRENT_TIMESTAMP, this.getSession()
				.prepare(updateCurrentTimestamp));
	}
	
	private long getOrCreateCurrentTimestamp() {
		long res = -1;
		try {
			Long l = getCurrentTimestamp();
			if(l == null) {
				res = 1;
				updateCurrentTimestamp(res);
			} else if(l == -1) {
				res = 2;
				updateCurrentTimestamp(res);
			} else {
				res = l.longValue();
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return res;
	}
	
	private Long getCurrentTimestamp() {
		Long res = null;
		try {
			BoundStatement statement = new BoundStatement(preparedStatements.get(GET_CURRENT_TIMESTAMP));
			statement.setString(0, TablesNames.STUDENT_ASSIGN_EVENTS);
			Row row = this.getSession().execute(statement).one();
			res = (row == null) ? -1 : row.getLong(0);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return res;
	}
	
	@Override
	public boolean updateCurrentTimestamp(long bucket) {
		try {
			BoundStatement statement = new BoundStatement(preparedStatements.get(UPDATE_CURRENT_TIMESTAMP));
			statement.setLong(0, bucket);
			statement.setString(1, TablesNames.STUDENT_ASSIGN_EVENTS);
		    this.getSession().execute(statement);
		    return true;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			return false;
		}
	}
	
	//enforcing same partition key (course id) when saving data in batch
	@Override
	public void saveStudentAssignEvents(long courseId, List<StudentAssignEventData> events) { 
		try {
			BatchStatement batchStatement = new BatchStatement(BatchStatement.Type.UNLOGGED);
		    batchStatement.enableTracing();

		    for (StudentAssignEventData event : events) {
		        batchStatement.add(getBoundStatementForEvent(courseId, event));
		    }

		    this.getSession().execute(batchStatement);
			
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	private BoundStatement getBoundStatementForEvent(long courseId, StudentAssignEventData event) 
			throws Exception {
		try {
			BoundStatement statement = null;
			if(event.getType() == StudentAssign.ASSIGNED) {
				statement = new BoundStatement(
					this.preparedStatements.get(UPDATE_STUDENT_ASSINGED_ASSIGNED));
				
			} else {
				statement = new BoundStatement(
					this.preparedStatements.get(UPDATE_STUDENT_ASSINGED_UNASSIGNED));
			}
			List<Long> paramList = new ArrayList<>();
			paramList.add(event.getStudentId());
			statement.setList(0, paramList);
			statement.setLong(1, courseId);
			statement.setLong(2, bucket);
			statement.setLong(3, event.getInstructorId());
			
			return statement;
		} catch(Exception e) {
			throw new Exception("Error while binding statements");
		}
	}

	public long getBucket() {
		return bucket;
	}

	public void setBucket(long bucket) {
		this.bucket = bucket;
	}
	

}
