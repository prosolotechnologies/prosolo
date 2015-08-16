package org.prosolo.bigdata.spark;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.prosolo.bigdata.algorithms.fpgrowth.CompetenceAlgoFPGrowth;
import org.prosolo.bigdata.algorithms.fpgrowth.association_rules.AlgoAgrawalFaster94;
import org.prosolo.bigdata.algorithms.fpgrowth.association_rules.AssocRule;
import org.prosolo.bigdata.algorithms.fpgrowth.association_rules.AssocRules;
import org.prosolo.bigdata.algorithms.fpgrowth.patterns.Itemset;
import org.prosolo.bigdata.algorithms.fpgrowth.patterns.Itemsets;
import org.prosolo.bigdata.dal.cassandra.AnalyticalEventDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.AnalyticalEventDBManagerImpl;
import org.prosolo.bigdata.dal.cassandra.impl.AnalyzedResultsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.AnalyzedResultsDBmanagerImpl;
import org.prosolo.bigdata.es.AssociationRulesIndexer;
import org.prosolo.bigdata.es.AssociationRulesIndexerImpl;

/**
 * @author Zoran Jeremic May 10, 2015
 *
 */

public class CompetenceActivitiesAssociationRulesAnalyzer implements
		Serializable {
	private static Logger logger = Logger
			.getLogger(CompetenceActivitiesAssociationRulesAnalyzer.class);
	private final int minPatternCount = 100;
	private final double minMinsupp = 0.0005;
	private final int minAssocRulesCount = 100;
	private final double minMinconf = 0.1;

	public void analyzeCompetenceActivitesAssociationRules() {
		// find all competences

		AnalyticalEventDBManager eventDBManager = new AnalyticalEventDBManagerImpl();
		final AnalyzedResultsDBManager dbManager = new AnalyzedResultsDBmanagerImpl();
		final AssociationRulesIndexer indexer = new AssociationRulesIndexerImpl();
		List<Long> competences = eventDBManager.findAllCompetences();
		System.out.println("COMPETENCES NUMBER:" + competences.size());
		JavaSparkContext javaSparkContext = SparkLauncher.getSparkContext();
		JavaRDD<Long> competencesRDD = javaSparkContext
				.parallelize(competences);
		JavaRDD<Long> competencesRDD2 = competencesRDD
				.map(new Function<Long, Long>() {

					@Override
					public Long call(Long competenceid) throws Exception {
						CompetenceAlgoFPGrowth fpgrowth = new CompetenceAlgoFPGrowth(
								competenceid);
						// String output =
						// ".//fpgrowthoutput"+competenceid+".txt";
						double minsupp = 0.6;
						Itemsets patterns = null;
						try {
							int patternsCount = 0;
							while (patternsCount < minPatternCount
									&& minsupp > minMinsupp) {
								logger.debug("PATTERNS:competence:"
										+ competenceid + " patterns count:"
										+ patternsCount + " minsupp:" + minsupp
										* 2);
								patterns = fpgrowth.runAlgorithm(minsupp);
								patternsCount = patterns.getItemsetsCount();
								minsupp = minsupp / 2;
							}

							logger.debug("FINISHED PATTERNS:competence:"
									+ competenceid + " patterns count:"
									+ patternsCount + " minsupp:" + minsupp * 2);

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						fpgrowth.printStats();
						int databaseSize = fpgrowth.getDatabaseSize();

						// STEP 2: Generating all rules from the set of frequent
						// itemsets (based on Agrawal & Srikant, 94)
						// double minconf = 0.60;
						double minconf = 0.6;
						AlgoAgrawalFaster94 algoAgrawal = new AlgoAgrawalFaster94();
						try {
							int assocRulesCount = 0;
							AssocRules assocRules = null;
							while (assocRulesCount < minAssocRulesCount
									&& minconf > minMinconf) {
								assocRules = algoAgrawal.runAlgorithm(patterns,
										databaseSize, minconf);
								assocRulesCount = assocRules.getRules().size();
								logger.debug("ASSOCIATION RULES competence:"
										+ competenceid + " rules number:"
										+ assocRulesCount + " minconf:"
										+ minconf);
								minconf = minconf / 2;
							}
							patterns = null;
							algoAgrawal.printStats();
							logger.debug("FINISHED ASSOCIATION RULES competence:"
									+ competenceid
									+ " rules number:"
									+ assocRulesCount
									+ " minconf:"
									+ minconf
									* 2);
							if (assocRules.getRules().size() > 0) {
								indexer.deleteAssociationRulesForCompetence(competenceid);
								for (AssocRule rule : assocRules.getRules()) {
									System.out.println("RULE:"
											+ Arrays.toString(rule
													.getItemset1())
											+ " ==>"
											+ Arrays.toString(rule
													.getItemset2())
											+ " support:"
											+ rule.getAbsoluteSupport()
											+ " confidence:"
											+ rule.getConfidence());
									indexer.saveAssociationRulesForCompetence(
											competenceid, rule);
								}
							}
							logger.debug("ASSOCIATION RULES STORED IN ELASTICSEARCH for competence:"
									+ competenceid
									+ " rules number:"
									+ assocRulesCount
									+ " minconf:"
									+ minconf
									* 2);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						return competenceid;
					}

				});
		System.out.println("COUNT:" + competencesRDD2.count());
	}

}
