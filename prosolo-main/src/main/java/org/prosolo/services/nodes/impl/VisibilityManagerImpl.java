package org.prosolo.services.nodes.impl;

import java.util.HashMap;
import java.util.Map;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.organization.Visible;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.VisibilityManager;
import org.prosolo.services.nodes.exceptions.VisibilityCoercionError;
import org.prosolo.services.nodes.util.VisibilityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.VisibilityManager")
public class VisibilityManagerImpl extends AbstractManagerImpl implements VisibilityManager {
	
	private static final long serialVersionUID = 320441624551114295L;
	
	@Autowired private EventFactory eventFactory;

	@Override
	@Transactional
	public Visible setResourceVisibility(User user, long resId, VisibilityType visType, String context) throws EventException, ResourceCouldNotBeLoadedException {
		Node resource = loadResource(Node.class, resId);
		return setResourceVisibility(user, (Visible) resource, visType, context);
	}
	
	@Override
	@Transactional
	public Visible setResourceVisibility(User user, Visible resource, String newVisibility, String context) throws VisibilityCoercionError, EventException {
		VisibilityType visType = VisibilityUtil.parseVisibilityType(newVisibility);
		
		return setResourceVisibility(user, resource, visType, context);
	}
	
	@Override
	@Transactional (readOnly = false)
	public Visible setResourceVisibility(User user, Visible resource, VisibilityType visType, String context) throws EventException {
		resource = (Visible) merge((BaseEntity) resource);
		
		if (visType.equals(VisibilityType.PRIVATE) || visType.equals(VisibilityType.PUBLIC)) {
			resource.setVisibility(visType);
			saveEntity(resource);
			
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("context", context);
			parameters.put("visibility", visType.name());
			
			eventFactory.generateChangeVisibilityEvent(user, (BaseEntity) resource, visType, parameters);
			return resource;
		}
		return null;
	}
	
	@Override 
	@Transactional (readOnly = true)
	public VisibilityType retrieveTargetCompetenceVisibility(Long tCompId) {
		String query=
			"SELECT tGoal.visibility " +
			"FROM TargetLearningGoal tGoal "+
			"LEFT JOIN tGoal.targetCompetences tComp " +
			"WHERE tComp.id = :tCompId ";
		VisibilityType visibility =(VisibilityType) persistence.currentManager().createQuery(query)
				.setLong("tCompId", tCompId)
				.uniqueResult();
		return visibility;
	}
	
	@Override 
	@Transactional (readOnly = true)
	public VisibilityType retrieveTargetActivityVisibility(Long tActId) {
 		String query=
			"SELECT tGoal.visibility " +
			"FROM TargetLearningGoal tGoal "+
			"LEFT JOIN tGoal.targetCompetences tComp " +
			"LEFT JOIN tComp.targetActivities tAct "+
			"WHERE tAct.id = :tActId ";
		System.out.println("Retrieve Target Activity query:"+query);
		VisibilityType visibility =(VisibilityType) persistence.currentManager().createQuery(query)
				.setLong("tActId", tActId)
				.uniqueResult();
 		return visibility;
	}
 

}
