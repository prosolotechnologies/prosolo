/**
 * 
 */
package org.prosolo.web.courses.credential;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialInstructor;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.search.util.credential.InstructorSortOption;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.indexing.impl.NodeChangeObserver;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.instructor.InstructorData;
import org.prosolo.services.nodes.data.instructor.StudentAssignData;
import org.prosolo.services.nodes.data.instructor.StudentInstructorPair;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.util.pagination.Paginable;
import org.prosolo.web.courses.util.pagination.PaginationLink;
import org.prosolo.web.courses.util.pagination.Paginator;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialInstructorsBean")
@Component("credentialInstructorsBean")
@Scope("view")
public class CredentialInstructorsBean implements Serializable, Paginable {

	private static final long serialVersionUID = -4892911343069292524L;

	private static Logger logger = Logger.getLogger(CredentialInstructorsBean.class);

	private List<InstructorData> instructors;

	@Inject private UrlIdEncoder idEncoder;
	@Inject private TextSearch textSearch;
	@Inject private CredentialManager credManager;
	@Inject private CredentialInstructorManager credInstructorManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private EventFactory eventFactory;
	@Inject @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	@Inject private NodeChangeObserver nodeChangeObserver;
	@Inject private RoleManager roleManager;
	@Inject private StudentAssignBean studentAssignBean;

	// PARAMETERS
	private String id;
	private long decodedId;

	private String searchTerm = "";
	private int credentialInstructorsNumber;
	private int page = 1;
	private int limit = 10;
	private InstructorSortOption sortOption = InstructorSortOption.Date;
	private List<PaginationLink> paginationLinks;
	private int numberOfPages;
	
	private InstructorData instructorForRemoval;
	private boolean reassignAutomatically = true;
	//private InstructorData instructorForStudentAssign;
	
	private String context;
	
	private String credentialTitle;
	
	private InstructorSortOption[] sortOptions;
	
	//for searching unassigned instructors
	private String instructorSearchTerm;
	private List<UserData> unassignedInstructors;
	private long instructorRoleId;
	//list of ids of instructors that are already assigned to this credential
	private List<Long> excludedInstructorIds = new ArrayList<>();

	public void init() {
		sortOptions = InstructorSortOption.values();
		
		decodedId = idEncoder.decodeId(id);
		
		if (decodedId > 0) {
			context = "name:CREDENTIAL|id:" + decodedId;
			try {
				String title = credManager.getCredentialTitleForCredentialWithType(
						decodedId, LearningResourceType.UNIVERSITY_CREATED);
				if(title != null) {
					credentialTitle = title;	
					//manuallyAssignStudents = credManager.areStudentsManuallyAssignedToInstructor(decodedId);
					searchCredentialInstructors();
					studentAssignBean.init(decodedId, context);
				} else {
					try {
						FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
					} catch (IOException e) {
						logger.error(e);
					}
				}
			} catch (Exception e) {
				PageUtil.fireErrorMessage("Error while loading instructor data");
			}
		}
	}

	public void searchCredentialInstructors() {
		try {
			if (instructors != null) {
				instructors.clear();
			}

			getCredentialInstructors();
			generatePagination();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
	
	public void searchUnassignedInstructors() {
		try {
			unassignedInstructors = new ArrayList<>();
			TextSearchResponse1<UserData> result = textSearch
					.searchUsersWithInstructorRole(instructorSearchTerm, decodedId, instructorRoleId,
							excludedInstructorIds);
			unassignedInstructors = result.getFoundNodes();
		} catch(Exception e) {
			logger.error(e);
		}
	}
	
	public void prepareAddingInstructor() {
		try {
			if(instructorRoleId == 0) {
				List<Long> roleIds = roleManager.getRoleIdsForName("INSTRUCTOR");
				
				if (roleIds.size() == 1) {
					instructorRoleId = roleIds.get(0);
				}
			}
			instructorSearchTerm = "";
			searchUnassignedInstructors();
		} catch(Exception e) {
			logger.error(e);
			//TODO
		}
	}
	
	public void prepareInstructorForDelete(InstructorData id) {
		instructorForRemoval = id;
	}

	public void addInstructorToCredential(UserData user) {
		try {
			CredentialInstructor inst = credInstructorManager
					.addInstructorToCredential(decodedId, user.getId(), 0);
			page = 1;
			searchTerm = "";
			sortOption = InstructorSortOption.Date;
			credentialInstructorsNumber = (int) credInstructorManager.getCredentialInstructorsCount(decodedId); 
			instructors = credInstructorManager.getCredentialInstructors(decodedId, true, limit, true);
			for(InstructorData id : instructors) {
				excludedInstructorIds.add(id.getUser().getId());
			}
			generatePagination();
			
			String page = PageUtil.getPostParameter("page");
			String service = PageUtil.getPostParameter("service");
			final String lContext = context;
			Date dateAssigned = inst.getDateAssigned();
			
			taskExecutor.execute(new Runnable() {
				@Override
				public void run() {
					Credential1 cred = new Credential1();
					cred.setId(decodedId);
					User instr = new User();
					instr.setId(user.getId());
					Map<String, String> params = new HashMap<>();
					String dateString = null;
					if(dateAssigned != null) {
						DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
						dateString = df.format(dateAssigned);
					}
					params.put("dateAssigned", dateString);
					try {
						eventFactory.generateEvent(EventType.INSTRUCTOR_ASSIGNED_TO_COURSE, 
								loggedUserBean.getUserId(), instr, cred, page, lContext, service, params);
					} catch (EventException e) {
							logger.error(e);
					}
				}
			});
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
		}
		
	}

	private void generatePagination() {
		//if we don't want to generate all links
		Paginator paginator = new Paginator(credentialInstructorsNumber, limit, page, 
				1, "...");
		//if we want to genearate all links in paginator
//		Paginator paginator = new Paginator(courseMembersNumber, limit, page, 
//				true, "...");
		numberOfPages = paginator.getNumberOfPages();
		paginationLinks = paginator.generatePaginationLinks();
	}

	public void getCredentialInstructors() {
		TextSearchResponse1<InstructorData> searchResponse = textSearch.searchInstructors(
				searchTerm, page - 1, limit, decodedId, sortOption, null); 
	
		credentialInstructorsNumber = (int) searchResponse.getHitsNumber();
		instructors = searchResponse.getFoundNodes();
		for(InstructorData id : instructors) {
			excludedInstructorIds.add(id.getUser().getId());
		}
	}
	
	public void resetAndSearch() {
		this.page = 1;
		searchCredentialInstructors();
	}
	
//	public void automaticallyReassignStudents() {
//		try {
//			StudentAssignData assignData = credInstructorManager.reassignStudentsAutomatically(
//					instructorForStudentAssign.getInstructorId(), decodedId);
//			String appPage = PageUtil.getPostParameter("page");
//			String service = PageUtil.getPostParameter("service");
//			fireReassignEvents(instructorForStudentAssign, appPage, service, assignData, true);
//			searchCredentialInstructors();
//			instructorForStudentAssign = null;
//			PageUtil.fireSuccessfulInfoMessage("Students successfully reassigned");
//		} catch (DbConnectionException e) {
//			logger.error(e);
//			PageUtil.fireErrorMessage(e.getMessage());
//		}
//	}
	
	public void applySortOption(InstructorSortOption sortOption) {
		this.sortOption = sortOption;
		this.page = 1;
		searchCredentialInstructors();
	}
	
	public void removeInstructorFromCredential() {
		try {
			StudentAssignData res = credInstructorManager.removeInstructorFromCredential(
					instructorForRemoval.getInstructorId(), decodedId, reassignAutomatically);
			String appPage = PageUtil.getPostParameter("page");
			String service = PageUtil.getPostParameter("service");
			String lContext = context + "|context:/name:INSTRUCTOR|id:" 
					+ instructorForRemoval.getInstructorId() + "/";
			
			Credential1 cred = new Credential1();
			cred.setId(decodedId);
			User instr = new User();
			instr.setId(instructorForRemoval.getUser().getId());
			try {
				@SuppressWarnings("unchecked")
				Event event = eventFactory.generateEvent(
						EventType.INSTRUCTOR_REMOVED_FROM_COURSE, 
						loggedUserBean.getUserId(), instr, cred, 
						appPage, lContext, service, 
						new Class[] {NodeChangeObserver.class}, null);
				nodeChangeObserver.handleEvent(event);
				fireReassignEvents(instructorForRemoval, appPage, service, res, reassignAutomatically);
			} catch (EventException e) {
				logger.error(e);
			}
			excludedInstructorIds.remove(new Long(instructorForRemoval.getUser().getId()));
			searchCredentialInstructors();
			instructorForRemoval = null;
			PageUtil.fireSuccessfulInfoMessage("Instructor successfully removed from credential");
		} catch(DbConnectionException e) {
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}

	private void fireReassignEvents(InstructorData instructorData, String appPage, 
			String service, StudentAssignData assignData, boolean automatic) {
		long instructorUserId = instructorData.getUser().getId();
		long instructorId = instructorData.getInstructorId();
		String lContext = context + "|context:/name:INSTRUCTOR|id:" + instructorId + "/";
		
		if(automatic) {
			List<StudentInstructorPair> assigned = assignData.getAssigned();
			if(assigned != null && !assigned.isEmpty()) {
				Map<String, String> parameters = new HashMap<String, String>();
				parameters.put("credId", decodedId + "");
				parameters.put("reassignedFromInstructorUserId", instructorUserId + "");
				for(StudentInstructorPair pair : assigned) {
					long studentUserId = credManager.getUserIdForTargetCredential(pair.getTargetCredId());
					long insUserId = pair.getInstructor().getUser().getId();
					try {
						User target = new User();
						target.setId(insUserId);
						User object = new User();
						object.setId(studentUserId);
						@SuppressWarnings("unchecked")
						Event event = eventFactory.generateEvent(
								EventType.STUDENT_REASSIGNED_TO_INSTRUCTOR, 
								loggedUserBean.getUserId(), object, target, 
								appPage, lContext, service, 
								new Class[] {NodeChangeObserver.class}, parameters);
						nodeChangeObserver.handleEvent(event);
					} catch(Exception e) {
						logger.error(e);
					}
				}
			}	
		} 
			
		List<Long> unassignedTargetCredIds = assignData.getUnassigned();
		if(unassignedTargetCredIds != null && !unassignedTargetCredIds.isEmpty()) {
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("credId", decodedId + "");
			List<Long> unassignedUserIds = credManager
					.getUserIdsForTargetCredentials(unassignedTargetCredIds);
			for (Long userId : unassignedUserIds) {
				try {
					User target = new User();
					target.setId(instructorData.getUser().getId());
					User object = new User();
					object.setId(userId);
					
					@SuppressWarnings("unchecked")
					Event event = eventFactory.generateEvent(
							EventType.STUDENT_UNASSIGNED_FROM_INSTRUCTOR, 
							loggedUserBean.getUserId(), object, target, 
							appPage, lContext, service, 
							new Class[] {NodeChangeObserver.class}, parameters);
					nodeChangeObserver.handleEvent(event);
				} catch (EventException e) {
					logger.error(e);
				}
			}
		}
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
			searchCredentialInstructors();
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
		return credentialInstructorsNumber == 0;
	}
	
	@Override
	public boolean shouldBeDisplayed() {
		return numberOfPages > 1;
	}

	/*
	 * PARAMETERS
	 */
	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	/*
	 * GETTERS / SETTERS
	 */

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
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

	public List<PaginationLink> getPaginationLinks() {
		return paginationLinks;
	}

	public void setPaginationLinks(List<PaginationLink> paginationLinks) {
		this.paginationLinks = paginationLinks;
	}

	public List<InstructorData> getInstructors() {
		return instructors;
	}

	public void setInstructors(List<InstructorData> instructors) {
		this.instructors = instructors;
	}

	public InstructorSortOption getSortOption() {
		return sortOption;
	}

	public void setSortOption(InstructorSortOption sortOption) {
		this.sortOption = sortOption;
	}

	public InstructorData getInstructorForRemoval() {
		return instructorForRemoval;
	}

	public void setInstructorForRemoval(InstructorData instructorForRemoval) {
		this.instructorForRemoval = instructorForRemoval;
	}

//	public InstructorData getInstructorForStudentAssign() {
//		return instructorForStudentAssign;
//	}
//
//	public void setInstructorForStudentAssign(InstructorData instructorForStudentAssign) {
//		this.instructorForStudentAssign = instructorForStudentAssign;
//	}

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public void setCredentialTitle(String credentialTitle) {
		this.credentialTitle = credentialTitle;
	}

	public InstructorSortOption[] getSortOptions() {
		return sortOptions;
	}

	public void setSortOptions(InstructorSortOption[] sortOptions) {
		this.sortOptions = sortOptions;
	}

	public String getInstructorSearchTerm() {
		return instructorSearchTerm;
	}

	public void setInstructorSearchTerm(String instructorSearchTerm) {
		this.instructorSearchTerm = instructorSearchTerm;
	}

	public List<UserData> getUnassignedInstructors() {
		return unassignedInstructors;
	}

	public void setUnassignedInstructors(List<UserData> unassignedInstructors) {
		this.unassignedInstructors = unassignedInstructors;
	}

	public boolean isReassignAutomatically() {
		return reassignAutomatically;
	}

	public void setReassignAutomatically(boolean reassignAutomatically) {
		this.reassignAutomatically = reassignAutomatically;
	}

}
