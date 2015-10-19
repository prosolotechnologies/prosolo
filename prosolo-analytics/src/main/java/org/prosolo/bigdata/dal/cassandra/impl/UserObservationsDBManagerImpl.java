package org.prosolo.bigdata.dal.cassandra.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

import org.prosolo.bigdata.dal.cassandra.UserObservationsDBManager;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;

/**
 * @author Zoran Jeremic, Oct 11, 2015
 *
 */
public class UserObservationsDBManagerImpl  extends SimpleCassandraClientImpl
implements Serializable, UserObservationsDBManager{
	static HashMap<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();
	HashMap<String, String> queries = new HashMap<String, String>();

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
}
