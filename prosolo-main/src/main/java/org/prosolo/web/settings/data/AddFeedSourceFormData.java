package org.prosolo.web.settings.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public class AddFeedSourceFormData implements Serializable {

	private static final long serialVersionUID = -7660608212229228052L;
	
	private String linkToAdd;
	private List<FeedSourceData> feedSources;
	
	public AddFeedSourceFormData() {
		feedSources = new ArrayList<FeedSourceData>();
	}

	public String getLinkToAdd() {
		return linkToAdd;
	}

	public void setLinkToAdd(String linkToAdd) {
		this.linkToAdd = linkToAdd;
	}

	public List<FeedSourceData> getFeedSources() {
		return feedSources;
	}

	public void setFeedSources(List<FeedSourceData> feedSources) {
		this.feedSources = feedSources;
	}

	public FeedSourceData getSelectedFeedSource() {
		for (FeedSourceData feedSourceData : feedSources) {
			if (feedSourceData.isToAdd()) {
				return feedSourceData;
			}
		}
		return null;
	}
	
}
