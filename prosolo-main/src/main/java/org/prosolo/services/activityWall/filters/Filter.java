package org.prosolo.services.activityWall.filters;

import org.prosolo.domainmodel.interfacesettings.FilterType;

/**
 * @author Zoran Jeremic Jan 22, 2015
 *
 */

public abstract class Filter {
	FilterType filterType;
	
	public FilterType getFilterType() {
		return filterType;
	}
	
	public void setFilterType(FilterType filterType) {
		this.filterType = filterType;
	}
	public String getFilterTypeClassName(){
		return this.filterType.getClass().getSimpleName();
	}
}
