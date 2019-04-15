package org.prosolo.services.user.data;

import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.web.administration.data.RoleData;
import org.prosolo.web.util.AvatarUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class UserData implements Serializable {

	private static final long serialVersionUID = 8668238017709751223L;

	private long id;
	private String fullName;
	private String name;
	private String lastName;
	private String password;
	private String avatarUrl;
	private String position;
	private String email;
	private boolean followedByCurrentUser;
	private boolean userSet;
	private UserType type = UserType.REGULAR_USER;
	private List<RoleData> roles = new ArrayList<>();
	private List<Long> roleIds = new ArrayList<>();
	private ObjectStatus objectStatus;
	private String locationName;
	private Double longitude;
	private Double latitude;
	private String newPassword;

	public UserData() {
		this.roles = new LinkedList<RoleData>();
	}

	public UserData(User user) {
		this();
		this.id = user.getId();
		this.name = user.getName();
		this.lastName = user.getLastname();
		setFullName(user.getName(), user.getLastname());
		this.avatarUrl = AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size120x120);
		this.position = user.getPosition();
		this.email = user.getEmail();
		this.password = user.getPassword();
		this.locationName = user.getLocationName();
		this.latitude = user.getLatitude();
		this.longitude = user.getLongitude();
		this.objectStatus = ObjectStatus.UP_TO_DATE;
		for(Role r : user.getRoles()){
			this.roleIds.add(r.getId());
		}
	}

	public UserData(User user, Collection<Role> roles) {
		this(user);

		if(roles != null) {
			for(Role role : roles) {
				this.roles.add(new RoleData(role));
			}
		}
	}

	public UserData(long id, String firstName, String lastName, String avatar, String position,
					String email, boolean isAvatarReady) {
		this(id, getFullName(firstName, lastName) , avatar, position, email, isAvatarReady);
		this.setName(firstName);
		this.setLastName(lastName);
	}

	public UserData(long id, String fullName, String avatar, String position,
					String email, boolean isAvatarReady) {
		this.id = id;
		this.fullName = fullName;
		String readyAvatar = avatar;
		if(avatar != null && !isAvatarReady) {
			readyAvatar = AvatarUtils.getAvatarUrlInFormat(avatar, ImageFormat.size120x120);
		}
		this.avatarUrl = readyAvatar;
		this.position = position;
		this.email = email;
	}

	public UserData(long id, String fullName){
		this.id = id;
		this.fullName = fullName;
	}

	public void addRoleId(long id) {
		this.roleIds.add(id);
	}

	public boolean hasRoleId(long roleId){
		return roleIds.contains(roleId);
	}

	public boolean hasRole(long roleId) {
		return roles.stream().anyMatch(r -> r.getId() == roleId);
	}

	public void setFullName(String name, String lastName) {
		this.fullName = getFullName(name, lastName);
	}

	private static String getFullName(String name, String lastName) {
		String fName = name != null ? name + " " : "";
		String lName = lastName != null ? lastName : "";
		return fName + lName;
	}

	public String getCommaSeparatedFullName() {
		String fName = name != null ? name : "";
		String lName = lastName != null ? lastName : "";
		String infix = !fName.isEmpty() && !lName.isEmpty() ? ", " : "";
		return lName + infix + fName;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		userSet = true;
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isFollowedByCurrentUser() {
		return followedByCurrentUser;
	}

	public void setFollowedByCurrentUser(boolean followedByCurrentUser) {
		this.followedByCurrentUser = followedByCurrentUser;
	}

	public UserType getType() {
		return type;
	}

	public void setType(UserType type) {
		this.type = type;
	}

	public List<RoleData> getRoles() {
		return roles;
	}

	public void setRoles(List<RoleData> roles) {
		this.roles = roles;
	}

	public boolean isUserSet() {
		return userSet;
	}

	public void setUserSet(boolean userSet) {
		this.userSet = userSet;
	}

	public List<Long> getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(List<Long> roleIds) {
		this.roleIds = roleIds;
	}

	public ObjectStatus getObjectStatus() {
		return objectStatus;
	}

	public void setObjectStatus(ObjectStatus objectStatus) {
		this.objectStatus = objectStatus;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getRolesCSV() {
		String rolesString = "";
		if(roles != null) {
			for(RoleData rd : roles) {
				if(!rolesString.isEmpty()) {
					rolesString += ", ";
				}
				rolesString += rd.getName();
			}
		}
		return rolesString;
	}

}