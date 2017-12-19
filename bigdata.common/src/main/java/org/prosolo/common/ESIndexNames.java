package org.prosolo.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.prosolo.common.config.CommonSettings;

public class ESIndexNames {
 
	public static final String INDEX_ASSOCRULES = CommonSettings.getInstance().config.elasticSearch.associationrulesIndex+CommonSettings.getInstance().config.getNamespaceSufix();
	public static final String INDEX_LOGS = CommonSettings.getInstance().config.elasticSearch.logsIndex+CommonSettings.getInstance().config.getNamespaceSufix();;
	public static String INDEX_NODES=CommonSettings.getInstance().config.elasticSearch.nodesIndex+CommonSettings.getInstance().config.getNamespaceSufix();//"nodes";
	public static String INDEX_CREDENTIALS=CommonSettings.getInstance().config.elasticSearch.nodesIndex+CommonSettings.getInstance().config.getNamespaceSufix();//"nodes";
	public static String INDEX_COMPETENCES=CommonSettings.getInstance().config.elasticSearch.nodesIndex+CommonSettings.getInstance().config.getNamespaceSufix();//"nodes";
	public static String INDEX_USERS=CommonSettings.getInstance().config.elasticSearch.usersIndex+CommonSettings.getInstance().config.getNamespaceSufix();//"users";
	public static String INDEX_RECOMMENDATION_DATA=CommonSettings.getInstance().config.elasticSearch.recommendationdataIndex+CommonSettings.getInstance().config.getNamespaceSufix();
	public static String INDEX_USER_GROUP = CommonSettings.getInstance().config.elasticSearch.userGroupIndex + CommonSettings.getInstance().config.getNamespaceSufix();
	public static String INDEX_JOBS_LOGS = CommonSettings.getInstance().config.elasticSearch.jobsLogsIndex + CommonSettings.getInstance().config.getNamespaceSufix();
	public static String INDEX_RUBRIC_NAME = CommonSettings.getInstance().config.elasticSearch.rubricsIndex + CommonSettings.getInstance().config.getNamespaceSufix();
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

	/**
	 * Returns all indexes that can be created from scratch based on database (SQL) data
	 *
	 * @return
	 */
	public static List<String> getRecreatableIndexes() {
		return Arrays.asList(INDEX_USERS, INDEX_NODES, INDEX_USER_GROUP, INDEX_RUBRIC_NAME);
	}

	public static List<String> getSystemIndexes() {
		return Arrays.asList(INDEX_USERS, INDEX_LOGS, INDEX_ASSOCRULES, INDEX_RECOMMENDATION_DATA, INDEX_JOBS_LOGS);
	}

	public static List<String> getOrganizationIndexes() {
		return Arrays.asList(INDEX_NODES, INDEX_USERS, INDEX_USER_GROUP, INDEX_RUBRIC_NAME);
	}

	/**
	 * Returns all indexes that contain data which can't be repopulated based on existing data in a database(s)
	 */
	public static List<String> getNonrecreatableSystemIndexes() {
		return getSystemIndexes()
				.stream()
				.filter(ind -> getRecreatableIndexes()
						.stream()
						.noneMatch(rInd -> ind == rInd))
				.collect(Collectors.toList());
	}

	/**
	 * Returns all system level indexes that can be created from scratch based on database (SQL) data
	 *
	 * @return
	 */
	public static List<String> getRecreatableSystemIndexes() {
		return getSystemIndexes()
				.stream()
				.filter(ind -> getRecreatableIndexes()
						.stream()
						.anyMatch(rInd -> ind == rInd))
				.collect(Collectors.toList());
	}

	/**
	 * Returns all organization level indexes that can be created from scratch based on database (SQL) data
	 *
	 * @return
	 */
	public static List<String> getRecreatableOrganizationIndexes() {
		return getOrganizationIndexes()
				.stream()
				.filter(ind -> getRecreatableIndexes()
						.stream()
						.anyMatch(rInd -> ind == rInd))
				.collect(Collectors.toList());
	}
	
	private static boolean isRightName(String name) {
		return name.startsWith("INDEX_");
	}
}
 
