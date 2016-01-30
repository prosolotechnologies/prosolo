package org.prosolo.bigdata.dal.cassandra;

import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.OuterInteractionsCount;
import org.prosolo.bigdata.common.dal.pojo.SocialInteractionCount;
import org.prosolo.bigdata.common.dal.pojo.SocialInteractionsCount;
import org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl;

import com.datastax.driver.core.Row;

public interface SocialInteractionStatisticsDBManager {

	List<SocialInteractionCount> getSocialInteractionCounts(Long courseid);

	List<Row> getSocialInteractions(Long courseid);

	List<SocialInteractionCount> getSocialInteractionCounts(Long courseid, Long userid);

	void updateCurrentTimestamp(SocialInteractionStatisticsDBManagerImpl.TableNames tablename, Long timestamp);

	void insertInsideClusterInteractions(Long timestamp, Long course, Long cluster, Long student,
										 List<String> interactions);

	void insertOutsideClusterInteractions(Long timestamp, Long course,  Long student,Long cluster, String direction,
										 List<String> interactions);

	List<SocialInteractionsCount> getClusterInteractions(Long course);

	List<OuterInteractionsCount> getOuterInteractions(Long course, Long student);

}
