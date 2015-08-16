package org.prosolo.bigdata.algorithms.fpgrowth.association_rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AssocRules {
	// a list of association rules
	public final List<AssocRule> rules = new ArrayList<AssocRule>(); // rules

	// a name that an algorithm can give to this list of association rules
	private final String name;

	/**
	 * Sort the rules by confidence
	 */
	public void sortByConfidence() {
		Collections.sort(rules, new Comparator<AssocRule>() {
			public int compare(AssocRule r1, AssocRule r2) {
				return (int) ((r2.getConfidence() - r1.getConfidence()) * Integer.MAX_VALUE);
			}
		});
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 *            a name for this list of association rules (string)
	 */
	public AssocRules(String name) {
		this.name = name;
	}

	/**
	 * Print all the rules in this list to System.out.
	 * 
	 * @param databaseSize
	 *            the number of transactions in the transaction database where
	 *            the rules were found
	 */
	public void printRules(int databaseSize) {
		System.out.println(" ------- " + name + " -------");
		int i = 0;
		for (AssocRule rule : rules) {
			System.out.print("  rule " + i + ":  " + rule.toString());
			System.out.print("support :  "
					+ rule.getRelativeSupport(databaseSize) + " ("
					+ rule.getAbsoluteSupport() + "/" + databaseSize + ") ");
			System.out.print("confidence :  " + rule.getConfidence());
			System.out.println("");
			i++;
		}
		System.out.println(" --------------------------------");
	}

	/**
	 * Print all the rules in this list to System.out, and include information
	 * about the lift measure.
	 * 
	 * @param databaseSize
	 *            the number of transactions in the transaction database where
	 *            the rules were found
	 */
	public void printRulesWithLift(int databaseSize) {
		System.out.println(" ------- " + name + " -------");
		int i = 0;
		for (AssocRule rule : rules) {
			System.out.print("  rule " + i + ":  " + rule.toString());
			System.out.print("support :  "
					+ rule.getRelativeSupport(databaseSize) + " ("
					+ rule.getAbsoluteSupport() + "/" + databaseSize + ") ");
			System.out.print("confidence :  " + rule.getConfidence());
			System.out.print(" lift :  " + rule.getLift());
			System.out.println("");
			i++;
		}
		System.out.println(" --------------------------------");
	}

	/**
	 * Return a string representation of this list of rules
	 * 
	 * @param databaseSize
	 *            the number of transactions in the database where the rules
	 *            were found.
	 * @return a string
	 */
	public String toString(int databaseSize) {
		// create a string buffer
		StringBuilder buffer = new StringBuilder(" ------- ");
		buffer.append(name);
		buffer.append(" -------\n");
		int i = 0;
		// for each rule
		for (AssocRule rule : rules) {
			// append the rule, its support and confidence.
			buffer.append("   rule ");
			buffer.append(i);
			buffer.append(":  ");
			buffer.append(rule.toString());
			buffer.append("support :  ");
			buffer.append(rule.getRelativeSupport(databaseSize));

			buffer.append(" (");
			buffer.append(rule.getAbsoluteSupport());
			buffer.append("/");
			buffer.append(databaseSize);
			buffer.append(") ");
			buffer.append("confidence :  ");
			buffer.append(rule.getConfidence());
			buffer.append("\n");
			i++;
		}
		return buffer.toString(); // return the string
	}

	/**
	 * Add a rule to this list of rules
	 * 
	 * @param rule
	 *            the rule to be added
	 */
	public void addRule(AssocRule rule) {
		rules.add(rule);
	}

	/**
	 * Get the number of rules in this list of rules
	 * 
	 * @return the number of rules
	 */
	public int getRulesCount() {
		return rules.size();
	}

	/**
	 * Get the list of rules.
	 * 
	 * @return a list of rules.
	 */
	public List<AssocRule> getRules() {
		return rules;
	}

}
