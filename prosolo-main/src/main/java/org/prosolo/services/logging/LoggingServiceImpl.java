package org.prosolo.services.logging;

import static java.lang.String.format;
import static java.util.Calendar.DAY_OF_MONTH;
import static org.prosolo.common.domainmodel.activities.events.EventType.Comment;
import static org.prosolo.common.domainmodel.activities.events.EventType.Dislike;
import static org.prosolo.common.domainmodel.activities.events.EventType.EVALUATION_ACCEPTED;
import static org.prosolo.common.domainmodel.activities.events.EventType.EVALUATION_GIVEN;
import static org.prosolo.common.domainmodel.activities.events.EventType.EVALUATION_REQUEST;
import static org.prosolo.common.domainmodel.activities.events.EventType.JOIN_GOAL_INVITATION;
import static org.prosolo.common.domainmodel.activities.events.EventType.JOIN_GOAL_INVITATION_ACCEPTED;
import static org.prosolo.common.domainmodel.activities.events.EventType.JOIN_GOAL_REQUEST;
import static org.prosolo.common.domainmodel.activities.events.EventType.JOIN_GOAL_REQUEST_APPROVED;
import static org.prosolo.common.domainmodel.activities.events.EventType.JOIN_GOAL_REQUEST_DENIED;
import static org.prosolo.common.domainmodel.activities.events.EventType.Like;
import static org.prosolo.common.domainmodel.activities.events.EventType.PostShare;
import static org.prosolo.common.domainmodel.activities.events.EventType.SEND_MESSAGE;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activities.requests.NodeRequest;
import org.prosolo.common.domainmodel.activitywall.NodeSocialActivity;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity;
import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.activitywall.UserSocialActivity;
import org.prosolo.common.domainmodel.activitywall.comments.NodeComment;
import org.prosolo.common.domainmodel.activitywall.comments.SocialActivityComment;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.evaluation.EvaluationSubmission;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityreport.LoggedEvent;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.context.ContextName;
import org.prosolo.services.event.context.LearningContext;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.logging.exception.LoggingException;
import org.prosolo.services.messaging.LogsMessageDistributer;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.web.ApplicationPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/*import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
*/
/**
 * @author Zoran Jeremic 2013-10-07
 * 
 */
@Service("org.prosolo.services.logging.LoggingService")
public class LoggingServiceImpl extends AbstractDB implements LoggingService {
	
	@Autowired LogsMessageDistributer logsMessageDistributer;
	@Autowired
	CourseManager courseManager;
	@Autowired LogsDataManager logsDataManager;

	private static Logger logger = Logger.getLogger(LoggingService.class.getName());
	
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	@Autowired private EventFactory eventFactory;

	private static String pageNavigationCollection = "log_page_navigation";
	private static String serviceUseCollection = "log_service_use";
	private static String eventsHappenedCollection = "log_events_happened";

	private static String logReportDatesCollection= "log_report_dates";
	private EventType[] interactions=new EventType[]{
			Comment, EVALUATION_REQUEST,EVALUATION_ACCEPTED, EVALUATION_GIVEN,
			JOIN_GOAL_INVITATION, JOIN_GOAL_INVITATION_ACCEPTED,JOIN_GOAL_REQUEST,
			JOIN_GOAL_REQUEST_APPROVED, JOIN_GOAL_REQUEST_DENIED,
			Like,Dislike, SEND_MESSAGE, PostShare};
	
	@PostConstruct
	public void init() {
		this.ensureIndexes();
	}

	private void ensureIndexes() {
		//this.index(userLatestActivityTimeCollection, "{userid:1}");
		//this.index(userActivitiesCollection, "{userid:1, date:1}");

	}
	
	/*@Override
	public Collection<LoggedEvent> getLoggedEventsList(DBObject filterQuery) {		
		
		DBCursor res = getEventObservedCollection().find(filterQuery);
		
		Collection<LoggedEvent> eventCollection = new ArrayList<LoggedEvent>();		
		
		while(res.hasNext()) {
			LoggedEvent currentEvent = new LoggedEvent(res.next());												
			eventCollection.add(currentEvent);
		}
		return eventCollection;
	}*/

	@Override
	public void logServiceUse(User user, String componentName,
			String parametersJson, String ipAddress) throws LoggingException {

		Map<String, String> parameters = convertToMap(parametersJson);
		
		logEventObserved(EventType.SERVICEUSE, user, componentName, 0, null,
				null, 0, null, 0, parameters, ipAddress);
	}

	@Override
	public void logServiceUse(User user, ComponentName component, String link,
			Map<String, String> parameters, String ipAddress) throws LoggingException {

		parameters.put("link", link);
		logEventObserved(EventType.SERVICEUSE, user, component.name(), 0, link,
				null, 0, null, 0, parameters, ipAddress);
	}

	@Override
	public void logNavigation(User user, String link, String ipAddress) throws LoggingException {

		Map<String, String> parameters = new HashMap<>();
		parameters.put("link", link);
		logEventObserved(EventType.NAVIGATE, user, "page", 0, link, null, 0,
				null, 0, parameters, ipAddress);
	}

	@Override
	public void logNavigationFromContext(User user, String link,
			String context, String parametersString, String ipAddress) throws LoggingException {

		Map<String, String> parameters = convertToMap(parametersString);
		
		if (context != null && context.length() > 0) {
			parameters.put("context", context);
		}
		parameters.put("objectType", "page");
		parameters.put("link", link);
		
		try {
			eventFactory.generateEvent(EventType.NAVIGATE, user, null, parameters);
		} catch (EventException e) {
			logger.error("Generate event failed.", e);
		}
	}
	
	@Override
	public void logEmailNavigation(User user, String link,
			String parametersString, String ipAddress,
			LearningContextData lContext) throws LoggingException {

		Map<String, String> parameters = convertToMap(parametersString);
		
		parameters.put("objectType", "email");
		parameters.put("link", link);
		
		try {
			eventFactory.generateEvent(EventType.NAVIGATE, user, null, null, 
					lContext.getPage(), lContext.getLearningContext(), lContext.getService(), parameters);
		} catch (EventException e) {
			logger.error("Generate event failed.", e);
		}
	}

	@Override
	public void logTabNavigationFromContext(User user, String tabName,
			String context, String parametersString, String ipAddress) throws LoggingException {

		Map<String, String> parameters = convertToMap(parametersString);
	
		if (context != null && context.length() > 0) {
			parameters.put("context", context);
		}
		
		parameters.put("link", tabName);
		
		logEventObserved(EventType.NAVIGATE, user, "tab", 0, null, null, 0,
				null, 0, parameters, ipAddress);
	}

	@Override
	public void logEvent(final EventType eventType, final User actor, final String ipAddress) {
		if (actor != null) {
			taskExecutor.execute(new Runnable() {
			    @Override
			    public void run() {
			    	try {
						logEventObserved(eventType, actor, null, 0, null, null, 0, null, 0, null, ipAddress);
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
		    		logEventObserved(eventType, actor, objectType, objectId, null, null, 0, null, 0, parameters, ipAddress);
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
			Map<String, String> parameters, String ipAddress) throws LoggingException {

		if (!Settings.getInstance().config.init.formatDB) {
	 
			if (parameters == null) {
				parameters = new HashMap<String, String>();
			}
	
			parameters.put("ip", ipAddress);
	
			//DBObject logObject = new BasicDBObject();
			JSONObject logObject=new JSONObject();
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
			
			String link = parameters.get("link");
	
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

			logObject.put("courseId",extractCourseIdForUsedResource(objectType, objectId, targetType, targetId, reasonType, reasonId,null));
			Long targetUserId=(long) 0;
			if(Arrays.asList(interactions).contains(eventType)){
				//System.out.println("INTERACTION SHOULD BE PROCESSED:"+logObject.toString());
				 targetUserId=extractSocialInteractionTargetUser(logObject, eventType);
			}
			logObject.put("targetUserId", targetUserId);
			if (parameters != null && !parameters.isEmpty()) {
				Iterator<Map.Entry<String, String>> it = parameters.entrySet()
						.iterator();
				//DBObject parametersObject = new BasicDBObject();
				JSONObject parametersObject = new JSONObject();
	
				while (it.hasNext()) {
					Map.Entry<String, String> pairs = (Map.Entry<String, String>) it
							.next();
					parametersObject.put(pairs.getKey(), pairs.getValue());
				}
				logObject.put("parameters", parametersObject);
			}

			Object timestamp = logObject.get("timestamp");
			
			String linkString = logObject.get("link") != null ? "\nlink: " + logObject.get("link") : "";
			String context = parameters.get("context") != null ? parameters.get("context") : "";
			String action = parameters.get("action") != null ? parameters.get("action") : "";
			
			logger.info("\ntimestamp: " + timestamp + 
		 			"\neventType: " + eventType + 
		 			"\nactorId: " + logObject.get("actorId") + 
		 			"\nactorFullname: " + logObject.get("actorFullname") + 
		 			"\nobjectType: " + objectType + 
		 			(((Long) logObject.get("objectId")) > 0 ? "\nobjectId: " + logObject.get("objectId") : "") + 
		 			(logObject.get("objectTitle") != null ? "\nobjectTitle: " + logObject.get("objectTitle") : "") + 
		 			(logObject.get("targetType") != null ? "\ntargetType: " + logObject.get("targetType") : "") + 
					(((Long) logObject.get("targetId")) > 0 ? "\ntargetId: " + logObject.get("targetId") : "") +
					(((Long) logObject.get("courseId")) > 0 ? "\ncourseId: " + logObject.get("courseId") : "") +
					(((Long) logObject.get("targetUserId")) > 0 ? "\ntargetUserId: " + logObject.get("targetUserId") : "") +
					(logObject.get("reasonType") != null ? "\nreasonType: " + logObject.get("reasonType") : "") + 
					(((Long) logObject.get("reasonId")) > 0 ? "\nreasonId: " + logObject.get("reasonId") : "") + 
					linkString +
				 	"\nparameters: " + logObject.get("parameters"));

			String targetTypeString = logObject.get("targetType") != null ? (String) logObject.get("targetType") : "";
			
			// timestamp,eventType,objectType,targetType,link,context,action
			logger.info(timestamp + "," + eventType + "," + objectType + "," + targetTypeString + "," + link + "," + context + "," + action);
			
			logsMessageDistributer.distributeMessage(logObject);
				
		/*	try {
				@SuppressWarnings("unused")
				WriteResult wr = this.getEventObservedCollection().insert(logObject);
			} catch (Exception e) {
				logger.error("Exception to log observed event for:" + logObject.toString(), e);
			}
*/
		}
	}

	private Long extractSocialInteractionTargetUser(JSONObject logObject, EventType eventType){
		long actorId=(long) logObject.get("actorId");
		String objectType=(String) logObject.get("objectType");
		long objectId= (long) logObject.get("objectId");
		String targetType=   (String) logObject.get("targetType");
		long targetId= (long) logObject.get("targetId");
		String reasonType= (String) logObject.get("reasonType");
		long reasonId=(long) logObject.get("reasonId");
		Long targetUserId=(long)0;
		if(objectType.equals(NodeRequest.class.getSimpleName())){
			targetUserId=logsDataManager.getRequestMaker(actorId, objectId);
		}else if(objectType.equals(EvaluationSubmission.class.getSimpleName())){
			targetUserId=logsDataManager.getEvaluationSubmissionRequestMaker(actorId,objectId);
		}else {
			if(eventType.equals(EventType.SEND_MESSAGE)){
				JSONObject parameters=(JSONObject) logObject.get("parameters");
				if(logObject==null){
					System.out.println("X");
				}
				if(parameters==null){
					System.out.println("Y");
				}
				if(parameters.get("user")==null){
					System.out.println("Z");
				}
				System.out.println("SEND MESSAGE:"+logObject.toString()+" USER:"+parameters.get("user"));
				targetUserId=  Long.valueOf(parameters.get("user").toString());
			}else if(eventType.equals(EventType.Like) || eventType.equals(EventType.Dislike)){
				if(objectType.equals(PostSocialActivity.class.getSimpleName())||objectType.equals(SocialActivityComment.class.getSimpleName())||
						objectType.equals(TwitterPostSocialActivity.class.getSimpleName())||objectType.equals(UserSocialActivity.class.getSimpleName())||
						objectType.equals(UserSocialActivity.class.getSimpleName())||objectType.equals(NodeSocialActivity.class.getSimpleName())||
						objectType.equals(NodeComment.class.getSimpleName())){
					targetUserId=logsDataManager.getSocialActivityMaker(actorId,objectId);
					System.out.println("GET FOR SOCIAL ACTIVITY:"+actorId+" targetUser:"+targetUserId+" objectId:"+objectId);
				}else if(objectType.equals(TargetActivity.class.getSimpleName())){
					targetUserId=logsDataManager.getActivityMakerForTargetActivity(actorId,objectId);
				}
			}else if(eventType.equals(EventType.Comment)){
				if(objectType.equals(SocialActivityComment.class.getSimpleName())){
					targetUserId=logsDataManager.getSocialActivityMaker(actorId,targetId);
				}else if(objectType.equals(NodeComment.class.getSimpleName())){
						if(targetType.equals(TargetActivity.class.getSimpleName())){
							targetUserId=logsDataManager.getActivityMakerForTargetActivity(actorId,targetId);
					}
				}
			}else if(eventType.equals(EventType.PostShare)){
				if(objectType.equals(Post.class.getSimpleName())){
					targetUserId=logsDataManager.getPostMaker(actorId,objectId);
				}
			}
		}
		System.out.println("TARGET USER ID:"+targetUserId);
		return targetUserId;
	}

	private Long extractCourseIdForUsedResource(String objectType, long objectId, String targetType, long targetId, String reasonType, long reasonId,LearningContext learningContext) {
		Map<String, Long> types=new HashMap<String, Long>();
		types.put(objectType, objectId);
		types.put(targetType, targetId);
		types.put(reasonType, reasonId);
		Long courseId=0l;
		if(types.containsKey(TargetLearningGoal.class.getSimpleName())){
			courseId= courseManager.findCourseIdForTargetLearningGoal(types.get(TargetLearningGoal.class.getSimpleName()));
		}else if(types.containsKey(TargetCompetence.class.getSimpleName())){
			courseId= courseManager.findCourseIdForTargetCompetence(types.get(TargetCompetence.class.getSimpleName()));
		}else if(types.containsKey(TargetActivity.class.getSimpleName())){
			courseId= courseManager.findCourseIdForTargetActivity(types.get(TargetActivity.class.getSimpleName()));
		}/*else  if(types.containsKey("NodeRequest ...")){
			System.out.println("Node request ...");
		}else  if(types.containsKey("PostSocialActivity")){
			System.out.println("Post Social Activity ...");
		}else if(types.containsKey("SocialActivityComment")){
			System.out.println("SHOULD GET SOCIAL ACTIVITY COMMENT");
		}*/
		if(courseId==0 && learningContext!=null && learningContext.getPage()!=null){
			if(learningContext.getPage().equals(ApplicationPage.LEARN)){
				if(learningContext.getContext().getName().equals(ContextName.CREDENTIAL)){
					courseId=learningContext.getContext().getId();
				}
			}
		}
			System.out.println("ExtractedCourse id:"+courseId);

		return courseId;
	}


	@Override
	public void recordUserActivity(long userid, long time) throws LoggingException {
		if (!Settings.getInstance().config.init.formatDB) {
			/*DBObject query = new BasicDBObject();
			query.put("userid", userid);
	
			DBObject update = new BasicDBObject();
			update.put("$set", new BasicDBObject().append("time", time));
			
			DBCollection collection = this.getUserLatestActivityTimeCollection();
			
			try {
				collection.update(query, update, true, true);
			} catch (MongoException me) {
				throw new LoggingException("Mongo store is not available");
			}*/
		}
	}

	@Override
	public void increaseUserActivityLog(long userid, long date) {
		if (!Settings.getInstance().config.init.formatDB) {
		/*	DBObject query = new BasicDBObject();
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
			*/
		}
	}

	/*public DBCollection getPageNavigationCollection() {
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
	}*/
	private Map<String, String> convertToMap(String parametersJson) {
		Map<String, String> parameters = new HashMap<String, String>();
		
		if (parametersJson != null && parametersJson.length() > 0) {
			//BasicDBObject parametersObject = (BasicDBObject) JSON.parse(parametersJson);
			try{
				JSONObject parametersObject=(JSONObject) new JSONParser().parse(parametersJson);
				Set<String> keys = parametersObject.keySet();

				for (String key : keys) {
					parameters.put(key, parametersObject.get(key).toString());
				}
			}catch(ParseException x){
				logger.error(x);
			}

		}
		return parameters;
	}

/*	@Override
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
*/
	
	/*@Override
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
	}*/
	
/*	@Override
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
	*/
/*	@Override
	public List<Date> getReportDays(Date start, Date end, Long userId) {
		
		List<LocalDate> dates = getDaysBetween(start, end); // get all dates in the range
		
		HashMap<String, List<Integer>> reportDays = getMonthReportDaysMap(userId, dates); // each element of the map is a month while the list are all the dates for which we have recorded an activity log 
		
		List<Date> result = new ArrayList<Date>();
		for(LocalDate d: dates) // go through the list of dates and see if there was a activity report generated on a given day
			if(reportDays.get(getMonthYear(d)).contains(d.getDayOfMonth()))
				result.add(d.toDateTimeAtStartOfDay().toDate());
		
		return result;
	}*/
	/*@Override
	public boolean collectionExists(String collectionName) { 
		return getDb().collectionExists(collectionName);
	}

	@Override
	public boolean reportDatesCollectionExists() {
		return collectionExists(logReportDatesCollection);
	}

*/
	private static List<LocalDate> getDaysBetween(Date start, Date end) {
		List<LocalDate> dates = new ArrayList<LocalDate>();

		for (int i=0; i < Days.daysBetween(new LocalDate(start), new LocalDate(end)).getDays(); i++)
			dates.add(new LocalDate(start).withFieldAdded(DurationFieldType.days(), i));
		
		return dates;
	}
	
	/*private HashMap<String, List<Integer>> getMonthReportDaysMap(Long userId, List<LocalDate> dates) {
		HashMap<String, List<Integer>> reportDays = new HashMap<String, List<Integer>>();
		
		for(String monthYear : getUniqueMonths(dates)) // typically 2-3 months are covering the whole period
			reportDays.put(monthYear, getReportDaysForMonth(userId, monthYear)); // for each of them find all dates which are having activity reports
		
		return reportDays;
	}*/

	private Set<String> getUniqueMonths(List<LocalDate> dates) {
		Set<String> months = new HashSet<String>();
		
		for(LocalDate d: dates)
			months.add(getMonthYear(d));
		
		return months;
	}
	
	private static final String getMonthYear(LocalDate d) {
		return Integer.toString(d.getYear()) + format("%02d", d.getMonthOfYear());	
	}
	/*
	private List<Integer> getReportDaysForMonth(Long userId, String monthYear) {
		DBObject dbResult = getReportDatesCollection().findOne(new BasicDBObject("actorId", userId).append("month", monthYear));

		List<Integer> days = new ArrayList<Integer>();
		if(dbResult != null)
			for(Object day: (BasicDBList) dbResult.get("days"))
				days.add((Integer) day);
		
		return days;
	}*/
	/*@Override

    public Long getOldestObservedEventTime() {
		return (Long) getEventObservedCollection().find(new BasicDBObject("actorId", new BasicDBObject("$ne", 0)))
				.sort(new BasicDBObject("timestamp", 1))
				.limit(1)
				.next().get("timestamp");
    }
*/
	@Override
	public void logSessionEnded(EventType eventType, User actor, String ipAddress) {
		Map<String, String> parameters = new HashMap<>();
		//parameters.put("ip", ipAddress);
		try {
			//ip address will be null
			eventFactory.generateEvent(eventType, actor, null, parameters);
		} catch (EventException e) {
			logger.error("Generate event failed.", e);
		}
	}

	//added for migration to new context approach
	@Override
	public void logEventObserved(EventType eventType, User actor,
			String objectType, long objectId, String objectTitle,
			String targetType, long targetId, String reasonType, long reasonId,
			Map<String, String> parameters, String ipAddress, LearningContext learningContext) throws LoggingException {

		if (!Settings.getInstance().config.init.formatDB) {
			 
			if (parameters == null) {
				parameters = new HashMap<String, String>();
			}
	
			parameters.put("ip", ipAddress);
	
			//DBObject logObject = new BasicDBObject();
			JSONObject logObject=new JSONObject();
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
			
			String link = parameters.get("link");
	
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

			
			if(learningContext != null) {
				Gson gson = new GsonBuilder().create();
				String learningContextJson = gson.toJson(learningContext);
				//DBObject lContext = (DBObject) JSON.parse(learningContextJson);
				try {
					JSONObject lContext = (JSONObject) new JSONParser().parse(learningContextJson);
					logObject.put("learningContext", lContext);
					System.out.println("HAS LEARNING CONTEXT...:" + lContext.toString());
				}catch(ParseException e){
					logger.error(e);
				}
			}

			logObject.put("courseId",extractCourseIdForUsedResource(objectType, objectId, targetType, targetId, reasonType, reasonId,learningContext));
			Long targetUserId=(long) 0;

			if (parameters != null && !parameters.isEmpty()) {
				Iterator<Map.Entry<String, String>> it = parameters.entrySet()
						.iterator();
				//DBObject parametersObject = new BasicDBObject();
				JSONObject parametersObject=new JSONObject();
	
				while (it.hasNext()) {
					Map.Entry<String, String> pairs = (Map.Entry<String, String>) it
							.next();
					parametersObject.put(pairs.getKey(), pairs.getValue());
				}
				logObject.put("parameters", parametersObject);
			}
			if(Arrays.asList(interactions).contains(eventType)){
				System.out.println("INTERACTION SHOULD BE PROCESSED:"+logObject.toString());
				targetUserId=extractSocialInteractionTargetUser(logObject, eventType);
			}else{
				System.out.println("We are not interested in this interaction:"+eventType.name());
			}
			logObject.put("targetUserId", targetUserId);

			Object timestamp = logObject.get("timestamp");
			
			String linkString = logObject.get("link") != null ? "\nlink: " + logObject.get("link") : "";
			String context = parameters.get("context") != null ? parameters.get("context") : "";
			String action = parameters.get("action") != null ? parameters.get("action") : "";
			
			logger.info("\ntimestamp: " + timestamp + 
		 			"\neventType: " + eventType + 
		 			"\nactorId: " + logObject.get("actorId") + 
		 			"\nactorFullname: " + logObject.get("actorFullname") + 
		 			"\nobjectType: " + objectType + 
		 			(((Long) logObject.get("objectId")) > 0 ? "\nobjectId: " + logObject.get("objectId") : "") + 
		 			(logObject.get("objectTitle") != null ? "\nobjectTitle: " + logObject.get("objectTitle") : "") + 
		 			(logObject.get("targetType") != null ? "\ntargetType: " + logObject.get("targetType") : "") + 
					(((Long) logObject.get("targetId")) > 0 ? "\ntargetId: " + logObject.get("targetId") : "") +
					(((Long) logObject.get("courseId")) > 0 ? "\ncourseId: " + logObject.get("courseId") : "") +
					(((Long) logObject.get("targetUserId")) > 0 ? "\ntargetUserId: " + logObject.get("targetUserId") : "") +
					(logObject.get("reasonType") != null ? "\nreasonType: " + logObject.get("reasonType") : "") + 
					(((Long) logObject.get("reasonId")) > 0 ? "\nreasonId: " + logObject.get("reasonId") : "") + 
					linkString +
				 	"\nparameters: " + logObject.get("parameters"));

			String targetTypeString = logObject.get("targetType") != null ? (String) logObject.get("targetType") : "";
			
			// timestamp,eventType,objectType,targetType,link,context,action
			logger.info(timestamp + "," + eventType + "," + objectType + "," + targetTypeString + "," + link + "," + context + "," + action);
			
			logsMessageDistributer.distributeMessage(logObject);
			/*
			try {
				@SuppressWarnings("unused")
				WriteResult wr = this.getEventObservedCollection().insert(logObject);
			} catch (Exception e) {
				logger.error("Exception to log observed event for:" + logObject.toString(), e);
			}*/

		}
	}

}
