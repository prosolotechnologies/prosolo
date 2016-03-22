package org.prosolo.web.competences;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.ObjectNotFoundException;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.data.PublishedStatus;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.competences.data.BasicCompetenceData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "competenceOverallBean")
@Component("competenceOverallBean")
@Scope("view")
public class CompetenceOverallBean implements Serializable {

	private static final long serialVersionUID = -1927889372567171188L;

	private static Logger logger = Logger.getLogger(CompetenceOverallBean.class);

	@Inject private LoggedUserBean loggedUser;
	@Inject private TagManager tagManager;
	@Inject private CompetenceManager competenceManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private EventFactory eventFactory;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	private BasicCompetenceData compData;
	private BasicCompetenceData compDataBackup;
	
	private String id;
	
	private PublishedStatus[] statusArray;
	
	private int[] validityOptions = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
	private int[] durationOptions = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
	
	public CompetenceOverallBean() {

	}
	
	public void init() {
		statusArray = PublishedStatus.values();
		long decodedId = idEncoder.decodeId(id);
		boolean notFound = false;
		if (decodedId > 0) {
			logger.info("INIT COMPETENCE ID:"+id);
			try {
				Competence competence = competenceManager.loadResource(Competence.class, decodedId);
				compData = new BasicCompetenceData(competence);
				compDataBackup = BasicCompetenceData.copyBasicCompetenceData(compData);
			} catch(ObjectNotFoundException onf) {
				logger.error(onf);
				notFound = true;
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		} else {
			notFound = true;
		}
		
		if(notFound) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	public void updateCompetence() {
		try {
			compData.setPublishedStatus();
			Competence comp = competenceManager.updateCompetence(compData.getId(), compData.getTitle(), compData.getDescription(),
					compData.getDuration(), compData.getValidity(), compData.isPublished(),
					new HashSet<Tag>(tagManager.parseCSVTagsAndSave(compData.getTagsString())));
			
			compDataBackup = BasicCompetenceData.copyBasicCompetenceData(compData);
			PageUtil.fireSuccessfulInfoMessage("Competence is saved");
			
			taskExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						eventFactory.generateEvent(EventType.Edit, loggedUser.getUser(), comp);
					} catch (EventException e1) {
						logger.error(e1);
					}
				}
			});
		} catch(DbConnectionException e) {
			compData = BasicCompetenceData.copyBasicCompetenceData(compDataBackup);
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
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

	public BasicCompetenceData getCompData() {
		return compData;
	}

	public void setCompData(BasicCompetenceData competenceData) {
		this.compData = competenceData;
	}

	public int[] getValidityOptions() {
		return validityOptions;
	}

	public void setValidityOptions(int[] validityOptions) {
		this.validityOptions = validityOptions;
	}

	public int[] getDurationOptions() {
		return durationOptions;
	}

	public void setDurationOptions(int[] durationOptions) {
		this.durationOptions = durationOptions;
	}

	public PublishedStatus[] getStatusArray() {
		return statusArray;
	}

	public void setStatusArray(PublishedStatus[] statusArray) {
		this.statusArray = statusArray;
	}
	
}
