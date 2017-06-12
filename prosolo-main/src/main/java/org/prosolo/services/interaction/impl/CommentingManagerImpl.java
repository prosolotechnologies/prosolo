package org.prosolo.services.interaction.impl;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.old.comments.Comment;
import org.prosolo.common.domainmodel.activitywall.old.comments.NodeComment;
import org.prosolo.common.domainmodel.activitywall.old.comments.SocialActivityComment;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.CommentingManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.interaction.CommentingManager")
public class CommentingManagerImpl extends AbstractManagerImpl implements CommentingManager {
	
	private static final long serialVersionUID = 3622530637041186392L;

	private static Logger logger = Logger.getLogger(CommentingManagerImpl.class);
	
	@Override
	@Transactional
	public Comment addComment(BaseEntity resource, long userId, 
			String commentText, Date created, String context, Session session) 
			throws EventException, ResourceCouldNotBeLoadedException {
	   	 
		if (resource != null && userId > 0) {
			logger.debug("Adding comment: \"" + commentText + "\" to the resource " + resource.getId() + " by the user "
					+ userId);
			
			Comment comment = null;
			
			if (resource instanceof SocialActivity) {
				SocialActivity socialActivity = (SocialActivity) resource;
				socialActivity.setLastAction(new Date());
				
				session.save(resource);
				comment = new SocialActivityComment();
				comment.setVisibility(socialActivity.getVisibility());
			} else if (resource instanceof Node) {
				comment = new NodeComment();
				comment.setVisibility(((Node) resource).getVisibility());
			}
			
			comment.setText(commentText);
			comment.setDateCreated(created);
			comment.setObject(resource);
			comment.setLastAction(created);
			comment.setMaker(loadResource(User.class, userId));
			comment.setAction(EventType.Comment);
			
			session.save(comment);
			
			return comment;
		} 
		return null;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<Comment> getComments(BaseEntity resource) {
		String query = null;
		
		if (resource instanceof SocialActivity) {
			query = 
				"SELECT DISTINCT comment " + 
				"FROM SocialActivityComment comment " +
				"WHERE comment.socialActivity = :res " + 
				"ORDER BY comment.dateCreated ASC ";
		} else if (resource instanceof Node) {
			query = 
				"SELECT DISTINCT comment " + 
				"FROM NodeComment comment " +
				"WHERE comment.commentedNode = :res " + 
				"ORDER BY comment.dateCreated ASC ";
		}
		
		@SuppressWarnings("unchecked")
		List<Comment> comments = persistence.currentManager().createQuery(query)
				.setEntity("res", resource)
				.list();
		return comments;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<Comment> getCommentsForSocialActivity(long socialActivityId) {
		String query = 
			"SELECT DISTINCT comment " + 
			"FROM SocialActivityComment comment " +
			"WHERE comment.socialActivity.id = :socialActivityId " + 
			"ORDER BY comment.dateCreated ASC ";
		
		@SuppressWarnings("unchecked")
		List<Comment> comments = persistence.currentManager().createQuery(query)
			.setLong("socialActivityId", socialActivityId)
			.list();
		return comments;
	}
	
//	@Override
//	@Transactional (readOnly = true)
//	public SocialActivity getSocialActivityForActivity(Activity activity){
//		String query = 
//				"SELECT DISTINCT event " + 
//				"FROM Activity activity " +
//				"LEFT JOIN socialActivity.target as socialActivity "+
//				"WHERE activity = :activity ";
//		SocialActivity sEvent = (SocialActivity) persistence.currentManager().createQuery(query)
//				.setEntity("activity", activity)
//				.uniqueResult();
//		return sEvent;
//	}
	
}
