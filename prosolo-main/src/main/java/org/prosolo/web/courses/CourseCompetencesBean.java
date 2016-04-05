package org.prosolo.web.courses;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.course.CourseCompetence;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.data.CompetenceData;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.ObjectStatusTransitions;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.courses.data.CredentialStructure;
import org.prosolo.web.search.data.SortingOption;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "courseCompetencesBean")
@Component("courseCompetencesBean")
@Scope("view")
public class CourseCompetencesBean implements Serializable {

	private static final long serialVersionUID = -3403062463674613642L;
	
	private static Logger logger = Logger.getLogger(CourseCompetencesBean.class);
	
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CourseManager courseManager;
	@Inject private TextSearch textSearch;
	
	private String id;
	private long decodedId;
	
	private String courseTitle;
	
	private List<CompetenceData> competences;
	private List<CompetenceData> compSearchResults;
	private List<CompetenceData> compsToRemove;
	
	private String compSearchTerm;
	
	private List<Long> compsToAdd;
	private List<Long> compsToExcludeFromSearch;
	
	private int currentNumberOfComps;
	
	private CredentialStructure credentialStructure; 
	private CredentialStructure[] credentialStructureArray;
	
	public void init() {
		decodedId = idEncoder.decodeId(id);
		
		if (decodedId > 0) {
			compsToAdd = new ArrayList<>();
			competences = new ArrayList<>();
			compsToExcludeFromSearch = new ArrayList<>();
			compsToRemove = new ArrayList<>();
			credentialStructureArray = CredentialStructure.values();
			try {
				if(courseTitle == null) {
					courseTitle = courseManager.getCourseTitle(decodedId);
				}
				boolean mandatory = courseManager.isMandatoryStructure(decodedId);
				setCredentialStructure(mandatory);
				List<CourseCompetence> comps = courseManager.getCourseCompetences(decodedId);
				
				for(CourseCompetence cc : comps) {
					CompetenceData cd = new CompetenceData(cc);
					competences.add(cd);
					compsToExcludeFromSearch.add(cd.getCompetenceId());
				}
				currentNumberOfComps = competences.size();
				logger.info("Loaded course competences for course with id "+ 
						decodedId);
			} catch(DbConnectionException dbce){
				logger.error(dbce);
				PageUtil.fireErrorMessage(dbce.getMessage());
			} catch(Exception ex){
				logger.error(ex);
			}
		} else {
			try {
				FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	
	private void setCredentialStructure(boolean mandatory) {
		if(mandatory) {
			this.credentialStructure = CredentialStructure.MANDATORY;
		} else {
			this.credentialStructure = CredentialStructure.OPTIONAL;
		}
	}
	
	public boolean isMandatory() {
		switch(credentialStructure) {
			case MANDATORY:
				return true;
			case OPTIONAL:
				return false;
			default:
				return false;
		}
	}
	
	public void searchCompetences() {
		compSearchResults = new ArrayList<>();
		if(compSearchTerm != null && !compSearchTerm.isEmpty()) {
			int size = compsToExcludeFromSearch.size();
			long [] toExclude = new long[size];
			for(int i = 0; i < size; i++) {
				toExclude[i] = compsToExcludeFromSearch.get(i);
			}
			TextSearchResponse searchResponse = textSearch.searchCompetences(
					compSearchTerm,
					0, 
					Integer.MAX_VALUE,
					false,
					toExclude,
					null,
					SortingOption.ASC);
			
			@SuppressWarnings("unchecked")
			List<Competence> comps = (List<Competence>) searchResponse.getFoundNodes();
			if(comps != null) {
				for(Competence c : comps) {
					compSearchResults.add(new CompetenceData(c));
				}
			}
		} 
	}
	
	public void addComp(CompetenceData compData) {
		compData.setOrder(competences.size() + 1);
		competences.add(compData);
		compsToAdd.add(compData.getCompetenceId());
		compsToExcludeFromSearch.add(compData.getCompetenceId());
		currentNumberOfComps ++;
		compSearchResults = new ArrayList<>();
	}
	
	public void moveDown(int index) {
		moveComp(index, index + 1);
	}
	
	public void moveUp(int index) {
		moveComp(index - 1, index);
	}
	
	public void moveComp(int i, int k) {
		CompetenceData cd1 = competences.get(i);
		cd1.setOrder(cd1.getOrder() + 1);
		cd1.setStatus(ObjectStatusTransitions.changeTransition(cd1.getStatus()));
		CompetenceData cd2 = competences.get(k);
		cd2.setOrder(cd2.getOrder() - 1);
		cd2.setStatus(ObjectStatusTransitions.changeTransition(cd2.getStatus()));
		Collections.swap(competences, i, k);
	}
	
	public void removeComp(int index) {
		CompetenceData cd = competences.remove(index);
		if(cd.getCourseCompetenceId() > 0) {
			cd.setStatus(ObjectStatus.REMOVED);
			compsToRemove.add(cd);
		}
		currentNumberOfComps--;
		long compId = cd.getCompetenceId();
		removeIdFromExcludeList(compId);
		shiftOrderOfCompetencesUp(index);
	}
	
	private void shiftOrderOfCompetencesUp(int index) {
		for(int i = index; i < currentNumberOfComps; i++) {
			CompetenceData comp = competences.get(i);
			comp.setOrder(comp.getOrder() - 1);
			comp.setStatus(ObjectStatusTransitions.changeTransition(comp.getStatus()));
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

	public void saveCourseCompetences() {
		try {
			List<CompetenceData> comps = new ArrayList<>(competences);
			comps.addAll(compsToRemove);
			courseManager.updateCourseCompetences(decodedId, isMandatory(), comps);
			init();
			PageUtil.fireSuccessfulInfoMessage("Changes are saved");
		} catch(DbConnectionException e) {
			logger.error(e);
			init();
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}

	//GETTERS - SETTERS
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCourseTitle() {
		return courseTitle;
	}

	public void setCourseTitle(String courseTitle) {
		this.courseTitle = courseTitle;
	}

	public List<CompetenceData> getCompetences() {
		return competences;
	}

	public void setCompetences(List<CompetenceData> competences) {
		this.competences = competences;
	}

	public String getCompSearchTerm() {
		return compSearchTerm;
	}

	public void setCompSearchTerm(String compSearchTerm) {
		this.compSearchTerm = compSearchTerm;
	}

	public List<CompetenceData> getCompSearchResults() {
		return compSearchResults;
	}

	public void setCompSearchResults(List<CompetenceData> compSearchResults) {
		this.compSearchResults = compSearchResults;
	}

	public int getCurrentNumberOfComps() {
		return currentNumberOfComps;
	}

	public void setCurrentNumberOfComps(int currentNumberOfComps) {
		this.currentNumberOfComps = currentNumberOfComps;
	}

	public CredentialStructure getCredentialStructure() {
		return credentialStructure;
	}

	public void setCredentialStructure(CredentialStructure credentialStructure) {
		this.credentialStructure = credentialStructure;
	}

	public CredentialStructure[] getCredentialStructureArray() {
		return credentialStructureArray;
	}

	public void setCredentialStructureArray(CredentialStructure[] credentialStructureArray) {
		this.credentialStructureArray = credentialStructureArray;
	}
	
}
