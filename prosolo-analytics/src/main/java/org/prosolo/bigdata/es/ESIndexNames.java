package org.prosolo.bigdata.es;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.config.CommonSettings;

 

/**
@author Zoran Jeremic May 9, 2015
 *
 */

public class ESIndexNames {
	
	public static final String INDEX_RECOMMENDATIONDATA = CommonSettings.getInstance().config.elasticSearch.recommendationdataIndex;
	public static String INDEX_ASSOCRULES=CommonSettings.getInstance().config.elasticSearch.associationrulesIndex;//"association rules index";

	public static List<String> getAllIndexes(){
		List<String> indexes=new ArrayList<String>();
		Field[] fields = ESIndexNames.class.getDeclaredFields();
		for (Field f : fields) {
			if(isRightName(f.getName())){
			
				try {
					indexes.add((String) f.get(null));
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 
			}
		}
		return indexes;
	}
	private static boolean isRightName(String name){
		if(name.startsWith("INDEX_")){
			return true;
		} 
			return false;
		 
	}

}

