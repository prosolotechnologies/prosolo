package org.prosolo.services.activityreport;

import static java.lang.Long.parseLong;
import static org.apache.commons.lang.StringUtils.join;
import static org.prosolo.services.activityreport.ArchiveExportUtil.capitalizeName;
import static org.prosolo.services.activityreport.ArchiveExportUtil.prepareForCSV;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * This class represents a single line of the prosolo activity log which is
 * exported by the scheduler and put into a CSV file.
 * 
 * @author vita
 */
public class LoggedEvent {

	protected static Logger logger = Logger.getLogger(LoggedEvent.class);

	private static final String SQ = "'";
	private static final int USER_ID_START_POS = 25;
	private String eventType;
	private Long timestamp;

	private String actorFullName;
	private Long actorId;

	private Map<String, String> parameters;

	private String objectTitle;
	private String objectType;
	private Long objectId;

	private String targetType;
	private Long targetId;

	private String reasonType;
	private Long reasonId;

	private String link;

	private static final Map<String, String> replacements = new HashMap<String, String>();

	static final String NL = "\n";

	static final String Q = "\"";

	static final String D = ",";

	static final String QD = Q + D;

	private static final SimpleDateFormat formatter = new SimpleDateFormat(
			"MM/dd/yyyy HH:mm:ss:SSS z");

	private static final String targetcompRegex = "(targetComp\\.)(.*?)(?=\\.|$)";
	private static final String compRegex = "(comp\\.)(.*?)(?=\\.|$)";
	private static final String pageRegex = "^(.*?)(.\\b)";
	private static final String targetGoalRegex = "(targetGoal\\.)(.*?)(?=\\.|$)";
	private static final String goalRegex = "(goal\\.)(.*?)(?=\\.|$)";
	private static final String activityRegex = "(activity\\.)(.*?)(?=\\.|$)";
	private static final String commentRegex = "(comment\\.)(.*?)(?=\\.|$)";
	private static final String postRegex = "(post\\.)(.*?)(?=\\.|$)";

	// artefacts
	private static final String courseRegex = "(course\\.)(.*?)(?=\\.|$)";
	private static final String activeCoursesRegex = "(activeCourses\\.)(.*?)(?=\\.|$)";
	private static final String completedRegex = "(completed\\.)(.*?)(?=\\.|$)";
	private static final String goalDialogRegex = "(goalDialog\\.)(.*?)(?=\\.|$)";
	private static final String navigationRegex = "navigation";
	private static final String recommendRegex = "recommendation";
	private static final String activityRecommendRegex = "activityRecommend";
	private static final String inviteRegex = "inviteCollaborator";
	private static final String evaluationRegex = "askForEvaluation";

	private static final Pattern patternTargetComp = Pattern
			.compile(targetcompRegex);
	private static final Pattern patternComp = Pattern.compile(compRegex);
	private static final Pattern patternPage = Pattern.compile(pageRegex);
	private static final Pattern patternTargetGoal = Pattern
			.compile(targetGoalRegex);
	private static final Pattern patternGoal = Pattern.compile(goalRegex);
	private static final Pattern patternActivity = Pattern
			.compile(activityRegex);
	private static final Pattern patternComment = Pattern.compile(commentRegex);
	private static final Pattern patternPost = Pattern.compile(postRegex);

	// artefacts
	private static final Pattern patternCourse = Pattern.compile(courseRegex);
	private static final Pattern patternActiveCourse = Pattern
			.compile(activeCoursesRegex);
	private static final Pattern patternCompleted = Pattern
			.compile(completedRegex);
	private static final Pattern patternGoalDialog = Pattern
			.compile(goalDialogRegex);
	private static final Pattern patternNavigation = Pattern
			.compile(navigationRegex);
	private static final Pattern patternRecommend = Pattern
			.compile(recommendRegex);
	private static final Pattern patternActivityRecommend = Pattern
			.compile(activityRecommendRegex);
	private static final Pattern patternInvite = Pattern.compile(inviteRegex);
	private static final Pattern patternEvaluation = Pattern
			.compile(evaluationRegex);

	private static final String NOTIFICATION_QUERY = "SELECT sa.text FROM prosolo.social_activity_notification san, prosolo.social_activity sa " + 
				"WHERE san.social_activity = sa.id and san.id = :target_id";
	
	private static final String ACTIVITY_QUERY = "SELECT text FROM prosolo.social_activity WHERE id = :target_id";

	static {

		replacements.put("requested to join the", "joinRequest");
		// replacements.put("Inbox", "openInbox");
		// replacements.put("Notifications", "openNotifications");
		// replacements.put("completed", "askForEvaluation");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

	}

	public LoggedEvent(DBObject o) {

		this.eventType = (String) o.get("eventType");
		this.timestamp = (Long) o.get("timestamp");

		this.actorFullName = (String) o.get("actorFullname");
		this.actorId = (Long) o.get("actorId");

		this.objectTitle = (String) o.get("objectTitle");
		this.objectType = (String) o.get("objectType");
		this.objectId = (Long) o.get("objectId");

		this.targetType = (String) o.get("targetType");
		this.targetId = (Long) o.get("targetId");

		this.reasonType = (String) o.get("reasonType");
		this.reasonId = (Long) o.get("reasonId");

		this.link = (String) o.get("link");

		BasicDBObject parametersList = (BasicDBObject) o.get("parameters");

		this.parameters = new HashMap<String, String>();
		if (parametersList != null)
			for (String key : parametersList.keySet()) {
				Object val = parametersList.get(key);
				if (val == null)
					val = "";
				parameters.put(key, (String) val.toString());
			}
	}

	@Override
	public String toString() {
		return "LoggedEvent [eventType=" + eventType + ", timestamp="
				+ timestamp + ", actorFullName=" + actorFullName + ", actorId="
				+ actorId + ", parameters=" + parameters + ", objectTitle="
				+ objectTitle + ", objectType=" + objectType + ", objectId="
				+ objectId + ", targetType=" + targetType + ", targetId="
				+ targetId + ", reasonType=" + reasonType + ", reasonId="
				+ reasonId + ", link=" + link + "]";
	}

	public static String getCSVHeaderRow() {
		return "EVENT_TYPE,TIME,TIME_STAMP,ACTOR_ID,IP_ADDRESS,OBJECT_NAME,OBJECT_TYPE,ACTION,TARGET,DETAILS,PAGE,GOAL_ID,GOAL,COMPETENCE_ID,COMPETENCE,ACTIVITY_ID,ACTIVITY,ARTEFACT_TYPE,ARTEFACT_ID,POST_ID,POST_TEXT,COMMENT_ID, COMMENT_TEXT";
	}

	public static String getCSVHeaderDebugAddon() {
		return ",RAW_PARAMS,OBJECT_TITLE,OBJECT_TYPE,OBJECT_ID,TARGET_TYPE,TARGET_ID,REASON_TYPE,REASON_ID,LINK";
	}

	public List<String> toCVSString(Map<Long, String> allUsers,
			Map<Long, String> nodeIdNameMap,

			Map<Long, String> nodeIdTypeMap, Map<Long, String> idPostTextMap,
			Map<Long, String> socialActivityTextMap,
			Map<Long, String> notificationIdTextMap, Session session) {

		String postId = getPostID(nodeIdNameMap, nodeIdTypeMap);
		if (postId.equals("0"))
			postId = "";
		String postText = getPostString(idPostTextMap, postId,
				notificationIdTextMap, session).replace("\"", "'");
		if (postText.equals("") && !postId.equals(""))
			postId = ""; // remove post_id for records for which we can't find a
							// corresponding post_text.
		String commentId = getCommentID(nodeIdNameMap, nodeIdTypeMap);
		String normal =
		getBasicCSV() + D +
		getObjectTitle() + D +
		getObjectType() + D +
		getAction() + D +
		Q + getTargetString(allUsers, nodeIdNameMap, nodeIdTypeMap) + QD +
		Q + getDetails() + QD +
		Q + getPageString() + QD +
		Q + getGoalID(nodeIdNameMap, nodeIdTypeMap) + QD +
		Q + getGoalString(nodeIdNameMap, nodeIdTypeMap) + QD +
		Q + getCompetenceID(nodeIdNameMap, nodeIdTypeMap) + QD +
		Q + getCompetenceString(nodeIdNameMap, nodeIdTypeMap) + QD +
		Q + getActivityID(nodeIdNameMap, nodeIdTypeMap) + QD +
		prepareForCSV(getActivityString(nodeIdNameMap, nodeIdTypeMap)) + D +
		Q + getArtefactType() + QD +
		getArtefactID() + D +
		postId + D +
		Q + postText + QD +
		commentId + D +
		 Q + getCommentText(commentId, socialActivityTextMap, session) + Q;

		List<String> result = new ArrayList<String>();

		result.add(normal);

		result.add(getDebugCSV());

		return result;

	}
	
	private String getTextForId(Session session, String query, Long id) {
//		System.out.println("RUNNING QUERY" + query + " ID: " + id);
		String r = (String) session.createSQLQuery(query).setLong("target_id", id).uniqueResult();
//		System.out.println("DONE");
		if(r == null)
			return "";

		return r;
	}

	private String getCommentText(String commentId,
			Map<Long, String> socialActivityTextMap, Session session) {
		return getFromMapIfExists(commentId, socialActivityTextMap, session, ACTIVITY_QUERY);

	}

	private String getBasicCSV() {
		return eventType + D + Q + formatter.format(new Date(timestamp)) + QD
				+ timestamp + D + actorId + D + Q + getIpAddress() + Q;
	}

	private String getDebugCSV() {
		return Q + parameters + QD + prepareForCSV(objectTitle) + D + Q
				+ objectType + QD + objectId + D + Q + targetType + QD
				+ targetId + D + Q + reasonType + QD + reasonId + D + Q + link
				+ QD + prepareForCSV(actorFullName);

	}

	private String getPageString() {

		List<String> elems = new ParamList("page");

		elems.add(getPageFromContext(patternPage));

		return elems.toString();

	}

	private String getAction() {

		List<String> elems = new ParamList("action");

		elems.add(getParam("action", null, true, false, false));

		elems.add(replace(getActionObjectType()));

		return elems.toString();

	}

	private String getGoalID(Map<Long, String> nodeIdNameMap,
			Map<Long, String> nodeIdTypeMap) {

		List<String> elems = new ParamList("goal_id");

		elems.add(getIDFromMap("goalId", null, nodeIdNameMap));

		elems.add(getIDFromMap("targetGoalId", null, nodeIdNameMap));

		elems.add(getIDFromContext(patternTargetGoal, null, nodeIdNameMap));

		elems.add(getIDFromContext(patternGoal, null, nodeIdNameMap));

		return elems.toString();

	}

	private String getGoalString(Map<Long, String> nodeIdNameMap,
			Map<Long, String> nodeIdTypeMap) {

		List<String> elems = new ParamList("goal");

		elems.add(getFromMap("goalId", null, nodeIdNameMap));

		elems.add(getFromMap("targetGoalId", null, nodeIdNameMap));

		elems.add(getFromMap("learningGoalId", null, nodeIdNameMap));

		elems.add(getFromContext(patternTargetGoal, null, nodeIdNameMap));

		elems.add(getFromContext(patternGoal, null, nodeIdNameMap));

		return elems.toString();

	}

	private String getCompetenceID(Map<Long, String> nodeIdNameMap,
			Map<Long, String> nodeIdTypeMap) {

		List<String> elems = new ParamList("competence_id");

		elems.add(getIDFromMap("compId", null, nodeIdNameMap));

		elems.add(getIDFromMap("targetComp", null, nodeIdNameMap));

		elems.add(getIDFromContext(patternTargetComp, null, nodeIdNameMap));

		elems.add(getIDFromContext(patternComp, null, nodeIdNameMap));

		return elems.toString();

	}

	private String getCompetenceString(Map<Long, String> nodeIdNameMap,
			Map<Long, String> nodeIdTypeMap) {
		List<String> elems = new ParamList("competence");

		elems.add(getFromMap("compId", null, nodeIdNameMap));
		elems.add(getFromMap("targetComp", null, nodeIdNameMap));
		elems.add(getFromContext(patternTargetComp, null, nodeIdNameMap));
		elems.add(getFromContext(patternComp, null, nodeIdNameMap));
		return elems.toString();
	}

	private String getActivityID(Map<Long, String> nodeIdNameMap,
			Map<Long, String> nodeIdTypeMap) {
		List<String> elems = new ParamList("activity_id");

		elems.add(getIDFromMap("activityId", null, nodeIdNameMap));
		elems.add(getIDFromContext(patternActivity, null, nodeIdNameMap));
		return elems.toString();
	}

	private String getActivityString(Map<Long, String> nodeIdNameMap,
			Map<Long, String> nodeIdTypeMap) {
		List<String> elems = new ParamList("activity_text");

		elems.add(getFromMap("activityId", null, nodeIdNameMap));
		elems.add(getFromContext(patternActivity, null, nodeIdNameMap));
		return elems.toString();
	}

	private String getCommentID(Map<Long, String> nodeIdNameMap,
			Map<Long, String> nodeIdTypeMap) {
		List<String> elems = new ParamList("comment_id");
		elems.add(getIDFromContext(patternComment, null, nodeIdNameMap));
		return elems.toString();
	}

	private String getPostID(Map<Long, String> nodeIdNameMap,
			Map<Long, String> nodeIdTypeMap) {
		List<String> elems = new ParamList("post_id");
		elems.add(getIDFromContext(patternPost, null, nodeIdNameMap));
		return elems.toString();
	}

	private String getPostString(Map<Long, String> idPostMap, String postId,
			Map<Long, String> notificationIdTextMap, Session session) {

		List<String> elems = new ParamList("post_text");

		elems.add(getFromContextPost(patternPost, null, idPostMap));

		elems.add(getFromMapIfExists(postId, idPostMap, null, null));

		elems.add(getFromMapIfExists(postId, notificationIdTextMap, session, NOTIFICATION_QUERY));

		return elems.toString();

	}

	private String getFromMapIfExists(String id, Map<Long, String> map, Session session, String query) {
		if (id == null || id.equals(""))
			return "";
		Long idL = Long.parseLong(id);
		if (map.containsKey(idL))
			return map.get(idL);
		
		if(query == null) {
			return ""; // do not go to database
			
		} else {
			String text = getTextForId(session, query, idL);
			map.put(idL, text);
		}
		return "";
	}

	private String getArtefactType() {
		List<String> elems = new ParamList("artifact_type");
		elems.add(getArtefactFromContext(patternCourse, null));
		elems.add(getArtefactFromContext(patternActiveCourse, null));
		elems.add(getArtefactFromContext(patternCompleted, null));
		elems.add(getArtefactFromContext(patternGoalDialog, null));
		elems.add(getArtefactFromContext(patternNavigation, null));
		elems.add(getArtefactFromContext(patternRecommend, null));
		elems.add(getArtefactFromContext(patternActivityRecommend, null));
		elems.add(getArtefactFromContext(patternInvite, null));
		elems.add(getArtefactFromContext(patternEvaluation, null));

		if (getParam("exCreditId", null, true, false, false) != "") {
			elems.add("External Credit");
		}

		return elems.toString();
	}

	private String getArtefactID() {
		List<String> elems = new ParamList("artifact_id");
		elems.add(getArtefactIDFromContext(patternCourse, null));
		elems.add(getArtefactIDFromContext(patternActiveCourse, null));
		elems.add(getArtefactIDFromContext(patternCompleted, null));
		elems.add(getArtefactIDFromContext(patternGoalDialog, null));

		if (getParam("exCreditId", null, true, false, false) != "") {
			elems.add(getParam("exCreditId", null, true, false, false));
		}

		return elems.toString();
	}

	private String getPageFromContext(Pattern p) {
		String context = parameters.get("context");
		if (context != null) {
			Matcher matcher = p.matcher(context);
			if (matcher.find()) {
				String page = matcher.group();

				if (page.toLowerCase().equals("globalsearch")
						|| page.toLowerCase().equals("footer")
						|| page.toLowerCase().equals("floatingsupporticon")
						|| page.toLowerCase().equals("publicpage")) {
					return "mainmenu";
				} else if (page.toLowerCase().equals("statuswall")
						|| page.toLowerCase().equals("listpeople")
						|| page.toLowerCase().equals("recommendpeople")
						|| page.toLowerCase().equals("learningprogress")
						|| page.toLowerCase().equals("suggestedlearning")
						|| page.toLowerCase().equals("featurednews")) {
					return "index";
				} else if (page.toLowerCase().equals("competencedetailsdialog")
						|| page.toLowerCase().equals("goaldetailsdialog")
						|| page.toLowerCase().equals("activitydetails")
						|| page.toLowerCase().equals("addtodoal")
						|| page.toLowerCase().equals("allcompetencesdialog")
						|| page.toLowerCase().equals("peoplelistdialog")
						|| page.toLowerCase().equals("activitydetails")
						|| page.toLowerCase().equals("goaldialog")
						|| page.toLowerCase().equals("competencedialog")) {
					return "detailsdialog";
				} else if (page.toLowerCase().equals("sessiontimeout")) {
					return "";
				} else {
					return page;
				}

			}
		}
		return "";
	}

	private String getArtefactFromContext(Pattern p, Object object) {

		String context = parameters.get("context");

		if (context != null) {

			Matcher matcher = p.matcher(context);

			if (matcher.find()) {

				try {

					String val = matcher.group().split("\\.")[0];

					return val;

				} catch (Exception ex) {

				}

			}

		}

		return "";

	}

	private String getArtefactIDFromContext(Pattern p, Object object) {

		String context = parameters.get("context");

		if (context != null) {

			Matcher matcher = p.matcher(context);

			if (matcher.find()) {

				try {

					String mat = matcher.group();

					String id = "";

					if (mat.split("\\.").length > 1)

						id = mat.split("\\.")[1];

					return id;

				} catch (Exception ex) {

				}

			}

		}

		return "";

	}

	private String getFromContext(Pattern p, Object object,
			Map<Long, String> nodeIdNameMap) {

		String context = parameters.get("context");

		if (context != null) {

			Matcher matcher = p.matcher(context);

			if (matcher.find()) {

				try {

					String id = matcher.group().split("\\.")[1];

					String val = nodeIdNameMap.get(parseLong(id));

					return val;

				} catch (Exception ex) {

				}

			}

		}

		return "";

	}

	private String getFromContextPost(Pattern p, Object object,
			Map<Long, String> idPostMap) {

		String context = parameters.get("context");

		if (context != null) {

			Matcher matcher = p.matcher(context);

			if (matcher.find()) {

				try {

					String id = matcher.group().split("\\.")[1];

					String val = idPostMap.get(parseLong(id));

					return val;

				} catch (Exception ex) {

				}

			}

		}

		return "";

	}

	private String getIDFromContext(Pattern p, Object object,
			Map<Long, String> nodeIdNameMap) {
		String context = parameters.get("context");
		if (context != null) {
			Matcher matcher = p.matcher(context);
			if (matcher.find()) {
				try {
					String id = matcher.group().split("\\.")[1];

					if (ArchiveExportUtil.isNumeric(id))
						return id;
					else
						return "";
				} catch (Exception ex) {

				}
			}
		}
		return "";
	}

	private String getIpAddress() {
		return parameters.get("ip");
	}

	private String getDetails() {
		List<String> elems = new ParamList();

		elems.add(getReason());
		elems.add(getParam("query", "query", false, false, true));
		elems.add(getParam("notificationType", "type", false, true, false));
		elems.add(getParam("activities", "activities", false, false, true));

		String link = getLinkString();
		if (link != null && !link.equals(""))
			elems.add("link:" + link);

		return elems.toString();
	}

	private String getTargetString(Map<Long, String> allUsers,
			Map<Long, String> nodeIdNameMap, Map<Long, String> nodeIdTypeMap) {
		List<String> elems = new ParamList();

		// elems.add(getFromIdNameTypeMaps(objectId, nodeIdNameMap,
		// nodeIdTypeMap));
		elems.add(getFromIdNameTypeMaps(targetId, nodeIdNameMap, nodeIdTypeMap));

		String user = getUserString(allUsers);
		if (user != null && !user.equals(""))
			elems.add("user: " + user);

		return elems.toString();
	}

	private String getLinkString() {
		List<String> elems = new ParamList("link");

		elems.add(getLink());
		elems.add(getParam("link", null, false, false, false));

		return elems.toString();
	}

	private String getUserString(Map<Long, String> allUsers) {
		List<String> elems = new ParamList("user");

		elems.add(getTargetUser(allUsers));
		elems.add(getFromMap("userId", null, allUsers));
		elems.add(getFromMap("user", null, allUsers));

		return elems.toString();
	}

	private String getObjectType() {
		// && !objectType.equals("tab") && !objectType.equals("page")
		if (objectType != null && !objectType.equals("null")
				&& !objectType.toLowerCase().equals("mouse_click")
				&& !objectType.toLowerCase().equals("search_hashtags")
				&& !objectType.toLowerCase().equals("search_activities")
				&& !objectType.toLowerCase().equals("global_search")
				&& !objectType.toLowerCase().equals("search_people")
				&& !objectType.toLowerCase().equals("tooltip")
				&& !objectType.toLowerCase().equals("inbox")
				&& !objectType.toLowerCase().equals("notifications")
				&& !objectType.toLowerCase().equals("tutorial_played")) {

			String retValue = objectType;
			if (objectType.toLowerCase().contains("competence"))
				retValue = "Competence";
			else if (objectType.toLowerCase().equals("user_location_dialog")
					|| objectType.toLowerCase().equals("user_dialog")
					|| objectType.toLowerCase().equals("people_list_dialog")
					|| objectType.toLowerCase().equals(
							"location_recommendation"))
				retValue = "User";
			else if (objectType.toLowerCase().equals(
					"ask_for_evaluation_dialog")
					|| objectType.toLowerCase()
							.equals("evaluation_list_dialog"))
				retValue = "Evaluation";
			else if (objectType.toLowerCase().contains("goal"))
				retValue = "Goal";
			else if ((objectType.toLowerCase().contains("activity") && !objectType
					.toLowerCase().contains("comment"))
					|| objectType.toLowerCase().contains("video")
					|| objectType.toLowerCase().contains("rich"))
				retValue = "Activity";
			else if (objectType.toLowerCase().contains("comment"))
				retValue = "Comment";
			else if (objectType.toLowerCase().contains("post"))
				retValue = "Post";
			else if (objectType.toLowerCase().contains("message"))
				retValue = "Message";
			else if (objectType.toLowerCase().contains("course"))
				retValue = "Course";
			else if (objectType.toLowerCase().contains("reminder"))
				retValue = "Reminder";
			else if (objectType.toLowerCase().contains("credit"))
				retValue = "ExternalCredit";

			return capitalizeName(retValue);
		}

		return "";
	}

	private String getActionObjectType() {
		if (objectType != null
				&& (objectType.toLowerCase().equals("mouse_click")
						|| objectType.toLowerCase().contains("search")
						|| objectType.toLowerCase().equals("tooltip")
						|| objectType.toLowerCase().equals("inbox")
						|| objectType.toLowerCase().equals("notifications") || objectType
						.toLowerCase().equals("tutorial_played")))
			return capitalizeName(objectType);

		return "";
	}

	private String getReason() {
		String context = parameters.get("context");

		if (context != null && context.equals("SESSIONTIMEOUT"))
			return "reason: session_timeout";

		return "";
	}

	private String getTargetUser(Map<Long, String> allUsers) {
		if (link != null && link.startsWith("profile/"))
			return allUsers.get(Long.parseLong(link
					.substring(USER_ID_START_POS)));

		return "";
	}

	private static boolean isValid(String s) {
		return s != null && !s.equals("") && !s.equals("null");
	}

	private String getParam(String param, String prefix, boolean capitalize,
			boolean replace, boolean quote) {

		String p = parameters.get(param);

		if (p != null) {

			if (replace)
				p = replace(p);
			else if (capitalize)
				p = capitalizeName(p);
			if (quote)
				p = SQ + p + SQ;

			if (prefix == null) {
				return p;
			}

			return prefix + ": " + p;

		}
		return "";
	}

	private String getFromIdNameTypeMaps(Long id, Map<Long, String> mapIdName,
			Map<Long, String> mapIdType) {
		if (id != 0 && mapIdName.containsKey(id))
			return mapIdType.get(id) + ": '" + mapIdName.get(id) + SQ;

		return "";
	}

	private String getFromMap(String key, String prefix, Map<Long, String> map) {
		String id = parameters.get(key);

		if (id != null) {

			try {
				String val = map.get(Long.parseLong(id));

				if (prefix == null) {
					return val;
				}

				return prefix + ": '" + val + SQ;
			} catch (Exception ex) {
				logger.warn(key + ex.getMessage());
			}
		}
		return "";
	}

	private String getIDFromMap(String key, String prefix, Map<Long, String> map) {
		String id = parameters.get(key);

		return id;
	}

	private String replace(String s) {
		if (replacements.containsKey(s))
			return replacements.get(s);
		return s;
	}

	public String getObjectTitle() {
		if (isValid(objectTitle))
			return prepareForCSV(objectTitle);

		return "";
	}

	public String getLink() {
		if (isValid(link)) {
			if (link.startsWith("profile/"))
				return "profile";

			return link;
		}
		return "";
	}

	public Long getActorId() {
		return actorId;
	}

	private static final class ParamList extends ArrayList<String> {
		private static final long serialVersionUID = 1L;
		private boolean exclusive;
		private String name;

		public ParamList(String name) {
			this(true, name);
		}

		private ParamList(boolean exclusive, String name) {
			this.exclusive = exclusive;
			this.name = name;
		}

		public ParamList() {
			this(false, "");
		}

		@Override
		public boolean add(String e) {
			if (e != null && !e.equals(""))
				super.add(e);
			return true;
		}

		private boolean allSame() {
			if (size() < 2)

				return true;

			Iterator<String> i = this.iterator();

			String first = i.next();

			while (i.hasNext())

				if (!i.next().equals(first))

					return false;

			return true;

		}

		@Override
		public String toString() {

			if (size() == 0)

				return "";

			else if (exclusive) {

				if (!allSame())

					logger.warn(name
							+ " declared as non exclusive, but several values found: "
							+ join(this, D) + ". Returning first value: "
							+ get(0));

				return get(0);

			} else

				return join(this, D);

		}

	}

}
