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
		boolean canRead = false, canAccess = privileges.isEmpty() || privileges.contains(UserGroupPrivilege.None), 
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
					//if resource is not draft instructor with right privilege can access resource
					if(userAccess.getDatePublished() != null) {
						if(!canAccess && hasNeededPrivilege) {
							canAccess = true;
						}
						canInstruct = true;
					}
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
		
		//if full access is allowed, than user also have privilege to read resource content.
		if(canAccess) {
			canRead = true;
		} 
		/*
		 * if user can't access resource there is a situation where he can still access the resource in read only mode:
		 * when one of the privileges required is Learn privilege and resource is not draft (which means it was published once).
		 * That means that when Learn privilege is enough to access resource in given context, user can access that resource
		 * in read only mode no matter which privileges he has (even if he does not have any privilege) as long as resource
		 * is not draft.
		 */
		else {
			canRead = userAccess.getDatePublished() != null && privileges.contains(UserGroupPrivilege.Learn);
		}
		
		return new ResourceAccessData(canRead, canAccess, canEdit, canLearn, canInstruct);
	}

	private boolean hasRightAccessMode(AccessMode accessMode, LearningResourceType resourceType) {
		return accessMode == AccessMode.NONE 
				? true 
				: (accessMode == AccessMode.USER 
						? resourceType == LearningResourceType.USER_CREATED
						: resourceType == LearningResourceType.UNIVERSITY_CREATED);
	}
}
