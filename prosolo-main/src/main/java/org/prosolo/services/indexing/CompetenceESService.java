package org.prosolo.services.indexing;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.services.nodes.observers.learningResources.CompetenceChangeTracker;

public interface CompetenceESService  extends AbstractBaseEntityESService {

	void saveCompetenceNode(Competence1 comp, Session session);
	
	/**
	 * 
	 * @param comp
	 * @param changeTracker
	 * @param session
	 */
	void updateCompetenceNode(Competence1 comp, CompetenceChangeTracker changeTracker, Session session);
	
	/**
	 * adds user to appropriate collection of users which have privilege of viewing or editing competence 
	 * with id - {@code compId}
	 * @param compId
	 * @param userId
	 * @param privilege
	 */
	void addUserToIndex(long compId, long userId, UserGroupPrivilege privilege);
	
	/**
	 * removes user from appropriate colleciton of users which have privilege of viewing or editing competence
	 * with id - {@code compId}
	 * @param compId
	 * @param userId
	 * @param privilege
	 */
	void removeUserFromIndex(long compId, long userId, UserGroupPrivilege privilege);
	
	void updateVisibleToAll(long compId, boolean value);
	
	void updateCompetenceUsersWithPrivileges(long compId, Session session);

}
