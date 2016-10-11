package org.prosolo.services.logging;

import static java.lang.String.format;
import static org.prosolo.common.domainmodel.activities.events.EventType.Comment;
import static org.prosolo.common.domainmodel.activities.events.EventType.Comment_Reply;
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
import static org.prosolo.common.domainmodel.activities.events.EventType.RemoveLike;
import static org.prosolo.common.domainmodel.activities.events.EventType.SEND_MESSAGE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

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
import org.prosolo.common.domainmodel.activitywall.old.NodeSocialActivity;
import org.prosolo.common.domainmodel.activitywall.old.PostSocialActivity;
import org.prosolo.common.domainmodel.activitywall.old.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.activitywall.old.UserSocialActivity;
import org.prosolo.common.domainmodel.activitywall.old.comments.NodeComment;
import org.prosolo.common.domainmodel.activitywall.old.comments.SocialActivityComment;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.evaluation.EvaluationSubmission;
import org.prosolo.common.event.context.LearningContext;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.util.date.DateEpochUtil;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.indexing.LoggingESService;
import org.prosolo.services.logging.exception.LoggingException;
import org.prosolo.services.messaging.LogsMessageDistributer;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.common.event.context.LearningContext;
import org.prosolo.common.event.context.data.LearningContextData;

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
	
	private static Logger logger = Logger.getLogger(LoggingService.class.getName());
	
	@Inject
	private LogsMessageDistributer logsMessageDistributer;
	@Inject 
	private LogsDataManager logsDataManager;
	@Inject
	private ContextJsonParserService contextJsonParserService;
	@Inject
	private EventFactory eventFactory;
	@Inject @Qualifier("taskExecutor")
	private ThreadPoolTaskExecutor taskExecutor;

	@Inject
	private LoggingESService loggingESService;

	@Autowired
	private ApplicationBean applicationBean;

	private static String pageNavigationCollection = "log_page_navigation";
	private static String serviceUseCollection = "log_service_use";
	private static String eventsHappenedCollection = "log_events_happened";
	private static String logReportDatesCollection= "log_report_dates";
	
	private EventType[] interactions=new EventType[]{
			Comment, EVALUATION_REQUEST,EVALUATION_ACCEPTED, EVALUATION_GIVEN,
			JOIN_GOAL_INVITATION, JOIN_GOAL_INVITATION_ACCEPTED,JOIN_GOAL_REQUEST,
			JOIN_GOAL_REQUEST_APPROVED, JOIN_GOAL_REQUEST_DENIED,
			Like,Dislike, SEND_MESSAGE, PostShare,
			Comment_Reply, RemoveLike};
	
	@Override
	public void logServiceUse(long actorId, String componentName,
			String parametersJson, String ipAddress) throws LoggingException {

		Map<String, String> parameters = convertToMap(parametersJson);
		
		logEventObserved(EventType.SERVICEUSE, actorId, componentName, 0, null,
				null, 0, parameters, ipAddress);
	}

	@Override
	public void logServiceUse(long userId, ComponentName component, String link,
			Map<String, String> parameters, String ipAddress) throws LoggingException {

		parameters.put("link", link);
		logEventObserved(EventType.SERVICEUSE, userId, component.name(), 0, link,
				null, 0, parameters, ipAddress);
	}

	@Override
	public void logNavigation(long userId, String link, String ipAddress) throws LoggingException {

		Map<String, String> parameters = new HashMap<>();
		parameters.put("link", link);
		logEventObserved(EventType.NAVIGATE, userId, "page", 0, link, null, 0, parameters, ipAddress);
	}

	@Override
	public void logNavigationFromContext(long userId, String link,
			String context, String page, String learningContext, String service, 
			String parametersString, String ipAddress) throws LoggingException {

		Map<String, String> parameters = convertToMap(parametersString);
		
		if (context != null && context.length() > 0) {
			parameters.put("context", context);
		}
		parameters.put("objectType", "page");
		parameters.put("link", link);
		
		try {
			eventFactory.generateEvent(EventType.NAVIGATE, userId, null, null,
					page, learningContext, service, parameters);
		} catch (EventException e) {
			logger.error("Generate event failed.", e);
		}
	}
	
	@Override
	public void logEmailNavigation(long actorId, String link,
			String parametersString, String ipAddress,
			LearningContextData lContext) throws LoggingException {

		Map<String, String> parameters = convertToMap(parametersString);
		
		parameters.put("objectType", "email");
		parameters.put("link", link);
		
		try {
			eventFactory.generateEvent(EventType.NAVIGATE, actorId, null, null, 
					lContext.getPage(), lContext.getLearningContext(), lContext.getService(), parameters);
		} catch (EventException e) {
			logger.error("Generate event failed.", e);
		}
	}

	@Override
	public void logTabNavigationFromContext(long userId, String tabName,
			String context, String parametersString, String ipAddress) throws LoggingException {

		Map<String, String> parameters = convertToMap(parametersString);
	
		if (context != null && context.length() > 0) {
			parameters.put("context", context);
		}
		
		parameters.put("link", tabName);
		
		logEventObserved(EventType.NAVIGATE, userId, "tab", 0, null, null, 0, parameters, ipAddress);
	}

	@Override
	public void logEvent(final EventType eventType, final long actorId, final String ipAddress) {
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					logEventObserved(eventType, actorId, null, 0, null, null, 0, null, ipAddress);
				} catch (LoggingException e) {
					logger.error(e);
				}
			}
		});
	}
	
	@Override
	public void logEventObserved(final EventType eventType, final long actorId, final String objectType, final long objectId, 
			final Map<String, String> parameters, final String ipAddress) {
		taskExecutor.execute(new Runnable() {
		    @Override
		    public void run() {
		    	try {
		    		logEventObserved(eventType, actorId, objectType, objectId, null, null, 0, parameters, ipAddress);
				} catch (LoggingException e) {
					logger.error(e);
				}
		    }
		});
	}
	
	@Override
	public void logEventObserved(EventType eventType, long actorId,
			String objectType, long objectId, String objectTitle,
			String targetType, long targetId,
			Map<String, String> parameters, String ipAddress) throws LoggingException {

		//if (!Settings.getInstance().config.init.formatDB) {
	 
			if (parameters == null) {
				parameters = new HashMap<String, String>();
			}
	
			parameters.put("ip", ipAddress);
	
			//DBObject logObject = new BasicDBObject();
			JSONObject logObject=new JSONObject();
			logObject.put("timestamp", System.currentTimeMillis());
			logObject.put("eventType", eventType.name());
	
			String link = parameters.get("link");
	
			logObject.put("actorId", actorId);
			logObject.put("objectType", objectType);
			logObject.put("objectId", objectId);
			logObject.put("objectTitle", objectTitle);
			logObject.put("targetType", targetType);
			logObject.put("targetId", targetId);
			logObject.put("link", link);

			Long targetUserId=(long) 0;
			if(Arrays.asList(interactions).contains(eventType)){
				 System.out.println("TARGET USER SHOULD BE PROVIDED:"+logObject.toString());
				 targetUserId=extractSocialInteractionTargetUser(logObject, eventType);
				System.out.println("TARGET USER IS:"+targetUserId);
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
			Long courseId = parameters.get("courseId") != null ? Long.parseLong(parameters.get("courseId")) : 0;
			
			logger.info("\ntimestamp: " + timestamp + 
		 			"\neventType: " + eventType + 
		 			"\nactorId: " + logObject.get("actorId") + 
		 			"\nobjectType: " + objectType + 
		 			(((Long) logObject.get("objectId")) > 0 ? "\nobjectId: " + logObject.get("objectId") : "") + 
		 			(logObject.get("objectTitle") != null ? "\nobjectTitle: " + logObject.get("objectTitle") : "") + 
		 			(logObject.get("targetType") != null ? "\ntargetType: " + logObject.get("targetType") : "") + 
					(((Long) logObject.get("targetId")) > 0 ? "\ntargetId: " + logObject.get("targetId") : "") +
					(courseId > 0 ? "\ncourseId: " + courseId : "") +
					(((Long) logObject.get("targetUserId")) > 0 ? "\ntargetUserId: " + logObject.get("targetUserId") : "") +
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
	//	}
	}

	private Long extractSocialInteractionTargetUser(JSONObject logObject, EventType eventType){
		long actorId=(long) logObject.get("actorId");
		String objectType=(String) logObject.get("objectType");
		long objectId= (long) logObject.get("objectId");
		String targetType=   (String) logObject.get("targetType");
		long targetId= (long) logObject.get("targetId");
		Long targetUserId=(long)0;
		if(objectType.equals(NodeRequest.class.getSimpleName())){
			targetUserId=logsDataManager.getRequestMaker(actorId, objectId);
		}else if(objectType.equals(EvaluationSubmission.class.getSimpleName())){
			targetUserId=logsDataManager.getEvaluationSubmissionRequestMaker(actorId,objectId);
		}else if(objectType.equals(Comment1.class.getSimpleName())){
			if(eventType.equals(EventType.Like) || eventType.equals(EventType.RemoveLike)){
				targetUserId=logsDataManager.getCommentMaker(objectId);
			}else targetUserId=logsDataManager.getParentCommentMaker(objectId);
		}


		else {
			if(eventType.equals(EventType.SEND_MESSAGE)){
				JSONObject parameters=(JSONObject) logObject.get("parameters");
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
		//TODO this method should be changed because domain model changed
		return targetUserId != null ? targetUserId : 0;
	}

 	private Long extractCourseIdForUsedResource(LearningContext learningContext) {
		Long courseId=extractCourseIdFromContext(learningContext.getContext());
		/*if(learningContext != null && learningContext.getContext() != null) {
			if(learningContext.getContext().getContext().getName().equals(ContextName.CREDENTIAL)){
					courseId=learningContext.getContext().getContext().getId();
					System.out.println("ExtractedCourse id:"+courseId);
			}
		}*/
		System.out.println("EXTRACTED COURSE ID:"+courseId);
		return courseId;
	}
	private Long extractCourseIdFromContext(Context context){
		if(context==null){
			return 0l;
		}else	if(context.getName().equals(ContextName.CREDENTIAL)){
			return context.getId();
		}else return extractCourseIdFromContext(context.getContext());
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
	public void logSessionEnded(EventType eventType, long userId, String ipAddress) {
		Map<String, String> parameters = new HashMap<>();
		//parameters.put("ip", ipAddress);
		try {
			//ip address will be null
			eventFactory.generateEvent(eventType, userId, null, null, parameters);
		} catch (EventException e) {
			logger.error("Generate event failed.", e);
		}
	}

	//added for migration to new context approach
	@Override
	public void logEventObserved(EventType eventType, long actorId,
			String objectType, long objectId, String objectTitle,
			String targetType, long targetId,
			Map<String, String> parameters, String ipAddress, LearningContext learningContext) throws LoggingException {

		//if (!Settings.getInstance().config.init.formatDB) {
			 
			if (parameters == null) {
				parameters = new HashMap<String, String>();
			}
	
			parameters.put("ip", ipAddress);
	
			//DBObject logObject = new BasicDBObject();
			JSONObject logObject=new JSONObject();
			logObject.put("timestamp", System.currentTimeMillis());
			logObject.put("date", DateEpochUtil.getDaysSinceEpoch(System.currentTimeMillis()));
			logObject.put("eventType", eventType.name());
	
			if (actorId > 0) {
				this.recordUserActivity(actorId, System.currentTimeMillis());
			}
			
			String link = parameters.get("link");
	
			logObject.put("actorId", actorId);
		    logObject.put("sessionId",getSessionId(actorId));
			logObject.put("objectType", objectType);
			logObject.put("objectId", objectId);
			logObject.put("objectTitle", objectTitle);
			logObject.put("targetType", targetType);
			logObject.put("targetId", targetId);
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

			  logObject.put("courseId",extractCourseIdForUsedResource(learningContext));
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
				System.out.println("TARGET USER SHOULD BE EXTRACTED FOR THIS EVENT:"+logObject.toString());
				targetUserId=extractSocialInteractionTargetUser(logObject, eventType);

			}else{
				System.out.println("We are not interested in this interaction for target user id:"+eventType.name());
			}
			logObject.put("targetUserId", targetUserId);

			Object timestamp = logObject.get("timestamp");
			
			String linkString = logObject.get("link") != null ? "\nlink: " + logObject.get("link") : "";
			String context = parameters.get("context") != null ? parameters.get("context") : "";
			String action = parameters.get("action") != null ? parameters.get("action") : "";

			logger.debug("LOG:"+logObject.toJSONString());
			logger.debug("\ntimestamp: " + timestamp + 
		 			"\neventType: " + eventType + 
		 			"\nactorId: " + logObject.get("actorId") + 
		 			"\nobjectType: " + objectType + 
		 			(((Long) logObject.get("objectId")) > 0 ? "\nobjectId: " + logObject.get("objectId") : "") + 
		 			(logObject.get("objectTitle") != null ? "\nobjectTitle: " + logObject.get("objectTitle") : "") + 
		 			(logObject.get("targetType") != null ? "\ntargetType: " + logObject.get("targetType") : "") + 
					(((Long) logObject.get("targetId")) > 0 ? "\ntargetId: " + logObject.get("targetId") : "") +
					(((Long) logObject.get("targetUserId")) > 0 ? "\ntargetUserId: " + logObject.get("targetUserId") : "") +
					linkString +
				 	"\nparameters: " + logObject.get("parameters") +
				 	"\nlearning context:\n " + logObject.get("learningContext"));

			String targetTypeString = logObject.get("targetType") != null ? (String) logObject.get("targetType") : "";
			
			// timestamp,eventType,objectType,targetType,link,context,action
			logger.info(timestamp + "," + eventType + "," + objectType + "," + targetTypeString + "," + link + "," + context + "," + action);
			loggingESService.storeEventObservedLog(logObject);
			logsMessageDistributer.distributeMessage(logObject);
			/*
			try {
				@SuppressWarnings("unused")
				WriteResult wr = this.getEventObservedCollection().insert(logObject);
			} catch (Exception e) {
				logger.error("Exception to log observed event for:" + logObject.toString(), e);
			}*/

	//	}
	}

	private String getSessionId(long userId){
		System.out.println("getSessionIdForUser:"+userId);
		String sessionId="";
		if(userId>0){
			HttpSession httpSession = applicationBean.getUserSession(userId);
			if (httpSession != null) {
				LoggedUserBean loggedUserBean = (LoggedUserBean) httpSession
						.getAttribute("loggeduser");
				if (loggedUserBean != null) {
					if (!loggedUserBean.isInitialized()) {
						loggedUserBean.initializeSessionData(httpSession);
					}
					sessionId=loggedUserBean.getSessionData().getSessionId();
					System.out.println("SESSION ID:"+sessionId);
				}
			}
		}
		return sessionId;
	}
	
	@Override
	public void logServiceUse(long userId, ComponentName component, Map<String, String> params, 
			String ipAddress, LearningContextData context) throws LoggingException {
		LearningContext learningContext = null;
		if(context != null) {
			learningContext = contextJsonParserService
					.parseCustomContextString(context.getPage(), context.getLearningContext(), 
							context.getService());
		}
		logEventObserved(EventType.SERVICEUSE, userId, component.name(), 0, null,
				null, 0, params, ipAddress, learningContext);
	}

}
