package org.prosolo.web.settings.data;

import java.io.Serializable;

import org.prosolo.domainmodel.feeds.FeedSource;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public class FeedSourceData implements Serializable {
	
	private static final long serialVersionUID = -1414750456942232264L;
	
	private String title;
	private String link;
	private boolean toAdd;
	
	public FeedSourceData() { }
	
	public FeedSourceData(FeedSource feedSource) {
		this.title = feedSource.getTitle();
		this.link = feedSource.getLink();
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getLink() {
		return link;
	}
	
	public void setLink(String link) {
		this.link = link;
	}

	public boolean isToAdd() {
		return toAdd;
	}

	public void setToAdd(boolean toAdd) {
		this.toAdd = toAdd;
	}
	
}
