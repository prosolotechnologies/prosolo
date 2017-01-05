package org.prosolo.web.notification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.util.pagination.Paginable;
import org.prosolo.web.courses.util.pagination.PaginationData;
import org.prosolo.web.notification.data.FilterNotificationType;
import org.prosolo.web.notification.data.NotificationTypeFilter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "notificationBean")
@Component("notificationBean")
@Scope("view")
public class NotificationBean implements Serializable, Paginable {
	
	private static final long serialVersionUID = 8874333324943561974L;

	private static Logger logger = Logger.getLogger(NotificationBean.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private NotificationManager notificationManager;
	
	private List<NotificationData> notifications = new ArrayList<>();
	private PaginationData paginationData = new PaginationData(5);
	private List<FilterNotificationType> filters = new ArrayList<>();
	private List<NotificationType> notificationTypes = new ArrayList<>();
	
	@PostConstruct
	public void init() {
		for (NotificationTypeFilter filterEnum : NotificationTypeFilter.values()) {
			FilterNotificationType f = new FilterNotificationType(filterEnum, true);
			filters.add(f);
			notificationTypes.add(filterEnum.getType());
		}
		loadNotifications(true);
	}
	
	public void loadNotifications(boolean recalculateTotalNumber) {
		try {
			if (notificationTypes.isEmpty()) {
				paginationData.update(0);
				notifications = new ArrayList<>();
			} else {
				/*
				 * if all filters are applied, it is more performant to just not filter notifications
				 * which is achieved by passing null for notification type list
				 */
				List<NotificationType> filterTypes = notificationTypes.size() == filters.size() 
						? null 
						: notificationTypes;
				
				paginationData.update(notificationManager.getNumberOfNotificationsForUser(
						loggedUser.getUserId(), filterTypes));
				
				if (paginationData.getNumberOfResults() > 0) {
					notifications = notificationManager.getNotificationsForUser(
							loggedUser.getUserId(),
							paginationData.getPage() - 1, paginationData.getLimit(), filterTypes, loggedUser.getLocale());
				} else {
					notifications = new ArrayList<>();
				}
			}
		} catch (DbConnectionException e) {
			logger.error(e);
		}
	}
	
	public void filterChanged(FilterNotificationType filter) {
		if(filter.isApplied()) {
			notificationTypes.add(filter.getFilter().getType());
		} else {
			notificationTypes.remove(filter.getFilter().getType());
		}
		paginationData.setPage(1);
		loadNotifications(true);
	}
	
	public void checkAllFilters() {
		if(notificationTypes.size() != filters.size()) {
			for(FilterNotificationType filter : filters) {
				if(!filter.isApplied()) {
					filter.setApplied(true);
					notificationTypes.add(filter.getFilter().getType());
				}
			}
			paginationData.setPage(1);
			loadNotifications(true);
		}
	}
	
	public void uncheckAllFilters() {
		if(!notificationTypes.isEmpty()) {
			notificationTypes.clear();
			for(FilterNotificationType filter : filters) {
				filter.setApplied(false);
			}
			paginationData.setPage(1);
			loadNotifications(true);
		}
	}
	
	//pagination helper methods
	@Override
	public void changePage(int page) {
		if (paginationData.getPage() != page) {
			paginationData.setPage(page);
			loadNotifications(false);
		}
	}
	
	/*
	 * GETTERS/SETTERS
	 */
	public List<NotificationData> getNotifications() {
		return notifications;
	}
	
	public void setNotifications(List<NotificationData> notifications) {
		this.notifications = notifications;
	}

	public PaginationData getPaginationData() {
		return paginationData;
	}

	public List<FilterNotificationType> getFilters() {
		return filters;
	}

	public void setFilters(List<FilterNotificationType> filters) {
		this.filters = filters;
	}
	
}
