package org.prosolo.services.annotation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.prosolo.domainmodel.annotation.Tag;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.twitter.impl.StreamListData;

public interface TagManager extends AbstractManager {
	
	Tag getOrCreateTag(String title);

	Set<Tag> getOrCreateTags(Collection<String> titles);
	
	Set<Tag> parseCSVTagsAndSave(String csvString);

	Tag getTag(String title);

	Tag createTag(String title);
	
	Map<User, Set<Tag>> getUsersFollowingHashtags(Collection<String> hashtags, Session session);

	Set<Tag> getTagsForResource(BaseEntity resource);

	Set<Tag> getHashtagsForResource(BaseEntity resource);
	
	List<Tag> getSubscribedHashtags(User user);

	Map<String, StreamListData> readAllHashtagsAndLearningGoalsIds();

	Map<String, List<Long>> readAllUserPreferedHashtagsAndUserIds();

}
