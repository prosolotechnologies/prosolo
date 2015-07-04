package org.prosolo.similarity;

/**
 * @author Zoran Jeremic 2013-08-17
 *
 */
public interface CosineSimilarity {

	public abstract float findVectorsSimilarity(String firstString,
			String secondString);

}