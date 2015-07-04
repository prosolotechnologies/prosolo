package org.prosolo.services.activityreport;

import org.prosolo.services.general.AbstractManager;

import com.mongodb.DBObject;

/**
 * Defines an interface for generating of Prosolo activity reports.
 * 
 * @author vita
 */
public interface ActivityExportManager extends AbstractManager {
	
	final int REPORTING_PERIOD_DAYS = 7;
	
	void runActivityExport();

	String exportCompleteLog(DBObject filterQuery);
}
