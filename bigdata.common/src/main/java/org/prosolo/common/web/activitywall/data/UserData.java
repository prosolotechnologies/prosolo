/**
 * 
 */
package org.prosolo.common.web.activitywall.data;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;

import java.io.Serializable;
import java.util.Date;


 


/**
 * @author "Nikola Milikic"
 * 
 */
public class UserData implements Serializable, Comparable<UserData> {

	private static final long serialVersionUID = 4431149644251825589L;

	private long id;
	private String name = "";
	private String profileUrl = "";
	private String avatarUrl ="size120x120.png";
	private String position = "";
	private boolean disabled;
	private boolean followed;
	private boolean loggedUser;
	private boolean externalUser;
	private String firstName;
	private String lastName;
	
	private PublishingServiceData publishingService;

	// location
	private String locationName;
	private String latitude;
	private String longitude;
	
	private Date lastAction;
	
	public UserData() {	}

	public UserData(long userId, String firstName,String lastName, String avatar){
		this();
		this.id = userId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.avatarUrl = avatar;
	}

	public UserData(long userId, String name, String avatar) {
		this.setId(userId);
		this.setName(name);
//		userData.setProfileUrl(user.getProfileUrl());
		this.setAvatarUrl(avatar);
		
//		if (user.getUserType().equals(UserType.TWITTER_USER)) {
//			userData.setPosition("Twitter User");
//			userData.setExternalUser(true);
//		} else {
//			userData.setPosition(user.getPosition());
//		}
		
//		// location
//		userData.setLocationName(user.getLocationName());
//		if(user.getLatitude()!=null)
//			userData.setLatitude(String.valueOf(user.getLatitude()));
//		if(user.getLongitude()!=null)
//			userData.setLongitude(String.valueOf(user.getLongitude()));
	}
//	public UserData(User user, ImageFormat imageFormat) {
//		UserData userData=createUserData(user);
//		userData.setAvatarUrl(AvatarUtils.getAvatarUrlInFormat(user, imageFormat));
//		return userData;
//	}

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

	public void setFullName(String name, String lastName) {
		this.name = getFullName(name, lastName);
	}

	public String getFullName(String name, String lastName) {
		String fName = name != null ? name + " " : "";
		String lName = lastName != null ? lastName : "";
		return fName + lName;
	}

	public String getCommaSeparatedFullName() {
		String fName = firstName != null ? firstName : "";
		String lName = lastName != null ? lastName : "";
		String infix = !fName.isEmpty() && !lName.isEmpty() ? ", " : "";
		return lName + infix + fName;
	}

	@Override
	public String toString() {
		return "UserData [id=" + id + ", name=" + name + ", profileUrl=" + profileUrl + ", avatarUrl=" + avatarUrl
				+ ", position=" + position + ", disabled=" + disabled + ", followed=" + followed + ", loggedUser="
				+ loggedUser + ", externalUser=" + externalUser + ", publishingService=" + publishingService
				+ ", locationName=" + locationName + ", latitude=" + latitude + ", longitude=" + longitude
				+ ", lastAction=" + lastAction + "]";
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
	
	public String getInitials() {
		if(name != null && name.length() >= 2) {
			String[] firstAndLastName = name.split(" ");
			//if we only have name or last name, return first two characters uppercased
			if(firstAndLastName.length == 1) {
				return name.substring(0, 1).toUpperCase();
			}
			else return (firstAndLastName[0].charAt(0) + "" + firstAndLastName[1].charAt(0)).toUpperCase();
		}
		else {
			return "N/A";
		}
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
