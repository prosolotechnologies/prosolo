package org.prosolo.web.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse;
import org.prosolo.services.admin.ResourceSettingsManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.web.activitywall.data.UserData;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.ResourceDataUtil;
import org.prosolo.web.util.exceptions.KeyNotFoundInBundleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created a separate view scoped class only for setting users in order not to have a reference from application scoped
 * class to data that is not used often, i.e. evaluato search data
 * 
 * @author "Nikola Milikic"
 *
 * Aug 27, 2014
 */
@ManagedBean(name = "evaluationSettings")
@Component("evaluationSettings")
@Scope("view")
public class EvaluationSettingsBean implements Serializable {
	
	private static final long serialVersionUID = -4740884840002696780L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(EvaluationSettingsBean.class.getName());
	
	@Autowired private RoleManager roleManager;
	@Autowired private TextSearch textSearch;
	@Autowired private ResourceSettingsManager resourceSettingsManager;
	
	private boolean usersChanged = false;
	
	private List<UserData> userSearchResults = new ArrayList<UserData>();
	private List<UserData> selectedEvaluators = new ArrayList<UserData>();
	private List<UserData> removedEvaluators = new ArrayList<UserData>();
	private String keyword;
	
	// used for validation whether to allow submit
	private String userNo;
	
	@PostConstruct
	public void init() {
		try {
			String evaluatorRoleName = ResourceBundleUtil.getMessage("admin.roles.predefinedRole.evaluator.name", new Locale("en", "US"));
			Collection<User> users = roleManager.getUsersWithRole(evaluatorRoleName);
			
			if (users != null && !users.isEmpty()) {
				for (User user : users) {
					selectedEvaluators.add(new UserData(user));
				}
				
				calculateUserNo();
			}
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		}
	}
	
	private void calculateUserNo() {
		if (selectedEvaluators.isEmpty()) {
			userNo = "";
		} else {
			userNo = String.valueOf(selectedEvaluators.size());
		}
	}
	
	public void executeTextSearch(String toExcludeString) {
		long[] moreToExclude = StringUtil.fromStringToLong(toExcludeString);
		
		List<Long> totalListToExclude = new ArrayList<Long>();
		
		if (moreToExclude != null) {
			for (long l : moreToExclude) {
				totalListToExclude.add(l);
			}
		}
		
		userSearchResults.clear();
		
		if (!keyword.isEmpty() && (keyword != null)) {
			TextSearchResponse usersResponse = textSearch.searchUsers(keyword.toString(), 0, 4, false, totalListToExclude);	
			
			@SuppressWarnings("unchecked")
			List<User> result = (List<User>) usersResponse.getFoundNodes();
			
			for (User user : result) {
				UserData userData = new UserData(user);
				
				userSearchResults.add(userData);
			}
		}
	}
	
	public void addUser(UserData userData) {
		if (userData != null) {
			selectedEvaluators.add(userData);
			userData.setDisabled(true);
			
			// check if this user is in removed evaluators
			Iterator<UserData> iterator = this.removedEvaluators.iterator();
			
			while (iterator.hasNext()) {
				UserData u = (UserData) iterator.next();
				
				if (u.equals(userData)) {
					u.setDisabled(false);
					iterator.remove();
					break;
				}
			}
			
			usersChanged = true;
			calculateUserNo();
		}
		
		userSearchResults.clear();
		keyword = "";
	}
	
	public void removeUser(UserData userData) {
		if (userData != null) {
			Iterator<UserData> iterator = this.selectedEvaluators.iterator();
			
			while (iterator.hasNext()) {
				UserData u = (UserData) iterator.next();
				
				if (u.equals(userData)) {
					u.setDisabled(false);
					iterator.remove();
					break;
				}
			}
			
			removedEvaluators.add(userData);
			usersChanged = true;
			calculateUserNo();
		}
		
		userSearchResults.clear();
	}
	
	public String getToExcludeIds() {
		Collection<Long> toExclude = new ArrayList<Long>();
		toExclude.addAll(ResourceDataUtil.getUserIds(selectedEvaluators));
		return toExclude.toString();
	}
	
	/* GETTERS / SETTERS */

	public List<UserData> getSelectedEvaluators() {
		return selectedEvaluators;
	}
	
	public List<UserData> getRemovedEvaluators() {
		return removedEvaluators;
	}

	public String getKeyword() {
		return keyword;
	}
	
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public List<UserData> getUserSearchResults() {
		return userSearchResults;
	}

	public boolean isUsersChanged() {
		return usersChanged;
	}

	public String getUserNo() {
		return userNo;
	}

	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}
	
}
