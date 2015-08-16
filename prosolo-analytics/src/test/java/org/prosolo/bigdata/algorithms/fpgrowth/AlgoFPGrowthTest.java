package org.prosolo.bigdata.algorithms.fpgrowth;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;
import org.prosolo.bigdata.algorithms.fpgrowth.association_rules.AlgoAgrawalFaster94;
import org.prosolo.bigdata.algorithms.fpgrowth.patterns.Itemsets;

/**
 * @author Zoran Jeremic May 1, 2015
 *
 */

public class AlgoFPGrowthTest {

	@Test
	public void test() {
		String input = null;
		try {
			input = fileToPath("context2.txt");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String output = ".//fpgrowthoutput4.txt";
		// String output = "C:\\patterns\\association_rules.txt";

		// STEP 1: Applying the FP-GROWTH algorithm to find frequent itemsets
		// double minsupp = 0.5;
		double minsupp = 0.01;
		AlgoFPGrowth fpgrowth = new AlgoFPGrowth();
		Itemsets patterns = null;
		try {
			patterns = fpgrowth.runAlgorithm(input, null, minsupp);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// patterns.printItemsets(database.size());
		fpgrowth.printStats();
		int databaseSize = fpgrowth.getDatabaseSize();

		// STEP 2: Generating all rules from the set of frequent itemsets (based
		// on Agrawal & Srikant, 94)
		// double minconf = 0.60;
		double minconf = 0.01;
		AlgoAgrawalFaster94 algoAgrawal = new AlgoAgrawalFaster94();
		try {
			algoAgrawal.runAlgorithm(patterns, databaseSize, minconf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		algoAgrawal.printStats();
	}

	public static String fileToPath(String filename)
			throws UnsupportedEncodingException {
		URL url = AlgoFPGrowthTest.class.getResource(filename);
		// URL url=Thread
		// .currentThread()
		// .getContextClassLoader().getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}

	@Test
	public void randomInputGenerator() {
		Random r = new Random();
		int counter = 0;
		for (int i = 0; i < 100000; i++) {
			int numberOfAct = r.nextInt(10);
			if (numberOfAct < 2)
				continue;
			counter++;
			Set<Integer> list = new TreeSet<Integer>();
			while (list.size() < numberOfAct) {
				Integer val = r.nextInt(50);
				if (val > 0) {
					list.add(val);
					System.out.print(val + " ");
				}

			}
			System.out.println();

		}
		System.out.println("TOTAL NUMBER OF GENERATED LOGS:" + counter);
	}

}
