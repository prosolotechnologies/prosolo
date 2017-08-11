package org.prosolo.common.domainmodel.user;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.UnitRoleMembership;


@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class User extends BaseEntity {

	private static final long serialVersionUID = 6133982108322552108L;

	private String name;
	private String lastname;
	private String avatarUrl;
	private Set<Role> roles;
	private Organization organization;
	
	@Type(type="encryptedString")
	private String password;
	private int passwordLength = 0;
	private String profileUrl;
	private UserType userType;
	private String position;
	private boolean system;
	
	// location
	private String locationName;
	private Double latitude;
	private Double longitude;
	
	// email
	private String email;
	private boolean verified;
	private String verificationKey;

	private List<UnitRoleMembership> unitMemberships;
	
	public User() {
		roles = new HashSet<Role>();
	}
	
	public User(long id) {
		this();
		setId(id);
	}

	@Column(name = "name", nullable = true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "lastname", nullable = true)
	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lname) {
		this.lastname = lname;
	}
	
	@Transient
	public String getFullName() {
		return getName() + " " + getLastname();
	}

	@Column(name = "avatarUrl")
	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	@Column(name = "password", nullable = true)
	public String getPassword() {
		return password;
	}
	
	@Column(nullable = true)
	public int getPasswordLength() {
		return passwordLength;
	}

	public void setPasswordLength(int passwordLength) {
		this.passwordLength = passwordLength;
	}

	 //@Override
	public int compareTo(User o) {
		return getEmail().toString().compareTo(( (User) o).getEmail().toString());
	}

	@Type(type="true_false")
	@Column(name="system", columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isSystem() {
		return system;
	}

	public void setSystem(boolean system) {
		this.system = system;
	}
	
	@ManyToMany
	@JoinTable(name = "user_User_Role")
	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	
	public void addRole(Role role) {
		if (role != null && !getRoles().contains(role))
			getRoles().add(role);
	}
	
	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}
	
	public String getProfileUrl() {
		return profileUrl;
	}

	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}
	
	@Enumerated
	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	@Override
	public String toString() {
		return name + " " + lastname +" (id=" + getId() + ")";
	}
	
	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	
	@Type(type = "true_false")
	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean confirmed) {
		this.verified = confirmed;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "verificationKey", nullable = true)
	public String getVerificationKey() {
		return verificationKey;
	}

	public void setVerificationKey(String confirmationKey) {
		this.verificationKey = confirmationKey;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	public Organization getOrganization(){
		return organization;
	}
	
	public void setOrganization(Organization organization){
		this.organization = organization;
	}

	@OneToMany(mappedBy = "user")
	public List<UnitRoleMembership> getUnitMemberships() {
		return unitMemberships;
	}

	public void setUnitMemberships(List<UnitRoleMembership> unitMemberships) {
		this.unitMemberships = unitMemberships;
	}
}
