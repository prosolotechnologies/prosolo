package org.prosolo.domainmodel.annotation;

import javax.persistence.Entity;

import org.prosolo.domainmodel.annotation.Tag;
import org.prosolo.domainmodel.general.BaseEntity;

@Entity
public class Tag extends BaseEntity implements Comparable<Tag>{

	private static final long serialVersionUID = 232153988226105332L;

	@Override
	public String toString() {
		return "Tag [title=" + getTitle() + "]";
	}
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (this.getClass() != obj.getClass())
//			return false;
//		Tag otherTag=(Tag) obj;
//		if(this.getTitle().equals(otherTag.getTitle())){
//			return true;
//		}else return false;
//	}

	@Override
	public int compareTo(Tag o) {
		return this.getTitle().compareTo(o.getTitle());
	}
	
}