package org.prosolo.bigdata.dal.cassandra;

import java.util.Collection;
import java.util.List;

import com.datastax.driver.core.Row;
import org.prosolo.bigdata.common.dal.pojo.SocialIneractionsCount;
import org.prosolo.bigdata.common.dal.pojo.SocialInteractionCount;
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

	List<SocialIneractionsCount> getClusterInteractions(Long course);

	List<SocialIneractionsCount> getOuterInteractions(Long course, Long student);

}
