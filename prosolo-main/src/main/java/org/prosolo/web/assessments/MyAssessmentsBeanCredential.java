package org.prosolo.web.assessments;

import org.apache.log4j.Logger;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentData;
import org.prosolo.services.assessment.data.filter.AssessmentStatusFilter;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "myAssessmentsBeanCredential")
@Component("myAssessmentsBeanCredential")
@Scope("view")
public class MyAssessmentsBeanCredential implements Paginable, Serializable {

	private static final long serialVersionUID = 7344090333263528353L;
	private static Logger logger = Logger.getLogger(MyAssessmentsBeanCredential.class);

	@Inject private AssessmentManager assessmentManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private AssessmentAvailabilityBean assessmentAvailabilityBean;

	private int page;

	private List<AssessmentData> assessmentsData;
	private AssessmentStatusFilter assessmentStatusFilter = AssessmentStatusFilter.ALL;
	private AssessmentStatusFilter[] assessmentStatusFilters;

	private PaginationData paginationData = new PaginationData(5);

	public void init() {
		try {
			if (page > 0) {
				paginationData.setPage(page);
			}
			assessmentStatusFilters = AssessmentStatusFilter.values();
			loadAssessments();
			assessmentAvailabilityBean.init();
		} catch (Exception e) {
			logger.error("Error loading assessments", e);
			PageUtil.fireErrorMessage("Error loading the page");
		}
	}

	private void loadAssessments() {
		PaginatedResult<AssessmentData> res = assessmentManager.getPaginatedCredentialPeerAssessmentsForAssessor(
				loggedUserBean.getUserId(), assessmentStatusFilter, (paginationData.getPage() - 1) * paginationData.getLimit(), paginationData.getLimit());
		paginationData.update((int) res.getHitsNumber());
		assessmentsData = res.getFoundNodes();
	}

	private void loadAssessmentsWithExceptionHandling() {
		try {
			loadAssessments();
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error loading the data");
		}
	}

	@Override
	public void changePage(int page) {
		if(this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			loadAssessmentsWithExceptionHandling();
		}
	}

	public void applyAssessmentStatusFilter(AssessmentStatusFilter filter) {
		this.assessmentStatusFilter = filter;
		this.paginationData.setPage(1);
		loadAssessmentsWithExceptionHandling();
	}

	public String getLabelForSelectedFilter() {
		return getLabelForFilter(assessmentStatusFilter);
	}

	public String getLabelForFilter(AssessmentStatusFilter filter) {
		if (filter == AssessmentStatusFilter.ALL) {
			return ResourceBundleUtil.getLabel("enum.AssessmentStatusFilter." + filter.name());
		} else {
			return ResourceBundleUtil.getLabel("enum.AssessmentStatus." + filter.getStatuses().get(0).name());
		}
	}

	public List<AssessmentData> getAssessmentsData() {
		return assessmentsData;
	}

	public AssessmentStatusFilter getAssessmentStatusFilter() {
		return assessmentStatusFilter;
	}

	public void setAssessmentStatusFilter(AssessmentStatusFilter assessmentStatusFilter) {
		this.assessmentStatusFilter = assessmentStatusFilter;
	}

	public PaginationData getPaginationData() {
		return paginationData;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public AssessmentStatusFilter[] getAssessmentStatusFilters() {
		return assessmentStatusFilters;
	}
}
