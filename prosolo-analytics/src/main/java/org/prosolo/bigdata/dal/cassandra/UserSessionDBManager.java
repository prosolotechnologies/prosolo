package org.prosolo.bigdata.dal.cassandra;

/**
 * @author Nikola Maric
 *
 */
public interface UserSessionDBManager {
	
	public void userSessionStarted(Long userId, Long loginTime);
	
	public void userSessionEnded(Long userId, Long logoutTime,String reason);

}
