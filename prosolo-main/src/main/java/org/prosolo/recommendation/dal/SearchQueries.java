package org.prosolo.recommendation.dal;

import java.util.Collection;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.services.general.AbstractManager;

public interface SearchQueries extends AbstractManager {
	
	Collection<Tag> getTagsForUser(String userUri) throws Exception;

	Session getSession();

	Tag getTagByTitle(String tagLabel);

}
