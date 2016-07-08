package org.prosolo.services.activityreport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.prosolo.services.logging.LoggingService;
import org.prosolo.web.LoggedUserBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Bean which is associated with a calendar component and shows days with
 * generated activity reports. On click, the download starts immediately. 
 * 
 * @author vita
 */
//@ManagedBean(name = "activityReportArchiveBean")
//@Component("activityReportArchiveBean")
//@Scope("view")

	@Deprecated
public class ActivityReportArchiveBean implements Serializable {

	private static final long serialVersionUID = -403990870683147690L;

	protected static Logger logger = Logger
			.getLogger(ActivityReportArchiveBean.class);

	@Autowired private LoggedUserBean loggedUser;

	@Autowired private LoggingService loggingService;

	private List<ActivityReport> reports;
	
	public List<ActivityReport> getAll() {
		if(reports == null) {
			reports = new ArrayList<ActivityReport>();

			DateTime end = new DateTime();
			DateTime start = end.minusYears(2);
			Long userId = loggedUser.getUserId();
			if(userId == 1)
				userId = -1l;// for system user use report which was generated for all user (userId=-1)
			
		//	List<Date> reportDates = loggingService.getReportDays(start.toDate(), end.toDate(), userId);
			
		//	for(Date reportDay: reportDates)
			//	reports.add(new ActivityReport(reportDay, userId));
		}
		return reports;
	}

	public int getSize() {
		return getAll().size();
	}
	
	public List<ActivityReport> getReports() {
		return reports;
	}
}