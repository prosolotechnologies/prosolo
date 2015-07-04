package org.prosolo.bigdata.dal.cassandra.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

 




import org.prosolo.bigdata.common.dal.pojo.ActivityAccessCount;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

/**
@author Zoran Jeremic May 9, 2015
 *
 */

public class AnalyzedResultsDBmanagerImpl  extends SimpleCassandraClientImpl implements AnalyzedResultsDBManager, Serializable{
	static HashMap<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();
	HashMap<String, String> queries = new HashMap<String, String>();
	
	public AnalyzedResultsDBmanagerImpl(){
		super();
		this.prepareStatements();
	}
	public HashMap<String, PreparedStatement> getPreparedStatements(){
		return this.preparedStatements;
	}
	private void prepareStatements() {
		String insert_frequentcompetenceactivities = "INSERT INTO frequentcompetenceactivities(competenceid,activities) VALUES (?, ?);";
		this.queries.put("insert_frequentcompetenceactivities", insert_frequentcompetenceactivities);
		
		String find_frequentcompetenceactivities = "SELECT * FROM frequentcompetenceactivities WHERE competenceid=? ALLOW FILTERING;";
		this.queries.put("find_frequentcompetenceactivities", find_frequentcompetenceactivities);
		
		Set<String> stQueries=this.queries.keySet();
		for(String query:stQueries){
			this.preparedStatements.put(query, this.getSession() 
					.prepare(queries.get(query)));
		}
	}
	@Override
	@Deprecated
	public void insertFrequentCompetenceActivities(long competenceid, List<Long> activities){
		BoundStatement boundStatement = new BoundStatement(
				this.preparedStatements.get("insert_frequentcompetenceactivities"));
		boundStatement.setLong(0, competenceid);
		boundStatement.setList(1, activities);
 
		this.getSession().execute(boundStatement);
		 
	}
	
	@Override
	public List<ActivityAccessCount> findFrequentCompetenceActivities(long competenceId) {
		List<ActivityAccessCount> frequentActivities=new ArrayList<ActivityAccessCount>();
		BoundStatement boundStatement = new BoundStatement(
				this.preparedStatements
						.get("find_frequentcompetenceactivities"));
		boundStatement.setLong(0, competenceId);
		ResultSet rs = this.getSession().execute(boundStatement);
		Row row=rs.one();
		if(row!=null){
			List<Long> ids=row.getList(1, Long.class);
			for(Long id:ids){
				ActivityAccessCount activityAccess=new ActivityAccessCount(id,competenceId,0);
				frequentActivities.add(activityAccess);
			}
		}
		return frequentActivities;
	}
}

