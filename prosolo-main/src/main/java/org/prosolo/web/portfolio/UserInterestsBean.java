package org.prosolo.web.portfolio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.annotation.Tag;
import org.prosolo.domainmodel.user.User;
import org.prosolo.domainmodel.user.preferences.TopicPreference;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.util.nodes.AnnotationUtil;
import org.prosolo.util.nodes.NodeTitleComparator;
import org.prosolo.util.nodes.NodeUtil;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.portfolio.data.UserInterestsData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name="userinterests")
@Component("userinterests")
@Scope("view")
public class UserInterestsBean implements Serializable {
	
	private static final long serialVersionUID = -5638068595107518227L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(UserInterestsBean.class);
	
	@Autowired private TagManager tagManager;
	@Autowired private UserManager userManager;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	@Autowired private LoggingNavigationBean actionLogger;

	private List<Tag> interests;
	private UserInterestsData interestsData = new UserInterestsData();;
	
	private User user;
	
	public void init(User user) {
		if (user != null) {
			this.user = userManager.merge(user);
			init();
		}
	}
	
	public void init() {
		this.interests = new ArrayList<Tag>();
		this.interestsData = new UserInterestsData();
		
		TopicPreference topicPreference = (TopicPreference) userManager.getUserPreferences(user, TopicPreference.class);
		Set<Tag> preferredKeywords = topicPreference.getPreferredKeywords();
		
		if (preferredKeywords != null && !preferredKeywords.isEmpty()) {
			this.interests.addAll(preferredKeywords);
			Collections.sort(this.interests, new NodeTitleComparator());
			
			String csvInterests = AnnotationUtil.getCSVString(this.interests, ", ");
			this.interestsData.setInterestsText(csvInterests);
			this.interestsData.setInterestsTextEdit(csvInterests);
		}
	}
	
	public void updateUserInterests(final String context) {
		final Set<Tag> newTagList = tagManager.parseCSVTagsAndSave(interestsData.getInterestsTextEdit());
		TopicPreference topicPreference = (TopicPreference) userManager.getUserPreferences(user, TopicPreference.class);
		topicPreference.setPreferredKeywords(newTagList);
		tagManager.saveEntity(topicPreference);
		
		this.interestsData.setInterestsText(this.interestsData.getInterestsTextEdit());
		PageUtil.fireSuccessfulInfoMessage("Updated user interests!");
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Map<String, String> parameters = new HashMap<String, String>();
				parameters.put("context", context);
				parameters.put("interests", NodeUtil.getCSVStringOfIds(newTagList));
				
				actionLogger.logEvent(EventType.INTERESTS_UPDATED, parameters);
			}
		});
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public UserInterestsData getInterestsData() {
		return interestsData;
	}
	
	public void setInterestsData(UserInterestsData interestsData) {
		this.interestsData = interestsData;
	}
	
	public List<Tag> getUserInterests() {
		return this.interests;
	}

}
