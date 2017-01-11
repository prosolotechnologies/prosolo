package org.prosolo.services.nodes.factory;

import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.services.nodes.data.UserGroupPrivilegeData;
import org.springframework.stereotype.Component;

@Component
public class UserGroupPrivilegeDataFactory {

	public UserGroupPrivilege getUserGroupPrivilege(UserGroupPrivilegeData priv) {
		if(priv == null) {
			return UserGroupPrivilege.None;
		}
		switch(priv) {
			case Edit:
				return UserGroupPrivilege.Edit;
			case View:
				return UserGroupPrivilege.View;
		}
		return UserGroupPrivilege.None;
	}
	
	public UserGroupPrivilegeData getUserGroupPrivilegeData(UserGroupPrivilege priv) {
		if(priv == null) {
			return UserGroupPrivilegeData.View;
		} 
		switch(priv) {
			case Edit:
				return UserGroupPrivilegeData.Edit;
			default:
				return UserGroupPrivilegeData.View;
		}
	}

}
