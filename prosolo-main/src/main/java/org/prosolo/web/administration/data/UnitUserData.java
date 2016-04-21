package org.prosolo.web.administration.data;

import java.util.ArrayList;
import java.util.List;
//import org.prosolo.common.domainmodel.user.UserType;
//import org.prosolo.web.activitywall.data.UserData;
//import org.prosolo.web.activitywall.data.UserDataFactory;
//import org.prosolo.web.util.AvatarUtils;

@Deprecated
public class UnitUserData extends UserData {

	private static final long serialVersionUID = -6086111495385442574L;

	private List<RoleData> roles = new ArrayList<RoleData>();
	private boolean active;
	private String status;
	private long unitUserId;

	public UnitUserData() {

	}

//	public UnitUserData(Unit_User unitUser) {
//		super(unitUser.getUser());
//		this.active = unitUser.isActive();
//		this.unitUserId = unitUser.getId();
//		this.status = this.active ? "Active" : "Inactive";
////		Set<Unit_User_Role> unitRoles = unitUser.getUnitUserRole();
////		Iterator<Unit_User_Role> iterator = unitRoles.iterator();
////
////		while (iterator.hasNext()) {
////			Unit_User_Role unitRole = iterator.next();
////			RoleData roleData = new RoleData(unitRole.getRole());
////			roleData.setActive(unitRole.isActive());
////			roles.add(roleData);
////		}
//	}

	public long getUnitUserId() {
		return unitUserId;
	}

	public void setUnitUserId(long unitUserId) {
		this.unitUserId = unitUserId;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public List<RoleData> getRoles() {
		return roles;
	}

	public void setRoles(List<RoleData> roles) {
		this.roles = roles;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
