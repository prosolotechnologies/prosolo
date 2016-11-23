package org.prosolo.bigdata.services.credentials;/**
 * Created by zoran on 27/08/16.
 */

import java.util.Date;

import org.prosolo.bigdata.jobs.data.Resource;
import org.quartz.SchedulerException;

/**
 * zoran 27/08/16
 */
public interface VisibilityService {

	void updateVisibilityAtSpecificTime(long resourceId, Resource resource, Date startDate);
	
	void changeVisibilityUpdateTime(long resourceId, Resource resource, Date startDate);
	
	void cancelVisibilityUpdate(long resourceId, Resource res);
	
	boolean visibilityUpdateJobExists(long resourceId, Resource res) throws SchedulerException;
}
