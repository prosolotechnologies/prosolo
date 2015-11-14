package org.prosolo.bigdata.dal.cassandra.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.prosolo.bigdata.dal.cassandra.UserObservationsDBManager;
import org.prosolo.bigdata.events.analyzers.ObservationType;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

/**
 * @author Zoran Jeremic, Oct 11, 2015
 *
 */
public class UserObservationsDBManagerImpl  extends SimpleCassandraClientImpl
implements Serializable, UserObservationsDBManager{
	static HashMap<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();
	HashMap<String, String> queries = new HashMap<String, String>();
	ObservationType[] observationTypes=ObservationType.class.getEnumConstants();

	public UserObservationsDBManagerImpl() {
		super();
		this.prepareStatements();
	}
 
	

	public HashMap<String, PreparedStatement> getPreparedStatements() {
		return preparedStatements;
	}

	private void prepareStatements() {
	 
		String updateClusteringusersobservationsbydate = "UPDATE clusteringusersobservationsbydate  SET login=login+?,lmsuse=lmsuse+?, resourceview=resourceview+?, discussionview=discussionview+? WHERE date=? AND userid=?;";
		this.queries.put("updateClusteringusersobservationsbydate", updateClusteringusersobservationsbydate);
		
		String findClusteringusersobservationsbydate = "SELECT date, userid, discussionview, lmsuse, resourceview FROM clusteringusersobservationsbydate WHERE date=?;";
		this.queries.put("findClusteringusersobservationsbydate", findClusteringusersobservationsbydate);
		
		
		String updateUserprofileactionsobservationsbydate = "UPDATE userprofileactionsobservationsbydate  "
				+ "SET attach=attach+?,progress=progress+?, comment=comment+?, creating=creating+?,"
				+ "evaluation=evaluation+?, join=join+?,like=like+?, login=login+?,"
				+ "posting=posting+?, content_access=content_access+?, message=message+?, search=search+? "
				+ "WHERE date=? AND userid=?;";
		this.queries.put("updateUserprofileactionsobservationsbydate", updateUserprofileactionsobservationsbydate);
		
		String findUserprofileactionsobservationsbydate = "SELECT date, userid, "
				+ "attach,  progress,  comment,  creating,  evaluation,join,like,"
				+ "login ,posting,content_access,message,search "
				+ "FROM userprofileactionsobservationsbydate "
				+ "WHERE date=?;";
		this.queries.put("findUserprofileactionsobservationsbydate", findUserprofileactionsobservationsbydate);
		
		Set<String> stQueries = this.queries.keySet();
		for (String query : stQueries) {
			preparedStatements.put(query,
					this.getSession().prepare(queries.get(query)));
		}
	}
	@Override
	public boolean updateUserObservationsCounter(Long date, Long userid,
			long login, long lmsuse, long resourceview, long discussionview) {
		BoundStatement updateStatement = new BoundStatement(this.preparedStatements.get("updateClusteringusersobservationsbydate"));
		updateStatement.setLong(0, login);
		updateStatement.setLong(1, lmsuse);
		updateStatement.setLong(2, resourceview);
		updateStatement.setLong(3, discussionview);
		updateStatement.setLong(4, date);
		updateStatement.setLong(5, userid);
		try {
			ResultSet rs=this.getSession().execute(updateStatement);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	@Override
	public boolean updateUserProfileActionsObservationCounter(Long date, Long userid,
			ObservationType observationType ) {
	BoundStatement updateStatement = new BoundStatement(this.preparedStatements.get("updateUserprofileactionsobservationsbydate"));
		try {
			for (int i=0;i<observationTypes.length;i++){
				ObservationType oType=observationTypes[i];
				if(oType.equals(observationType)){
					updateStatement.setLong(oType.name().toLowerCase(), 1);
				}else{
					updateStatement.setLong(oType.name().toLowerCase(),0);
				}
		}
		updateStatement.setLong("date", date);
		updateStatement.setLong("userid", userid);
			ResultSet rs=this.getSession().execute(updateStatement);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	@Override
	public List<Row> findAllUsersObservationsForDate(Long date) {
		BoundStatement boundStatement = new BoundStatement(
				preparedStatements.get("findClusteringusersobservationsbydate"));
		 boundStatement.setLong(0, date);
		 List<Row> rows =null;
		 try{
				ResultSet rs = this.getSession().execute(boundStatement);
				rows = rs.all();
		 }catch(Exception ex){
			 ex.printStackTrace();
		 }
		return rows;
	}
}
