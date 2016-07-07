package org.prosolo.common.domainmodel.activitywall;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Entity
public class PostReshareSocialActivity extends SocialActivity1 {

	private static final long serialVersionUID = -1227809596469711400L;
	
	private PostSocialActivity1 postObject;

	@ManyToOne(fetch = FetchType.LAZY)
	public PostSocialActivity1 getPostObject() {
		return postObject;
	}

	public void setPostObject(PostSocialActivity1 postObject) {
		this.postObject = postObject;
	}

}
