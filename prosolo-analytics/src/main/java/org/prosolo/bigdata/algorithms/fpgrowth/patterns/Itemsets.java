package org.prosolo.bigdata.algorithms.fpgrowth.patterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Zoran Jeremic May 1, 2015
 *
 */
public class Itemsets {
	/**
	 * We store the itemsets in a list named "levels". Position i in "levels"
	 * contains the list of itemsets of size i
	 */
	private final List<List<Itemset>> levels = new ArrayList<List<Itemset>>();
	/** the total number of itemsets **/
	private int itemsetsCount = 0;
	/** a name that we give to these itemsets (e.g. "frequent itemsets") */
	private String name;

	private List<Itemset> highestLevel = new ArrayList<Itemset>();
	private int currentLevel = 0;
	private int highestLevelItemsetsCount = 0;
	private int higestAbsoluteSupport;

	public List<Itemset> getHighestLevel() {
		return highestLevel;
	}

	public void setHighestLevel(List<Itemset> highestLevel) {
		this.highestLevel = highestLevel;
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(int currentLevel) {
		this.currentLevel = currentLevel;
	}

	public int getHighestLevelItemsetsCount() {
		return highestLevelItemsetsCount;
	}

	public void setHighestLevelItemsetsCount(int highestLevelItemsetsCount) {
		this.highestLevelItemsetsCount = highestLevelItemsetsCount;
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 *            the name of these itemsets
	 */
	public Itemsets(String name) {
		this.name = name;
		levels.add(new ArrayList<Itemset>()); // We create an empty level 0 by
												// default.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.pfv.spmf.patterns.itemset_array_integers_with_count.AbstractItemsets
	 * #printItemsets(int)
	 */
	public void printItemsets(int nbObject) {
		System.out.println(" ------- " + name + " -------");
		int patternCount = 0;
		int levelCount = 0;
		// for each level (a level is a set of itemsets having the same number
		// of items)
		for (List<Itemset> level : levels) {
			// print how many items are contained in this level
			System.out.println("  L" + levelCount + " ");
			// for each itemset
			for (Itemset itemset : level) {
				Arrays.sort(itemset.getItems());
				// print the itemset
				System.out.print("  pattern " + patternCount + ":  ");
				itemset.print();
				// print the support of this itemset
				System.out.print("support :  " + itemset.getAbsoluteSupport());
				// + itemset.getRelativeSupportAsString(nbObject));
				patternCount++;
				System.out.println("");
			}
			levelCount++;
		}
		System.out.println(" --------------------------------");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.pfv.spmf.patterns.itemset_array_integers_with_count.AbstractItemsets
	 * #addItemset
	 * (ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset, int)
	 */
	public void addItemset(Itemset itemset, int k) {
		// System.out.println("Add itemset K:"+k+" levels:"+levels.size()+
		// " itemsets count:"+itemsetsCount);
		while (levels.size() <= k) {
			levels.add(new ArrayList<Itemset>());
		}
		levels.get(k).add(itemset);
		itemsetsCount++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.pfv.spmf.patterns.itemset_array_integers_with_count.AbstractItemsets
	 * #addItemset
	 * (ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset, int)
	 */
	public boolean addHighestLevelItemset(Itemset itemset, int k) {
		addItemset(itemset, k);// memory critical
		boolean isHighest = false;
		if (k > this.currentLevel) {
			this.currentLevel = k;
			this.highestLevelItemsetsCount = 0;
			this.higestAbsoluteSupport = 0;
		}
		if (k == this.currentLevel) {
			if (itemset.getAbsoluteSupport() > this.higestAbsoluteSupport) {
				this.higestAbsoluteSupport = itemset.getAbsoluteSupport();
				isHighest = true;
			}
		}
		itemsetsCount++;
		this.highestLevelItemsetsCount++;
		return isHighest;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.pfv.spmf.patterns.itemset_array_integers_with_count.AbstractItemsets
	 * #getLevels()
	 */
	public List<List<Itemset>> getLevels() {
		return levels;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.pfv.spmf.patterns.itemset_array_integers_with_count.AbstractItemsets
	 * #getItemsetsCount()
	 */
	public int getItemsetsCount() {
		return itemsetsCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.pfv.spmf.patterns.itemset_array_integers_with_count.AbstractItemsets
	 * #setName(java.lang.String)
	 */
	public void setName(String newName) {
		name = newName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.pfv.spmf.patterns.itemset_array_integers_with_count.AbstractItemsets
	 * #decreaseItemsetCount()
	 */
	public void decreaseItemsetCount() {
		itemsetsCount--;
	}
}
