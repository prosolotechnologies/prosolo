/**
 * 
 */
package org.prosolo.web.lti;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.CompetenceActivity;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseCompetence;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.TextSearch;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.activitywall.data.NodeData;
import org.prosolo.web.courses.data.CourseData;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.lti.data.ExternalToolData;
import org.prosolo.web.lti.data.ExternalToolFilterData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 * 
 */
@ManagedBean(name = "manageExtenalToolsBean")
@Component("manageExtenalToolsBean")
@Scope("view")
public class ManageExternalToolsBean implements Serializable {

	private static final long serialVersionUID = 6383363883663936346L;

	private static Logger logger = Logger.getLogger(ManageExternalToolsBean.class);
	
	@Autowired private DefaultManager defaultManager;
	@Autowired private TextSearch textSearch;
	@Autowired private LoggingNavigationBean loggingNavigationBean;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;

	private long id;
	private CourseData courseData;
	private List<ExternalToolFilterData> resourceFilter;
	private ExternalToolFilterData selectedFilter;
	
	private List<ExternalToolData> externalTools;
	
	public void init() {
		if (id > 0) {
			try {
				Course course = defaultManager.loadResource(Course.class, id);

				courseData = new CourseData(course);
				
				resourceFilter = new LinkedList<ExternalToolFilterData>();
				
				ExternalToolFilterData noFilterItem = new ExternalToolFilterData(-1, "------------------", -1);
				this.selectedFilter = noFilterItem;
				resourceFilter.add(noFilterItem);
				
				for (CourseCompetence courseCompetences : course.getCompetences()) {
					Competence comp = courseCompetences.getCompetence();
					
					ExternalToolFilterData compItem = new ExternalToolFilterData(comp.getId(), comp.getTitle(), courseData.getId());
					
					for (CompetenceActivity compActivity : comp.getActivities()) {
						Activity activity = compActivity.getActivity();
						
						ExternalToolFilterData activityItem = new ExternalToolFilterData(activity.getId(), activity.getTitle(), comp.getId());
						
						compItem.addChild(activityItem);
					}
					
					resourceFilter.add(compItem);
				}
				
				loadData();
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		} else {
		}
	}
	
	public void selectFilter(ExternalToolFilterData selectedFilter) {
		this.selectedFilter = selectedFilter;
	}
	
	/*
	 * Search
	 */
	
	public void loadData() {
		// MOCK DATA
		externalTools = new LinkedList<>();
		externalTools.add(new ExternalToolData(724, "UTA Moodle Course displays activity 'Gephi'", new NodeData(4, null, Activity.class, "Gephi")));
		externalTools.add(new ExternalToolData(633, "UTA Moodle Course displays competence 'Define social network analysis'", new NodeData(4, null, Competence.class, "Define social network analysis")));
		externalTools.add(new ExternalToolData(724, "UTA Sakai - activity 'Upload your own visualization'", new NodeData(4, null, Activity.class, "Upload your own visualization")));
	}
	
	/*
	 * PARAMETERS
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public long getId() {
		return id;
	}

	public List<ExternalToolFilterData> getResourceFilter() {
		return resourceFilter;
	}

	public ExternalToolFilterData getSelectedFilter() {
		return selectedFilter;
	}
	
	public List<ExternalToolData> getExternalTools() {
		return externalTools;
	}
	
}
