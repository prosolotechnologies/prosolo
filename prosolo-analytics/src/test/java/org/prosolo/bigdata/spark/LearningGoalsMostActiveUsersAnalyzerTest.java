package org.prosolo.bigdata.spark;


import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.prosolo.bigdata.es.RecommendationDataIndexer;
import org.prosolo.bigdata.es.RecommendationDataIndexerImpl;
import org.prosolo.bigdata.utils.DateUtil;


/**
@author Zoran Jeremic May 24, 2015
 *
 */

public class LearningGoalsMostActiveUsersAnalyzerTest {

	@Test
	public void testAnalyzeLearningGoalsMostActiveUsers() {
		LearningGoalsMostActiveUsersAnalyzer analyzer=new LearningGoalsMostActiveUsersAnalyzer();
		final long daysSinceEpoch = DateUtil.getDaysSinceEpoch();
		//final RecommendationDataIndexer indexer=new RecommendationDataIndexerImpl();
		//List<Long> daysToAnalyze=new ArrayList<Long>();
		long[] days=new long[7];
		for(int i=0;i<7;i++){
			//daysToAnalyze.add(daysSinceEpoch-i);
			days[i]=daysSinceEpoch-i;
		}
		//long[] days={16587, 16586, 16585, 16584, 16583, 16582, 16581};
		for(int i=0;i<days.length;i++){
			System.out.println("ANALYZING DAY:"+days[i]);
			analyzer.analyzeLearningGoalsMostActiveUsersForDay(days[i]);
		}
		
	}
	@Test
	public void testAnalyzeLearningGoalsMostActiveUsersForWeek(){
		System.out.println("FIND BY WEEK");
		LearningGoalsMostActiveUsersAnalyzer analyzer=new LearningGoalsMostActiveUsersAnalyzer();
		analyzer.analyzeLearningGoalsMostActiveUsersForWeek();
		//AnalyticalEventDBManager dbManager=new AnalyticalEventDBManagerImpl();
		//dbManager.findMostActiveUsersForGoalsByDate(16587);
	}

}

