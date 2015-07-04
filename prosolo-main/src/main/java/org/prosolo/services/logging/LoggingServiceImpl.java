package org.prosolo.services.logging;

import static java.lang.String.format;
import static java.util.Calendar.DAY_OF_MONTH;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDate;
import org.prosolo.app.Settings;
import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.activityreport.LoggedEvent;
import org.prosolo.services.logging.exception.LoggingException;
import org.prosolo.services.messaging.LogsMessageDistributer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

/**
 * @author Zoran Jeremic 2013-10-07
 * @param <E>
 * 
 */
@Service("org.prosolo.services.logging.LoggingService")
public class LoggingServiceImpl extends AbstractDB implements LoggingService {
	
	@Autowired LogsMessageDistributer logsMessageDistributer;

	private static Logger logger = Logger.getLogger(LoggingService.class.getName());
	
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;

	private static String pageNavigationCollection = "log_page_navigation";
	private static String serviceUseCollection = "log_service_use";
	private static String eventsHappenedCollection = "log_events_happened";

	private static String logReportDatesCollection= "log_report_dates";
	
	@PostConstruct
	public void init() {
		this.ensureIndexes();
	}

	private void ensureIndexes() {
		this.index(userLatestActivityTimeCollection, "{userid:1}");
		this.index(userActivitiesCollection, "{userid:1, date:1}");

	}
	
	@Override
	public Collection<LoggedEvent> getLoggedEventsList(DBObject filterQuery) {		
		
		DBCursor res = getEventObservedCollection().find(filterQuery);
		
		Collection<LoggedEvent> eventCollection = new ArrayList<LoggedEvent>();		
		
		while(res.hasNext()) {
			LoggedEvent currentEvent = new LoggedEvent(res.next());												
			eventCollection.add(currentEvent);
		}
		return eventCollection;
	}

	@Override
	public void logServiceUse(User user, String componentName,
			String parametersJson, String ipAddress) throws LoggingException {

		Map<String, String> parameters = convertToMap(parametersJson);

		logEventObserved(EventType.SERVICEUSE, user, componentName, 0, null,
				null, 0, null, 0, parameters, null, ipAddress);
	}

	@Override
	public void logServiceUse(User user, ComponentName component, String link,
			Map<String, String> parameters, String ipAddress) throws LoggingException {

		logEventObserved(EventType.SERVICEUSE, user, component.name(), 0, link,
				null, 0, null, 0, parameters, link, ipAddress);
	}

	@Override
	public void logNavigation(User user, String link, String ipAddress) throws LoggingException {

		logEventObserved(EventType.NAVIGATE, user, "page", 0, link, null, 0,
				null, 0, null, link, ipAddress);
	}

	@Override
	public void logNavigationFromContext(User user, String link,
			String context, String parametersString, String ipAddress) throws LoggingException {

		Map<String, String> parameters = convertToMap(parametersString);
		
		if (context != null && context.length() > 0) {
			parameters.put("context", context);
		}

		logEventObserved(EventType.NAVIGATE, user, "page", 0, null, null, 0,
				null, 0, parameters, link, ipAddress);
	}

	@Override
	public void logTabNavigationFromContext(User user, String tabName,
			String context, String parametersString, String ipAddress) throws LoggingException {

		Map<String, String> parameters = convertToMap(parametersString);
		
		if (context != null && context.length() > 0) {
			parameters.put("context", context);
		}
		
		logEventObserved(EventType.NAVIGATE, user, "tab", 0, null, null, 0,
				null, 0, parameters, tabName, ipAddress);
	}

	@Override
	public void logEvent(final EventType eventType, final User actor, final String ipAddress) {
		if (actor != null) {
			taskExecutor.execute(new Runnable() {
			    @Override
			    public void run() {
			    	try {
						logEventObserved(eventType, actor, null, 0, null, null, 0, null, 0, null, null, ipAddress);
					} catch (LoggingException e) {
						logger.error(e);
					}
			    }
			});
		}
	}
	
	@Override
	public void logEventObserved(final EventType eventType, final User actor, final String objectType, final long objectId, 
			final Map<String, String> parameters, final String ipAddress) {
		taskExecutor.execute(new Runnable() {
		    @Override
		    public void run() {
		    	try {
		    		logEventObserved(eventType, actor, objectType, objectId, null, null, 0, null, 0, parameters, null, ipAddress);
				} catch (LoggingException e) {
					logger.error(e);
				}
		    }
		});
	}

	@Override
	public void logEventObserved(EventType eventType, User actor,
			String objectType, long objectId, String objectTitle,
			String targetType, long targetId, String reasonType, long reasonId,
			Map<String, String> parameters, String link, String ipAddress) throws LoggingException {
		
		if (!Settings.getInstance().config.init.formatDB) {
	 
			if (parameters == null) {
				parameters = new HashMap<String, String>();
			}
	
			parameters.put("ip", ipAddress);
	
			DBObject logObject = new BasicDBObject();
			logObject.put("timestamp", System.currentTimeMillis());
			logObject.put("eventType", eventType.name());
	
			long actorId = 0;
			String actorName = "";
	
			if (actor != null) {
				actorId = actor.getId();
				actorName = actor.getName() + " " + actor.getLastname();
				this.recordUserActivity(actorId, System.currentTimeMillis());
			} else {
				actorId = 0;
				actorName = "ANONYMOUS";
			}
	
			logObject.put("actorId", actorId);
			logObject.put("actorFullname", actorName);
			logObject.put("objectType", objectType);
			logObject.put("objectId", objectId);
			logObject.put("objectTitle", objectTitle);
			logObject.put("targetType", targetType);
			logObject.put("targetId", targetId);
			logObject.put("reasonType", reasonType);
			logObject.put("reasonId", reasonId);
			logObject.put("link", link);
	
			if (parameters != null && !parameters.isEmpty()) {
				Iterator<Map.Entry<String, String>> it = parameters.entrySet()
						.iterator();
				DBObject parametersObject = new BasicDBObject();
	
				while (it.hasNext()) {
					Map.Entry<String, String> pairs = (Map.Entry<String, String>) it
							.next();
					parametersObject.put(pairs.getKey(), pairs.getValue());
				}
				logObject.put("parameters", parametersObject);
			}

			logger.trace("\n timestamp: " + logObject.get("timestamp") + 
		 			"\n eventType: " + logObject.get("eventType") + 
		 			"\n actorId: " + logObject.get("actorId") + 
		 			"\n actorFullname: " + logObject.get("actorFullname") + 
		 			"\n objectType: " + logObject.get("objectType") + 
		 			(((Long) logObject.get("objectId")) > 0 ? "\n objectId: " + logObject.get("objectId") : "") + 
		 			(logObject.get("objectTitle") != null ? "\n objectTitle: " + logObject.get("objectTitle") : "") + 
		 			(logObject.get("targetType") != null ? "\n targetType: " + logObject.get("targetType") : "") + 
					(((Long) logObject.get("targetId")) > 0 ? "\n targetId: " + logObject.get("targetId") : "") + 
					(logObject.get("reasonType") != null ? "\n reasonType: " + logObject.get("reasonType") : "") + 
					(((Long) logObject.get("reasonId")) > 0 ? "\n reasonId: " + logObject.get("reasonId") : "") + 
					(logObject.get("link") != null ? "\n link: " + logObject.get("link") : "") +
				 	"\n parameters: " + logObject.get("parameters"));
			logsMessageDistributer.distributeMessage(logObject);
				
			try {
				@SuppressWarnings("unused")
				WriteResult wr = this.getEventObservedCollection().insert(logObject);
			} catch (Exception e) {
				logger.error("Exception to log observed event for:" + logObject.toString(), e);
			}
		}
	}

	@Override
	public void recordUserActivity(long userid, long time) throws LoggingException {
		if (!Settings.getInstance().config.init.formatDB) {
			DBObject query = new BasicDBObject();
			query.put("userid", userid);
	
			DBObject update = new BasicDBObject();
			update.put("$set", new BasicDBObject().append("time", time));
			
			DBCollection collection = this.getUserLatestActivityTimeCollection();
			
			try {
				collection.update(query, update, true, true);
			} catch (MongoException me) {
				throw new LoggingException("Mongo store is not available");
			}
		}
	}

	@Override
	public void increaseUserActivityLog(long userid, long date) {
		if (!Settings.getInstance().config.init.formatDB) {
			DBObject query = new BasicDBObject();
			query.put("userid", userid);
			query.put("date", date);
			
			DBCollection collection = this.getUserActivitiesCollection();
			DBObject record = collection.findOne(query);

			if (record == null) {
				query.put("counter", 1);
				collection.insert(query);
			} else {
				DBObject modifier = new BasicDBObject("counter", 1);
				DBObject incQuery = new BasicDBObject("$inc", modifier);
				collection.update(query, incQuery);
			}
		}
	}

	public DBCollection getPageNavigationCollection() {
		return this.getCollection(pageNavigationCollection);
	}

	public DBCollection getServiceUseCollection() {
		return this.getCollection(serviceUseCollection);
	}

	public DBCollection getEventHappenedCollection() {
		return this.getCollection(eventsHappenedCollection);
	}
	public DBCollection getReportDatesCollection() {
		return this.getCollection(logReportDatesCollection);
	}
	private Map<String, String> convertToMap(String parametersJson) {
		Map<String, String> parameters = new HashMap<String, String>();
		
		if (parametersJson != null && parametersJson.length() > 0) {
			BasicDBObject parametersObject = (BasicDBObject) JSON.parse(parametersJson);
			Set<String> keys = parametersObject.keySet();
			
			for (String key : keys) {
				parameters.put(key, parametersObject.getString(key));
			}
		}
		return parameters;
	}

	@Override
	public Map<Long, Collection<LoggedEvent>> getAllLoggedEvents(Date start, Date end) {
		BasicDBObject query = new BasicDBObject("timestamp", new BasicDBObject("$gt", start.getTime()).append("$lte", end.getTime())).append("actorId", new BasicDBObject("$ne", 0));

		DBCursor res = getEventObservedCollection().find(query);
		
		Map<Long, Collection<LoggedEvent>> result = new HashMap<Long, Collection<LoggedEvent>>();
		
		while(res.hasNext()) {
			LoggedEvent currentEvent = new LoggedEvent(res.next());			
			Long actorId = currentEvent.getActorId();
			
			Collection<LoggedEvent> currentCol = null;
			if(result.containsKey(actorId) == false) {
				currentCol = new ArrayList<LoggedEvent>();				
				result.put(actorId, currentCol);
			} else {
				currentCol = result.get(actorId);
			}
			currentCol.add(currentEvent);
		}
		return result;
	}

	
	@Override
	public Map<Long, Collection<LoggedEvent>> getAllLoggedEvents(DBObject filterQuery) {		

		DBCursor res = getEventObservedCollection().find(filterQuery);
		
		Map<Long, Collection<LoggedEvent>> result = new HashMap<Long, Collection<LoggedEvent>>();
		
		while(res.hasNext()) {
			LoggedEvent currentEvent = new LoggedEvent(res.next());			
			Long actorId = currentEvent.getActorId();
			
			Collection<LoggedEvent> currentCol = null;
			if(result.containsKey(actorId) == false) {
				currentCol = new ArrayList<LoggedEvent>();				
				result.put(actorId, currentCol);
			} else {
				currentCol = result.get(actorId);
			}
			currentCol.add(currentEvent);
		}
		return result;
	}
	
	@Override
	public void recordActivityReportGenerated(List<Long> userIds, Date reportGenerationDate) {
		Calendar reportDate = Calendar.getInstance();
		reportDate.setTimeZone(TimeZone.getTimeZone("UTC"));
		reportDate.setTime(reportGenerationDate);
		
		BasicDBObjectBuilder queryTemplate = BasicDBObjectBuilder.start("month", getMonthYear(new LocalDate(reportGenerationDate)));
		BasicDBObject updateObject = new BasicDBObject("$addToSet", new BasicDBObject("days", reportDate.get(DAY_OF_MONTH)));
		
		for(Long userId: userIds){
			getReportDatesCollection().update(queryTemplate.add("actorId", userId).get(), updateObject, true, true);
		}
	}
	
	@Override
	public List<Date> getReportDays(Date start, Date end, Long userId) {
		
		List<LocalDate> dates = getDaysBetween(start, end); // get all dates in the range
		
		HashMap<String, List<Integer>> reportDays = getMonthReportDaysMap(userId, dates); // each element of the map is a month while the list are all the dates for which we have recorded an activity log 
		
		List<Date> result = new ArrayList<Date>();
		for(LocalDate d: dates) // go through the list of dates and see if there was a activity report generated on a given day
			if(reportDays.get(getMonthYear(d)).contains(d.getDayOfMonth()))
				result.add(d.toDateTimeAtStartOfDay().toDate());
		
		return result;
	}
	@Override
	public boolean collectionExists(String collectionName) { 
		return getDb().collectionExists(collectionName);
	}

	@Override
	public boolean reportDatesCollectionExists() {
		return collectionExists(logReportDatesCollection);
	}


	private static List<LocalDate> getDaysBetween(Date start, Date end) {
		List<LocalDate> dates = new ArrayList<LocalDate>();

		for (int i=0; i < Days.daysBetween(new LocalDate(start), new LocalDate(end)).getDays(); i++)
			dates.add(new LocalDate(start).withFieldAdded(DurationFieldType.days(), i));
		
		return dates;
	}
	
	private HashMap<String, List<Integer>> getMonthReportDaysMap(Long userId, List<LocalDate> dates) {
		HashMap<String, List<Integer>> reportDays = new HashMap<String, List<Integer>>();
		
		for(String monthYear : getUniqueMonths(dates)) // typically 2-3 months are covering the whole period
			reportDays.put(monthYear, getReportDaysForMonth(userId, monthYear)); // for each of them find all dates which are having activity reports
		
		return reportDays;
	}

	private Set<String> getUniqueMonths(List<LocalDate> dates) {
		Set<String> months = new HashSet<String>();
		
		for(LocalDate d: dates)
			months.add(getMonthYear(d));
		
		return months;
	}
	
	private static final String getMonthYear(LocalDate d) {
		return Integer.toString(d.getYear()) + format("%02d", d.getMonthOfYear());	
	}
	
	private List<Integer> getReportDaysForMonth(Long userId, String monthYear) {
		DBObject dbResult = getReportDatesCollection().findOne(new BasicDBObject("actorId", userId).append("month", monthYear));

		List<Integer> days = new ArrayList<Integer>();
		if(dbResult != null)
			for(Object day: (BasicDBList) dbResult.get("days"))
				days.add((Integer) day);
		
		return days;
	}
	@Override

    public Long getOldestObservedEventTime() {
		return (Long) getEventObservedCollection().find(new BasicDBObject("actorId", new BasicDBObject("$ne", 0)))
				.sort(new BasicDBObject("timestamp", 1))
				.limit(1)
				.next().get("timestamp");
    }


}
