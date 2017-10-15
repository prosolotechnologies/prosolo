/**
 * 
 */
package org.prosolo.common.domainmodel.interfacesettings;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.interfacesettings.FilterType;

/**
 * @author "Nikola Milikic"
 *
 */
@Entity
public class ActivityWallSettings extends BaseEntity {
	
	private static final long serialVersionUID = 789654779624372363L;
	
	private FilterType chosenFilter = FilterType.ALL;
	private long courseId;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public FilterType getChosenFilter() {
		return chosenFilter;
	}
	
	public void setChosenFilter(FilterType chosenFilter) {
		this.chosenFilter = chosenFilter;
	}

	public long getCourseId() {
		return courseId;
	}

	public void setCourseId(long courseId) {
		this.courseId = courseId;
	}
	
}
