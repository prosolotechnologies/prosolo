package org.prosolo.services.nodes.data.resourceAccess;

import java.util.List;

import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.springframework.stereotype.Component;

@Component
public class ResourceAccessFactory {

	public ResourceAccessData determineAccessRights(ResourceAccessRequirements req, UserAccessSpecification userAccess) {
		//collection of needed privileges - user has to have one of them
		List<UserGroupPrivilege> privileges = req.getPrivileges();
		/*
		 * If collection of needed privileges is empty or contains None privilege resource can be accessed 
		 * no matter which privileges user has.
		 */
		boolean canAccess = privileges.isEmpty() || privileges.contains(UserGroupPrivilege.None), 
				canEdit = false, canLearn = false, canInstruct = false;
		boolean hasRightAccessMode = hasRightAccessMode(req.getAccessMode(), userAccess.getResourceType());

		for(UserGroupPrivilege p : userAccess.getPrivileges()) {
			boolean hasNeededPrivilege = privileges.contains(p);
			switch(p) {
				case Learn:
					if(userAccess.isResourcePublished()) {
						canLearn = true;
						if(!canAccess && hasNeededPrivilege) {
							canAccess = true;
						}
					}
					break;
				case Instruct:
					if(!canAccess && hasNeededPrivilege) {
						canAccess = true;
					}
					canInstruct = true;
					break;
				case Edit:
					if(hasRightAccessMode) {
						canEdit = true;
						if(!canAccess && hasNeededPrivilege) {
							canAccess = true;
						}
					}
					break;
				default:
					break;
			}
		}
		
		if(!canLearn || !canAccess) {
			if(userAccess.isResourceVisibleToAll() && userAccess.isResourcePublished()) {
				canLearn = true;
				if(privileges.contains(UserGroupPrivilege.Learn)) {
					canAccess = true;
				}
			}
		}
		
		if(!canEdit || !canAccess) {
			if(userAccess.isUserResourceOwner() && hasRightAccessMode) {
				canEdit = true;
				if(privileges.contains(UserGroupPrivilege.Edit)) {
					canAccess = true;
				}
			}
		}
		
		return new ResourceAccessData(canAccess, canEdit, canLearn, canInstruct);
	}

	private boolean hasRightAccessMode(AccessMode accessMode, LearningResourceType resourceType) {
		return accessMode == AccessMode.NONE 
				? true 
				: (accessMode == AccessMode.USER 
						? resourceType == LearningResourceType.USER_CREATED
						: resourceType == LearningResourceType.UNIVERSITY_CREATED);
	}
}
