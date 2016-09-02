package org.prosolo.web.courses.credential.announcements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.Announcement;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.AnnouncementManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.AnnouncementData;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.util.pagination.Paginable;
import org.prosolo.web.courses.util.pagination.PaginationLink;
import org.prosolo.web.courses.util.pagination.Paginator;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "announcementBean")
@Component("announcementBean")
@Scope("view")
public class AnnouncementBean implements Serializable, Paginable {

	private static final long serialVersionUID = -4020351250960511686L;

	private static Logger logger = Logger.getLogger(AnnouncementBean.class);

	@Inject
	private LoggedUserBean loggedUser;
	@Inject
	private AnnouncementManager announcementManager;
	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private CredentialManager credManager;
	@Inject
	private ThreadPoolTaskExecutor taskExecutor;
	@Inject
	private EventFactory eventFactory;
	
	//credential related data
	private String credentialId;
	private List<AnnouncementData> credentialAnnouncements = new ArrayList<>();
	private String credentialTitle;
	private boolean credentialMandatoryFlow;
	private String credentialDurationString;

	//new announcement related data
	private String newAnnouncementTitle;
	private String newAnnouncementText;
	private AnnouncementPublishMode newAnouncementPublishMode = AnnouncementPublishMode.ALL_STUDENTS;

	//pagination related data
	private int page = 1;
	private int limit = 10;
	private int numberOfPages;
	private List<PaginationLink> paginationLinks;
	private int totalNumberOfAnnouncements = 0;

	public void init() {
		resetNewAnnouncementValues();
		
		if (StringUtils.isNotBlank(credentialId)) {
			try {
				credentialAnnouncements = announcementManager
						.getAllAnnouncementsForCredential(idEncoder.decodeId(credentialId), page - 1, limit);
				totalNumberOfAnnouncements = announcementManager.numberOfAnnouncementsForCredential(
						idEncoder.decodeId(credentialId));
				CredentialData basicCredentialData = credManager.getBasicCredentialData(idEncoder.decodeId(credentialId), loggedUser.getUserId());
				credentialTitle = basicCredentialData.getTitle();
				credentialMandatoryFlow = basicCredentialData.isMandatoryFlow();
				credentialDurationString = basicCredentialData.getDurationString();
				generatePagination();
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error("Could not initialize list of announcements", e);
				PageUtil.fireErrorMessage("Could not initialize list of announcements");
			}
		} else {
			logger.error("Could not initialize list of announcements, credentialId is null");
			PageUtil.fireErrorMessage("Could not initialize list of announcements");
		}
	}
	
	private void resetNewAnnouncementValues() {
		newAnnouncementTitle = "";
		newAnnouncementText = "";
	}

	public void publishAnnouncement() {
		AnnouncementData created = announcementManager.createAnnouncement(idEncoder.decodeId(credentialId), newAnnouncementTitle, 
				newAnnouncementText, loggedUser.getUserId(), newAnouncementPublishMode);
		
		created.setCreatorAvatarUrl(loggedUser.getAvatar());
		created.setCreatorFullName(loggedUser.getFullName());
		
		//TODO need to get theese parameters
		String page = PageUtil.getPostParameter("page");
		String lContext = PageUtil.getPostParameter("learningContext");
		String service = PageUtil.getPostParameter("service");
		notifyForAnnouncementAsync(idEncoder.decodeId(created.getEncodedId()), page, 
				lContext, service, idEncoder.decodeId(credentialId));
		
		PageUtil.fireSuccessfulInfoMessage("Announcement published");
		init();
	}
	
	public void setPublishMode() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String publishModeValue = params.get("value");
		if(StringUtils.isBlank(publishModeValue)){
			logger.error("User "+loggedUser.getUserId()+" has sent blank publish value");
		}
		else {
			newAnouncementPublishMode = AnnouncementPublishMode.fromString(publishModeValue);
		}
	}
	
	private void notifyForAnnouncementAsync(long announcementId, String page, String lContext, String service,
			long credentialId) {
		taskExecutor.execute(() -> {
			Announcement announcement = new Announcement();
			announcement.setId(announcementId);
			
			try {
				Credential1 cred = credManager.loadResource(Credential1.class, credentialId, true);
				Map<String, String> parameters = new HashMap<>();
				parameters.put("credentialId", credentialId + "");
				parameters.put("publishMode", newAnouncementPublishMode.getText());
				try {
					eventFactory.generateEvent(EventType.AnnouncementPublished, loggedUser.getUserId(), announcement, cred,
							page, lContext, service, parameters);
				} catch (Exception e) {
					logger.error("Eror sending notification for announcement", e);
				}
			} catch (Exception e1) {
				logger.error(e1);
			}
			
		});
	}

	public LoggedUserBean getLoggedUser() {
		return loggedUser;
	}

	public void setLoggedUser(LoggedUserBean loggedUser) {
		this.loggedUser = loggedUser;
	}

	public AnnouncementManager getAnnouncementManager() {
		return announcementManager;
	}

	public void setAnnouncementManager(AnnouncementManager announcementManager) {
		this.announcementManager = announcementManager;
	}

	public String getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(String credentialId) {
		this.credentialId = credentialId;
	}

	public UrlIdEncoder getIdEncoder() {
		return idEncoder;
	}

	public void setIdEncoder(UrlIdEncoder idEncoder) {
		this.idEncoder = idEncoder;
	}

	public List<AnnouncementData> getCredentialAnnouncements() {
		return credentialAnnouncements;
	}

	public void setCredentialAnnouncements(List<AnnouncementData> credentialAnnouncements) {
		this.credentialAnnouncements = credentialAnnouncements;
	}

	public String getNewAnnouncementTitle() {
		return newAnnouncementTitle;
	}

	public void setNewAnnouncementTitle(String newAnnouncementTitle) {
		this.newAnnouncementTitle = newAnnouncementTitle;
	}

	public String getNewAnnouncementText() {
		return newAnnouncementText;
	}

	public void setNewAnnouncementText(String newAnnouncementText) {
		this.newAnnouncementText = newAnnouncementText;
	}

	private void generatePagination() {
		Paginator paginator = new Paginator(totalNumberOfAnnouncements, limit, page, 1, "...");
		numberOfPages = paginator.getNumberOfPages();
		paginationLinks = paginator.generatePaginationLinks();
	}

	@Override
	public boolean isCurrentPageFirst() {
		return page == 1 || numberOfPages == 0;
	}

	@Override
	public boolean isCurrentPageLast() {
		return page == numberOfPages || numberOfPages == 0;
	}

	@Override
	public void changePage(int page) {
		if (this.page != page) {
			this.page = page;
			init();
		}

	}

	@Override
	public void goToPreviousPage() {
		changePage(page - 1);
	}

	@Override
	public void goToNextPage() {
		changePage(page + 1);
	}

	@Override
	public boolean isResultSetEmpty() {
		return totalNumberOfAnnouncements == 0;
	}

	public List<PaginationLink> getPaginationLinks() {
		return paginationLinks;
	}

	public void setPaginationLinks(List<PaginationLink> paginationLinks) {
		this.paginationLinks = paginationLinks;
	}

	public CredentialManager getCredManager() {
		return credManager;
	}

	public void setCredManager(CredentialManager credManager) {
		this.credManager = credManager;
	}

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public void setCredentialTitle(String credentialTitle) {
		this.credentialTitle = credentialTitle;
	}

	public boolean isCredentialMandatoryFlow() {
		return credentialMandatoryFlow;
	}

	public void setCredentialMandatoryFlow(boolean credentialMandatoryFlow) {
		this.credentialMandatoryFlow = credentialMandatoryFlow;
	}

	public String getCredentialDurationString() {
		return credentialDurationString;
	}

	public void setCredentialDurationString(String credentialDurationString) {
		this.credentialDurationString = credentialDurationString;
	}

	public ThreadPoolTaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public EventFactory getEventFactory() {
		return eventFactory;
	}

	public void setEventFactory(EventFactory eventFactory) {
		this.eventFactory = eventFactory;
	}

}
