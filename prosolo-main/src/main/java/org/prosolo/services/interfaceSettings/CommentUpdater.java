/**
 * 
 */
package org.prosolo.services.interfaceSettings;

import javax.servlet.http.HttpSession;

import org.prosolo.domainmodel.activitywall.comments.Comment;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.AbstractManager;

/**
 * @author "Nikola Milikic"
 *
 */
public interface CommentUpdater extends AbstractManager {

	void updateCommentData(BaseEntity commentedRes, Comment comment, int commentLikeCount, int commentDislikeCount, HttpSession userSession)
			throws ResourceCouldNotBeLoadedException;
	
	void addCommentData(long socialActivityId, Comment comment, HttpSession userSession) throws ResourceCouldNotBeLoadedException;
	
}
