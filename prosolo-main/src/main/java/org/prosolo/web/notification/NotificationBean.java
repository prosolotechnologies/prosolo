package org.prosolo.web.notification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.util.pagination.Paginable;
import org.prosolo.web.courses.util.pagination.PaginationLink;
import org.prosolo.web.courses.util.pagination.Paginator;
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
	private int notificationNumber;
	private final int limit = 5;
	private int page = 1;
	private int numberOfPages;
	private List<PaginationLink> paginationLinks;
	private List<FilterNotificationType> filters = new ArrayList<>();
	private List<NotificationType> notificationTypes = new ArrayList<>();
	
	@PostConstruct
	public void init() {
		for(NotificationTypeFilter filterEnum : NotificationTypeFilter.values()) {
			FilterNotificationType f = new FilterNotificationType(filterEnum, true);
			filters.add(f);
			notificationTypes.add(filterEnum.getType());
		}
		loadNotifications(true);
	}
	
	public void loadNotifications(boolean recalculateTotalNumber) {
		try {
			if(notificationTypes.isEmpty()) {
				notificationNumber = 0;
				notifications = new ArrayList<>();
			} else {
				/*
				 * if all filters are applied, it is more performant to just not filter notifications
				 * which is achieved by passing null for notification type list
				 */
				List<NotificationType> filterTypes = notificationTypes.size() == filters.size() 
						? null 
						: notificationTypes;
				if(recalculateTotalNumber) {
					notificationNumber = notificationManager.getNumberOfNotificationsForUser(
							loggedUser.getUserId(), filterTypes);
				}
				if(notificationNumber > 0) {
					notifications = notificationManager.getNotificationsForUser(
							loggedUser.getUserId(), 
							page - 1, limit, filterTypes, loggedUser.getLocale());
				} else {
					notifications = new ArrayList<>();
				}
			}
			generatePagination();
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
		page = 1;
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
			page = 1;
			loadNotifications(true);
		}
	}
	
	public void uncheckAllFilters() {
		if(!notificationTypes.isEmpty()) {
			notificationTypes.clear();
			for(FilterNotificationType filter : filters) {
				filter.setApplied(false);
			}
			page = 1;
			loadNotifications(true);
		}
	}
	
	private void generatePagination() {
		//if we don't want to generate all links
		Paginator paginator = new Paginator(notificationNumber, limit, page, 
				1, "...");
		numberOfPages = paginator.getNumberOfPages();
		paginationLinks = paginator.generatePaginationLinks();
		logger.info("Number of pages for user notifications: " + numberOfPages);
	}
	
	//pagination helper methods

	@Override
	public boolean isCurrentPageFirst() {
		return page == 1 || numberOfPages == 0;
	}
	
	@Override
	public boolean isCurrentPageLast() {
		return page == numberOfPages || numberOfPages == 0;
	}
	
	@Override
	public void changePage(int page) {
		if(this.page != page) {
			this.page = page;
			loadNotifications(false);
		}
	}
	
	@Override
	public void goToPreviousPage() {
		changePage(page - 1);
	}
	
	@Override
	public void goToNextPage() {
		changePage(page + 1);
	}
	
	@Override
	public boolean isResultSetEmpty() {
		return notificationNumber == 0;
	}
	
	@Override
	public boolean shouldBeDisplayed() {
		return numberOfPages > 1;
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

	public List<PaginationLink> getPaginationLinks() {
		return paginationLinks;
	}

	public void setPaginationLinks(List<PaginationLink> paginationLinks) {
		this.paginationLinks = paginationLinks;
	}

	public List<FilterNotificationType> getFilters() {
		return filters;
	}

	public void setFilters(List<FilterNotificationType> filters) {
		this.filters = filters;
	}
	
}
