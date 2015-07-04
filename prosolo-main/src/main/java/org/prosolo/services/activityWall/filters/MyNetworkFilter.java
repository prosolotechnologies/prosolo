package org.prosolo.services.activityWall.filters;

import java.util.Set;
import java.util.TreeSet;

import org.prosolo.domainmodel.interfacesettings.FilterType;

/**
 * @author Zoran Jeremic Jan 22, 2015
 *
 */

public class MyNetworkFilter extends Filter {
	private Set<Long> userIds;
	
	public MyNetworkFilter() {
		setUserIds(new TreeSet<Long>());
		filterType = FilterType.MY_NETWORK;
	}
	
	public Set<Long> getUserIds() {
		return userIds;
	}
	
	public void setUserIds(Set<Long> userIds) {
		this.userIds = userIds;
	}
	
	public void addUserId(Long userId) {
		this.userIds.add(userId);
	}
	public void removeUserId(Long userId) {
		if(this.userIds.contains(userId)){
			this.userIds.remove(userId);
		}
		
	}
	
	public boolean isContainsUsersId(Long userId) {
		return this.userIds != null && this.userIds.contains(userId);
	}
	
}
