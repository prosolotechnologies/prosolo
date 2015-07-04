package org.prosolo.common.domainmodel.user;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.Email;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserType;


@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class User extends BaseEntity {

	private static final long serialVersionUID = 6133982108322552108L;

	private String name;
	private String lastname;
	private String avatarUrl;
	private Set<TargetLearningGoal> learningGoals;
	private Set<Role> roles;
	private Email email;
	
	@Type(type="encryptedString")
	private String password;
	private int passwordLength = 0;
	private String profileUrl;
	private UserType userType;
	private Organization organization;
	private String position;
	private boolean system;
	
	// location
	private String locationName;
	private Double latitude;
	private Double longitude;
	
	public User() {
		learningGoals = new HashSet<TargetLearningGoal>();
		roles = new HashSet<Role>();
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

	@ManyToMany(fetch = FetchType.LAZY)
	@Cascade(org.hibernate.annotations.CascadeType.MERGE)
	@JoinTable(name = "user_learning_goals")
	public Set<TargetLearningGoal> getLearningGoals() {
		return learningGoals;
	}

	public void setLearningGoals(Set<TargetLearningGoal> learningGoals) {
		this.learningGoals = learningGoals;
	}

	public void addLearningGoal(TargetLearningGoal learningGoal) {
		if (null != learningGoal) {
			if (!getLearningGoals().contains(learningGoal)) {
				getLearningGoals().add(learningGoal);
			}
		}
	}

	 //@Override
	public int compareTo(User o) {
		return getEmail().toString().compareTo(( (User) o).getEmail().toString());

	}

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "user_user_organization")
	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
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
	
	@OneToOne
	@NotFound(action=NotFoundAction.IGNORE)
	public Email getEmail() {
		return email;
	}

	public void setEmail(Email email) {
		this.email = email;
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
	
}
