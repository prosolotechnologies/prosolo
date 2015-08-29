package org.prosolo.bigdata.algorithms.fpgrowth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;
import org.prosolo.bigdata.algorithms.fpgrowth.association_rules.AlgoAgrawalFaster94;
import org.prosolo.bigdata.algorithms.fpgrowth.association_rules.AssocRule;
import org.prosolo.bigdata.algorithms.fpgrowth.association_rules.AssocRules;
import org.prosolo.bigdata.algorithms.fpgrowth.patterns.Itemset;
import org.prosolo.bigdata.algorithms.fpgrowth.patterns.Itemsets;
import org.prosolo.bigdata.dal.cassandra.AnalyzedResultsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.AnalyzedResultsDBmanagerImpl;
import org.prosolo.bigdata.es.AssociationRulesIndexer;
import org.prosolo.bigdata.es.AssociationRulesIndexerImpl;
import org.prosolo.bigdata.es.ESAdministration;
import org.prosolo.bigdata.es.ESAdministrationImpl;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;

/**
 * @author Zoran Jeremic May 3, 2015
 *
 */

public class CompetenceAlgoFPGrowthTest {

	@SuppressWarnings("deprecation")
	@Test
	public void testRunAlgorithm() {
		ESAdministration admin = new ESAdministrationImpl();
		AnalyzedResultsDBManager dbManager = new AnalyzedResultsDBmanagerImpl();
		try {
			admin.createIndexes();
		} catch (IndexingServiceNotAvailable e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// long competenceid=4915341;
		AssociationRulesIndexer indexer = new AssociationRulesIndexerImpl();
		long competenceid = 24;
		CompetenceAlgoFPGrowth fpgrowth = new CompetenceAlgoFPGrowth(
				competenceid);
		String output = ".//fpgrowthoutput5.txt";
		double minsupp = 0.1;

		Itemsets patterns = null;
		try {
			patterns = fpgrowth.runAlgorithm(minsupp);
			List<List<Itemset>> levels = patterns.getLevels();
			System.out.println("Levels size:" + levels.size());
			/*
			 * for(List<Itemset> level:levels){ System.out.println("LEVEL 1");
			 * for(Itemset item:level){
			 * System.out.println("LEVEL 2:"+Arrays.toString
			 * (item.getItems())+" support:"+item.getAbsoluteSupport()); } }
			 */
			List<Itemset> lastLevel = levels.get(levels.size() - 1);
			Itemset mostfrequent = null;
			for (Itemset item : lastLevel) {
				if (mostfrequent == null) {
					mostfrequent = item;
				} else if (mostfrequent.getAbsoluteSupport() < item
						.getAbsoluteSupport()) {
					mostfrequent = item;
				}

				System.out.println("LAST LEVEL:"
						+ Arrays.toString(item.getItems()) + " support:"
						+ item.getAbsoluteSupport());
			}
			System.out.println("MOST FREQUENT:"
					+ Arrays.toString(mostfrequent.getItems()) + " support:"
					+ mostfrequent.getAbsoluteSupport());
			List<Long> activities = new ArrayList<Long>();
			for (int i = 0; i < mostfrequent.getItems().length; i++) {
				activities.add(mostfrequent.getItems()[i]);
			}
			dbManager.insertFrequentCompetenceActivities(competenceid,
					activities);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fpgrowth.printStats();
		int databaseSize = fpgrowth.getDatabaseSize();

		// STEP 2: Generating all rules from the set of frequent itemsets (based
		// on Agrawal & Srikant, 94)
		// double minconf = 0.60;
		double minconf = 0.1;
		AlgoAgrawalFaster94 algoAgrawal = new AlgoAgrawalFaster94();
		try {
			AssocRules assocRules = algoAgrawal.runAlgorithm(patterns,
					databaseSize, minconf);
			for (AssocRule rule : assocRules.getRules()) {
				System.out.println("RULE:"
						+ Arrays.toString(rule.getItemset1()) + " ==>"
						+ Arrays.toString(rule.getItemset2()) + " support:"
						+ rule.getAbsoluteSupport() + " confidence:"
						+ rule.getConfidence());
				indexer.saveAssociationRulesForCompetence(competenceid, rule);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		algoAgrawal.printStats();
	}

}
