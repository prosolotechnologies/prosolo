package org.prosolo.services.nodes.data.resourceAccess;

import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.credential.CredentialDeliveryStatus;
import org.prosolo.services.nodes.factory.CredentialDeliveryStatusFactory;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResourceAccessFactory {

	public ResourceAccessData determineAccessRights(long userId, long resourceId, ResourceAccessRequirements req, UserAccessSpecification userAccess) {
		//collection of needed privileges - user has to have one of them
		List<UserGroupPrivilege> privileges = req.getPrivileges();
		/*
		 * If collection of needed privileges is empty or contains None privilege resource can be accessed 
		 * no matter which privileges user has.
		 */
		boolean canRead = false, canAccess = privileges.isEmpty() || privileges.contains(UserGroupPrivilege.None), 
				canEdit = false, canLearn = false, canInstruct = false;
		
		/*
		 * instantiate visitor and pass it to accept method of UserAccessSpecification object in order to get needed
		 * data in a visitor.
		 */
		UserAccessSpecificationVisitorImpl visitor = new UserAccessSpecificationVisitorImpl(req.getAccessMode());
		userAccess.accept(visitor);
		
		for(UserGroupPrivilege p : userAccess.getPrivileges()) {
			boolean hasNeededPrivilege = privileges.contains(p);
			switch(p) {
				case Learn:
					if(visitor.allowedToLearn) {
						canLearn = true;
						if(!canAccess && hasNeededPrivilege) {
							canAccess = true;
						}
					}
					break;
				case Instruct:
					if(visitor.allowedToInstruct) {
						if(!canAccess && hasNeededPrivilege) {
							canAccess = true;
						}
						canInstruct = true;
					}
					break;
				case Edit:
					if(visitor.allowedToEdit) {
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
			if(userAccess.isResourceVisibleToAll() && visitor.allowedToLearn) {
				//check if user has student role in at least one unit that is connected to credential
				UserUnitAccessSpecificationVisitor visitor2 = new UserUnitAccessSpecificationVisitor(userId, resourceId);
				userAccess.accept(visitor2);

				if (visitor2.canAccess) {
					canLearn = true;
					if (privileges.contains(UserGroupPrivilege.Learn)) {
						canAccess = true;
					}
				}
			}
		}
		
		//if full access is allowed, than user also have privilege to read resource content.
		if(canAccess) {
			canRead = true;
		} 
		/*
		 * if user can't access resource in 'full access mode' there is a situation where he can still access the 
		 * resource in read only mode: when one of the privileges required is Learn privilege 
		 * and visitor.allowedToRead is true (which tells that preconditions to access resource in read mode are met).
		 * That means that when Learn privilege is enough to access resource in given context and preconditions
		 * are met, user can access that resource in read only mode no matter which privileges he has 
		 * (even if he does not have any privilege). For now, we do not define read mode for other privileges, just when
		 * learn is needed.
		 */
		else {
			canRead = visitor.allowedToRead && privileges.contains(UserGroupPrivilege.Learn);
		}
		
		return new ResourceAccessData(canRead, canAccess, canEdit, canLearn, canInstruct);
	}
	
	//Visitor which visits UserAccessSpecification objects and populates preconditions for different access rights
	private class UserAccessSpecificationVisitorImpl implements UserAccessSpecificationVisitor<Void> {

		private AccessMode accessMode;
		
		private boolean allowedToRead;
		private boolean allowedToLearn;
		private boolean allowedToInstruct;
		private boolean allowedToEdit;
		
		private UserAccessSpecificationVisitorImpl(AccessMode mode) {
			this.accessMode = mode;
		}
		
		@Override
		public Void visit(CredentialUserAccessSpecification spec) {
			//because credentials and deliveries can only be edited from manage section
			allowedToEdit = accessMode == AccessMode.MANAGER;
			/*
			 * original credential can only be edited, so all other flags should be false; only deliveries can be
			 * learned, instructed or accessed in read only mode.
			 */
			if(spec.getType() == CredentialType.Delivery) {
				CredentialDeliveryStatus status = ServiceLocator.getInstance().getService(
						CredentialDeliveryStatusFactory.class).getDeliveryStatus(
								spec.getDeliveryStart(), spec.getDeliveryEnd());
				/*
				 * resource can be accessed in read only mode when delivery is not pending or it is pending but delivery
				 * is scheduled (start date set);
				 * when delivery is ended it should remain visible in read only mode to everyone (inlcuding users
				 * without any privilege)
				 */
				allowedToRead = status != CredentialDeliveryStatus.PENDING || spec.getDeliveryStart() != null;
				//users can learn delivery only when it is active
				allowedToLearn = status == CredentialDeliveryStatus.ACTIVE;
				//users can always access resource with instruct privilege when it is delivery, no additional conditions are needed
				allowedToInstruct = true;
			}
			return null;
		}

		@Override
		public Void visit(CompetenceUserAccessSpecification spec) {
			//resource should not be draft in order to be accessed in read only mode
			allowedToRead = spec.getDatePublished() != null;
			//resource should be published in order for users to learn it
			allowedToLearn = spec.isResourcePublished();
			//resource must not be draft in order for users to access it with instruct privilege
			allowedToInstruct = spec.getDatePublished() != null;
			//resource can be edited only from section where it was created
			allowedToEdit = hasRightAccessMode(spec.getResourceType());
			return null;
		}
		
		private boolean hasRightAccessMode(LearningResourceType resourceType) {
			return accessMode == AccessMode.NONE 
					? true 
					: (accessMode == AccessMode.USER 
							? resourceType == LearningResourceType.USER_CREATED
							: resourceType == LearningResourceType.UNIVERSITY_CREATED);
		}
		
	}

	//Visitor which visits UserAccessSpecification objects and check if user has student role in appropriate units
	private class UserUnitAccessSpecificationVisitor implements UserAccessSpecificationVisitor<Void> {

		private long userId;
		private long resourceId;

		private boolean canAccess;

		public UserUnitAccessSpecificationVisitor(long userId, long resourceId) {
			this.userId = userId;
			this.resourceId = resourceId;
		}

		@Override
		public Void visit(CredentialUserAccessSpecification spec) {
			Long studentRoleId = ServiceLocator.getInstance().getService(RoleManager.class)
					.getRoleIdByName(SystemRoleNames.USER);
			
			canAccess = ServiceLocator.getInstance().getService(UnitManager.class)
					.checkIfUserHasRoleInUnitsConnectedToCredential(userId,
							ServiceLocator.getInstance().getService(CredentialManager.class).getCredentialIdForDelivery(resourceId),
							studentRoleId);
			return null;
		}

		@Override
		public Void visit(CompetenceUserAccessSpecification spec) {
			Long studentRoleId = ServiceLocator.getInstance().getService(RoleManager.class)
					.getRoleIdByName(SystemRoleNames.USER);
					
			canAccess = ServiceLocator.getInstance().getService(UnitManager.class)
					.checkIfUserHasRoleInUnitsConnectedToCompetence(userId, resourceId, studentRoleId);
			return null;
		}

	}
}
