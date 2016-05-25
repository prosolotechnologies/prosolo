package org.prosolo.common.domainmodel.general;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Type;
 
@MappedSuperclass
public class BaseEntity implements Serializable { 

	private static final long serialVersionUID = -9040908565811200216L;

	private long id;
	private String title;
	private String description;
	private Date dateCreated;
	private boolean deleted;
	
	@Column(name = "title", nullable = true, length=1000)
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	@Column(length = 90000)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
//	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created")
//	@Type(type="date")
	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	@Column(nullable=true)
	@Type(type="true_false")
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = (int) this.getId();
		result = (int) (prime * result+this.getId());
		return result;
	}	 

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		
		BaseEntity other = (BaseEntity) obj;
		if (this.id > 0 && other.id > 0) {
			return this.id == other.getId();
		}  
		return true;
	}
	 
}