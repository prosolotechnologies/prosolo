/**
 * 
 */
package org.prosolo.services.nodes;

import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.evaluation.Badge;
import org.prosolo.common.domainmodel.evaluation.BadgeType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.AbstractManager;

/**
 * @author "Nikola Milikic"
 *
 */
public interface BadgeManager extends AbstractManager {

	Badge getBadge(BadgeType type);

	Badge createBadge(BadgeType star, String string);
	
	Map<Badge, List<User>> getBadgesForResource(long resourceId, Class<? extends BaseEntity> clazz) throws ResourceCouldNotBeLoadedException;

	long getBadgeCountForResource(Class<? extends BaseEntity> clazz, long id);

	long getBadgeCountForResource(Class<? extends BaseEntity> class1, long id, Session session);

}