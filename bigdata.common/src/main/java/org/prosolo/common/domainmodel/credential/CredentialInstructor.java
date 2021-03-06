package org.prosolo.common.domainmodel.credential;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.user.User;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"user", "credential"})})
public class CredentialInstructor {

	private long id;
	private int maxNumberOfStudents;
	private User user;
	private Credential1 credential;
	private List<TargetCredential1> assignedStudents;
	private Date dateAssigned;
	private CredentialInstructorStatus status = CredentialInstructorStatus.ACTIVE;
	
	public CredentialInstructor() {
		assignedStudents = new ArrayList<>();
	}
	
	@Id
	@Column(name = "id", unique = true, nullable = false, insertable = false, updatable = false)
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Type(type = "long")
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getMaxNumberOfStudents() {
		return maxNumberOfStudents;
	}
	public void setMaxNumberOfStudents(int maxNumberOfStudents) {
		this.maxNumberOfStudents = maxNumberOfStudents;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Credential1 getCredential() {
		return credential;
	}
	public void setCredential(Credential1 credential) {
		this.credential = credential;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="instructor")
	@LazyCollection(LazyCollectionOption.EXTRA)
	public List<TargetCredential1> getAssignedStudents() {
		return assignedStudents;
	}
	
	public void setAssignedStudents(List<TargetCredential1> assignedStudents) {
		this.assignedStudents = assignedStudents;
	}
	
	public Date getDateAssigned() {
		return dateAssigned;
	}
	
	public void setDateAssigned(Date dateAssigned) {
		this.dateAssigned = dateAssigned;
	}

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public CredentialInstructorStatus getStatus() {
        return status;
    }

    public void setStatus(CredentialInstructorStatus status) {
        this.status = status;
    }
}
