/**
 * 
 */
package org.prosolo.web.lti;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.CompetenceActivity;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseCompetence;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.ResourceType;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.lti.LtiToolManager;
import org.prosolo.services.lti.filter.ToolSearchActivityFilter;
import org.prosolo.services.lti.filter.ToolSearchCompetenceFilter;
import org.prosolo.services.lti.filter.ToolSearchCredentialFilter;
import org.prosolo.services.lti.util.EntityConstants;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.data.CourseData;
import org.prosolo.web.lti.data.ExternalToolData;
import org.prosolo.web.lti.data.ExternalToolFilterData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 * 
 */
@ManagedBean(name = "manageExternalToolsBean")
@Component("manageExternalToolsBean")
@Scope("view")
public class ManageExternalToolsBean implements Serializable {

	private static final long serialVersionUID = 6383363883663936346L;

	private static Logger logger = Logger.getLogger(ManageExternalToolsBean.class);
	
	@Autowired private DefaultManager defaultManager;
	@Inject private LtiToolManager toolManager;
	@Inject private LoggedUserBean userBean;
	@Inject private UrlIdEncoder idEncoder;

	private String cred;
	private String comp;
	private String act;
	
	private long decodedCred;
	private long decodedComp;
	private long decodedAct;
	
	private long origin;
	
	private CourseData courseData;
	private List<ExternalToolFilterData> resourceFilter;
	private ExternalToolFilterData selectedFilter;
	
	private long currentCompetence;
	private long currentActivity;
	
	private List<ExternalToolData> externalTools;
	
	public void init() {
		logger.info("User with email "+userBean.getUser().getEmail().getAddress()+" redirected the page manage/tools.xhtml");
		decodedCred = decodeId(cred);
		decodedComp = decodeId(comp);
		decodedAct = decodeId(act);
		
		if (decodedCred > 0) {
			if (decodedAct > 0){
				origin = decodedAct;
			}else{
				if(decodedComp > 0){
					origin = decodedComp;
				}else{
					origin = decodedCred;
				}
			}
			try {
				Course course = defaultManager.loadResource(Course.class, decodedCred);

				courseData = new CourseData(course);
				
				resourceFilter = new LinkedList<ExternalToolFilterData>();
				
				ExternalToolFilterData noFilterItem = new ExternalToolFilterData(decodedCred, "------------------", -1, ResourceType.Credential, new ToolSearchCredentialFilter());
				setOriginFilter(noFilterItem);
				resourceFilter.add(noFilterItem);
				
				for (CourseCompetence courseCompetences : course.getCompetences()) {
					Competence comp = courseCompetences.getCompetence();
					
					ExternalToolFilterData compItem = new ExternalToolFilterData(comp.getId(), comp.getTitle(), courseData.getId(), ResourceType.Competence, new ToolSearchCompetenceFilter());
					if(selectedFilter == null){
						setOriginFilter(compItem);
					}
					for (CompetenceActivity compActivity : comp.getActivities()) {
						Activity activity = compActivity.getActivity();
						
						ExternalToolFilterData activityItem = new ExternalToolFilterData(activity.getId(), activity.getTitle(), comp.getId(), ResourceType.Activity, new ToolSearchActivityFilter());
						if(selectedFilter == null){
							setOriginFilter(activityItem);
						}
						compItem.addChild(activityItem);
					}
					
					resourceFilter.add(compItem);
				}
				
				if(selectedFilter == null){
					resourceFilter = new LinkedList<>();
					PageUtil.fireErrorMessage("That resource does not exist");
				}else{
					loadData();
				}
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		} else {
		}
	}
	
	public void selectFilter(ExternalToolFilterData selectedFilter) {
		setSelectedFilter(selectedFilter);
		loadData();
	}
	
	private void setOriginFilter(ExternalToolFilterData filter){
		if(filter.getId() == origin){
			setSelectedFilter(filter);
		}
	}
	
	public void setSelectedFilter (ExternalToolFilterData filter){
		this.selectedFilter = filter;
		if(selectedFilter.getResType().equals(ResourceType.Activity)){
			currentActivity = selectedFilter.getId();
			currentCompetence = selectedFilter.getParentId();
		}else{
			if(selectedFilter.getResType().equals(ResourceType.Competence)){
				currentCompetence = selectedFilter.getId();
				currentActivity = 0;
			}else{
				currentCompetence = 0;
				currentActivity = 0;
			}
		}
	}
	
	
	public void loadData() {
		Map <String, Object> params = prepareSearchParameters();
		List<LtiTool> tools = toolManager.searchTools(userBean.getUser().getId(), params, selectedFilter.getFilter());
		externalTools = new LinkedList<>();
		for(LtiTool t : tools){
			externalTools.add(new ExternalToolData(t));
		}
	}
	
	public String setEnabledButton(ExternalToolData tool){
		if(tool.isEnabled()){
			return "Disable";
		}
		return "Enable";
		
	}
	
	private Map<String, Object> prepareSearchParameters () {
		Map<String, Object> parameters= new HashMap<>();
		parameters.put(EntityConstants.CREDENTIAL_ID, decodedCred);
		switch(selectedFilter.getResType()){
			case Credential:
				break;
			case Competence:
				parameters.put(EntityConstants.COMPETENCE_ID, selectedFilter.getId());
				break;
			case Activity:
				parameters.put(EntityConstants.ACTIVITY_ID, selectedFilter.getId());
				parameters.put(EntityConstants.COMPETENCE_ID, selectedFilter.getParentId());
				break;
		}
		return parameters;
	}
	
	public void changeEnabled(ExternalToolData tool){
		try {
			boolean enabled = !tool.isEnabled();
			toolManager.changeEnabled(tool.getId(), enabled);
			tool.setEnabled(enabled);
			logger.info("LTI tool enabled status changed");
		} catch (Exception e) {
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	public void deleteTool(ExternalToolData tool){
		try {
			toolManager.deleteLtiTool(tool.getId());
			externalTools.remove(tool);
			logger.info("LTI tool deleted");
			//loadData();
		} catch (Exception e) {
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	public long decodeId(String encodedId){
		long decodedId = 0;
		if(encodedId != null){
			decodedId = idEncoder.decodeId(encodedId);
		}
		return decodedId;
	}
	
	public String encodeId(long id){
		return idEncoder.encodeId(id);
	}
	
	/*
	 * PARAMETERS
	 */
	
	public String getCred() {
		return cred;
	}
	public void setCred(String id) {
		this.cred = id;
	}
	
	public String getComp() {
		return comp;
	}

	public void setComp(String comp) {
		this.comp = comp;
	}
	
	public String getAct() {
		return act;
	}

	public void setAct(String act) {
		this.act = act;
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public List<ExternalToolFilterData> getResourceFilter() {
		return resourceFilter;
	}

	public ExternalToolFilterData getSelectedFilter() {
		return selectedFilter;
	}
	
	public List<ExternalToolData> getExternalTools() {
		return externalTools;
	}

	public long getCurrentCompetence() {
		return currentCompetence;
	}

	public void setCurrentCompetence(long currentCompetence) {
		this.currentCompetence = currentCompetence;
	}

	public long getCurrentActivity() {
		return currentActivity;
	}

	public void setCurrentActivity(long currentActivity) {
		this.currentActivity = currentActivity;
	}
	
	
}
