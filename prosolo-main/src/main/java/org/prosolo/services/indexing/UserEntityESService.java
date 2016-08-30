package org.prosolo.services.indexing;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.User;

/**
 * @author Zoran Jeremic 2013-06-29
 *
 */
public interface UserEntityESService  extends AbstractBaseEntityESService {

	void saveUserNode(User user, Session session);
	
	void addCredentialToUserIndex(long credId, long userId, long instructorId, String dateEnrolled);
	
	void assignInstructorToUserInCredential(long userId, long credId, long instructorId);
	
	void addInstructorToCredential(long credId, long userId, String dateAssigned);
	
	void removeInstructorFromCredential(long credId, long userId);
	
	void changeCredentialProgress(long userId, long credId, int progress);
	
	void updateBasicUserData(User user, Session session);
	
	
//	/**
//	 * For particular user adds user that followed him.
//	 * 
//	 * @param userId
//	 * @param followerId
//	 */
//	void addFollowerForUser(long followerId, long userId);
//	
//	/**
//	 * For particular user removes user that unfollowed him.
//	 * 
//	 * @param userId
//	 * @param followerId
//	 */
//	void removeFollowerForUser(long followerId, long userId);

}
