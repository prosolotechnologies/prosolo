package org.prosolo.bigdata.dal.persistence;

import java.util.List;
import java.util.Map;

public interface UserDAO {

	Map<Long, Map<String, String>> getUsersData(Long[] users);
	
	List<String> getUserNames(List<Long> userIds);
	
	Map<String, String> getUserNameAndEmail(long userId);
	
}
