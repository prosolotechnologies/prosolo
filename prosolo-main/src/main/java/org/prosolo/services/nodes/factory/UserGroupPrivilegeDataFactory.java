package org.prosolo.services.nodes.factory;

import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.services.user.data.UserGroupPrivilegeData;
import org.springframework.stereotype.Component;

@Component
@Deprecated
public class UserGroupPrivilegeDataFactory {

	public UserGroupPrivilege getUserGroupPrivilege(UserGroupPrivilegeData priv) {
		if(priv == null) {
			return UserGroupPrivilege.None;
		}
		switch(priv) {
			case Edit:
				return UserGroupPrivilege.Edit;
			case Learn:
				return UserGroupPrivilege.Learn;
		}
		return UserGroupPrivilege.None;
	}
	
	public UserGroupPrivilegeData getUserGroupPrivilegeData(UserGroupPrivilege priv) {
		if(priv == null) {
			return UserGroupPrivilegeData.Learn;
		} 
		switch(priv) {
			case Edit:
				return UserGroupPrivilegeData.Edit;
			default:
				return UserGroupPrivilegeData.Learn;
		}
	}

}
