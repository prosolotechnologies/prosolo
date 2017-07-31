package org.prosolo.services.nodes.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.common.observable.StandardObservable;
import org.prosolo.web.util.AvatarUtils;

public class ResourceVisibilityMember extends StandardObservable implements Serializable {

	private static final long serialVersionUID = 2969424744312704839L;
	
	private long id;
	private String firstName;
	private String lastName;
	private String name;
	private ResourceVisibilityMemberType type;
	//user group specific
	private long groupId;
	private long userCount;
	//user specific
	private long userId;
	private String avatar;
	private String position;
	
	private UserGroupPrivilege privilege;
	
	private ObjectStatus status = ObjectStatus.UP_TO_DATE;
	
	public ResourceVisibilityMember(boolean listenChanges) {
		this.listenChanges = listenChanges;
	}
	
	public ResourceVisibilityMember(long resourceGroupId, long groupId, String groupName, 
			long userCount, UserGroupPrivilege priv, boolean listenChanges) {
		this.id = resourceGroupId;
		this.groupId = groupId;
		this.name = groupName;
		this.userCount = userCount;
		this.privilege = priv;
		this.type = ResourceVisibilityMemberType.Group;
		this.listenChanges = listenChanges;
	}
	
	public ResourceVisibilityMember(long userGroupUserId, User user, UserGroupPrivilege priv,
			boolean listenChanges) {
		this.id = userGroupUserId;
		this.userId = user.getId();
		this.firstName = user.getName();
		this.lastName = user.getLastname();
		setFullName(user.getName(), user.getLastname());
		this.avatar = AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size120x120);
		this.position = user.getPosition();
		this.privilege = priv;
		this.type = ResourceVisibilityMemberType.User;
		this.listenChanges = listenChanges;
	}
	
	public void setFullName(String name, String lastName) {
		this.name = getFullName(name, lastName);
	}
	
	private static String getFullName(String name, String lastName) {
		return name + (lastName != null ? " " + lastName : "");
	}

	public String getCommaSeparatedFullName() {
		String fName = firstName != null ? firstName : "";
		String lName = lastName != null ? lastName : "";
		String infix = !fName.isEmpty() && !lName.isEmpty() ? ", " : "";
		return lName + infix + fName;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public long getUserCount() {
		return userCount;
	}

	public void setUserCount(long userCount) {
		this.userCount = userCount;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public UserGroupPrivilege getPrivilege() {
		return privilege;
	}

	public void setPrivilege(UserGroupPrivilege privilege) {
		observeAttributeChange("privilege", this.privilege, privilege);
		this.privilege = privilege;
	}

	public ObjectStatus getStatus() {
		return status;
	}

	public void setStatus(ObjectStatus status) {
		this.status = status;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getGroupId() {
		return groupId;
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	public ResourceVisibilityMemberType getType() {
		return type;
	}

	public void setType(ResourceVisibilityMemberType type) {
		this.type = type;
	}
	
}
