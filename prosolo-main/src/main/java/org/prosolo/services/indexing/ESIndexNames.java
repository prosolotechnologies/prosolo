package org.prosolo.services.indexing;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.ElasticSearchConfig;

public class ESIndexNames {
 
	public static final String INDEX_ASSOCRULES = CommonSettings.getInstance().config.elasticSearch.associationrulesIndex;
	public static String INDEX_DOCUMENTS=CommonSettings.getInstance().config.elasticSearch.documentsIndex;//"documents";
	public static String INDEX_NODES=CommonSettings.getInstance().config.elasticSearch.nodesIndex;//"nodes";
	public static String INDEX_USERS=CommonSettings.getInstance().config.elasticSearch.usersIndex;//"users";
	public static String INDEX_RECOMMENDATION_DATA=ElasticSearchConfig.recommendationdataIndex;
 
	
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
 
