package org.prosolo.bigdata.algorithms.fpgrowth.association_rules;

import org.prosolo.bigdata.algorithms.fpgrowth.patterns.Rule;

 

/**
@author Zoran Jeremic May 1, 2015
 *
 */
public class AssocRule extends Rule{

	/** lift of the rule */
	private double lift;

	/**
	 * Constructor
	 * 
	 * @param itemset1
	 *            the antecedent of the rule (an itemset)
	 * @param itemset2
	 *            the consequent of the rule (an itemset)
	 * @param supportAntecedent the coverage of the rule (support of the antecedent)
	 * @param transactionCount
	 *            the absolute support of the rule (integer)
	 * @param confidence
	 *            the confidence of the rule
	 * @param lift   the lift of the rule
	 */
	public AssocRule(long[] itemset1, long[] itemset2, int supportAntecedent,
			int transactionCount, double confidence, double lift) {
		super(itemset1, itemset2, supportAntecedent, transactionCount, confidence);
		this.lift = lift;
	}

	/**
	 * Get the lift of this rule.
	 * 
	 * @return the lift.
	 */
	public double getLift() {
		return lift;
	}

	/**
	 * Print this rule to System.out.
	 */
	public void print() {
		System.out.println(toString());
	}

}
