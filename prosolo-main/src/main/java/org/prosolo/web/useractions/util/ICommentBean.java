package org.prosolo.web.useractions.util;

import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.interaction.data.CommentsData;

public interface ICommentBean {

	public void saveTopLevelComment(CommentsData commentsData);
	
	public void saveNewComment(CommentData parent, CommentsData commentsData);
}
