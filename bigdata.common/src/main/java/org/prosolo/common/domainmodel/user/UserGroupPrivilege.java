package org.prosolo.common.domainmodel.user;

public enum UserGroupPrivilege {

	None,
	Learn,
	Instruct,
	Edit;
	
	public boolean isPrivilegeIncluded(UserGroupPrivilege priv) {
		switch(this) {
			case None:
				return true;
			case Learn:
				return priv == Learn || priv == Instruct || priv == Edit;
			case Instruct:
				return priv == Instruct || priv == Edit;
			case Edit:
				return priv == Edit;
		}
		return false;
	}
}
