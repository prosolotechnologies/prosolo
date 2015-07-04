/**
 * 
 */
package org.prosolo.web.activitywall.data;

import java.io.Serializable;
import java.util.Date;

import org.prosolo.app.Settings;
import org.prosolo.domainmodel.user.User;
import org.prosolo.domainmodel.user.UserType;
import org.prosolo.util.ImageFormat;
import org.prosolo.web.util.AvatarUtils;

/**
 * @author "Nikola Milikic"
 * 
 */
public class UserData implements Serializable, Comparable<UserData> {

	private static final long serialVersionUID = 4431149644251825589L;

	private long id;
	private String name = "";
	private String profileUrl = "";
	private String avatarUrl = "/" + Settings.getInstance().config.services.userService.defaultAvatarPath + "size60x60.png";;
	private String position = "";
	private boolean disabled;
	private boolean followed;
	private boolean loggedUser;
	private boolean externalUser;
	
	private PublishingServiceData publishingService;
	
	// location
	private String locationName;
	private String latitude;
	private String longitude;
	
	private Date lastAction;
	
	public UserData() {}

	public UserData(User user) {
		if (user != null){
			this.id = user.getId();
			this.name = user.getName() + ((user.getLastname() != null) ? " " + user.getLastname() : "");
			this.profileUrl = user.getProfileUrl();
			this.avatarUrl = AvatarUtils.getAvatarUrlInFormat(user, ImageFormat.size120x120);
			
			if (user.getUserType().equals(UserType.TWITTER_USER)) {
				this.position = "Twitter User";
				this.externalUser = true;
			} else {
				this.position = user.getPosition();
			}
			
			// location
			this.locationName = user.getLocationName();
			if(user.getLatitude()!=null)
			this.latitude = String.valueOf(user.getLatitude());
			if(user.getLongitude()!=null)
			this.longitude = String.valueOf(user.getLongitude());
		}
	}

	public UserData(User user, ImageFormat imageFormat) {
		this(user);
		this.avatarUrl = AvatarUtils.getAvatarUrlInFormat(user, imageFormat);
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

	public String getProfileUrl() {
		return profileUrl;
	}

	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
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

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isFollowed() {
		return followed;
	}

	public void setFollowed(boolean followed) {
		this.followed = followed;
	}

	public boolean isLoggedUser() {
		return loggedUser;
	}

	public void setLoggedUser(boolean loggedUser) {
		this.loggedUser = loggedUser;
	}
	
	public PublishingServiceData getPublishingService() {
		return publishingService;
	}

	public void setPublishingService(PublishingServiceData publishingService) {
		this.publishingService = publishingService;
	}
	
	public boolean isExternalUser() {
		return externalUser;
	}

	public void setExternalUser(boolean externalUser) {
		this.externalUser = externalUser;
	}

	@Override
	public String toString() {
		return "UserData [id=" + id + ", name=" + name + ", profileUrl="
				+ profileUrl + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		UserData userData = (UserData) obj;
		return this.getId() == userData.getId();
	}
	
	@Override
	public int compareTo(UserData o) {
		return this.name.compareTo(o.getName()) ;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public Date getLastAction() {
		return lastAction;
	}

	public void setLastAction(Date lastAction) {
		this.lastAction = lastAction;
	}
	
}
