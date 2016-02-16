package org.prosolo.web.competences;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "competenceActivitiesBean")
@Component("competenceActivitiesBean")
@Scope("view")
public class CompetenceActivitiesBean implements Serializable {

	private static final long serialVersionUID = -6145577969728042249L;

	private static Logger logger = Logger.getLogger(CompetenceActivitiesBean.class);

	@Autowired private CompetenceManager competenceManager;
	@Inject private UrlIdEncoder idEncoder;
	
	private String id;
	private String title;
	
	private List<ActivityData> activities;
	private ActivityData activityToEdit;
	private ActivityData activityToEditBackup;
	
	public CompetenceActivitiesBean() {

	}
	
	public void init() {
		long decodedId = idEncoder.decodeId(id);
		if (decodedId > 0) {
			logger.info("Searching activities for competence with id: " + decodedId);
			try {
				if(title == null || "".equals(title)) {
					title = competenceManager.getCompetenceTitle(decodedId);
				}
				activities = competenceManager.getCompetenceActivities(decodedId);
			} catch(DbConnectionException e) {
				logger.error(e);
				PageUtil.fireErrorMessage(e.getMessage());
			}
		} else {
			try {
				FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	/*
	 * GETTERS / SETTERS
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<ActivityData> getActivities() {
		return activities;
	}

	public void setActivities(List<ActivityData> activities) {
		this.activities = activities;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public ActivityData getActivityToEdit() {
		return activityToEdit;
	}

	public void setActivityToEdit(ActivityData activityToEdit) {
		this.activityToEdit = activityToEdit;
	}

	public ActivityData getActivityToEditBackup() {
		return activityToEditBackup;
	}

	public void setActivityToEditBackup(ActivityData activityToEditBackup) {
		this.activityToEditBackup = activityToEditBackup;
	}

}
