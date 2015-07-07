package org.prosolo.web.dialogs;

import java.io.Serializable;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.ResourceActivity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.goals.util.CompWallActivityConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="activityDetailsBean")
@Component("activityDetailsBean")
@Scope("view")
public class ActivityDetailsBean implements Serializable {
	
	private static final long serialVersionUID = 2696661825112424544L;
	
	private static Logger logger = Logger.getLogger(ActivityDetailsBean.class);

	@Autowired private LoggedUserBean loggedUser; 
	@Autowired private ActivityManager activityManager;
	@Autowired private CompWallActivityConverter compWallActivityConverter;
	
	private Activity activity;
	private ActivityWallData activityData;
	private User maker;
	
	private String targetCompIdToPreselectWhenAdding;
	private String toUpdateAfterAddingToCompetence;
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}

	public String getResourceLink() {
		if (activity != null &&
				(activity instanceof ResourceActivity) &&
				((ResourceActivity) activity).getRichContent() != null) {
			return ((ResourceActivity) this.activity).getRichContent().getLink();
		}
		return null;
	}

	public void openActivityDetailsById(long id) {
		try {
			Activity targetActivity = activityManager.loadResource(Activity.class, id, true);
			initData(targetActivity);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void openActivityDetailsByTargetActivityId(long id) {
		try {
			TargetActivity act = activityManager.loadResource(TargetActivity.class, id, true);
			initData(act.getActivity());
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void openActivityDetails(Activity act) {
		act = activityManager.merge(act);
		initData(act);
	}
	
	public void openActivityDetailsForTargetActivity(TargetActivity targetActivity) {
		targetActivity = activityManager.merge(targetActivity);
		initData(targetActivity.getActivity());
	}
	
	public void initData(Activity activity) {
		logger.debug("openActivityDetails for activity:"+activity.getTitle());

		setActivity(activity);
		
		Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
		this.activityData = compWallActivityConverter.convertActivityToActivityWallData(activity, loggedUser.getUser(), locale, true, false); 
		setMaker(activity.getMaker());
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public Activity getActivity() {
		return activity;
	}
	
	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public User getMaker() {
		return maker;
	}

	public void setMaker(User maker) {
		this.maker = maker;
	}

	public ActivityWallData getActivityData() {
		return activityData;
	}

	public String getTargetCompIdToPreselectWhenAdding() {
		return targetCompIdToPreselectWhenAdding;
	}

	public void setTargetCompIdToPreselectWhenAdding(
			String targetCompIdToPreselectWhenAdding) {
		this.targetCompIdToPreselectWhenAdding = targetCompIdToPreselectWhenAdding;
	}

	public String getToUpdateAfterAddingToCompetence() {
		return toUpdateAfterAddingToCompetence;
	}

	public void setToUpdateAfterAddingToCompetence(
			String toUpdateAfterAddingToCompetence) {
		this.toUpdateAfterAddingToCompetence = toUpdateAfterAddingToCompetence;
	}
	
}
