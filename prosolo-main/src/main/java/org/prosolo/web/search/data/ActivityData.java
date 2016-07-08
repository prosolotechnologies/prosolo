/**
 * 
 */
package org.prosolo.web.search.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.ResourceActivity;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.common.web.activitywall.data.UserData;

/**
 * @author "Nikola Milikic"
 * 
 */
public class ActivityData implements Serializable {

	private static final long serialVersionUID = -2865577987822251075L;
	
	private long id;
	private String title;
	private String description;
	private UserData creator;
	
	private String link;
	private String imageUrl;
	private String previewTitle;
	private String previewDescription;
	private boolean hasRichContent;
	
	private Activity activity;
	
	private boolean viewed;


	public ActivityData(Activity activity) {
		
		this.id = activity.getId();
		this.title = activity.getTitle();
		this.description = activity.getDescription();
		this.creator = new UserData(activity.getMaker());
		this.activity = activity;
		
		if (activity instanceof ResourceActivity) {
			RichContent richContent = ((ResourceActivity) activity).getRichContent();
			
			if (richContent != null) {
				this.hasRichContent = true;
				this.link = richContent.getLink();
				this.imageUrl = richContent.getImageUrl();
				this.previewTitle = richContent.getTitle();
				this.previewDescription = richContent.getDescription();
			}
		}
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public UserData getCreator() {
		return creator;
	}

	public void setCreator(UserData creator) {
		this.creator = creator;
	}
	
	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getPreviewTitle() {
		return previewTitle;
	}

	public void setPreviewTitle(String previewTitle) {
		this.previewTitle = previewTitle;
	}

	public String getPreviewDescription() {
		return previewDescription;
	}

	public void setPreviewDescription(String previewDescription) {
		this.previewDescription = previewDescription;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public boolean isViewed() {
		return viewed;
	}

	public void setViewed(boolean viewed) {
		this.viewed = viewed;
	}

	public boolean isHasRichContent() {
		return hasRichContent;
	}

	public void setHasRichContent(boolean hasRichContent) {
		this.hasRichContent = hasRichContent;
	}
	
}
