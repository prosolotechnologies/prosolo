package org.prosolo.bigdata.jobs;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.spark.CompetenceActivitiesAssociationRulesAnalyzer;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.prosolo.bigdata.scala.analyzers.CompetenceActivitiesAssociationRules;

/**
 * @author Zoran Jeremic May 19, 2015
 *
 */

public class AssociationRulesForCompetencesDiscoveryJob implements Job {
	private static Logger logger = Logger
			.getLogger(AssociationRulesForCompetencesDiscoveryJob.class
					.getName());

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		logger.info("executed job for association rules discovery");
		CompetenceActivitiesAssociationRules analyzer=new CompetenceActivitiesAssociationRules();
		analyzer.analyzeCompetenceActivitesAssociationRules();
		logger.info("job for association rules discovery. JOB FINISHED");
		//CompetenceActivitiesAssociationRulesAnalyzer analyzer = new CompetenceActivitiesAssociationRulesAnalyzer();
		//analyzer.analyzeCompetenceActivitesAssociationRules();

	}

}
