/**
 * 
 */
package org.prosolo.web.settings.data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.prosolo.common.domainmodel.organization.OrganizationalUnit;
import org.prosolo.common.domainmodel.user.Email;

/**
 * @author "Nikola Milikic"
 * 
 */
public class AccountData implements Serializable {

	private static final long serialVersionUID = -1762307654954041067L;

	private long id;
	private String avatarPath;
	private String firstName;
	private String lastName;
	private String password;
	private String passwordConfirm;
	private Email defaultEmail;
	private List<Email> emails;
	private String position;
	private OrganizationalUnit department;

	private String locationName;
	private String latitude;
	private String longitude;

	public AccountData() {
		emails = new LinkedList<Email>();
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAvatarPath() {
		return avatarPath;
	}

	public void setAvatarPath(String avatarPath) {
		this.avatarPath = avatarPath;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getPasswordConfirm() {
		return passwordConfirm;
	}

	public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}

	public Email getDefaultEmail() {
		return defaultEmail;
	}

	public void setDefaultEmail(Email defaultEmail) {
		this.defaultEmail = defaultEmail;
	}

	public List<Email> getEmails() {
		return emails;
	}

	public void setEmails(List<Email> emails) {
		this.emails = emails;
	}

	public boolean addEmail(Email email) {
		if (email != null) {
			return emails.add(email);
		}
		return false;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public OrganizationalUnit getDepartment() {
		return department;
	}

	public void setDepartment(OrganizationalUnit department) {
		this.department = department;
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
	
}
