package org.prosolo.common.email.generators;

import java.util.List;

import org.prosolo.common.domainmodel.user.TimeFrame;
import org.prosolo.common.web.digest.data.FeedsDigestData;


public class FeedsEmailGenerator extends EmailContentGenerator {
	
	@Override
	public String getTemplateName() {
		return "feedDigest";
	}
	
	@Override
	public String getSubject() {
		return "ProSolo Feed Digest";
	}
	
	private String name;
	private List<FeedsDigestData> feedsDigests;
	private String dashedDate;
	private String interval;
	
	public FeedsEmailGenerator(String name, List<FeedsDigestData> feedsDigests, String dashedDate, TimeFrame interval) {
		this.name = name;
		this.feedsDigests = feedsDigests;
		this.dashedDate = dashedDate;
		this.interval = interval.name().toLowerCase();
	}
	
	public String getName() {
		return name;
	}

	public List<FeedsDigestData> getFeedsDigests() {
		return feedsDigests;
	}

	public String getDashedDate() {
		return dashedDate;
	}

	public String getInterval() {
		return interval;
	}
	
}