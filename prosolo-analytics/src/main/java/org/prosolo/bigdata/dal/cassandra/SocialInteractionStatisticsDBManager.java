package org.prosolo.bigdata.dal.cassandra;

import java.util.List;

import com.datastax.driver.core.Row;
import org.prosolo.bigdata.common.dal.pojo.SocialInteractionCount;
import org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl;

public interface SocialInteractionStatisticsDBManager {
	
	List<SocialInteractionCount> getSocialInteractionCounts(Long courseid);

	List<Row> getSocialInteractions(Long courseid);

	List<SocialInteractionCount> getSocialInteractionCounts(Long courseid, Long userid);

	void updateCurrentTimestamp(SocialInteractionStatisticsDBManagerImpl.TableNames tablename, Long timestamp);
}
