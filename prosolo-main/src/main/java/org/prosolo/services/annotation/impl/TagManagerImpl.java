package org.prosolo.services.annotation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.indexing.TagEntityESService;
import org.prosolo.services.twitter.impl.StreamListData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.annotation.TagManager")
public class TagManagerImpl extends AbstractManagerImpl implements TagManager {

	private static final long serialVersionUID = -7000755772743769358L;

	private static Logger logger = Logger.getLogger(TagManagerImpl.class);

	@Override
	@Transactional (readOnly = false)
	public Tag getOrCreateTag(String title) {
		Tag tag = getTag(title);

		if (tag != null) {
			return merge(tag);
		} else {
			return createTag(title);
		}
	}
	
	@Override
	@Transactional (readOnly = true)
	public Tag getTag(String title) {
		title = title.toLowerCase();
		
		String query = 
			"SELECT DISTINCT tag " +
			"FROM Tag tag " +
			"WHERE tag.title = :title";
		
		@SuppressWarnings("unchecked")
		List<Tag> result = persistence.currentManager().createQuery(query).
				setString("title", title).
				list();
		
		if (result != null && !result.isEmpty()) {
			return result.iterator().next();
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = false)
	public Tag createTag(String title) {
		Tag newTag = new Tag();
		newTag.setTitle(title);
		newTag = saveEntity(newTag);
		return newTag;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Set<Tag> getOrCreateTags(Collection<String> titles) {
		Set<Tag> tags = new HashSet<Tag>();
		
		if (titles != null) {
			for (String t : titles) {
				tags.add(getOrCreateTag(t));
			}
		}
		
		return tags;
	}

	@Override
	@Transactional
	public Set<Tag> parseCSVTagsAndSave(String csvString) {
		return getOrCreateTags(StringUtil.convertCSVToList(csvString));
	}
	
	@Override
	@Transactional (readOnly = true)
	public Set<Tag> getTagsForResource(BaseEntity resource) {
		String queryString = null;

		if (resource instanceof Node) {
			queryString = 
				"SELECT DISTINCT tag " +
				"FROM Node node " +
				"LEFT JOIN node.tags tag " +
				"WHERE node = :resource ";
		} else if (resource instanceof Course) {
			queryString = 
				"SELECT DISTINCT tag " +
				"FROM Course course " +
				"LEFT JOIN course.tags tag " +
				"WHERE course = :resource ";
		}
		
		Query query = persistence.currentManager().createQuery(queryString).
			setEntity("resource", resource);
		
		@SuppressWarnings("unchecked")
		List<Tag> result = query.list();
		
		if (result != null && !result.isEmpty()) {
			return new HashSet<Tag>(result);
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = true)
	public Set<Tag> getHashtagsForResource(BaseEntity resource) {
		String queryString = null;
		
		if (resource instanceof Node) {
			queryString = 
				"SELECT DISTINCT hashtag " +
				"FROM Node node " +
				"LEFT JOIN node.hashtags hashtag " +
				"WHERE node = :resource ";
		} else if (resource instanceof Course) {
			queryString = 
				"SELECT DISTINCT hashtag " +
				"FROM Course course " +
				"LEFT JOIN course.hashtags hashtag " +
				"WHERE course = :resource ";
		}
		
		Query query = persistence.currentManager().createQuery(queryString).
				setEntity("resource", resource);
		
		@SuppressWarnings("unchecked")
		List<Tag> result = query.list();
		
		if (result != null && !result.isEmpty()) {
			return new HashSet<Tag>(result);
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = true)
	public Map<User, Set<Tag>> getUsersFollowingHashtags(Collection<String> hashtagsString, Session session) {
		String query = 
			"SELECT DISTINCT user, hashtag " + 
			"FROM TopicPreference topicPreference " +
			"LEFT JOIN topicPreference.user user " +
			"LEFT JOIN topicPreference.preferredHashtags hashtag "+
			"WHERE hashtag.title IN (:hashtag)";

		@SuppressWarnings("unchecked")
		List<Object> result = session.createQuery(query)
				.setParameterList("hashtags", hashtagsString)
				.list();
		
		if (result != null) {
			Map<User, Set<Tag>> userHashtags = new HashMap<User, Set<Tag>>();
			
			Iterator<?> iterator = result.iterator();
			
			while (iterator.hasNext()) {
				Object[] objects = (Object[]) iterator.next();
				User user = (User) objects[0];
				Tag hashtag = (Tag) objects[1];
				
				if (user != null && hashtag != null) {
					Set<Tag> hashtags = null;
					
					if (userHashtags.containsKey(user)) {
						hashtags = userHashtags.get(user);
					} else {
						hashtags = new HashSet<Tag>();
					}
					hashtags.add(hashtag);

					userHashtags.put(user, hashtags);
				}
			}
			return userHashtags;
		} 
		return new HashMap<User, Set<Tag>>();
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<Tag> getSubscribedHashtags(User user) {
		String query = 
			"SELECT DISTINCT hashtag " + 
			"FROM TopicPreference topicPreference " +
			"LEFT JOIN topicPreference.user user " +
			"LEFT JOIN topicPreference.preferredHashtags hashtag "+
			"WHERE hashtag.id > 0";
		
		if (user != null) {
			query += " AND user = :user";
		}
		
		Query q = persistence.currentManager().createQuery(query);
		
		if (user != null) {
			q.setEntity("user", user);
		}
		
		@SuppressWarnings("unchecked")
		List<Tag> result = q.list();
		
		if (result != null) {
			return result;
		} else {
			return new ArrayList<Tag>();
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public Map<String, StreamListData> readAllHashtagsAndLearningGoalsIds() {
		String query = 
			"SELECT DISTINCT hashtag.title, lGoal.id " +
			"FROM LearningGoal lGoal " +
			"LEFT JOIN lGoal.hashtags hashtag WHERE hashtag.id > 0";
		
		logger.debug("hb query:" + query);
		List<Object> result =  persistence.currentManager().createQuery(query)
				.list();
		
		Map<String, StreamListData> hashtagsLearningGoalIds = new HashMap<String, StreamListData>();
			 
		if (result != null) {
			Iterator<Object> resultIt = result.iterator();
			
			while (resultIt.hasNext()) {
				Object[] object = (Object[]) resultIt.next();
				String title = (String) object[0];
				Long lgId = (Long) object[1];
				
				if (hashtagsLearningGoalIds.containsKey(title)) {
					hashtagsLearningGoalIds.get(title).addGoalId(lgId);
				} else {
					StreamListData listData = new StreamListData(title);
					listData.addGoalId(lgId);
					hashtagsLearningGoalIds.put(title, listData);
				}
			}
		}
		return hashtagsLearningGoalIds;
	}
	
	@Override
	@Transactional (readOnly = true)
	@SuppressWarnings("unchecked")
	public Map<String, List<Long>> readAllUserPreferedHashtagsAndUserIds() {
		String query = 
			"SELECT DISTINCT hashtag.title, user.id " +
			"FROM TopicPreference topicPreference " +
			"LEFT JOIN topicPreference.user user " +
			"LEFT JOIN topicPreference.preferredHashtags hashtag  WHERE hashtag.id > 0";
			 
		List<Object> result = persistence.currentManager().createQuery(query).list();
		
		Map<String, List<Long>> hashtagsUserIds = new HashMap<String, List<Long>>();
		
		if (result != null) {
			Iterator<Object> resultIt = result.iterator();
			
			while (resultIt.hasNext()) {
				Object[] object = (Object[]) resultIt.next();
				String title = (String) object[0];
				Long lgId = (Long) object[1];
				if (hashtagsUserIds.containsKey(title)) {
					List<Long> ids = hashtagsUserIds.get(title);
					ids.add(lgId);
				} else {
					List<Long> ids = new ArrayList<Long>();
					ids.add(lgId);
					hashtagsUserIds.put(title, ids);
				}
			}
		}
		return hashtagsUserIds;
	}
}
