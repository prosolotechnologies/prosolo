package org.prosolo.bigdata.algorithms.fpgrowth;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.algorithms.fpgrowth.patterns.Itemset;
import org.prosolo.bigdata.algorithms.fpgrowth.patterns.Itemsets;
import org.prosolo.bigdata.algorithms.fpgrowth.patterns.MemoryLogger;
//import org.prosolo.bigdata.algorithms.fpgrowth.patterns.MemoryLogger;
import org.prosolo.bigdata.common.dal.pojo.TargetCompetenceActivities;
import org.prosolo.bigdata.dal.cassandra.AnalyticalEventDBManager;
import org.prosolo.bigdata.dal.cassandra.AnalyzedResultsDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.AnalyticalEventDBManagerImpl;
import org.prosolo.bigdata.dal.cassandra.impl.AnalyzedResultsDBmanagerImpl;
import org.prosolo.bigdata.es.AssociationRulesIndexer;
import org.prosolo.bigdata.es.AssociationRulesIndexerImpl;

/**
 * @author Zoran Jeremic May 3, 2015
 *
 */

public class CompetenceAlgoFPGrowth {
	private static Logger logger = Logger
			.getLogger(CompetenceAlgoFPGrowth.class);
	//AnalyticalEventDBManager dbManager = new AnalyticalEventDBManagerImpl();
	//final AnalyzedResultsDBManager analyzeddbManager = new AnalyzedResultsDBmanagerImpl();
	AssociationRulesIndexer esIndexer = new AssociationRulesIndexerImpl();
	private long competenceId;
	private long startTimestamp; // start time of the latest execution
	private long endTime; // end time of the latest execution
	private int transactionCount = 0; // transaction count in the database
	private int itemsetCount; // number of freq. itemsets found
	// parameter
	public int minSupportRelative;// the relative minimum support

	// BufferedWriter writer = null; // object to write the output file

	// The patterns that are found
	// (if the user want to keep them into memory)
	protected Itemsets patterns = null;

	// This variable is used to determine the size of buffers to store itemsets.
	// A value of 50 is enough because it allows up to 2^50 patterns!
	final int BUFFERS_SIZE = 50;

	// buffer for storing the current itemset that is mined when performing
	// mining
	// the idea is to always reuse the same buffer to reduce memory usage.
	private long[] itemsetBuffer = null;
	// another buffer
	private long[] itemsetTempBuffer = null;

	// This buffer is used to store an itemset that will be written to file
	// so that the algorithm can sort the itemset before it is output to file
	// (when the user choose to output result to file).
	private long[] itemsetOutputBuffer = null;

	public CompetenceAlgoFPGrowth(long competenceid) {
		this.competenceId = competenceid;
	}

	/**
	 * Method to run the FPGRowth algorithm.
	 * 
	 * @param input
	 *            the path to an input file containing a transaction database.
	 * @param output
	 *            the output file path for saving the result (if null, the
	 *            result will be returned by the method instead of being saved).
	 * @param minsupp
	 *            the minimum support threshold.
	 * @return the result if no output file path is provided.
	 * @throws IOException
	 *             exception if error reading or writing files
	 */
	public Itemsets runAlgorithm(double minsupp) throws IOException {
		// record start time
		startTimestamp = System.currentTimeMillis();
		// number of itemsets found
		itemsetCount = 0;

		// initialize tool to record memory usage
		MemoryLogger.getInstance().reset();
		MemoryLogger.getInstance().checkMemory();

		// if the user want to keep the result into memory
		// if(output == null){
		// writer = null;
		patterns = new Itemsets("FREQUENT ITEMSETS");
		// }else{ // if the user want to save the result to a file
		// patterns = null;
		// writer = new BufferedWriter(new FileWriter(output));
		// itemsetOutputBuffer = new long[BUFFERS_SIZE];
		// }

		// (1) PREPROCESSING: Initial database scan to determine the frequency
		// of each item
		// The frequency is stored in a map:
		// key: item value: support
		List<TargetCompetenceActivities> tCompActivities = AnalyticalEventDBManagerImpl.getInstance()
				.findAllActivitiesByTargetCompetenceForCompetence(competenceId);
		final Map<Long, Integer> mapSupport = scanDatabaseToDetermineFrequencyOfSingleItems(tCompActivities);

		// convert the minimum support as percentage to a
		// relative minimum support
		this.minSupportRelative = (int) Math.ceil(minsupp * transactionCount);

		// (2) Scan the database again to build the initial FP-Tree
		// Before inserting a transaction in the FPTree, we sort the items
		// by descending order of support. We ignore items that
		// do not have the minimum support.

		FPTree tree = new FPTree();
		for (TargetCompetenceActivities tcActivities : tCompActivities) {
			List<Long> transaction = new ArrayList<Long>();
			for (Long item : tcActivities.getActivities()) {
				Integer count = mapSupport.get(item);
				if (count >= minSupportRelative) {
					transaction.add(item);
				}
			}
			Collections.sort(transaction, new Comparator<Long>() {
				public int compare(Long item1, Long item2) {
					// compare the frequency
					int compare = mapSupport.get(item2) - mapSupport.get(item1);
					// if the same frequency, we check the lexical ordering!
					if (compare == 0) {
						return (int) (item1 - item2);
					}
					// otherwise, just use the frequency
					return compare;
				}
			});
			// add the sorted transaction to the fptree.
			tree.addTransaction(transaction);
		}
		// read the file
		// BufferedReader reader = new BufferedReader(new FileReader(input));

		// We create the header table for the tree using the calculated support
		// of single items
		tree.createHeaderList(mapSupport);

		// (5) We start to mine the FP-Tree by calling the recursive method.
		// Initially, the prefix alpha is empty.
		// if at least an item is frequent
		if (tree.headerList.size() > 0) {
			// initialize the buffer for storing the current itemset
			itemsetBuffer = new long[BUFFERS_SIZE];
			// and another buffer
			itemsetTempBuffer = new long[BUFFERS_SIZE];
			// recursively generate frequent itemsets using the fp-tree
			// Note: we assume that the initial FP-Tree has more than one path
			// which should generally be the case.
			fpgrowth(tree, itemsetBuffer, 0, transactionCount, mapSupport);
		}

		// close the output file if the result was saved to a file
		/*
		 * if(writer != null){ writer.close(); }
		 */
		// record the execution end time
		// endTime= System.currentTimeMillis();

		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
		endTime = System.currentTimeMillis();
		// return the result (if saved to memory)
		return patterns;

	}

	private Map<Long, Integer> scanDatabaseToDetermineFrequencyOfSingleItems(
			List<TargetCompetenceActivities> tCompActivities) {

		// a map for storing the support of each item (key: item, value:
		// support)

		Map<Long, Integer> mapSupport = new HashMap<Long, Integer>();
		// List<TargetCompetenceActivities> tCompActivities=
		// dbManager.findAllActivitiesByTargetCompetenceForCompetence(competenceId);
		for (TargetCompetenceActivities tcActivities : tCompActivities) {
			for (Long item : tcActivities.getActivities()) {
				Integer count = mapSupport.get(item);
				if (count == null) {
					mapSupport.put(item, 1);
				} else {
					mapSupport.put(item, ++count);
				}
			}
			// increase the transaction count
			transactionCount++;
		}
		return mapSupport;

		// return null;
	}

	/**
	 * Mine an FP-Tree having more than one path.
	 * 
	 * @param tree
	 *            the FP-tree
	 * @param prefix
	 *            the current prefix, named "alpha"
	 * @param mapSupport
	 *            the frequency of items in the FP-Tree
	 * @throws IOException
	 *             exception if error writing the output file
	 */
	private void fpgrowth(FPTree tree, long[] prefix, int prefixLength,
			int prefixSupport, Map<Long, Integer> mapSupport)
			throws IOException {
		// // ======= DEBUG ========
		// System.out.print("###### Prefix: ");
		// for(int k=0; k< prefixLength; k++) {
		// System.out.print(prefix[k] + "  ");
		// }
		// System.out.println("\n");
		// // ========== END DEBUG =======
		// System.out.println(tree);

		// We will check if the FPtree contains a single path
		boolean singlePath = true;
		// We will use a variable to keep the support of the single path if
		// there is one
		int singlePathSupport = 0;
		// This variable is used to count the number of items in the single path
		// if there is one
		int position = 0;
		// if the root has more than one child, than it is not a single path
		if (tree.root.childs.size() > 1) {
			singlePath = false;
		} else {
			// Otherwise,
			// if the root has exactly one child, we need to recursively check
			// childs
			// of the child to see if they also have one child
			FPNode currentNode = tree.root.childs.get(0);
			while (true) {
				// if the current child has more than one child, it is not a
				// single path!
				if (currentNode.childs.size() > 1) {
					singlePath = false;
					break;
				}
				// otherwise, we copy the current item in the buffer and move to
				// the child
				// the buffer will be used to store all items in the path
				itemsetTempBuffer[position] = currentNode.itemID;
				// we keep the support of the path
				singlePathSupport = currentNode.counter;
				position++;
				// if this node has no child, that means that this is the end of
				// this path
				// and it is a single path, so we break
				if (currentNode.childs.size() == 0) {
					break;
				}
				currentNode = currentNode.childs.get(0);
			}
		}

		// Case 1: the FPtree contains a single path
		if (singlePath && singlePathSupport >= minSupportRelative) {
			// We save the path, because it is a maximal itemset
			saveAllCombinationsOfPrefixPath(itemsetTempBuffer, position,
					prefix, prefixLength, singlePathSupport);
		} else {
			// For each frequent item in the header table list of the tree in
			// reverse order.
			for (int i = tree.headerList.size() - 1; i >= 0; i--) {
				// get the item
				Long item = tree.headerList.get(i);

				// get the item support
				int support = mapSupport.get(item);

				// Create Beta by concatening prefix Alpha by adding the current
				// item to alpha
				prefix[prefixLength] = item;

				// calculate the support of the new prefix beta
				int betaSupport = (prefixSupport < support) ? prefixSupport
						: support;

				// save beta to the output file
				saveItemset(prefix, prefixLength + 1, betaSupport);

				// === (A) Construct beta's conditional pattern base ===
				// It is a subdatabase which consists of the set of prefix paths
				// in the FP-tree co-occuring with the prefix pattern.
				List<List<FPNode>> prefixPaths = new ArrayList<List<FPNode>>();
				FPNode path = tree.mapItemNodes.get(item);

				// Map to count the support of items in the conditional prefix
				// tree
				// Key: item Value: support
				Map<Long, Integer> mapSupportBeta = new HashMap<Long, Integer>();

				while (path != null) {
					// if the path is not just the root node
					if (path.parent.itemID != -1) {
						// create the prefixpath
						List<FPNode> prefixPath = new ArrayList<FPNode>();
						// add this node.
						prefixPath.add(path); // NOTE: we add it just to keep
												// its support,
						// actually it should not be part of the prefixPath

						// ####
						int pathCount = path.counter;

						// Recursively add all the parents of this node.
						FPNode parent = path.parent;
						while (parent.itemID != -1) {
							prefixPath.add(parent);

							// FOR EACH PATTERN WE ALSO UPDATE THE ITEM SUPPORT
							// AT THE SAME TIME
							// if the first time we see that node id
							if (mapSupportBeta.get(parent.itemID) == null) {
								// just add the path count
								mapSupportBeta.put(parent.itemID, pathCount);
							} else {
								// otherwise, make the sum with the value
								// already stored
								mapSupportBeta.put(parent.itemID,
										mapSupportBeta.get(parent.itemID)
												+ pathCount);
							}
							parent = parent.parent;
						}
						// add the path to the list of prefixpaths
						prefixPaths.add(prefixPath);
					}
					// We will look for the next prefixpath
					path = path.nodeLink;
				}

				// (B) Construct beta's conditional FP-Tree
				// Create the tree.
				FPTree treeBeta = new FPTree();
				// Add each prefixpath in the FP-tree.
				for (List<FPNode> prefixPath : prefixPaths) {
					treeBeta.addPrefixPath(prefixPath, mapSupportBeta,
							minSupportRelative);
				}

				// Mine recursively the Beta tree if the root has child(s)
				if (treeBeta.root.childs.size() > 0) {

					// Create the header list.
					treeBeta.createHeaderList(mapSupportBeta);
					// recursive call
					fpgrowth(treeBeta, prefix, prefixLength + 1, betaSupport,
							mapSupportBeta);
				}
			}
		}

	}

	/**
	 * This method saves all combinations of a prefix path if it has enough
	 * support
	 * 
	 * @param prefix
	 *            the current prefix
	 * @param prefixLength
	 *            the current prefix length
	 * @param prefixPath
	 *            the prefix path
	 * @throws IOException
	 *             if exception while writting to output file
	 */
	private void saveAllCombinationsOfPrefixPath(long[] itemsetBuffer,
			int position, long[] prefix, int prefixLength, int support)
			throws IOException {

		// Generate all subsets of the prefixPath except the empty set
		// and output them
		// We use bits to generate all subsets.
		for (long i = 1, max = 1 << position; i < max; i++) {

			// we create a new subset
			int newPrefixLength = prefixLength;

			// for each bit
			for (int j = 0; j < position; j++) {
				// check if the j bit is set to 1
				int isSet = (int) i & (1 << j);
				// if yes, add the bit position as an item to the new subset
				if (isSet > 0) {
					prefix[newPrefixLength++] = itemsetBuffer[j];
				}
			}
			// save the itemset
			saveItemset(prefix, newPrefixLength, support);
		}
	}

	/**
	 * Write a frequent itemset that is found to the output file or keep into
	 * memory if the user prefer that the result be saved into memory.
	 */
	private void saveItemset(long[] itemset, int itemsetLength, int support)
			throws IOException {

		// increase the number of itemsets found for statistics purpose
		itemsetCount++;

		// if the result should be saved to a file
		/*
		 * if(writer != null){ // copy the itemset in the output buffer and sort
		 * items System.arraycopy(itemset, 0, itemsetOutputBuffer, 0,
		 * itemsetLength); Arrays.sort(itemsetOutputBuffer, 0, itemsetLength);
		 * 
		 * // Create a string buffer StringBuilder buffer = new StringBuilder();
		 * // write the items of the itemset for(int i=0; i< itemsetLength;
		 * i++){ buffer.append(itemsetOutputBuffer[i]); if(i !=
		 * itemsetLength-1){ buffer.append(' '); } } // Then, write the support
		 * buffer.append(" #SUP: "); buffer.append(support); // write to file
		 * and create a new line writer.write(buffer.toString());
		 * writer.newLine(); }// otherwise the result is kept into memory else{
		 */
		// create an object Itemset and add it to the set of patterns
		// found.
		long[] itemsetArray = new long[itemsetLength];
		System.arraycopy(itemset, 0, itemsetArray, 0, itemsetLength);

		// sort the itemset so that it is sorted according to lexical ordering
		// before we show it to the user
		Arrays.sort(itemsetArray);

		Itemset itemsetObj = new Itemset(itemsetArray);
		itemsetObj.setAbsoluteSupport(support);
		// patterns.addItemset(itemsetObj, itemsetLength);
		boolean isHighest = patterns.addHighestLevelItemset(itemsetObj,
				itemsetLength);
		if (isHighest) {
			List<Long> activities = new ArrayList<Long>();
			for (int i = 0; i < itemsetObj.getItems().length; i++) {
				activities.add(itemsetObj.getItems()[i]);
			}
			// analyzeddbManager.insertFrequentCompetenceActivities(this.competenceId,activities);
			esIndexer.saveFrequentCompetenceActivities(this.competenceId,
					activities);
		}
		// }
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		logger.debug("=============  FP-GROWTH STATS ============= competence:"
				+ competenceId);
		long temps = endTime - startTimestamp;
		logger.debug(" Transactions count from database : " + transactionCount);
		// System.out.print(" Max memory usage: " +
		// MemoryLogger.getInstance().getMaxMemory() + " mb \n");
		logger.debug(" Frequent itemsets count : " + itemsetCount);
		logger.debug("Highest level of frequent itemsets:"
				+ this.patterns.getCurrentLevel());
		logger.debug(" Frequent itemsets on highest level count : "
				+ this.patterns.getHighestLevelItemsetsCount());
		logger.debug(" Total time ~ " + temps + " ms");
		logger.debug("===================================================");
	}

	/**
	 * Get the number of transactions in the last transaction database read.
	 * 
	 * @return the number of transactions.
	 */
	public int getDatabaseSize() {
		return transactionCount;
	}

}
