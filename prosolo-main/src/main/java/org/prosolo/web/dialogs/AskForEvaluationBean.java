package org.prosolo.web.dialogs;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.omnifaces.util.Ajax;
import org.prosolo.common.domainmodel.activities.requests.Request;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.nodes.EvaluationManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.administration.ResourceSettingsBean;
import org.prosolo.web.communications.util.UserDataConverter;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.util.PageUtil;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.ResourceDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="askForEvaluation")
@Component("askForEvaluation")
@Scope("view")
public class AskForEvaluationBean implements Serializable {
	
	private static final long serialVersionUID = 5004810142702166055L;

	private static Logger logger = Logger.getLogger(AskForEvaluationBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private UserManager userManager;
	@Autowired private EvaluationManager evaluationManager;
	@Autowired private RoleManager roleManager;
	@Autowired private TextSearch textSearch;
	@Autowired private EventFactory eventFactory;
	@Autowired private ResourceSettingsBean resourceSettings;
	@Autowired private LoggingNavigationBean actionLogger;
	
	private BaseEntity resource;
	
	private String keyword;
	private String context;
	private String message = "";
	
	private String learningContext;
	
	private List<UserData> userSearchResults;
	
	// users who are not allowed to create an evaluation because they have already 
	// given it once for the same resource and to the same user
	private List<UserData> existingEvaluators;
	private List<UserData> ignoredEvaluators;
	private List<UserData> evaluatorList;
	private List<Long> excludedUsersForSearch = new ArrayList<Long>();
	
	// used on the client for validating whether a form can be submitted
	private String userNo;
	
	private boolean evaluationRequestAlreadySent = false;
	
	@PostConstruct
	public void init(){
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
		initEvaluatorsList();  
	}
	
	public long getResourceId() {
		if (this.resource != null) {
			return resource.getId();
		} else
			return 0;
	}
	
	public String getResourceClassName() {
		if (this.resource != null) {
			return resource.getClass().getSimpleName();
		} else
			return null;
	}

	public void initEvaluatorsList() {
		userSearchResults = new ArrayList<UserData>();  
		evaluatorList = new ArrayList<UserData>();
		existingEvaluators = new ArrayList<UserData>();
		ignoredEvaluators = new ArrayList<UserData>();
		
		if (resourceSettings.getSettings().getSettings().isSelectedUsersCanDoEvaluation()) {
			try {
				String evaluatorRoleName = ResourceBundleUtil.getMessage("admin.roles.predefinedRole.evaluator.name", new Locale("en", "US"));
				List<User> evaluators = roleManager.getUsersWithRole(evaluatorRoleName);
				
				for (User user : evaluators) {
					UserData userData = new UserData(user);
					evaluatorList.add(userData);
				}
				recalculateUserNo();
			} catch (KeyNotFoundInBundleException e) {
				logger.error(e);
			}
		}
	}
	
	/*
	 * ACTIONS
	 */
	
	private void recalculateUserNo() {
		if (evaluatorList.isEmpty()) {
			userNo = "";
		} else {
			userNo = String.valueOf(evaluatorList.size());
		}
	}
	
	public void sendEvaluationRequests(){
		try {
			Collection<Request> requests = evaluationManager.sendEvaluationRequests(loggedUser.getUserId(), ResourceDataUtil.getUserIds(evaluatorList), resource, message);
		
			if (requests != null) {
				String page = PageUtil.getPostParameter("page");
				String service = PageUtil.getPostParameter("service");
				for (Request request : requests) {
					Map<String, String> parameters = new HashMap<String, String>();
					parameters.put("context", context+".askForEvaluation");
					parameters.put("resourceId", String.valueOf(resource.getId()));
					parameters.put("resourceType", resource.getClass().getSimpleName());
					parameters.put("user", String.valueOf(request.getSentTo().getId()));
					
					//migration to new context approach
					eventFactory.generateEvent(request.getRequestType(), request.getMaker().getId(), request, null,
							page, learningContext, service, parameters);
				}
			
				try {
					String messageSingular = ResourceBundleUtil.getMessage(
							"dialog.askForEvaluation.growl.evaluationsSentSingular", 
							loggedUser.getLocale());
					
					String messagePlural = ResourceBundleUtil.getMessage(
							"dialog.askForEvaluation.growl.evaluationsSentPlural", 
							loggedUser.getLocale(), 
							requests.size());
					
					PageUtil.fireSuccessfulInfoMessage(requests.size() == 1 ? messageSingular : messagePlural);
					Ajax.update("globalsearchform:globalSearchFormGrowl");
				} catch (KeyNotFoundInBundleException e) {
					logger.error(e);
				}
			}
		} catch (EventException e) {
			e.printStackTrace();
		}
		initEvaluatorsList();
	}
	
	public void executeTextSearch(String toExcludeString) {
		long[] moreToExclude = StringUtil.fromStringToLong(toExcludeString);
		
		List<Long> totalListToExclude = new ArrayList<Long>(excludedUsersForSearch);
		
		if (moreToExclude != null) {
			for (long l : moreToExclude) {
				totalListToExclude.add(l);
			}
		}
		userSearchResults.clear();
		
		TextSearchResponse usersResponse = textSearch.searchUsers(keyword, 0, 4, false, totalListToExclude);
		
		@SuppressWarnings("unchecked")
		List<User> result = (List<User>) usersResponse.getFoundNodes();
		for (User user : result) {
			UserData userData = new UserData(user);
			
			// disable all users that have previously created an evaluation for this resource and this user
//			if (existingEvaluators.contains(user)) {
//				userData.setDisabled(true);
//			}
			userSearchResults.add(userData);
		}
	}
	
	public void addUser(UserData userData) {
		userSearchResults.clear();
		keyword = "";
		
		if (userData != null) {
			for (UserData user : evaluatorList) {
				if (user.getId() == userData.getId()) {
					return;
				}
			}
			
			evaluatorList.add(userData);
			recalculateUserNo();
		}
	}
	
	public void removeUser(UserData userData) {
		if (userData != null) {
			Iterator<UserData> iterator = this.evaluatorList.iterator();
			
			while (iterator.hasNext()) {
				UserData u = (UserData) iterator.next();
				
				if (u.equals(userData)) {
					iterator.remove();
					break;
				}
			}
			recalculateUserNo();
		}
		
		userSearchResults.clear();
	}
	
	public String getToExcludeIds() {
		return ResourceDataUtil.getUserIds(evaluatorList).toString();
	}
	
	/*
	 * GETTERS/SETTERS
	 */
	public BaseEntity getResource() {
		return resource;
	}

	public void setResource(BaseEntity resource, String context, String learningContext) {
		this.resource = resource;
		
		if (resourceSettings.getSettings().getSettings().isSelectedUsersCanDoEvaluation()) {
			this.evaluationRequestAlreadySent = evaluationManager.hasUserRequestedEvaluation(loggedUser.getUserId(), resource);
		} else {
			this.evaluationRequestAlreadySent = false;
		}
		
		this.context = context;
		this.learningContext = learningContext;
		
		actionLogger.logServiceUse(
				ComponentName.ASK_FOR_EVALUATION_DIALOG, 
				"context", context,
				"resourceType", resource.getClass().getSimpleName(),
				"id", String.valueOf(resource.getId()));
		
		if (!evaluationRequestAlreadySent) {
			existingEvaluators = UserDataConverter.convertUsers(evaluationManager.getEvaluatorsWhoAcceptedResource(loggedUser.getUserId(), resource));
			ignoredEvaluators = UserDataConverter.convertUsers(evaluationManager.getEvaluatorsWhoIgnoredResource(loggedUser.getUserId(), resource));
			
			excludedUsersForSearch = new ArrayList<Long>();
			// exclude user making an evaluation request
			excludedUsersForSearch.add(loggedUser.getUserId());
			excludedUsersForSearch.addAll(ResourceDataUtil.getUserIds(ignoredEvaluators));
			excludedUsersForSearch.addAll(ResourceDataUtil.getUserIds(existingEvaluators));
			
			try {
				Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
				message = MessageFormat.format(
					ResourceBundleUtil.getMessage(
						"portfolio.askForRecommendation.message", 
						locale),
					ResourceBundleUtil.getMessage(
						"portfolio.resource."+resource.getClass().getSimpleName().toLowerCase(), 
						locale));
			} catch (KeyNotFoundInBundleException e) {
				logger.error(e);
			}
		}
	}
	
	public void setResourceById(long resourceId, String context) {
		try {
			Node resource = userManager.loadResource(Node.class, resourceId);
			resource = HibernateUtil.initializeAndUnproxy(resource);
			String lContext = PageUtil.getPostParameter("learningContext");
			setResource(resource, context, lContext);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<UserData> getUserSearchResults() {
		return userSearchResults;
	}
	
	public List<UserData> getEvaluatorList() {
		return evaluatorList;
	}

	public List<UserData> getExistingEvaluators() {
		return existingEvaluators;
	}

	public List<UserData> getIgnoredEvaluators() {
		return ignoredEvaluators;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getUserNo() {
		return userNo;
	}

	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}

	public boolean isEvaluationRequestAlreadySent() {
		return evaluationRequestAlreadySent;
	}

	public String getContext() {
		return context;
	}

	public String getLearningContext() {
		return learningContext;
	}

	public void setLearningContext(String learningContext) {
		this.learningContext = learningContext;
	}
	
}
