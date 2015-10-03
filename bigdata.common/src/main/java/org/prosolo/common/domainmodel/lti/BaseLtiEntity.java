package org.prosolo.common.domainmodel.lti;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Type;

@MappedSuperclass
public class BaseLtiEntity implements Serializable {

	private static final long serialVersionUID = -1658866732544321696L;
	
	private long id;
	private boolean deleted;
	
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
		
		BaseLtiEntity other = (BaseLtiEntity) obj;
		if (this.id > 0 && other.id > 0) {
			return this.id == other.getId();
		}  
		return true;
	}
}
