/**
 * 
 */
package org.prosolo.services.interfaceSettings;

import javax.servlet.http.HttpSession;

import org.prosolo.common.domainmodel.activitywall.comments.Comment;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
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
