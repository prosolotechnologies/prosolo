package org.prosolo.web.dialogs;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "activityListDialog")
@Component("activityListDialog")
@Scope("view")
public class ActivityListDialog implements Serializable {

	private static final long serialVersionUID = -2924247677092387785L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ActivityListDialog.class);
	
	@Autowired private LoggingNavigationBean actionLogger;
	
	private boolean editable;
	private List<ActivityWallData> activities;
	private String context;
	
	public void init(boolean editable, List<ActivityWallData> activities, long externalCreditId, String context) {
		this.editable = editable;
		this.activities = activities;
		this.context = context;
		
		actionLogger.logServiceUse(
				ComponentName.ACTIVITY_LIST_DIALOG, 
				"context", context,
				"exCreditId", String.valueOf(externalCreditId));
	}

	/*
	 * GETTERS / SETTERS
	 */
	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public List<ActivityWallData> getActivities() {
		return activities;
	}

	public void setActivities(List<ActivityWallData> activities) {
		this.activities = activities;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
	
}
