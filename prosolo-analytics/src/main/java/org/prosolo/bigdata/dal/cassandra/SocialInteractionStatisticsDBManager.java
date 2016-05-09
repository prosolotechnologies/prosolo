package org.prosolo.bigdata.dal.cassandra;

import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.OuterInteractionsCount;
//import org.prosolo.bigdata.common.dal.pojo.SocialInteractionCount;
import org.prosolo.bigdata.common.dal.pojo.SocialInteractionsCount;
import org.prosolo.bigdata.dal.cassandra.impl.SimpleCassandraClientImpl;
import org.prosolo.bigdata.dal.cassandra.impl.SimpleCassandraClientImpl.TableNames;
import org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl;

import com.datastax.driver.core.Row;
import org.prosolo.bigdata.events.analyzers.ObservationType;

public interface SocialInteractionStatisticsDBManager {

	//List<SocialInteractionCount> getSocialInteractionCounts(Long courseid);

	List<Row> getSocialInteractions(Long courseid);

	//List<SocialInteractionCount> getSocialInteractionCounts(Long courseid, Long userid);


	void updateCurrentTimestamp(TableNames tablename, Long timestamp);

	void insertInsideClusterInteractions(Long timestamp, Long course, Long cluster, Long student,
										 List<String> interactions);

	void insertOutsideClusterInteractions(Long timestamp, Long course,  Long student,Long cluster, String direction,
										 List<String> interactions);
	
	Long findStudentCluster(Long course, Long student);

	void insertStudentCluster(Long timestamp, Long course, Long student, Long cluster);

	List<SocialInteractionsCount> getClusterInteractions(Long course, Long student);

	List<OuterInteractionsCount> getOuterInteractions(Long course, Long student);

	void updateToFromInteraction(Long courseId, Long actorId, Long targetUserId, ObservationType observationType);

	List<Row> getSocialInteractionsByType(Long courseid);

	void insertStudentInteractionsByPeer(Long course, Long student, List<String> interactions);

	void insertStudentInteractionsByType(Long course, Long student, List<String> interactions);

	List<SocialInteractionsCount> getInteractionsByPeers(Long courseId, Long studentId);
	List<SocialInteractionsCount> getInteractionsByType(Long courseId, Long studentId);
}
