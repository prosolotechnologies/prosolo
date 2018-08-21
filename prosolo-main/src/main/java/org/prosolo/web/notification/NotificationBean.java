package org.prosolo.web.notification;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.user.notifications.NotificationSection;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationData;
import org.prosolo.services.notifications.factory.NotificationSectionDataFactory;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.notification.data.NotificationFilter;
import org.prosolo.web.notification.data.NotificationFilterType;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@ManagedBean(name = "notificationBean")
@Component("notificationBean")
@Scope("view")
public class NotificationBean implements Serializable, Paginable {
	
	private static final long serialVersionUID = 8874333324943561974L;

	private static Logger logger = Logger.getLogger(NotificationBean.class);

	private String domainPrefix = CommonSettings.getInstance().config.appConfig.domain.substring(0, CommonSettings.getInstance().config.appConfig.domain.length()-1);

	@Inject private LoggedUserBean loggedUser;
	@Inject private NotificationManager notificationManager;
	@Inject private NotificationSectionDataFactory notificationSectionDataFactory;

	private List<NotificationData> notifications = new ArrayList<>();
	private PaginationData paginationData = new PaginationData(5);
	private List<NotificationFilter> filters;

	public void init() {
		filters = new LinkedList<>();
		for (NotificationFilterType notificationFilterType : NotificationFilterType.values()) {
			filters.add(new NotificationFilter(notificationFilterType, true));
		}

		filters.sort(Comparator.comparing(f -> f.getType().getLabel()));

		String selectedFiltersParam = PageUtil.getGetParameter("filters");
		if (selectedFiltersParam != null && !selectedFiltersParam.isEmpty()) {
			List<String> selectedFilters = Arrays.stream(selectedFiltersParam.split(","))
					.map(String::toLowerCase)
					.collect(Collectors.toList());

			filters.stream()
					.filter(f -> !selectedFilters.contains((f.getType().getNotificationType().name().toLowerCase())))
					.forEach(f -> f.setApplied(false));
		}

		int page = PageUtil.getGetParameterAsInteger("p");
		this.paginationData.setPage(page == 0 ? 1 : page);

		loadNotifications(true);
	}
	
	public void loadNotifications(boolean recalculateTotalNumber) {
		try {
			if (filters.stream().noneMatch(NotificationFilter::isApplied)) {
				paginationData.update(0);
				notifications = new ArrayList<>();
			} else {
				/*
				 * if all filters are applied, it is more performant to just not filter notifications
				 * which is achieved by passing null for notification type list
				 */
				List<NotificationType> selectedFilters = filters.stream().allMatch(NotificationFilter::isApplied)
						? null
						: filters.stream()
							.filter(NotificationFilter::isApplied)
							.map(NotificationFilter::getType)
							.map(NotificationFilterType::getNotificationType)
							.collect(Collectors.toList());

				NotificationSection section = notificationSectionDataFactory.getSection(PageUtil.getSectionForView());
				
				paginationData.update(notificationManager.getNumberOfNotificationsForUser(
						loggedUser.getUserId(), selectedFilters, section));
				
				if (paginationData.getNumberOfResults() > 0) {
					notifications = notificationManager.getNotificationsForUser(
							loggedUser.getUserId(),
							paginationData.getPage() - 1,
							paginationData.getLimit(),
							selectedFilters,
							loggedUser.getLocale(), section);
				} else {
					notifications = new ArrayList<>();
				}
			}
		} catch (DbConnectionException e) {
			logger.error(e);
		}
	}
	
	public void filterChanged(NotificationFilter filter) {
		filters.stream().filter(f -> f.getType().equals(filter.getType())).forEach(f -> f.setApplied(filter.isApplied()));

		paginationData.setPage(1);
		loadNotifications(true);
	}
	
	public void checkAllFilters() {
		filters.stream().forEach(f -> f.setApplied(true));
		paginationData.setPage(1);
		loadNotifications(true);
	}
	
	public void uncheckAllFilters() {
		filters.stream().forEach(f -> f.setApplied(false));
		paginationData.setPage(1);
		loadNotifications(true);
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

	public List<NotificationFilter> getFilters() {
		return filters;
	}

	public String getDomainPrefix() {
		return domainPrefix;
	}
}
