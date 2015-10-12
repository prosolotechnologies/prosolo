package org.prosolo.web.lti;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.lti.LtiToolManager;
import org.prosolo.services.lti.filter.Filter;
import org.prosolo.services.lti.filter.ToolSearchActivityFilter;
import org.prosolo.services.lti.filter.ToolSearchCompetenceFilter;
import org.prosolo.services.lti.filter.ToolSearchCredentialFilter;
import org.prosolo.services.lti.filter.ToolSearchGeneralFilter;
import org.prosolo.services.lti.util.EntityConstants;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "ltitoolsearch")
@Component("ltitoolsearch")
@Scope("view")
public class LTIToolSearchBean implements Serializable{

	private static final long serialVersionUID = 7819429893304270167L;

	private static Logger logger = Logger.getLogger(LTIToolSearchBean.class);
	
	@Inject private LtiToolManager toolManager;
	@Inject private LoggedUserBean user;
	
	private List<LtiTool> tools;
	private List<Filter> filters;
	private Filter filter;
	private String search;
	
	private String resource;
	private long credentialId;
	private long competenceId;
	private long activityId;
	
	
	public List<LtiTool> getTools() {
		return tools;
	}

	public void setTools(List<LtiTool> tools) {
		this.tools = tools;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public long getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(long credentialId) {
		this.credentialId = credentialId;
	}

	public long getCompetenceId() {
		return competenceId;
	}

	public void setCompetenceId(long competenceId) {
		this.competenceId = competenceId;
	}

	public long getActivityId() {
		return activityId;
	}

	public void setActivityId(long activityId) {
		this.activityId = activityId;
	}
	
	

	public void init(){
		filters.add(new ToolSearchGeneralFilter());
		filters.add(new ToolSearchCredentialFilter());
		filters.add(new ToolSearchCompetenceFilter());
		filters.add(new ToolSearchActivityFilter());
		setFilter(getInitialFilter());
        searchTools();
	}
	
	public void searchTools(){
		setTools(toolManager.searchTools(user.getUser().getId(), search, prepareSearchParameters(), filter)); 
	}
	
	public void enableOrDisableTool(LtiTool tool){
		boolean enabled = false;
		if(tool.isEnabled()){
			enabled = false;
		}else{
			enabled = true;
		}
		try{
			toolManager.changeEnabled(tool.getId(), enabled);
			tool.setEnabled(enabled);
		}catch(Exception e){
			PageUtil.fireErrorMessage("Error while saving changes");
		}
	}
	
	public void deleteTool(LtiTool tool){
	    try {
			toolManager.deleteLtiTool(tool.getId());
			tool.setDeleted(true);
		} catch (Exception e) {
			PageUtil.fireErrorMessage("Error while saving changes");
		}
	}
	
	public String goToEditTool(LtiTool tool){
		Flash flash = FacesContext.getCurrentInstance().getExternalContext().getFlash();
		//when redirecting instead of forwarding which JSF does by default this is the way to pass parameters
		flash.put("tool", tool);
		//flash.put("resource", resource);
		//flash.put("credential", credentialId);
		//flash.put("competence", competenceId);
		//flash.put("activity", activityId);
		//test if it can pass f:viewParams with includeViewParams=true
		return "editexternaltool.xhtml?faces-redirect=true&amp;includeViewParams=true";
	}
	
	private Filter getInitialFilter(){
		switch(resource){
			case EntityConstants.RESOURCE_CREDENTIAL: 
				return new ToolSearchCredentialFilter();
			case EntityConstants.RESOURCE_COMPETENCE:
				return new ToolSearchCompetenceFilter();
			case EntityConstants.RESOURCE_ACTIVITY:
				return new ToolSearchActivityFilter();
		    default: 
		    	logger.error("Unsupported parameter");
		    	return null;
		}
	}
	private Map<String, Object> prepareSearchParameters () {
		Map<String, Object> parameters= new HashMap<>();
		parameters.put(EntityConstants.CREDENTIAL_ID, credentialId);
		parameters.put(EntityConstants.COMPETENCE_ID, competenceId);
		parameters.put(EntityConstants.ACTIVITY_ID, activityId);
		return parameters;
	}
	
	
}
