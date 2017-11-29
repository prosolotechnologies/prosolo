package org.prosolo.services.indexing;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.elasticsearch.AbstractESIndexer;
import org.prosolo.services.nodes.observers.learningResources.CompetenceChangeTracker;

import java.util.Date;

public interface CompetenceESService extends AbstractESIndexer {

	void saveCompetenceNode(Competence1 comp, Session session);
	
	/**
	 * 
	 * @param comp
	 * @param changeTracker
	 * @param session
	 */
	void updateCompetenceNode(Competence1 comp, CompetenceChangeTracker changeTracker, Session session);

	void updateVisibleToAll(long organizationId, long compId, boolean value);
	
	void updateCompetenceUsersWithPrivileges(long organizationId, long compId, Session session);
	
	void updateStatus(long organizationId, Competence1 comp);
	
	void updateCompetenceBookmarks(long organizationId, long compId, Session session);

	void updateStudents(long organizationId, long compId);

	void updateUnits(long organizationId, long compId, Session session);

	void updateArchived(long organizationId, long compId, boolean archived);

	void updateCompetenceOwner(long organizationId, long compId, long newOwnerId);

}
