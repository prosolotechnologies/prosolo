package org.prosolo.similarity.impl;
 
import java.util.HashSet;
import java.util.Set;

import org.prosolo.similarity.CosineSimilarity;
import org.springframework.stereotype.Service;

 

/**
 * @author Zoran Jeremic 2013-08-17
 *
 */
@Service("org.prosolo.similarity.CosineSimilarity")
public class CosineSimilarityImpl implements CosineSimilarity {
	 /* (non-Javadoc)
	 * @see org.prosolo.similarity.impl.CosineSimilarity#findVectorsSimilarity(java.lang.String, java.lang.String)
	 */
	@Override
	public float findVectorsSimilarity(String firstString, String secondString) {
		 
		 String[] firstVector = firstString.trim().split("\\s+"); 
		 String[] secondVector = secondString.trim().split("\\s+"); 
		// Collection<String> firstVector; Collection<String> secondVector;
		 
		  if((firstVector==null)||(secondVector==null)){
			  return 0;
		  }
	     if((firstVector.length==0)||(secondVector.length==0)){
	    	   return 0;
	       } 
         	final Set<String> allTokens = new HashSet<String>();
       	 for(String term1:firstVector){
       		// for(int i=0;i<firstVector.length;i++){
          	 	if ((term1!=null)&&(!allTokens.contains(term1.toLowerCase()))){
         			allTokens.add(term1.toLowerCase());
           	}
       	 }
	         final int termsInString1 = allTokens.size();
	        final Set<String> secondStringTokens = new HashSet<String>();
	         for(String term2:secondVector){
	          	if((term2!=null)&&(!secondStringTokens.contains(term2.toLowerCase()))){
       	 		secondStringTokens.add(term2.toLowerCase());
       	 	}
       	 }
	         final int termsInString2 = secondStringTokens.size();
	        //now combine the sets
	        allTokens.addAll(secondStringTokens);
	        final int commonTerms = (termsInString1 + termsInString2) - allTokens.size();
	        float similarity=(float) (commonTerms) / (float) (Math.pow((float) termsInString1, 0.5f) * Math.pow((float) termsInString2, 0.5f));
	        return similarity;
	    }
}
