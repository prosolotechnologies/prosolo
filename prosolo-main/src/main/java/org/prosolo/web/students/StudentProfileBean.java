package org.prosolo.web.students;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.user.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.SocialNetworkName;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserSocialNetworks;
import org.prosolo.common.domainmodel.user.preferences.TopicPreference;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.PortfolioManager;
import org.prosolo.services.nodes.SocialNetworksManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.portfolio.data.SocialNetworksData;
import org.prosolo.web.students.data.StudentData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@ManagedBean(name = "studentProfileBean")
@Component("studentProfileBean")
@Scope("view")
public class StudentProfileBean implements Serializable {

	private static final long serialVersionUID = -1878016181350581925L;
	
	private static Logger logger = Logger.getLogger(StudentProfileBean.class);
	
	@Inject 
	private ObservationBean observationBean;
	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private UserManager userManager;
	@Inject
	private SocialNetworksManager socialNetworksManager;
	@Inject
	private LoggedUserBean loggedUserBean;
	@Inject
	private PortfolioManager portfolioManager;
	
	private String id;
	private long decodedId;
	
	private StudentData student;
	private SocialNetworksData socialNetworksData;
	
	public void initStudent() {
		decodedId = idEncoder.decodeId(id);
		if (decodedId > 0) {
			try {
				User user = userManager.loadResource(User.class, decodedId, true);
				student = new StudentData(user);
				
				TopicPreference topicPreference = (TopicPreference) userManager.getUserPreferences(user, TopicPreference.class);
				Set<Tag> preferredKeywords = topicPreference.getPreferredKeywords();
				
				student.addInterests(preferredKeywords);
				
				initSocialNetworks();
				
				observationBean.setStudentId(decodedId);
				observationBean.setStudentName(student.getName());
				observationBean.initializeObservationData();
				
				logger.info("User with id "+ 
						loggedUserBean.getUser().getId() + 
						" came to the studentProfile page for student with id " + decodedId);
				
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
				try {
					FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			} catch(DbConnectionException dbce){
				logger.error(dbce);
				PageUtil.fireErrorMessage(dbce.getMessage());
			} catch(Exception ex){
				logger.error(ex);
			}
			
		}
		
	}

	public UserData getMessageReceiverData(){
		UserData ud = new UserData();
		ud.setId(decodedId);
		return ud;
	}
	
	public void initSocialNetworks() {
		if (socialNetworksData == null) {
			logger.debug("Initializing social networks data for user "+decodedId);
			
			User user = new User();
			user.setId(decodedId);
			
			UserSocialNetworks socialNetworks = socialNetworksManager.getSocialNetworks(user);
			
			socialNetworksData = new SocialNetworksData();
			socialNetworksData.setId(socialNetworks.getId());
			
			SocialNetworkAccount twitterAccount = socialNetworks.getAccount(SocialNetworkName.TWITTER);
			
			if (twitterAccount != null) {
				socialNetworksData.setTwitterLink(twitterAccount.getLink());
				socialNetworksData.setTwitterLinkEdit(twitterAccount.getLink());
			}
			
			SocialNetworkAccount facebookAccount = socialNetworks.getAccount(SocialNetworkName.FACEBOOK);
			
			if (facebookAccount != null) {
				socialNetworksData.setFacebookLink(facebookAccount.getLink());
				socialNetworksData.setFacebookLinkEdit(facebookAccount.getLink());
			}
			
			SocialNetworkAccount gplusAccount = socialNetworks.getAccount(SocialNetworkName.GPLUS);
			
			if (gplusAccount != null) {
				socialNetworksData.setGplusLink(gplusAccount.getLink());
				socialNetworksData.setGplusLinkEdit(gplusAccount.getLink());
			}
			
			SocialNetworkAccount blogAccount = socialNetworks.getAccount(SocialNetworkName.BLOG);
			
			if (blogAccount != null) {
				socialNetworksData.setBlogLink(blogAccount.getLink());
				socialNetworksData.setBlogLinkEdit(blogAccount.getLink());
			}
		}
	}
	
	public ObservationBean getObservationBean() {
		return observationBean;
	}

	public void setObservationBean(ObservationBean observationBean) {
		this.observationBean = observationBean;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public StudentData getStudent() {
		return student;
	}

	public void setStudent(StudentData student) {
		this.student = student;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public SocialNetworksData getSocialNetworksData() {
		return socialNetworksData;
	}

	public void setSocialNetworksData(SocialNetworksData socialNetworksData) {
		this.socialNetworksData = socialNetworksData;
	}

	
	
}
