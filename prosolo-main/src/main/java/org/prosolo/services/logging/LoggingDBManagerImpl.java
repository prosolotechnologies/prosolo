package org.prosolo.services.logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.services.logging.exception.LoggingException;
import org.prosolo.web.activitywall.data.UserData;
import org.prosolo.web.reports.LogParameter;
import org.prosolo.web.reports.LogRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
@author Zoran Jeremic Jan 28, 2014
 */
@Service("org.prosolo.services.logging.LoggingDBManager")
public class LoggingDBManagerImpl extends AbstractDB implements LoggingDBManager {
	
	@Autowired LoggingService loggingService;
	
	private static Logger logger = Logger.getLogger(LoggingDBManager.class.getName());
	
	@Override
	public int getLogsCount(DBObject filterQuery) {
		DBCollection eventsCollection = this.getEventObservedCollection();
		int count = 0;
		
		if (filterQuery == null) {
			count = eventsCollection.find().count();
		} else {
			count = eventsCollection.find(filterQuery).count();
		}
		return count;
	}

	@Override
	public List<String> getAllDistinctValuesOfEventField(String fieldName){
		DBCollection eventsCollection = this.getEventObservedCollection();
		
		@SuppressWarnings("unchecked")
		List<String> allDistinctValues = eventsCollection.distinct(fieldName);
		allDistinctValues.removeAll(Collections.singleton(null));
		return allDistinctValues;
	}
 
	@Override
	public DBObject createFilterQuery(List<UserData> usersToInclude,
			boolean turnOfTwitter, List<String> selectedEventTypes,
			List<String> selectedObjectTypes, Date startDate, Date endDate) {
		System.out.println("usersToInclude:"+usersToInclude.size());
		DBObject query = new BasicDBObject();
		
		if (usersToInclude != null && !usersToInclude.isEmpty()) {
			long[] users = new long[usersToInclude.size()];
			int i = 0;
			
			for (UserData uData : usersToInclude) {
				users[i] = uData.getId();
				i++;
			}
			query.put("actorId", new BasicDBObject("$in", users));
		}
		
		if (turnOfTwitter) {
			query.put("eventType", new BasicDBObject("$ne",	EventType.TwitterPost.name()));
		}
		
		if (selectedEventTypes != null && !selectedEventTypes.isEmpty()) {
			String[] eventTypes = new String[selectedEventTypes.size()];
			int i = 0;
			
			for (String eType : selectedEventTypes) {
				eventTypes[i] = eType;
				i++;
			}
			query.put("eventType", new BasicDBObject("$in", eventTypes));
		}
		
		if (selectedObjectTypes != null && !selectedObjectTypes.isEmpty()) {
			String[] objectTypes = new String[selectedObjectTypes.size()];
			int i = 0;
			
			for (String oType : selectedObjectTypes) {
				objectTypes[i] = oType;
				i++;
			}
			query.put("objectType", new BasicDBObject("$in", objectTypes));
		}
		
		if (startDate != null || endDate != null) {
			DBObject dateQuery = new BasicDBObject();
			
			if (startDate != null) {
				dateQuery.put("$gte", new ObjectId(startDate));
			}
			
			if (endDate != null) {
				dateQuery.put("$lte", new ObjectId(endDate));
			}
			query.put("_id", dateQuery);
		}
		return query;
	}

	@Override
	public List<LogRow> loadLogsForPage(int skip, int limit, DBObject filterQuery){
		DBCollection eventsCollection = this.getEventObservedCollection();
		DBObject sortBy = new BasicDBObject("_id", -1);
		DBCursor dbCursor = null;
		
		if (filterQuery == null) {
			dbCursor = eventsCollection.find().skip(skip).limit(limit).sort(sortBy);
		} else {
			dbCursor = eventsCollection.find(filterQuery).skip(skip).limit(limit).sort(sortBy);
		}
		
		List<LogRow> logs = new ArrayList<LogRow>();
		
		while (dbCursor.hasNext()) {
			DBObject dbObject = dbCursor.next();
			LogRow log = new LogRow();
			
			ObjectId id = (ObjectId) dbObject.get("_id");
			log.setId(id.toString());
			
			long time = id._time();
			Date date = new Date(time * 1000);
			log.setTime(date);
			
			long actorId = (Long) dbObject.get("actorId");
			log.setActorId(actorId);
			
			String eventType = (String) dbObject.get("eventType");
			log.setAction(eventType);
			
			String actorFullname = (String) dbObject.get("actorFullname");
			log.setActorFullname(actorFullname);
			
			String objectType = (String) dbObject.get("objectType");
			log.setObjectType(objectType);
			
			long objectId = (Long) dbObject.get("objectId");
			log.setObjectId(objectId);
			
			String objectTitle = (String) dbObject.get("objectTitle");
			log.setObjectTitle(objectTitle);
			
			String targetType = (String) dbObject.get("targetType");
			log.setTargetType(targetType);
			
			long targetId = (Long) dbObject.get("targetId");
			log.setTargetId(targetId);
			
			String targetTitle = (String) dbObject.get("targetTitle");
			log.setTargetTitle(targetTitle);

			BasicDBObject parametersList = (BasicDBObject) dbObject.get("parameters");

			if (parametersList != null) {
				Set<String> keys = parametersList.keySet();
				
				for (String key : keys) {
					LogParameter parameter = new LogParameter(key, (String) parametersList.get(key));
					log.addParameter(parameter);
				}
			}
			logs.add(log);
		}
		return logs;
	}
	
	@Override
	public DBCursor getMostActiveUsersForDate(long date, int limit) {
		DBObject query = new BasicDBObject();
		query.put("date", date);
		DBCollection activitiesCollection = this.getUserActivitiesCollection();
		DBCursor dbCursor = activitiesCollection.find(query).limit(limit);
		return dbCursor;
	}
	
	@Override
	public long getMostActiveUsersLastActivityTimestamp(long userid) {
		DBObject query = new BasicDBObject();
		query.put("userid", userid);
		DBCollection collection = this.getUserLatestActivityTimeCollection();
		DBObject dbObject = collection.findOne(query);
		long timestamp = 0;
		if (dbObject != null) {
			timestamp = (Long) dbObject.get("time");
		} else {
			DBCollection activitiesCollection = this.getUserActivitiesCollection();
			DBObject sortQuery = new BasicDBObject();
			sortQuery.put("_id", 1);
			DBCursor dbCursor = activitiesCollection.find(query).sort(sortQuery).limit(1);
			while (dbCursor.hasNext()) {
				DBObject userDateLog = dbCursor.next();
				ObjectId id = (ObjectId) userDateLog.get("_id");
				int objTime = id._time();
				Long daysSinceEpoch = Long.valueOf(userDateLog.get("date").toString());
				Date date = new Date(86400000 * daysSinceEpoch);
				timestamp = 86400000 * daysSinceEpoch;
				
				try {
					loggingService.recordUserActivity(userid, timestamp);
				} catch (LoggingException e) {
					logger.error(e);
				}
			}
		}
		return timestamp;
	}
	
}
