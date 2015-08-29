/**
 * 
 */
package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.workflow.evaluation.Badge;
import org.prosolo.common.domainmodel.workflow.evaluation.BadgeType;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.BadgeManager;
import org.prosolo.services.nodes.PortfolioManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.services.nodes.BadgeManager")
public class BadgeManagerImpl extends AbstractManagerImpl implements BadgeManager {

	private static final long serialVersionUID = -1820565174602610890L;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(BadgeManager.class);
	
	@Autowired private PortfolioManager portfolioManager;
	
	@Override
	@Transactional
	public Badge createBadge(BadgeType type, String title) {
		Badge badge = new Badge();
		badge.setTitle(title);
		badge.setType(type);
		
		return saveEntity(badge);
	}
	
	@Override
	@Transactional (readOnly = true)
	public Badge getBadge(BadgeType type){
		String query = 
			"SELECT DISTINCT badge " +
			"FROM Badge badge " +
			"WHERE badge.type = :type";
		
		Badge badge = (Badge) persistence.currentManager().createQuery(query).
				setString("type", type.toString()).
				uniqueResult();
		
		if (badge != null) {
			return badge;
		}
		
		return null;
	}
	
	@Override
	@Transactional (readOnly = true)
	public Map<Badge, List<User>> getBadgesForResource(long resourceId, Class<? extends BaseEntity> clazz) throws ResourceCouldNotBeLoadedException {
		List<?> resourceBadgeUserData = getBadgesUserDataForResource(resourceId, clazz);
		
		if (resourceBadgeUserData != null && !resourceBadgeUserData.isEmpty()) {
			Map<Badge, List<User>> badgeMap = new HashMap<Badge, List<User>>();
			
			Iterator<?> iterator = resourceBadgeUserData.iterator();
			
			while (iterator.hasNext()) {
				Object[] objects = (Object[]) iterator.next();
				Badge badge = (Badge) objects[0];
				User user = (User) objects[1];
				
				if (badge != null && user != null) {
					List<User> users = null;
					
					if (badgeMap.containsKey(badge)) {
						users = badgeMap.get(badge);
					} else {
						users = new ArrayList<User>();
					}
					users.add(user);

					badgeMap.put(badge, users);
				}
			}
			return badgeMap;
		}
		return null;
	}
	
	private List<Object[]> getBadgesUserDataForResource(long resourceId, Class<? extends BaseEntity> clazz) throws ResourceCouldNotBeLoadedException {
		BaseEntity resource = loadResource(clazz, resourceId);
		resource = HibernateUtil.initializeAndUnproxy(resource);
		
		String property = resource.getClass().getSimpleName();
		property = property.substring(0, 1).toLowerCase() + property.substring(1, property.length());
		
		String query = 
			"SELECT badge, ev.maker " +
			"FROM EvaluationSubmission evSubmission " +
			"LEFT JOIN evSubmission.evaluations ev " +
			"LEFT JOIN ev."+property+" res " +
			"LEFT JOIN ev.badges badge " +
			"WHERE res.id = :resourceId " +
				"AND evSubmission.finalized = :finalized " +
				"AND badge IS NOT NULL " +
			"ORDER BY ev.dateCreated ASC";
		
		@SuppressWarnings("unchecked")
		List<Object[]> result = persistence.currentManager().createQuery(query).
				setLong("resourceId", resourceId).
				setBoolean("finalized", true).
				list();
		
		if (result == null) {
			result = new ArrayList<Object[]>();
		}
		
		if (resource.getClass().equals(AchievedCompetence.class)) {
			TargetCompetence tComp = portfolioManager.getTargetCompetenceOfAchievedCompetence(resourceId);
			
			if (tComp != null) {
				List<Object[]> dataForTargetComp = getBadgesUserDataForResource(tComp.getId(), TargetCompetence.class);
				
				if (dataForTargetComp != null) {
					result.addAll(dataForTargetComp);
				}
			}
		}
		return result;
	}
	
	@Override
	@Transactional (readOnly = true)
	public long getBadgeCountForResource(Class<? extends BaseEntity> clazz, long resourceId) {
		return getBadgeCountForResource(clazz, resourceId, persistence.currentManager());
	}
	
	@Override
	@Transactional (readOnly = true)
	public long getBadgeCountForResource(Class<? extends BaseEntity> clazz, long resourceId, Session session) {
		String property = clazz.getSimpleName();
		property = property.substring(0, 1).toLowerCase() + property.substring(1, property.length());
		
		String query = 
			"SELECT COUNT(badge) " +
			"FROM EvaluationSubmission evSubmission " +
			"LEFT JOIN evSubmission.evaluations ev " +
			"LEFT JOIN ev."+property+" res " +
			"LEFT JOIN ev.badges badge " +
			"WHERE res.id = :resourceId " +
				"AND evSubmission.finalized = :finalized ";
		
		Long count = (Long) session.createQuery(query).
				setLong("resourceId", resourceId).
				setBoolean("finalized", true).
				uniqueResult();
		
		if (clazz.equals(AchievedCompetence.class)) {
			TargetCompetence tComp = portfolioManager.getTargetCompetenceOfAchievedCompetence(resourceId);
			
			if (tComp != null) {
				long tCompBadgeCount = getBadgeCountForResource(TargetCompetence.class, tComp.getId());
				
				count += tCompBadgeCount;
			}
		}
		return count;
	}
}
