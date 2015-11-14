package org.prosolo.bigdata.dal.cassandra;

import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.SocialInteractionCount;

public interface SocialInteractionStatisticsDBManager {
	
	List<SocialInteractionCount> getSocialInteractionCounts();

}
