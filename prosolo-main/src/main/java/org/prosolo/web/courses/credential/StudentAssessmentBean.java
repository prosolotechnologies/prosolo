package org.prosolo.web.courses.credential;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.AssessmentData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.util.pagination.Paginable;
import org.prosolo.web.courses.util.pagination.PaginationLink;
import org.prosolo.web.courses.util.pagination.Paginator;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "studentAssessmentBean")
@Component("studentAssessmentBean")
@Scope("view")
public class StudentAssessmentBean implements Paginable,Serializable {

	private static final long serialVersionUID = 7344090333263528353L;
	private static Logger logger = Logger.getLogger(StudentAssessmentBean.class);

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private CredentialManager credManager;
	@Inject
	private AssessmentManager assessmentManager;
	@Inject
	private LoggedUserBean loggedUserBean;

	private String context;
	private List<AssessmentData> assessmentData;
	private boolean searchForPending = true;
	private boolean searchForApproved = true;

	private List<PaginationLink> paginationLinks;
	private int page = 1;
	private int limit = 5;
	private int numberOfPages;
	private int assessmentsNumber;

	public void init() {

		try {
			assessmentsNumber = assessmentManager.countAssessmentsForUser(loggedUserBean.getUserId(),
					searchForPending, searchForApproved);
			assessmentData = assessmentManager.getAllAssessmentsForStudent(loggedUserBean.getUserId(),
					searchForPending, searchForApproved, idEncoder, new SimpleDateFormat("MMMM dd, yyyy"), page - 1,
					limit);
			generatePagination();

		} catch (Exception e) {
			logger.error("Error while loading assessment data", e);
			PageUtil.fireErrorMessage("Error while loading assessment data");
		}
	}

	public void searchForAll() {
		// only reinitialize when at least one is already false
		if (!searchForApproved || !searchForPending) {
			searchForApproved = true;
			searchForPending = true;
			page = 1;
			init();
		}
	}

	public void searchForNone() {
		// only reinitialize when at least one is true
		if (searchForApproved || searchForPending) {
			searchForApproved = false;
			searchForPending = false;
			page = 1;
			init();
		}
	}

	private void generatePagination() {
		// if we don't want to generate all links
		Paginator paginator = new Paginator(assessmentsNumber, limit, page, 1, "...");
		// if we want to genearte all links in paginator
		// Paginator paginator = new Paginator(courseMembersNumber, limit, page,
		// true, "...");
		numberOfPages = paginator.getNumberOfPages();
		paginationLinks = paginator.generatePaginationLinks();
	}

	public UrlIdEncoder getIdEncoder() {
		return idEncoder;
	}

	public void setIdEncoder(UrlIdEncoder idEncoder) {
		this.idEncoder = idEncoder;
	}

	public CredentialManager getCredManager() {
		return credManager;
	}

	public void setCredManager(CredentialManager credManager) {
		this.credManager = credManager;
	}

	public AssessmentManager getAssessmentManager() {
		return assessmentManager;
	}

	public void setAssessmentManager(AssessmentManager assessmentManager) {
		this.assessmentManager = assessmentManager;
	}

	public LoggedUserBean getLoggedUserBean() {
		return loggedUserBean;
	}

	public void setLoggedUserBean(LoggedUserBean loggedUserBean) {
		this.loggedUserBean = loggedUserBean;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public List<AssessmentData> getAssessmentData() {
		return assessmentData;
	}

	public void setAssessmentData(List<AssessmentData> assessmentData) {
		this.assessmentData = assessmentData;
	}

	public boolean isSearchForPending() {
		return searchForPending;
	}

	public void setSearchForPending(boolean searchForPending) {
		this.searchForPending = searchForPending;
		page = 1;
		init();
		RequestContext.getCurrentInstance().update("assessmentList:filterAssessmentsForm");
	}

	public boolean isSearchForApproved() {
		return searchForApproved;
	}

	public void setSearchForApproved(boolean searchForApproved) {
		this.searchForApproved = searchForApproved;
		page = 1;
		init();
		RequestContext.getCurrentInstance().update("assessmentList:filterAssessmentsForm");
	}

	public List<PaginationLink> getPaginationLinks() {
		return paginationLinks;
	}

	public void setPaginationLinks(List<PaginationLink> paginationLinks) {
		this.paginationLinks = paginationLinks;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getNumberOfPages() {
		return numberOfPages;
	}

	public void setNumberOfPages(int numberOfPages) {
		this.numberOfPages = numberOfPages;
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
		if(this.page != page) {
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
		return assessmentsNumber == 0;
	}
	
	

}
