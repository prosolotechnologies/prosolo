package org.prosolo.bigdata.dal.persistence;

import java.util.List;
import java.util.Map;

import org.prosolo.bigdata.twitter.StreamListData;

/**
@author Zoran Jeremic Jun 21, 2015
 *
 */

public interface TwitterStreamingDAO {

	Map<String, StreamListData> readAllHashtagsAndLearningGoalsIds();

	Map<String, List<Long>> readAllUserPreferedHashtagsAndUserIds();

}

