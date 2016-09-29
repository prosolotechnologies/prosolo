package org.prosolo.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.common.config.CommonSettings;

public class ESIndexNames {
 
	public static final String INDEX_ASSOCRULES = CommonSettings.getInstance().config.elasticSearch.associationrulesIndex+CommonSettings.getInstance().config.getNamespaceSufix();
	public static final String INDEX_LOGS = CommonSettings.getInstance().config.elasticSearch.logsIndex+CommonSettings.getInstance().config.getNamespaceSufix();;
	public static String INDEX_DOCUMENTS=CommonSettings.getInstance().config.elasticSearch.documentsIndex+CommonSettings.getInstance().config.getNamespaceSufix();//"documents";
	public static String INDEX_NODES=CommonSettings.getInstance().config.elasticSearch.nodesIndex+CommonSettings.getInstance().config.getNamespaceSufix();//"nodes";
	public static String INDEX_USERS=CommonSettings.getInstance().config.elasticSearch.usersIndex+CommonSettings.getInstance().config.getNamespaceSufix();//"users";
	public static String INDEX_RECOMMENDATION_DATA=CommonSettings.getInstance().config.elasticSearch.recommendationdataIndex+CommonSettings.getInstance().config.getNamespaceSufix();
	//public static String INDEX_RECOMMENDATIONDATA = CommonSettings
			//.getInstance().config.elasticSearch.recommendationdataIndex+CommonSettings.getInstance().config.getNamespaceSufix();
	//public static String INDEX_ASSOCRULES = CommonSettings.getInstance().config.elasticSearch.associationrulesIndex
		//	+ CommonSettings.getInstance().config.getNamespaceSufix();// "association rules index";
	
	private static Logger logger = Logger.getLogger(ESIndexNames.class);
 
	public static List<String> getAllIndexes() {
		List<String> indexes = new ArrayList<String>();
		Field[] fields = ESIndexNames.class.getDeclaredFields();
		for (Field f : fields) {
			if (isRightName(f.getName())) {
				
				try {
					indexes.add((String) f.get(null));
				} catch (IllegalArgumentException e) {
					logger.error(e);
				} catch (IllegalAccessException e) {
					logger.error(e);
				}
			}
		}
		return indexes;
	}
	
	private static boolean isRightName(String name) {
		return name.startsWith("INDEX_");
	}
}
 
