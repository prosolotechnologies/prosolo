package org.prosolo.services.nodes.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.web.administration.data.RoleData;
import org.prosolo.web.util.AvatarUtils;

public class UserData implements Serializable {

	private static final long serialVersionUID = 8668238017709751223L;
	
	private long id;
	private String fullName;
	private String avatarUrl;
	private String position;
	private String email;
	private boolean followedByCurrentUser;
	private boolean userSet;
	private UserType type = UserType.REGULAR_USER;
	private List<RoleData> roles = new ArrayList<>();
	
	public UserData() {
		this.roles = new LinkedList<RoleData>();
	}
	
	public UserData(User user) {
		this();
		this.id = user.getId();
		setFullName(user.getName(), user.getLastname());
		this.avatarUrl = AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size120x120);
		this.position = user.getPosition();
		this.email = user.getEmail();
	}
	
	public UserData(User user, List<Role> roles) {
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
	
	public void setFullName(String name, String lastName) {
		this.fullName = getFullName(name, lastName);
	}
	
	private static String getFullName(String name, String lastName) {
		String fName = name != null ? name : "";
		String lName = lastName != null ? lastName + " " : "";
		return lName + fName;
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

	public void setFullName(String fullName) {
		this.fullName = fullName;
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
