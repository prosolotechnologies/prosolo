package org.prosolo.bigdata.similarity;
/**
 * @author Zoran Jeremic, Aug 27, 2015
 *
 */
public interface CosineSimilarity {

	float findVectorsSimilarity(String firstString, String secondString);

}
