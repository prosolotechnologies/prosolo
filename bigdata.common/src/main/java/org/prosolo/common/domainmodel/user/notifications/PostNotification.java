package org.prosolo.common.domainmodel.user.notifications;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.user.notifications.Notification;

@Entity
@DiscriminatorValue("PostNotification")
public class PostNotification extends Notification {
	
	private static final long serialVersionUID = -3570393712747791441L;
	
	private Post post;
	
	@OneToOne
	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}
	
	@Transient
	public Post getObject() {
		return this.post;
	}
}
