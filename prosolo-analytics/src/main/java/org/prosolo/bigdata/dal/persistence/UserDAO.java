package org.prosolo.bigdata.dal.persistence;

import java.util.Map;

public interface UserDAO {

	Map<Long, Map<String, String>> getUsersData(Long[] users);
	
}
