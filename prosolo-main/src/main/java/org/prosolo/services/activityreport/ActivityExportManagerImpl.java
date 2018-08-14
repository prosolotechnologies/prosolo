package org.prosolo.services.activityreport;
/*
import static org.joda.time.DateTimeZone.UTC;
import static org.prosolo.services.activityreport.ArchiveExportUtil.createArchivePath;
import static org.prosolo.services.activityreport.ArchiveExportUtil.formatDate;
import static org.prosolo.services.activityreport.ArchiveExportUtil.formatDateDetailed;
import static org.prosolo.services.activityreport.LoggedEvent.D;
import static org.prosolo.services.activityreport.LoggedEvent.NL;
import static org.prosolo.services.activityreport.LoggedEvent.getCSVHeaderDebugAddon;
import static org.prosolo.services.activityreport.LoggedEvent.getCSVHeaderRow;
import static org.prosolo.util.urigenerator.AmazonS3Utility.createFullPathFromRelativePath;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.logging.LoggingService;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.upload.AmazonS3UploadManager;
import org.prosolo.util.urigenerator.AmazonS3Utility;
import org.prosolo.util.urigenerator.MD5HashUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.DBObject;*/

/**
 * Class that does the extraction of Prosolo activity reports for each user and creating of CSV files. The results of
 * the extraction process are stored in mongoDB database by the {@link LoggingService}.
 * 
 * @author vita
 */
//@Service("org.prosolo.services.nodes.ActivityExportManager")
@Deprecated
public class ActivityExportManagerImpl  {

/*	// excel requires that all csv files with unicode characters start with Byte Order Marker,
	// otherwise it does not display UTF characters properly
	private static final char UNICODE_BOM = '\ufeff';

	public static final boolean DEVELOPMENT = Settings.getInstance().config.application.projectMode;

	private static boolean running = false;

	private static final long serialVersionUID = 1L;

	@Autowired
	LoggingService loggingService;

	@Autowired
	AmazonS3UploadManager uploadManager;

	@Autowired
	UserManager userManager;

	@Autowired
	CompetenceManager competenceManager;

	@Autowired
	NotificationManager notificationManager;

	@Autowired
	EventFactory eventFactory;
	
	private ByteArrayOutputStream uploadStream;
	private ZipOutputStream archiveStream; 

	private boolean isFirstRun() {
		return loggingService.reportDatesCollectionExists() == false;
	}

	private Long goBackOnePeridInTime(Long ts) {
		return new DateTime(ts).minusDays(REPORTING_PERIOD_DAYS).getMillis();
	}

	@Override
	public void runActivityExport() {
		if (running)
			return;

		running = true;
		long currentTime = System.currentTimeMillis();

		Session session = (Session) getPersistence().openSession();
		Map<Long, String> nodeIdNameMap = initNodeIdNameMap(session);
		Map<Long, String> nodeIdTypeMap = initNodeIdTypeMap(session);
		Map<Long, String> postIdTextMap = initPostIdTextMap(session);
		Map<Long, String> allUsers = initAllUsers(session);
		HibernateUtil.close(session);

		if (isFirstRun()) {
			logger.info("FIRST RUN OF ACTIVITY REPORT GENERATOR.");
			logger.info("GENERATION ACTIVITY REPORTS FOR ALL PREVIOUS PERIODS STARTED");

			long oldesttEventObservedTime = loggingService.getOldestObservedEventTime();

			logger.info("OLDEST EVENT OBSERVED ON: "
			        + formatDateDetailed(new DateTime(oldesttEventObservedTime).toDate()));

			while (oldesttEventObservedTime <= currentTime) {
				runActivityFromDate(currentTime, nodeIdNameMap, nodeIdTypeMap, postIdTextMap, allUsers);
				currentTime = goBackOnePeridInTime(currentTime);
			}
			logger.info("GENERATION ACTIVITY REPORTS FOR ALL PREVIOUS PERIODS FINISHED");

		} else {
			logger.info("RUN OF ACTIVITY REPORT GENERATOR.");
			runActivityFromDate(currentTime, nodeIdNameMap, nodeIdTypeMap, postIdTextMap, allUsers);
		}
		logger.info("FINISHED ACTIVITY REPORT GENERATOR.");
		running = false;
	}

	private void runActivityFromDate(long date, Map<Long, String> nodeIdNameMap, Map<Long, String> nodeIdTypeMap,
	        Map<Long, String> postIdTextMap, Map<Long, String> allUsers) {

		Date[] searchRange = getSearchTimeRange(date);

		logger.info("GENERATING ACTIVITY REPORTS FOR THE PERIOD BETWEEN " + formatDateDetailed(searchRange[0])
		        + " AND " + formatDateDetailed(searchRange[1]) + " STARTED");

		List<Long> generatedReportsUserIds = createAndUploadCSVs(loggingService.getAllLoggedEvents(searchRange[0], searchRange[1]), formatDate(searchRange[0]),
		        formatDate(searchRange[1]), nodeIdNameMap, nodeIdTypeMap, postIdTextMap,
		        allUsers, searchRange);
		
		loggingService.recordActivityReportGenerated(generatedReportsUserIds, searchRange[0]);

		logger.info("GENERATING ACTIVITY REPORTS FINISHED");
	}


	@Override
	public String exportCompleteLog(DBObject filterQuery) {
//		if (running)
//			return null;
		logger.info("exportCompeteLog");
		running = true;
		
		Collection<LoggedEvent> mongoEvents = loggingService.getLoggedEventsList(filterQuery);
				

		if (!DEVELOPMENT)
			running = false;

		return createAndUploadCSVZip(mongoEvents);
		
	}

	private String createAndUploadCSVZip(Collection<LoggedEvent> mongoEvents) {
		logger.info("createAndUploadCSVZip");
		StringBuffer allRawCsv = new StringBuffer();
		
		String resultURL = "";
		uploadStream = new ByteArrayOutputStream();
		archiveStream = new ZipOutputStream(uploadStream);
		Session session = (Session) getPersistence().openSession();
		Map<Long, String> nodeIdNameMap = initNodeIdNameMap(session);
		Map<Long, String> nodeIdTypeMap = initNodeIdTypeMap(session);
		Map<Long, String> postIdTextMap = initPostIdTextMap(session);
		Map<Long, String> allUsers = initAllUsers(session);

		List<String> rawCsv = createCSV(mongoEvents, nodeIdNameMap, nodeIdTypeMap, new HashMap<Long, String>(), postIdTextMap, allUsers, new HashMap<Long, String>(), session);

		if (rawCsv != null) {
			allRawCsv.append(rawCsv.get(1)); // append version with debug info

			// if (!DEVELOPMENT) {
			String csv = UNICODE_BOM + getCSVHeaderRow() + NL + rawCsv.get(0); // for
																				// regular
																				// users,
																				// use
																				// normal
																				// version
			csv = csv.replaceAll("\"\"", ""); // replace empty cells with
												// nothing, so that e.g., ,"",
												// becomes
												// just ,,

			// currently I'm using 1, as there will be only single document
			// once we have the split implemented, than this should be updated
			appendEntryToArchive(csv, "1");										

			try {
				archiveStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			resultURL = uploadZIPFile(uploadStream);
		}			
			
		HibernateUtil.close(session);
		return resultURL;
	}
	
	private void appendEntryToArchive(String rowEntries, String appendName){
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		Date date = new Date();		
		
		String filename = "export_data_" + dateFormat.format(date).replace("/", "_") +"_part_" + appendName + ".csv";		
		
		ZipEntry archiveEntry = new ZipEntry(filename);					
		try {					
			archiveStream.putNextEntry(archiveEntry);
			archiveStream.write(rowEntries.getBytes(Charset.forName("UTF-8")));			
			archiveStream.closeEntry();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}
	
	private String uploadZIPFile(ByteArrayOutputStream outputfile){	//
		Date now=new Date();
		String key = null;
		String returnURL = "";
		try {
			key = MD5HashUtility.getMD5Hex(String.valueOf(now.getTime()));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {			
			uploadManager.storeInputStreamByKey(new ByteArrayInputStream(outputfile.toByteArray()), key + ".zip", "application/zip");			
			String path = AmazonS3Utility.createFullPathFromRelativePath(key + ".zip");					
			logger.info("PATH: " + path);
			
			returnURL = path;			
			uploadStream.close();			
		} catch (Exception e) { 
			e.printStackTrace();			
		}
		
		return returnURL;
			
	}

	private Map<Long, String> createUsersIdNameMap(Collection<User> col) {
		Map<Long, String> res = new HashMap<Long, String>();
		for (User e : col)
			res.put(e.getId(), e.getName() + " " + e.getLastname());
		return res;
	}

	// private List<Map<Long, String>> getNodeIdNameMap() {
	// Map<Long, String> idNameMap = new HashMap<Long, String>();
	// Map<Long, String> idTypeMap = new HashMap<Long, String>();
	//
	// @SuppressWarnings("unchecked")
	// List<Object> vals = getPersistence().currentManager()
	// .createSQLQuery("SELECT title, id, dtype FROM prosolo.node")
	// .list();
	//
	// Iterator<Object> it = vals.iterator();
	// while (it.hasNext()) {
	// Object[] o = (Object[]) it.next();
	// String title = (String) o[0];
	// Long id = Long.parseLong(o[1].toString());
	// String type = (String) o[2];
	// idNameMap.put(id.longValue(), title);
	// idTypeMap.put(id.longValue(), type);
	// }
	// List<Map<Long, String>> result = new ArrayList<Map<Long, String>>();
	// result.add(idNameMap);
	// result.add(idTypeMap);
	// return result;
	// }

	// private Map<Long, String> getPostIdNameMap() {
	// Map<Long, String> idPostMap = new HashMap<Long, String>();
	//
	// @SuppressWarnings("unchecked")
	// List<Object> vals = getPersistence().currentManager()
	// .createSQLQuery("SELECT id, content FROM prosolo.post where dtype='Post'").list();
	//
	// Iterator<Object> it = vals.iterator();
	// while (it.hasNext()) {
	// Object[] o = (Object[]) it.next();
	// Long id = Long.parseLong(o[0].toString());
	// String content = (String) o[1];
	// idPostMap.put(id.longValue(), content);
	// }
	// return idPostMap;
	// }

	private List<Long> createAndUploadCSVs(Map<Long, Collection<LoggedEvent>> mongoEvents, String fromDateString,
	        String toDateString, Map<Long, String> nodeIdNameMap, Map<Long, String> nodeIdTypeMap, Map<Long, String> postIdTextMap, Map<Long, String> allUsers, Date[] searchRange) {

		
		Session session = (Session) getPersistence().openSession();
		
		Map<Long, String> socialActivityIdTextMap = initSocialActivityIdTextMap(session, searchRange);
		Map<Long, String> notificationIdTextMap = initNotificationIdTextMap(session, searchRange);

		List<Long> generatedReportsUserIds = new LinkedList<Long>();
		StringBuffer allRawCsv = new StringBuffer();

		int allSize = 0;

		for (Entry<Long, Collection<LoggedEvent>> currentEntry : mongoEvents.entrySet()) {
			Long userId = currentEntry.getKey();

			Collection<LoggedEvent> currentEvents = currentEntry.getValue();
			int size = currentEvents.size();
			allSize += size;

			List<String> rawCsv = createCSV(currentEvents, nodeIdNameMap, nodeIdTypeMap, notificationIdTextMap,
			        postIdTextMap, allUsers, socialActivityIdTextMap, session);

			if (rawCsv != null) {
				allRawCsv.append(rawCsv.get(1)); // append version with debug info

				if (!DEVELOPMENT) {
					String csv = createCSV(getCSVHeaderRow(), rawCsv.get(0));
					String path = createArchivePath(userId, fromDateString, toDateString);

					logger.info("userID:" + userId + " records: " + size + " archive path: "
					        + createFullPathFromRelativePath(path));

					try {
						uploadManager.storeInputStreamByKey(new ByteArrayInputStream(csv.getBytes("UTF-8")), path,
						        "text/csv");

						generatedReportsUserIds.add(userId);
						logger.debug("NOTIFY USER SKIPPED:" + userId);
						// this.notifyUser(userId);

					} catch (UnsupportedEncodingException e) {
						logger.error(e);
					}

				} else {
					generatedReportsUserIds.add(userId);
				}
			}
		}

		if (allSize > 0) {
//			System.out.println("Stigao do uploada");
			String path = uploadAllCsv(fromDateString, toDateString, allRawCsv);
//			System.out.println("zavrsio upload");
			generatedReportsUserIds.add(-1l);
			logger.info("ALL records: " + allSize + " archive path: " + createFullPathFromRelativePath(path));
		}
//		System.out.println("closing session");
		HibernateUtil.close(session);
		
		return generatedReportsUserIds; // return the ids of the users for whom we generated reports
	}

	private String uploadAllCsv(String fromDateString, String toDateString, StringBuffer allRawCsv) {
		String allCsv = createCSV(getCSVHeaderRow() + getCSVHeaderDebugAddon(), allRawCsv.toString());
		String path = createArchivePath(-1l, fromDateString, toDateString);

		try {
			uploadManager.storeInputStreamByKey(new ByteArrayInputStream(allCsv.getBytes("UTF-8")), path, "text/csv");
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		}
		return path;
	}

	private String createCSV(String header, String content) {
		String allCsv = UNICODE_BOM + header + NL + content;
		return allCsv.replaceAll("\"\"", "");
	}

	private void notifyUser(Long userId) {
		if (userId > 0) {
			// notificationManager.createNotification(resource, creator,
			// receiver, type, message, date, session)
			User sendTo = null;
			try {
				sendTo = loadResource(User.class, userId);
			} catch (ResourceCouldNotBeLoadedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				eventFactory.generateEvent(EventType.ACTIVITY_REPORT_AVAILABLE, sendTo);
			} catch (EventException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unused")
	private void notifyAllUsers(List<Long> generatedReportsUserIds) {
		for (Long userId : generatedReportsUserIds) {
			this.notifyUser(userId);
		}
	}

	private Date[] getSearchTimeRange(long currentTime) {
		DateTime end = getLastSundayMidnight(currentTime);
		DateTime start = end.minusDays(REPORTING_PERIOD_DAYS);
		end = end.minusMillis(1);
		return new Date[] { start.toDate(), end.toDate() };
	}

	private DateTime getLastSundayMidnight(long currentTime) {
		DateTime currentDay = new DateTime(currentTime, UTC).withTimeAtStartOfDay(); // last midnight
		while (currentDay.getDayOfWeek() != DateTimeConstants.MONDAY)
			currentDay = currentDay.minusDays(1);

		return currentDay;
	}

	private List<String> createCSV(Collection<LoggedEvent> allEvents, Map<Long, String> nodeIdNameMap,
	        Map<Long, String> nodeIdTypeMap, Map<Long, String> notificationIdTextMap, Map<Long, String> postIdTextMap,
	        Map<Long, String> allUsers, Map<Long, String> socialActivityIdTextMap, Session session) {
		logger.info("createCSV:"+allEvents.size());
		if (allEvents.size() == 0)
			return null;

		StringBuffer result = new StringBuffer();
		StringBuffer resultDebug = new StringBuffer();

		for (LoggedEvent e : allEvents) {

			// first elemenet in the csv list is the 'normal' csv row, the debug part is in the second list element.
			List<String> csv = e.toCVSString(allUsers, nodeIdNameMap, nodeIdTypeMap, postIdTextMap,
			        socialActivityIdTextMap, notificationIdTextMap, session);
			result.append(csv.get(0) + NL);
			resultDebug.append(csv.get(0) + D + csv.get(1) + NL);
		}

		List<String> res = new ArrayList<String>();
		res.add(result.toString());
		res.add(resultDebug.toString());
		return res;
	}

	/**
	 * @param query
	 *            , should return two values, first is id, the second is the string value that gets mapped.
	 */

 /*   private Map<Long, String> mapIdToVal(String query, Session session) {
//		System.out.println("RUNNING QUERY: " + query);
		List<Object> r = (List<Object>) session.createSQLQuery(query).list();
//		System.out.println("DONE");
		return populateResults(r);
	}

	@SuppressWarnings("unchecked")
	private Map<Long, String> mapIdToVal(String query, Session session, Date from, Date to) {
//		System.out.println("RUNNING QUERY: " + query + " FROM:" + from + " TO: " +  to);
		List<Object> r = (List<Object>) session.createSQLQuery(query).setDate("from", from).setDate("to", to).list();
//		System.out.println("DONE");
		return populateResults(r);
	}

	private Map<Long, String> populateResults(List<Object> vals) {
	    Map<Long, String> result = new HashMap<Long, String>();
		Iterator<Object> it = vals.iterator();
		while (it.hasNext()) {
			Object[] o = (Object[]) it.next();
			result.put(((Long) Long.parseLong(o[0].toString())).longValue(), (String) o[1]);
		}
	    return result;
    }

	private Map<Long, String> initNodeIdNameMap(Session session) {
		return mapIdToVal("SELECT id, title FROM prosolo.node", session);
	}

	private Map<Long, String> initNodeIdTypeMap(Session session) {
		return mapIdToVal("SELECT id, dtype FROM prosolo.node", session);
	}

	private Map<Long, String> initNotificationIdTextMap(Session session, Date[] searchRange) {
		return mapIdToVal("SELECT san.id, sa.text FROM prosolo.social_activity_notification san, prosolo.social_activity sa " + 
				"WHERE san.social_activity = sa.id and sa.last_action BETWEEN :from AND :to",
				session, searchRange[0], searchRange[1]);
	}

	private Map<Long, String> initSocialActivityIdTextMap(Session session, Date[] searchRange) {
		return mapIdToVal("SELECT id, text FROM prosolo.social_activity WHERE last_action BETWEEN :from AND :to", 
				session, searchRange[0], searchRange[1]);
	}

	private Map<Long, String> initPostIdTextMap(Session session) {
		return mapIdToVal("SELECT id, content FROM prosolo.post", session);
	}

	private Map<Long, String> initAllUsers(Session session) {
		return createUsersIdNameMap(userManager.getAllUsers());
	}
*/
}
