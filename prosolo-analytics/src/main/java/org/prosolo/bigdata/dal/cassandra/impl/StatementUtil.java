package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.Set;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;

class StatementUtil {

	public static BoundStatement statement(PreparedStatement prepared, Object... parameters) {
		BoundStatement statement = new BoundStatement(prepared);
		int index = 0;
		for (Object parameter : parameters) {
			if (parameter instanceof Long) {
				statement.setLong(index++, ((Long) parameter).longValue());
			} else if (parameter instanceof String) {
				statement.setString(index++, (String) parameter);
			} else if (parameter instanceof Boolean) {
				statement.setBool(index++, (Boolean) parameter);
			} else if (parameter instanceof Double) {
				statement.setDouble(index++, (Double) parameter);
			} else if (parameter instanceof Set) {
				for (Object element : ((Set<?>) parameter)) {
					if (element instanceof String) {
						statement.setString(index++, (String) element);
					} else {
						throw new IllegalStateException("Parameter type not supported.");
					}
				}
			} else {
				throw new IllegalStateException("Parameter type not supported.");
			}
		}
		return statement;
	}

}
