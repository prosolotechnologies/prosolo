package org.prosolo.services.interaction;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.comments.Comment;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;

public interface CommentingManager {

	Comment addComment(BaseEntity resource, User user, String commentText, 
			Date created, String context, Session session) throws EventException, ResourceCouldNotBeLoadedException;
	
	List<Comment> getComments(BaseEntity resource);

	List<Comment> getCommentsForSocialActivity(long socialActivityId);

}