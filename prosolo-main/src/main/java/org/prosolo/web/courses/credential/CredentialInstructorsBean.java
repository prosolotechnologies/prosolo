/**
 * 
 */
package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.credential.InstructorSortOption;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.instructor.InstructorData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "credentialInstructorsBean")
@Component("credentialInstructorsBean")
@Scope("view")
public class CredentialInstructorsBean implements Serializable, Paginable {

	private static final long serialVersionUID = -4892911343069292524L;

	private static Logger logger = Logger.getLogger(CredentialInstructorsBean.class);

	private List<InstructorData> instructors;

	@Inject private UrlIdEncoder idEncoder;
	@Inject private UserTextSearch userTextSearch;
	@Inject private CredentialManager credManager;
	@Inject private CredentialInstructorManager credInstructorManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	@Inject private RoleManager roleManager;
	@Inject private StudentAssignBean studentAssignBean;
	@Inject private UnitManager unitManager;

	// PARAMETERS
	private String id;
	private long decodedId;

	private String searchTerm = "";
	private InstructorSortOption sortOption = InstructorSortOption.Date;
	private PaginationData paginationData = new PaginationData();
	
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
	List<Long> unitIds;
	//list of ids of instructors that are already assigned to this credential
	private List<Long> excludedInstructorIds = new ArrayList<>();
	
	private ResourceAccessData access;

	public void init() {
		sortOptions = InstructorSortOption.values();
		
		decodedId = idEncoder.decodeId(id);
		
		if (decodedId > 0) {
			context = "name:CREDENTIAL|id:" + decodedId;
			try {
				String title = credManager.getCredentialTitle(decodedId, CredentialType.Delivery);
				if(title != null) {
					access = credManager.getResourceAccessData(decodedId, loggedUserBean.getUserId(),
								ResourceAccessRequirements.of(AccessMode.MANAGER)
														  .addPrivilege(UserGroupPrivilege.Edit));
					if(!access.isCanAccess()) {
						PageUtil.accessDenied();
					} else {
						credentialTitle = title;	
						//manuallyAssignStudents = credManager.areStudentsManuallyAssignedToInstructor(decodedId);
						searchCredentialInstructors();
						studentAssignBean.init(decodedId, context);
					}
				} else {
					PageUtil.notFound();
				}
			} catch (Exception e) {
				PageUtil.fireErrorMessage("Error while loading instructor data");
			}
		} else {
			PageUtil.notFound();
		}
	}

	public void searchCredentialInstructors() {
		try {
			if (instructors != null) {
				instructors.clear();
			}

			getCredentialInstructors();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
	
	public void searchUnassignedInstructors() {
		try {
			unassignedInstructors = new ArrayList<>();
			PaginatedResult<UserData> result = userTextSearch
					.searchUsersWithInstructorRole(loggedUserBean.getOrganizationId(), instructorSearchTerm, decodedId,
							instructorRoleId, unitIds, excludedInstructorIds);
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

				//retrieve unit ids for original credential, but only if not already initialized (condition instructorRoleId > 0)
				unitIds = unitManager.getAllUnitIdsCredentialIsConnectedTo(credManager.getCredentialIdForDelivery(decodedId));
			}
			instructorSearchTerm = "";
			searchUnassignedInstructors();
		} catch(Exception e) {
			logger.error("Error", e);
			//TODO
		}
	}
	
	public void prepareInstructorForDelete(InstructorData id) {
		instructorForRemoval = id;
	}

	public void addInstructorToCredential(UserData user) {
		try {
			String page = PageUtil.getPostParameter("page");
			String service = PageUtil.getPostParameter("service");
			PageContextData ctx = new PageContextData(page, context, service);
			credInstructorManager
					.addInstructorToCredential(decodedId, user.getId(), 0, loggedUserBean.getUserContext(ctx));
			paginationData.setPage(1);
			searchTerm = "";
			sortOption = InstructorSortOption.Date;
			paginationData.update((int) credInstructorManager.getCredentialInstructorsCount(decodedId)); 
			instructors = credInstructorManager.getCredentialInstructors(decodedId, true, paginationData.getLimit(), true);
			for (InstructorData id : instructors) {
				excludedInstructorIds.add(id.getUser().getId());
			}
		} catch(DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
		} catch (EventException e) {
			logger.error(e);
		}
		
	}

	public void getCredentialInstructors() {
		PaginatedResult<InstructorData> searchResponse = userTextSearch.searchInstructors(
				loggedUserBean.getOrganizationId(), searchTerm, paginationData.getPage() - 1, paginationData.getLimit(), decodedId, sortOption, null);
	
		paginationData.update((int) searchResponse.getHitsNumber());
		instructors = searchResponse.getFoundNodes();
		for (InstructorData id : instructors) {
			excludedInstructorIds.add(id.getUser().getId());
		}
	}
	
	public void resetAndSearch() {
		paginationData.setPage(1);
		searchCredentialInstructors();
	}
	
	public void applySortOption(InstructorSortOption sortOption) {
		this.sortOption = sortOption;
		paginationData.setPage(1);
		searchCredentialInstructors();
	}
	
	public void removeInstructorFromCredential() {
		try {
			String appPage = PageUtil.getPostParameter("page");
			String service = PageUtil.getPostParameter("service");
			String lContext = context + "|context:/name:INSTRUCTOR|id:" 
					+ instructorForRemoval.getInstructorId() + "/";
			PageContextData ctx = new PageContextData(appPage, lContext, service);
			credInstructorManager.removeInstructorFromCredential(
					instructorForRemoval.getInstructorId(), decodedId, reassignAutomatically,
					loggedUserBean.getUserContext(ctx));

			excludedInstructorIds.remove(new Long(instructorForRemoval.getUser().getId()));
			searchCredentialInstructors();
			instructorForRemoval = null;
			PageUtil.fireSuccessfulInfoMessage("The instructor has been removed from the " + ResourceBundleUtil.getMessage("label.credential").toLowerCase());
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage(e.getMessage());
		} catch (EventException ee) {
			logger.error(ee);
		}
	}
	
	@Override
	public void changePage(int page) {
		if(this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			searchCredentialInstructors();
		}
	}
	
	public boolean canEdit() {
		return access != null && access.isCanEdit();
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

	@Override
	public PaginationData getPaginationData() {
		return paginationData;
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

	public long getCredentialId() {
		return decodedId;
	}
}
