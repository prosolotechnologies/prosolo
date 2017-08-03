package org.prosolo.services.indexing;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.User;

/**
 * @author Zoran Jeremic 2013-06-29
 *
 */
public interface UserEntityESService  extends AbstractBaseEntityESService {

	void saveUserNode(User user, Session session);

	void addUserToOrganization(User user, long organizationId, Session session);

	void removeUserFromOrganization(User user, long organizationId);
	
	void addCredentialToUserIndex(long credId, long userId, long instructorId, int progress, String dateEnrolled);
	
	void assignInstructorToUserInCredential(long userId, long credId, long instructorId);
	
	void addInstructorToCredential(long credId, long userId, String dateAssigned);
	
	void removeInstructorFromCredential(long credId, long userId);
	
	void changeCredentialProgress(long userId, long credId, int progress);
	
	void updateBasicUserData(User user, Session session);
	
	/**
	 * Adds new follower to the index of the followed user.
	 * 
	 * @param followedUserId
	 * @param followerId
	 */
	void addFollowerIndex(long followedUserId, long followerId);
	
	/**
	 * Removes follower from the index of the followed user.
	 * 
	 * @param followedUserId
	 * @param followerId
	 */
	void removeFollowerIndex(long followedUserId, long followerId);
	
	void addCompetenceToUserIndex(long compId, long userId, String dateEnrolled);
	
	void updateCompetenceProgress(long userId, long compId, int progress, String completionDate);

	void addUserToUnitWithRole(long organizationId, long userId, long unitId, long roleId);

	void removeUserFromUnitWithRole(long organizationId, long userId, long unitId, long roleId);

	void addGroup(long orgId, long userId, long groupId);

	void removeGroup(long orgId, long userId, long groupId);

}
