package org.prosolo.web.dialogs;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.web.courses.CoursePortfolioBean;
import org.prosolo.web.courses.data.CourseData;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "deleteGoalCourseDialog")
@Component("deleteGoalCourseDialog")
@Scope("view")
public class DeleteCourseGoalDialogBean {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DeleteCourseGoalDialogBean.class);

	@Autowired private CoursePortfolioBean coursePortfolioBean;
	@Autowired private LoggingNavigationBean actionLogger;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	private CourseData courseToDelete;
	private boolean deleteLearningHistory = false;
	private String context;
	private String learningContext;
	private String service = "name:withdraw_course_dialog";
	
	public void initialize(final CourseData courseToDelete, final String context) {
		this.courseToDelete = courseToDelete;
		this.context = context;
		this.learningContext = PageUtil.getPostParameter("learningContext");
		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
            	actionLogger.logServiceUse(
        			ComponentName.WITHDRAW_COURSE_DIALOG, 
        			"context", context,
        			"course", String.valueOf(courseToDelete.getId()));
            }
		});
	}

	/*
	 * ACTIONS
	 */
	public void deleteCourse() {
		String page = PageUtil.getPostParameter("page");
		coursePortfolioBean.deleteCourse(courseToDelete, deleteLearningHistory, context,
				page, learningContext, service);
	}
	

	/*
	 * GETTERS / SETTERS
	 */
	
	public CourseData getCourseToDelete() {
		return courseToDelete;
	}
	
	public void setCourseToDelete(CourseData courseToDelete) {
		this.courseToDelete = courseToDelete;
	}

	public boolean isDeleteLearningHistory() {
		return deleteLearningHistory;
	}

	public void setDeleteLearningHistory(boolean deleteLearningHistory) {
		this.deleteLearningHistory = deleteLearningHistory;
	}

	public String getContext() {
		return context;
	}

}
