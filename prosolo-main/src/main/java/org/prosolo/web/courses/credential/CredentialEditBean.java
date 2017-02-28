package org.prosolo.web.courses.credential;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.CompetenceEmptyException;
import org.prosolo.bigdata.common.exceptions.CredentialEmptyException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.CompetenceTextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.logging.LoggingService;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.PublishedStatus;
import org.prosolo.services.nodes.data.Role;
import org.prosolo.services.nodes.factory.CredentialCloneFactory;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.search.data.SortingOption;
import org.prosolo.web.util.page.PageSection;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialEditBean")
@Component("credentialEditBean")
@Scope("view")
public class CredentialEditBean implements Serializable {

	private static final long serialVersionUID = 3430513767875001534L;

	private static Logger logger = Logger.getLogger(CredentialEditBean.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private CredentialManager credentialManager;
	@Inject private CredentialCloneFactory credentialCloneFactory;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CompetenceTextSearch compTextSearch;
	@Inject private Activity1Manager activityManager;
	@Inject private LoggingService loggingService;
	@Inject private ContextJsonParserService contextParser;
	@Inject private CredentialVisibilityBean visibilityBean;
	@Inject private ThreadPoolTaskExecutor taskExecutor;
	@Inject private EventFactory eventFactory;
	
	private String id;
	private long decodedId;
	
	private CredentialData credentialData;
	private List<CompetenceData1> compsToRemove;
	private List<CompetenceData1> compSearchResults;
	private String compSearchTerm;
	private List<Long> compsToExcludeFromSearch;
	private int currentNumberOfComps;
	private int competenceForRemovalIndex;
	
	private PublishedStatus[] courseStatusArray;
	
	//private Role role;

	private boolean manageSection;

	private String context;
	
	public void init() {
		initializeValues();
		manageSection = PageSection.MANAGE.equals(PageUtil.getSectionForView());
		
		if (id == null) {
			credentialData = new CredentialData(false);
		} else {
			try {
				decodedId = idEncoder.decodeId(id);
				setContext();
				logger.info("Editing credential with id " + decodedId);

				loadCredentialData(decodedId);
			} catch(Exception e) {
				logger.error(e);
				e.printStackTrace();
				credentialData = new CredentialData(false);
				PageUtil.fireErrorMessage("Error while trying to load credential data");
			}
		}
		initializeCredentialStatusArray();
	}
	
	public void initVisibilityManageData() {
		visibilityBean.init(decodedId, credentialData.getCreator(), manageSection);
	}
	
	private void setContext() {
		if(decodedId > 0) {
			context = "name:CREDENTIAL|id:" + decodedId;
		}
	}

	private void loadCredentialData(long id) {
		try {
			credentialData = credentialManager.getCredentialData(id, true, true, loggedUser.getUserId(), 
					UserGroupPrivilege.Edit);
			if(!credentialData.isCanAccess()) {
				try {
					FacesContext.getCurrentInstance().getExternalContext().dispatch("/accessDenied.xhtml");
				} catch (IOException e) {
					logger.error(e);
				}
			} else {
//				if(manageSection) {
//					role = Role.Manager;
//				} else {
//					role = Role.User;
//				}
				
				List<CompetenceData1> comps = credentialData.getCompetences();
				for(CompetenceData1 cd : comps) {
					compsToExcludeFromSearch.add(cd.getCompetenceId());
				}
				currentNumberOfComps = comps.size();
				
				logger.info("Loaded credential data for credential with id "+ id);
			}
		} catch(ResourceNotFoundException rnfe) {
			credentialData = new CredentialData(false);
			PageUtil.fireErrorMessage("Credential can not be found");
		}
	}
	
	public void loadCompetenceActivitiesIfNotLoaded(CompetenceData1 cd) {
		if(!cd.isActivitiesInitialized()) {
			List<ActivityData> activities = new ArrayList<>();
			activities = activityManager.getCompetenceActivitiesData(cd.getCompetenceId(), true);
			cd.setActivities(activities);
			cd.setActivitiesInitialized(true);
		}
	}

	private void initializeValues() {
		compsToRemove = new ArrayList<>();
		compsToExcludeFromSearch = new ArrayList<>();
	}

	private void initializeCredentialStatusArray() {
		courseStatusArray = Arrays.stream(PublishedStatus.values()).filter(
				s -> shouldIncludeStatus(s))
				.toArray(PublishedStatus[]::new);
	}
	
	private boolean shouldIncludeStatus(PublishedStatus s) {
		boolean published = credentialData.isPublished();
		if(published && s == PublishedStatus.SCHEDULED_PUBLISH 
				|| !published && s == PublishedStatus.SCHEDULED_UNPUBLISH) {
			return false;
		}
		return true;
	}

	public boolean hasMoreCompetences(int index) {
		return credentialData.getCompetences().size() != index + 1;
	}
	
	public boolean isCompetenceCreator(CompetenceData1 comp) {
		return comp.getCreator() == null ? false : 
			comp.getCreator().getId() == loggedUser.getUserId();
	}
	
	/*
	 * ACTIONS
	 */
	
	public void saveAndNavigateToCreateCompetence() {
		boolean saved = saveCredentialData(false);
		if(saved) {
			ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
			try {
				/*
				 * this will not work if there are multiple levels of directories in current view path
				 * example: /credentials/create-credential will return /credentials as a section but this
				 * may not be what we really want.
				 */
				extContext.redirect(extContext.getRequestContextPath() + PageUtil.getSectionForView().getPrefix() +
						"/competences/new?credId=" + id);
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	public void preview() {
		saveCredentialData(true);
	}
	
	public void save() {
		saveCredentialData(true);
	}
	
	public boolean saveCredentialData(boolean reloadData) {
		try {
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			String learningContext = context;
			if(lContext != null && !lContext.isEmpty()) {
				learningContext = contextParser.addSubContext(context, lContext);
			}
			LearningContextData lcd = new LearningContextData(page, learningContext, service);
			
			if(credentialData.getId() > 0) {
				credentialData.getCompetences().addAll(compsToRemove);
				if(credentialData.hasObjectChanged()) {
					credentialManager.updateCredential(credentialData, 
							loggedUser.getUserId(), lcd);
				}
			} else {
				Credential1 cred = credentialManager.saveNewCredential(credentialData,
						loggedUser.getUserId(), lcd);
				credentialData.setId(cred.getId());
				decodedId = credentialData.getId();
				id = idEncoder.encodeId(decodedId);
				credentialData.startObservingChanges();
				setContext();
			}
			if(reloadData && credentialData.hasObjectChanged()) {
				initializeValues();
				loadCredentialData(decodedId);
				initializeCredentialStatusArray();
			}
			PageUtil.fireSuccessfulInfoMessage("Changes are saved");
			return true;
		} catch(DbConnectionException | CredentialEmptyException | CompetenceEmptyException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
			return false;
		}
	}
	
	public void cancelScheduledUpdate() {
		String page = PageUtil.getPostParameter("page");
		String lContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
		String learningContext = context;
		if(lContext != null && !lContext.isEmpty()) {
			learningContext = contextParser.addSubContext(context, lContext);
		}
		Credential1 cr = new Credential1();
		cr.setId(decodedId);
		try {
			eventFactory.generateEvent(EventType.CANCEL_SCHEDULED_VISIBILITY_UPDATE, 
					loggedUser.getUserId(), cr, null, page, learningContext, 
					service, null);
			credentialData.setScheduledPublishDate(null);
			credentialData.setCredentialStatus();
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
	
	public void delete() {
		try {
			if(credentialData.getId() > 0) {
				/*
				 * decoded id is passed because we want to pass id of original version
				 * and not draft
				 */
				credentialManager.deleteCredential(decodedId, loggedUser.getUserId());
				credentialData = new CredentialData(false);
				PageUtil.fireSuccessfulInfoMessage("Changes are saved");
			} else {
				PageUtil.fireErrorMessage("Credential is not saved so it can't be deleted");
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	public void duplicate() {

		Credential1 cred = credentialCloneFactory.clone(credentialData.getId());
		
		// if we enable Duplicate button for the regular user (and not just Manager), path should be changed
		PageUtil.redirect(CommonSettings.getInstance().config.appConfig.domain + "manage/credentials/" + idEncoder.encodeId(cred.getId()) + "/edit");
		
		String page = PageUtil.getPostParameter("page");
		String lContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");

		taskExecutor.execute(() -> {
			try {
				String learningContext = context;
				if (lContext != null && !lContext.isEmpty()) {
					learningContext = contextParser.addSubContext(context, lContext);
				}
				LearningContextData lcd = new LearningContextData(page, learningContext, service);
				
        		HashMap<String, String> parameters = new HashMap<String, String>();
        		parameters.put("duplicate", "true");
        		
        		eventFactory.generateEvent(EventType.Create, loggedUser.getUserId(), cred, null, lcd.getPage(), 
        				lcd.getLearningContext(), lcd.getService(), parameters);
        	} catch (EventException e) {
        		logger.error(e);
        	}
		});
	}
	
	public void searchCompetences() {
		compSearchResults = new ArrayList<>();
		if(compSearchTerm != null && !compSearchTerm.isEmpty()) {
			int size = compsToExcludeFromSearch.size();
			long [] toExclude = new long[size];
			for(int i = 0; i < size; i++) {
				toExclude[i] = compsToExcludeFromSearch.get(i);
			}
			Role role = manageSection ? Role.Manager : Role.User;
			TextSearchResponse1<CompetenceData1> searchResponse = compTextSearch.searchCompetences(
					loggedUser.getUserId(),
					role,
					compSearchTerm,
					0, 
					1000,
					false,
					toExclude,
					null,
					SortingOption.ASC);
			
			List<CompetenceData1> comps = searchResponse.getFoundNodes();
			if(comps != null) {
				compSearchResults = comps;
			}
			
			String page = FacesContext.getCurrentInstance().getViewRoot().getViewId();
			LearningContextData lcd = new LearningContextData(page, context, null);
			Map<String, String> params = new HashMap<>();
			params.put("query", compSearchTerm);
			try {
				loggingService.logServiceUse(loggedUser.getUserId(), 
						ComponentName.SEARCH_COMPETENCES, 
						params, loggedUser.getIpAddress(), lcd);
			} catch(Exception e) {
				logger.error(e);
			}
		} 
	}
	
	public void addComp(CompetenceData1 compData) {
		List<CompetenceData1> competences = credentialData.getCompetences();
		CompetenceData1 removedComp = getCompetenceIfPreviouslyRemoved(compData);
		CompetenceData1 compToEdit = removedComp != null ? removedComp : compData;
		compToEdit.setOrder(competences.size() + 1);
		competences.add(compToEdit);
		credentialData.setDuration(compToEdit.getDuration() + compToEdit.getDuration());
		if(removedComp != null) {
			removedComp.statusBackFromRemovedTransition();
		} else {
			compData.setObjectStatus(ObjectStatus.CREATED);
			compData.startObservingChanges();
		}
		compsToExcludeFromSearch.add(compToEdit.getCompetenceId());
		currentNumberOfComps ++;
		compSearchResults = new ArrayList<>();
	}
	
	private CompetenceData1 getCompetenceIfPreviouslyRemoved(CompetenceData1 compData) {
		Iterator<CompetenceData1> iter = compsToRemove.iterator();
		while(iter.hasNext()) {
			CompetenceData1 cd = iter.next();
			if(cd.getCompetenceId() == compData.getCompetenceId()) {
				iter.remove();
				return cd;
			}
		}
		return null;
	}

	public void moveDown(int index) {
		moveComp(index, index + 1);
	}
	
	public void moveUp(int index) {
		moveComp(index - 1, index);
	}
	
	public void moveComp(int i, int k) {
		List<CompetenceData1> competences = credentialData.getCompetences();
		CompetenceData1 cd1 = competences.get(i);
		cd1.setOrder(cd1.getOrder() + 1);
		cd1.statusChangeTransitionBasedOnOrderChange();
		CompetenceData1 cd2 = competences.get(k);
		cd2.setOrder(cd2.getOrder() - 1);
		cd2.statusChangeTransitionBasedOnOrderChange();
		Collections.swap(competences, i, k);
	}
	
	public void removeComp() {
		removeComp(competenceForRemovalIndex);
	}
	
	public void removeComp(int index) {
		CompetenceData1 cd = credentialData.getCompetences().remove(index);
		credentialData.setDuration(credentialData.getDuration() - cd.getDuration());
		cd.statusRemoveTransition();
		if(cd.getObjectStatus() == ObjectStatus.REMOVED) {
			compsToRemove.add(cd);
		}
		currentNumberOfComps--;
		long compId = cd.getCompetenceId();
		removeIdFromExcludeList(compId);
		shiftOrderOfCompetencesUp(index);
	}
	
	private void shiftOrderOfCompetencesUp(int index) {
		List<CompetenceData1> competences = credentialData.getCompetences();
		for(int i = index; i < currentNumberOfComps; i++) {
			CompetenceData1 comp = competences.get(i);
			comp.setOrder(comp.getOrder() - 1);
			comp.statusChangeTransitionBasedOnOrderChange();
		}
	}

	private void removeIdFromExcludeList(long compId) {
		Iterator<Long> iterator = compsToExcludeFromSearch.iterator();
		while(iterator.hasNext()) {
			long id = iterator.next();
			if(id == compId) {
				iterator.remove();
				return;
			}
		}
	}
	
//	public void listener(AjaxBehaviorEvent event) {
//	     System.out.println("listener");
//	     System.out.println(credentialData.isMandatoryFlow());
//	}
	 
	public String getPageHeaderTitle() {
		return credentialData.getId() > 0 ? credentialData.getTitle() : "New Credential";
	}
	
	public boolean isCreateUseCase() {
		return credentialData.getId() == 0;
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public PublishedStatus[] getCourseStatusArray() {
		return courseStatusArray;
	}

	public void setCourseStatusArray(PublishedStatus[] courseStatusArray) {
		this.courseStatusArray = courseStatusArray;
	}

	public CredentialData getCredentialData() {
		return credentialData;
	}

	public void setCredentialData(CredentialData credentialData) {
		this.credentialData = credentialData;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<CompetenceData1> getCompSearchResults() {
		return compSearchResults;
	}

	public void setCompSearchResults(List<CompetenceData1> compSearchResults) {
		this.compSearchResults = compSearchResults;
	}

	public String getCompSearchTerm() {
		return compSearchTerm;
	}

	public void setCompSearchTerm(String compSearchTerm) {
		this.compSearchTerm = compSearchTerm;
	}

	public int getCurrentNumberOfComps() {
		return currentNumberOfComps;
	}

	public void setCurrentNumberOfComps(int currentNumberOfComps) {
		this.currentNumberOfComps = currentNumberOfComps;
	}
	
	public int getCompetenceForRemovalIndex() {
		return competenceForRemovalIndex;
	}

	public void setCompetenceForRemovalIndex(int competenceForRemovalIndex) {
		this.competenceForRemovalIndex = competenceForRemovalIndex;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}
	
}
