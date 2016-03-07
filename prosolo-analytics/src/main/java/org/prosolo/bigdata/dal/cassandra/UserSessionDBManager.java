package org.prosolo.bigdata.dal.cassandra;

import java.util.Optional;

import org.prosolo.bigdata.common.dal.pojo.SessionRecord;

/**
 * @author Nikola Maric
 *
 */
public interface UserSessionDBManager {
	
	public void userSessionStarted(Long userId, Long loginTime);
	
	public Optional<SessionRecord> userSessionEnded(Long userId, Long logoutTime,String reason);

}
