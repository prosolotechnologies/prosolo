package org.prosolo.services.indexing;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.services.nodes.observers.learningResources.CompetenceChangeTracker;

import java.util.Date;

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
	 *
	 * @param organizationId
	 * @param compId
	 * @param userId
	 * @param privilege
	 */
	void addUserToIndex(long organizationId, long compId, long userId, UserGroupPrivilege privilege);
	
	/**
	 * removes user from appropriate colleciton of users which have privilege of viewing or editing competence
	 * with id - {@code compId}
	 *
	 * @param organizationId
	 * @param compId
	 * @param userId
	 * @param privilege
	 */
	void removeUserFromIndex(long organizationId, long compId, long userId, UserGroupPrivilege privilege);
	
	void updateVisibleToAll(long organizationId, long compId, boolean value);
	
	void updateCompetenceUsersWithPrivileges(long organizationId, long compId, Session session);
	
	void updateStatus(long organizationId, long compId, boolean published, Date datePublished);
	
	void addBookmarkToCompetenceIndex(long organizationId, long compId, long userId);
	
	void removeBookmarkFromCompetenceIndex(long organizationId, long compId, long userId);
	
	void addStudentToCompetenceIndex(long organizationId, long compId, long userId);
	
	void removeStudentFromCompetenceIndex(long organizationId, long compId, long userId);
	
	void archiveCompetence(long organizationId, long compId);
	
	void restoreCompetence(long organizationId, long compId);

	void updateCompetenceOwner(long organizationId, long compId, long newOwnerId);

	void addUnitToCompetenceIndex(long organizationId, long compId, long unitId);

	void removeUnitFromCompetenceIndex(long organizationId, long compId, long unitId);

}
