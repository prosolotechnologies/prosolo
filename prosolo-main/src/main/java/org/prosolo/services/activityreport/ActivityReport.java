package org.prosolo.services.activityreport;

//import static org.prosolo.services.activityreport.ActivityExportManager.REPORTING_PERIOD_DAYS;
import static org.prosolo.services.activityreport.ArchiveExportUtil.createArchivePath;
import static org.prosolo.services.activityreport.ArchiveExportUtil.formatDateForm;
import static org.prosolo.util.urigenerator.AmazonS3Utility.createFullPathFromRelativePath;

import java.util.Date;

//import org.elasticsearch.common.joda.time.LocalDate;

public class ActivityReport {

	private Date from;

	private Date to;

	private Long userId;

	public ActivityReport(Date from, Long userId) {
		super();
		this.from = from;
	//	this.to = new LocalDate(from).plusDays(REPORTING_PERIOD_DAYS-1).toDate(); // should be 1 day less as we substracted 1 millisecond when we started generating reports
		this.userId = userId;
	}

	public String getFrom() {
		return formatDateForm(from);
	}

	public String getTo() {
		return formatDateForm(to);
	}

	public Long getUserId() {
		return userId;
	}
	
	public String getURL() {
		return createFullPathFromRelativePath(createArchivePath(userId, from, to));
	}
}
