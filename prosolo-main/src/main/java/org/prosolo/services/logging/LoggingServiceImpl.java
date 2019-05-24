package org.prosolo.services.logging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDate;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.GradingMode;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.common.event.context.LearningContext;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.util.date.DateEpochUtil;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.core.db.hibernate.HibernateUtil;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.indexing.LoggingESService;
import org.prosolo.services.interaction.AnalyticalServiceCollector;
import org.prosolo.services.logging.exception.LoggingException;
import org.prosolo.services.messaging.LogsMessageDistributer;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.ApplicationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

import static java.lang.String.format;
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
	private EventFactory eventFactory;
	@Inject @Qualifier("taskExecutor")
	private ThreadPoolTaskExecutor taskExecutor;
	@Inject private DefaultManager defaultManager;

    @Inject
    private AnalyticalServiceCollector analyticalServiceCollector;

	@Inject
	private LoggingESService loggingESService;

	@Autowired
	private ApplicationBean applicationBean;


	private static String pageNavigationCollection = "log_page_navigation";
	private static String serviceUseCollection = "log_service_use";
	private static String eventsHappenedCollection = "log_events_happened";
	private static String logReportDatesCollection= "log_report_dates";
	
	private EventType[] interactions=new EventType[]{
			EventType.Comment,
			EventType.AssessmentRequested,
			EventType.AssessmentApproved,
			EventType.GRADE_ADDED,
			EventType.Like,
			EventType.Dislike,
			EventType.SEND_MESSAGE,
			EventType.PostShare,
			EventType.Comment_Reply,
			EventType.RemoveLike,
			EventType.AssessmentComment
	};
	
	@Override
	public void logServiceUse(UserContextData context, String componentName,
			String parametersJson, String ipAddress) throws LoggingException {

		Map<String, String> parameters = convertToMap(parametersJson);

		logEventObserved(EventType.SERVICEUSE, context, componentName, 0,null, null, parameters, ipAddress);
	}

	@Override
	public void logNavigation(UserContextData context, String link, String ipAddress) throws LoggingException {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("link", link);
		logEventObserved(EventType.NAVIGATE, context, "page", 0, link, null, parameters, ipAddress);
	}

	@Override
	public void logEventObserved(EventType eventType, UserContextData context, String objectType, long objectId, String objectTitle,
							 String targetType, Map<String, String> params, String ipAddress) throws LoggingException {
		LearningContext learningContext = null;
		if (context.getContext() != null) {
			learningContext = ContextJsonParserService
					.parseCustomContextString(context.getContext().getPage(), context.getContext().getLearningContext(),
							context.getContext().getService());
		}
		logEventObserved(eventType, context.getActorId(), context.getOrganizationId(),
				context.getSessionId(), objectType, objectId, objectTitle, targetType,0,
				params, ipAddress, learningContext);
	}

	@Override
	public void logNavigationFromContext(UserContextData context, String link, String parametersString, String ipAddress)
			throws LoggingException {

		Map<String, String> parameters = convertToMap(parametersString);

		parameters.put("objectType", "page");
		parameters.put("link", link);

		eventFactory.generateAndPublishEvent(EventType.NAVIGATE, context, null, null,null, parameters);
	}
	
	@Override
	public void logEmailNavigation(UserContextData context, String link,
								   String parametersString, String ipAddress)
			throws LoggingException {

		Map<String, String> parameters = convertToMap(parametersString);
		
		parameters.put("objectType", "email");
		parameters.put("link", link);
		
		eventFactory.generateAndPublishEvent(EventType.NAVIGATE, context, null, null, null, parameters);
	}

	@Override
	public void logTabNavigationFromContext(UserContextData userContext, String tabName,
											String parametersString, String ipAddress) throws LoggingException {

		Map<String, String> parameters = convertToMap(parametersString);
		
		parameters.put("link", tabName);

		logEventObserved(EventType.NAVIGATE, userContext, "tab",0, null, null, parameters, ipAddress);
	}

	@Override
	public void logEvent(EventType eventType, UserContextData context, String ipAddress) {
		taskExecutor.execute(() -> {
			try {
				logEventObserved(eventType, context, null, 0, null, null, null, ipAddress);
			} catch (LoggingException e) {
				logger.error(e);
			}
		});
	}
	
	@Override
	public void logEventObservedAsync(EventType eventType, UserContextData context, String objectType, long objectId,
									  Map<String, String> parameters, String ipAddress) {
		taskExecutor.execute(() -> {
			try {
				logEventObserved(eventType, context, objectType, objectId, null, null, parameters, ipAddress);
			} catch (LoggingException e) {
				logger.error(e);
			}
		});
	}

	/*
	TODO discuss with Nikola and Zoran whether this is the best place for this logic.
	I think better place would be some kind of observer in analytics app which reacts on events of interest,
	retrieves target user id depending on event type (maybe even encapsulate logic for retrieving target user id in different classes with
	one class per event type) and saves interaction in db (cassandra)
	 */
	private Long extractSocialInteractionTargetUser(JSONObject logObject, EventType eventType){
//		long actorId=(long) logObject.get("actorId");
		String objectType=(String) logObject.get("objectType");
		long objectId= (long) logObject.get("objectId");
		String targetType=   (String) logObject.get("targetType");
		long targetId= (long) logObject.get("targetId");
		Long targetUserId=(long)0;
		if (objectType.equals(Comment1.class.getSimpleName())) {
			if (eventType.equals(EventType.Like) || eventType.equals(EventType.RemoveLike)){
				targetUserId=logsDataManager.getCommentMaker(objectId);
			} else if(eventType.equals(EventType.Comment)){
				targetUserId=logsDataManager.getUserOfTargetActivity(targetId);
			} else targetUserId=logsDataManager.getParentCommentMaker(objectId);
		} else if (eventType.equals(EventType.SEND_MESSAGE)) {
				JSONObject parameters=(JSONObject) logObject.get("parameters");
				System.out.println("SEND MESSAGE:"+logObject.toString()+" USER:"+parameters.get("user"));
				targetUserId=  Long.valueOf(parameters.get("user").toString());
		} else if (eventType == EventType.AssessmentRequested || eventType == EventType.AssessmentApproved) {
			targetUserId = targetId;
		} else if (eventType == EventType.AssessmentComment) {
			Session session = (Session) defaultManager.getPersistence().openSession();
			try {
				if (CredentialAssessment.class.getSimpleName().equals(targetType)) {
					CredentialAssessment ca = (CredentialAssessment) session.load(CredentialAssessment.class,  targetId);
					targetUserId = ca.getStudent().getId();
				} else if (CompetenceAssessment.class.getSimpleName().equals(targetType)) {
					CompetenceAssessment ca = (CompetenceAssessment) session.load(CompetenceAssessment.class, targetId);
					targetUserId = ca.getStudent().getId();
				} else {
					ActivityAssessment aa = (ActivityAssessment) session.load(ActivityAssessment.class, targetId);
					targetUserId = aa.getAssessment().getStudent().getId();
				}
			} finally {
				HibernateUtil.close(session);
			}
		} else if (eventType == EventType.GRADE_ADDED) {
			Session session = (Session) defaultManager.getPersistence().openSession();
			try {
				if (CredentialAssessment.class.getSimpleName().equals(objectType)) {
					CredentialAssessment ca = (CredentialAssessment) session.load(CredentialAssessment.class,  objectId);
					if (ca.getTargetCredential().getCredential().getGradingMode() != GradingMode.AUTOMATIC) {
						//if grading is automatic, we should not count grade added event as interaction between two users
						targetUserId = ca.getStudent().getId();
					}
				} else if (CompetenceAssessment.class.getSimpleName().equals(objectType)) {
					CompetenceAssessment ca = (CompetenceAssessment) session.load(CompetenceAssessment.class, objectId);
					if (ca.getCompetence().getGradingMode() != GradingMode.AUTOMATIC) {
						//if grading is automatic, we should not count grade added event as interaction between two users
						targetUserId = ca.getStudent().getId();
					}
				} else {
					ActivityAssessment aa = (ActivityAssessment) session.load(ActivityAssessment.class, objectId);
					if (aa.getActivity().getGradingMode() != GradingMode.AUTOMATIC) {
						//if grading is automatic, we should not count grade added event as interaction between two users
						targetUserId = aa.getAssessment().getStudent().getId();
					}
				}
			} finally {
				HibernateUtil.close(session);
			}
		}
//			else if(eventType.equals(EventType.Like) || eventType.equals(EventType.Dislike)){
//				if(objectType.equals(PostSocialActivity.class.getSimpleName())||objectType.equals(SocialActivityComment.class.getSimpleName())||
//						objectType.equals(TwitterPostSocialActivity.class.getSimpleName())||objectType.equals(UserSocialActivity.class.getSimpleName())||
//						objectType.equals(UserSocialActivity.class.getSimpleName())||objectType.equals(NodeSocialActivity.class.getSimpleName())||
//						objectType.equals(NodeComment.class.getSimpleName())){
//					targetUserId=logsDataManager.getSocialActivityMaker(actorId,objectId);
//					System.out.println("GET FOR SOCIAL ACTIVITY:"+actorId+" targetUser:"+targetUserId+" objectId:"+objectId);
//				}else if(objectType.equals(TargetActivity.class.getSimpleName())){
//					targetUserId=logsDataManager.getActivityMakerForTargetActivity(actorId,objectId);
//				}
//			}else if(eventType.equals(EventType.Comment)){
//				if(objectType.equals(SocialActivityComment.class.getSimpleName())){
//					targetUserId=logsDataManager.getSocialActivityMaker(actorId,targetId);
//				}else if(objectType.equals(NodeComment.class.getSimpleName())){
//						if(targetType.equals(TargetActivity.class.getSimpleName())){
//							targetUserId=logsDataManager.getActivityMakerForTargetActivity(actorId,targetId);
//					}
//				}
//			}else if(eventType.equals(EventType.PostShare)){
//				if(objectType.equals(Post.class.getSimpleName())){
//					targetUserId=logsDataManager.getPostMaker(actorId,objectId);
//				}
//			}
		System.out.println("TARGET USER ID:"+targetUserId);
		//TODO this method should be changed because domain model changed
		return targetUserId != null ? targetUserId : 0;
	}

 	private Long extractCourseIdForUsedResource(LearningContext learningContext) {
		Long courseId = Context.getIdFromSubContextWithName(learningContext.getContext(), ContextName.CREDENTIAL);
		/*if(learningContext != null && learningContext.getContext() != null) {
			if(learningContext.getContext().getContext().getName().equals(ContextName.CREDENTIAL)){
					courseId=learningContext.getContext().getContext().getId();
					System.out.println("ExtractedCourse id:"+courseId);
			}
		}*/
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
	public void logEventObserved(EventType eventType, long actorId, long organizationId, String sessionId,
			String objectType, long objectId, String objectTitle, String targetType, long targetId,
			Map<String, String> parameters, String ipAddress, LearningContext learningContext) throws LoggingException {

		//if (!Settings.getInstance().config.init.formatDB) {
			 
			if (parameters == null) {
				parameters = new HashMap<>();
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
			logObject.put("organizationId", organizationId);
		    logObject.put("sessionId", sessionId);
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
				}catch(ParseException e){
					logger.error(e);
				}
			}
            Long courseId=extractCourseIdForUsedResource(learningContext);
			  logObject.put("courseId",courseId);
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
			}

			logObject.put("targetUserId", targetUserId);

			Object timestamp = logObject.get("timestamp");
			
			String linkString = logObject.get("link") != null ? "\nlink: " + logObject.get("link") : "";
			String context = parameters.get("context") != null ? parameters.get("context") : "";
			String action = parameters.get("action") != null ? parameters.get("action") : "";

//			logger.debug("LOG:"+logObject.toJSONString());
			logger.debug("\ntimestamp: " + timestamp + 
		 			"\neventType: " + eventType + 
		 			"\nactorId: " + logObject.get("actorId") +
					"\norganizationId: " + logObject.get("organizationId") +
					"\nsessionId: " + logObject.get("sessionId") +
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
//			logger.info(timestamp + "," + eventType + "," + objectType + "," + targetTypeString + "," + link + "," + context + "," + action);
			loggingESService.storeEventObservedLog(logObject);
			logsMessageDistributer.distributeMessage(logObject);
        if(courseId>0 && actorId>0){
            System.out.println("INCREASING USER ACTIVITY FOR CREDENTIAL...");
            analyticalServiceCollector.increaseUserActivityForCredentialLog(actorId, courseId, DateUtil.getDaysSinceEpoch());
        }
			/*
			try {
				@SuppressWarnings("unused")
				WriteResult wr = this.getEventObservedCollection().insert(logObject);
			} catch (Exception e) {
				logger.error("Exception to log observed event for:" + logObject.toString(), e);
			}*/

	//	}
	}
	
	@Override
	public void logServiceUse(UserContextData context, ComponentName component, String link, Map<String, String> params,
			String ipAddress) throws LoggingException {
		if (link != null && params == null) {
			params = new HashMap<>();
		}
		if (link != null) {
			params.put("link", link);
		}

		logEventObserved(EventType.SERVICEUSE, context, component.name(), 0,null, null, params, ipAddress);
	}

}
