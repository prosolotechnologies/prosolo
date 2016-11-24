package org.prosolo.services.activityWall.impl.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserType;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.web.util.AvatarUtils;

public class UserData implements Serializable {

	private static final long serialVersionUID = 7589528879374526537L;
	
	private long id;
	private String fullName;
	private String avatar;
	private String nickName;
	private String profileUrl;
	private UserType type = UserType.REGULAR_USER;
	
	public UserData() {
		
	}
	
	/**
	 * constructor for regular user
	 * @param id
	 * @param firstName
	 * @param lastName
	 * @param avatar
	 * @param avatarReady - is avatar already formed and ready for use
	 */
	public UserData(long id, String firstName, String lastName, String avatar, boolean avatarReady) {
		this.id = id;
		setFullName(firstName, lastName);
		String fullAvatar = avatar;
		if(!avatarReady) {
			fullAvatar = AvatarUtils.getAvatarUrlInFormat(avatar, ImageFormat.size120x120);
		}
		this.avatar = fullAvatar;
	}
	
	public UserData(User user) {
		this.id = user.getId();
		setFullName(user.getName(), user.getLastname());
		this.avatar = AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size120x120);
	}
	
	/**
	 * constructor for twitter user
	 * @param nickname
	 * @param name
	 * @param avatar
	 * @param profileUrl
	 */
	//TODO if regular user has twitter post, should we set twitter nickname, name, avatar, profile url
	public UserData(String nickname, String name, String avatar, String profileUrl) {
		this.type = UserType.TWITTER_USER;
		this.nickName = nickname;
		this.fullName = name;
		this.avatar = avatar;
		this.profileUrl = profileUrl;
	}
	
	private void setFullName(String firstName, String lastName) {
		this.fullName = firstName + (lastName != null ? " " + lastName : "");
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getProfileUrl() {
		return profileUrl;
	}

	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}

	public UserType getType() {
		return type;
	}

	public void setType(UserType type) {
		this.type = type;
	}
	
}
