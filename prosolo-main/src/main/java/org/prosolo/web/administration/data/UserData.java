package org.prosolo.web.administration.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.model.UploadedFile;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.web.util.AvatarUtils;

@Deprecated
public class UserData implements Serializable {

	private static final long serialVersionUID = -8175020115862390741L;

	private long id;
	private String name;
	private String lastName;
	private String password;
	private String position;
	private String email;
	private String reEnterPassword;
	private String avatarUrl = "/" + CommonSettings.getInstance().config.services.userService.defaultAvatarPath + "size120x120.png";
	private boolean changePassword = false;
	private boolean sendEmail = false;
	private UploadedFile file;
	private String fullName;
	private List<RoleData> roles = new ArrayList<>();
	private List<Long> roleIds = new ArrayList<>();
	
	public UserData() {}
	
	public UserData(User user) {
		this(user, null);
	}
	
	public UserData(User user, List<Role> roles) {
		this.id = user.getId();
		this.name = user.getName();
		this.lastName = user.getLastname();
		setFullName(name, lastName);
		this.password = user.getPassword();
		this.email = user.getEmail();
		this.position = user.getPosition();
		this.avatarUrl = AvatarUtils.getAvatarUrlInFormat(user, ImageFormat.size120x120);
		if(roles != null) {
			for(Role role : roles) {
				this.roles.add(new RoleData(role));
			}
		}
	}
	
	public void addRoleId(long id) {
		this.roleIds.add(id);
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
	
	public void setFullName(String name, String lastName) {
		this.fullName = name + (lastName != null ? " " + lastName : "");
	}

	public boolean isChangePassword() {
		return changePassword;
	}

	public void setChangePassword(boolean changePassword) {
		this.changePassword = changePassword;
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

	public String getReEnterPassword() {
		return reEnterPassword;
	}

	public void setReEnterPassword(String reEnterPassword) {
		this.reEnterPassword = reEnterPassword;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void updateUser(User user) {
		if (user != null) {
			user.setName(this.name);
			user.setLastname(this.lastName);
			if (this.isChangePassword())
				user.setPassword(this.password);
			
			user.setPosition(this.position);
		}
	}

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}
	
	public boolean isSendEmail() {
		return sendEmail;
	}

	public void setSendEmail(boolean sendEmail) {
		this.sendEmail = sendEmail;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public List<RoleData> getRoles() {
		return roles;
	}

	public void setRoles(List<RoleData> roles) {
		this.roles = roles;
	}

	public List<Long> getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(List<Long> roleIds) {
		this.roleIds = roleIds;
	}
	
}
