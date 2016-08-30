package org.prosolo.web.dialogs;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.web.courses.CoursePortfolioBean;
import org.prosolo.web.courses.data.CourseData;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "activateCourseDialog")
@Component("activateCourseDialog")
@Scope("view")
public class ActivateCourseDialogBean {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ActivateCourseDialogBean.class);

	@Autowired private CoursePortfolioBean coursePortfolioBean;
	@Autowired private LoggingNavigationBean actionLogger;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	private CourseData courseToActivate;
	private boolean restorePreviousLearning = true;
	private boolean hasLearningHistory = false;
	private String context;
	private String learningContext;
	
	public void initialize(final CourseData courseToActivate, final String context) {
		this.courseToActivate = courseToActivate;
		this.context = context;
		learningContext = PageUtil.getPostParameter("learningContext");
		if (courseToActivate != null) {
			hasLearningHistory = courseToActivate.getEnrollmentId() > 0;
		}
		
		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
            	actionLogger.logServiceUse(
        			ComponentName.ACTIVATE_COURSE_DIALOG, 
        			"context", context,
        			"course", String.valueOf(courseToActivate.getId()));
            }
		});
	}
	
	/*
	 * ACTIONS
	 */
	public void activateCourse() {
		String page = PageUtil.getPostParameter("page");
		String service = PageUtil.getPostParameter("service");
		coursePortfolioBean.activateCourse(courseToActivate, restorePreviousLearning, context,
				page, learningContext, service);
	}

	
	/*
	 * GETTERS / SETTERS
	 */
	public CourseData getCourseToActivate() {
		return courseToActivate;
	}

	public void setCourseToActivate(CourseData courseToActivate) {
		this.courseToActivate = courseToActivate;
	}

	public boolean isRestorePreviousLearning() {
		return restorePreviousLearning;
	}

	public void setRestorePreviousLearning(boolean restorePreviousLearning) {
		this.restorePreviousLearning = restorePreviousLearning;
	}

	public boolean isHasLearningHistory() {
		return hasLearningHistory;
	}

	public String getContext() {
		return context;
	}
	
}
