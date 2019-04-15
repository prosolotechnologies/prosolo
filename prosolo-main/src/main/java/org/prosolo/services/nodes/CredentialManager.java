package org.prosolo.services.nodes;

import com.amazonaws.services.identitymanagement.model.EntityAlreadyExistsException;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.assessment.AssessorAssignmentMethod;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.credential.CredentialDeliverySortOption;
import org.prosolo.search.util.credential.CredentialMembersSearchFilter;
import org.prosolo.search.util.credential.CredentialSearchFilterManager;
import org.prosolo.search.util.credential.CredentialStudentsInstructorFilter;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.common.data.SortOrder;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.config.credential.CredentialLoadConfig;
import org.prosolo.services.nodes.data.*;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.credential.*;
import org.prosolo.services.nodes.data.resourceAccess.*;
import org.prosolo.services.user.data.StudentData;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.user.data.UserLearningProgress;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Date;
import java.util.List;

public interface CredentialManager extends AbstractManager {

	/**
	 * Saves new credential.
	 * 
	 * This method should be used for saving original credential only and not for delivery.
	 * 
	 * @param data
	 * @param context
	 * @return
	 * @throws DbConnectionException
	 */
	Credential1 saveNewCredential(CredentialData data, UserContextData context)
			throws DbConnectionException;

	Result<Credential1> saveNewCredentialAndGetEvents(CredentialData data, UserContextData context)
			throws DbConnectionException;
	
	void deleteDelivery(long deliveryId, UserContextData context) throws DbConnectionException, StaleDataException,
			DataIntegrityViolationException;
	
	Result<Void> deleteDeliveryAndGetEvents(long deliveryId, UserContextData context) throws DbConnectionException,
			DataIntegrityViolationException, StaleDataException;
	
	/**
	 * Returns user target credential data if user is enrolled in a credential, or credential data 
	 * if that is not the case.
	 * @param credentialId
	 * @param userId
	 * @throws ResourceNotFoundException, DbConnectionException
	 */
	CredentialData getFullTargetCredentialOrCredentialData(long credentialId, long userId)
			throws ResourceNotFoundException, DbConnectionException;
	
	/**
	 * Returns credential data with specified id.
	 *
	 * If {@code req.getAccessMode()} equals {@code AccessMode.User} competencies that are returned if competencies
	 * should be loaded are user competencies with progress if exists. Otherwise credential competencies without user
	 * progress are returned
	 * 
	 * @param credentialId
	 * @param loadCreatorData
	 * @param loadCompetences
	 * @param userId
	 * @param accessMode
	 * @return
	 * @throws ResourceNotFoundException
	 * @throws DbConnectionException
	 */
	CredentialData getCredentialData(long credentialId, boolean loadCreatorData, boolean loadCategoryData, boolean loadAssessmentConfig,
			boolean loadCompetences, long userId, AccessMode accessMode)
					throws ResourceNotFoundException, DbConnectionException;

	CredentialData getCredentialDataForEdit(long credentialId) throws DbConnectionException;

	List<LearningResourceLearningStage> getCredentialLearningStagesData(long orgId, long firstStageCredId) throws DbConnectionException;
	
	/**
	 * Returns Credential data for id: {@code credentialId} with user's progress
	 * for that credential if user is enrolled.
	 * @param credentialId
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	CredentialData getCredentialDataWithProgressIfExists(long credentialId, long userId) 
			throws DbConnectionException;
	
	/**
	 * Returns basic credential data with info whether credential is bookmarked by user or not.
	 * @param credentialId
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	CredentialData getBasicCredentialData(long credentialId, long userId) 
			throws DbConnectionException;
	
	/**
	 * Returns basic credential data with info whether credential is bookmarked by user or not, but only
	 * if credential is of type given by {@code type} parameter. Otherwise, null is returned.
	 * 
	 * @param credentialId
	 * @param userId
	 * @param type
	 * @return
	 * @throws DbConnectionException
	 */
	CredentialData getBasicCredentialData(long credentialId, long userId, CredentialType type) 
			throws DbConnectionException;
	
	Credential1 updateCredential(CredentialData data, UserContextData context)
			throws DbConnectionException, StaleDataException, IllegalDataStateException;
	
	Result<Credential1> updateCredentialData(CredentialData data, UserContextData context)
			throws StaleDataException, IllegalDataStateException;
	
	void enrollInCredential(long credentialId, UserContextData context)
			throws DbConnectionException;
	
	void enrollStudentsInCredential(long credId, long instructorId, List<Long> userIds,
									UserContextData context)
			throws DbConnectionException;
	
	Result<Void> enrollInCredentialAndGetEvents(long credentialId, long userId, 
			long instructorThatForcedEnrollId, UserContextData context) throws DbConnectionException;
	
	/**
	 * Adds competence to credential, updates credential duration. If credential is published, draft version 
	 * for credential is created, competence is added to that draft version and original credential becomes draft. 
	 * If draft version for credential already exists, competence will be attached to existing draft version.
	 * 
	 * Returns EventQueue from which events should be generated.
	 * 
	 * @param credId
	 * @param comp
	 * @param context
	 * @throws DbConnectionException
	 */
	EventQueue addCompetenceToCredential(long credId, Competence1 comp, UserContextData context)
			throws DbConnectionException;

	/**
	 * returns only published credentials
	 * @param compId
	 * @param type - if null credentials and deliveries both will be returned
	 * @return
	 * @throws DbConnectionException
	 */
	List<CredentialIdData> getCredentialsWithIncludedCompetenceBasicData(long compId, CredentialType type)
			throws DbConnectionException;

	List<Tag> getCredentialTags(long credentialId) 
			throws DbConnectionException;
	
	List<Tag> getCredentialTags(long credentialId, Session session) 
			throws DbConnectionException;
	
	List<Tag> getCredentialHashtags(long credentialId) 
			throws DbConnectionException;
	
	List<Tag> getCredentialHashtags(long credentialId, Session session) 
				throws DbConnectionException;

	List<CredentialBookmark> getBookmarkedByIds(long id) throws DbConnectionException;
	
	List<CredentialBookmark> getBookmarkedByIds(long credId, Session session) 
			throws DbConnectionException;
	
	void bookmarkCredential(long credId, UserContextData context)
			throws DbConnectionException;

	Result<Void> bookmarkCredentialAndGetEvents(long credId, UserContextData context)
			throws DbConnectionException;
	
	void deleteCredentialBookmark(long credId, UserContextData context)
			throws DbConnectionException;

	Result<Void> deleteCredentialBookmarkAndGetEvents(long credId, UserContextData context)
			throws DbConnectionException;

	/**
	 * Duration for all credentials with competence specified by {@code compId} is updated
	 * by adding/subtracting {@code duration} value.
	 * @param compId
	 * @param duration
	 * @param op
	 * @throws DbConnectionException
	 */
	void updateDurationForCredentialsWithCompetence(long compId, long duration, Operation op)
			throws DbConnectionException;

	EventQueue updateCredentialProgress(long targetCompId, UserContextData context)
			throws DbConnectionException;

	String getCredentialTitle(long id, CredentialType type) throws DbConnectionException;

	String getCredentialTitle(long id) throws DbConnectionException;

	CredentialData getTargetCredentialDataAndTargetCompetencesData(long credentialId,long userId) throws DbConnectionException;
	
	CredentialIdData getCredentialIdData(long id, CredentialType type) throws DbConnectionException;

	/**
	 * Method for getting all credentials (nevertheless the progress)
	 * 
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	List<TargetCredentialData> getAllCredentials(long userId) throws DbConnectionException;

	/**
	 * Retrieves all credentials of a student that the user (requesting those data) has an access to. Manager can access
	 * to all credentials, where an Instructor can access only credentials where they are (were) an assigned instructor
	 * the the student.
	 *
	 * @param studentId	id of the student
	 * @param userId	id the of the user requesting the credentials
	 * @param accessMode	access mode of the user requesting the credentials
	 * @return	list of credential progress data instances
	 * @throws DbConnectionException
	 */
	List<CredentialProgressData> getCredentialsWithAccessTo(long studentId, long userId, AccessMode accessMode) throws DbConnectionException;

	TargetCredential1 getTargetCredential(long credentialId, long userId, CredentialLoadConfig credentialLoadConfig) throws DbConnectionException;
	
	List<CredentialData> getTargetCredentialsProgressAndInstructorInfoForUser(long userId) throws DbConnectionException;
	
	List<CredentialData> getTargetCredentialsProgressAndInstructorInfoForUser(long userId, Session session) 
			throws DbConnectionException;
	
	List<TargetCredential1> getTargetCredentialsForInstructor(long instructorId) throws DbConnectionException;
	
	long getUserIdForTargetCredential(long targetCredId) throws DbConnectionException;
	
	List<Long> getUserIdsForTargetCredentials(List<Long> targetCredIds) throws DbConnectionException;
	
	List<TargetCredential1> getTargetCredentialsForUsers(List<Long> userIds, long credId) 
			throws DbConnectionException;
	
	boolean saveNewCredentialFeed(long credId, String feedLink) 
			throws DbConnectionException, EntityAlreadyExistsException;
	
	void removeFeed(long credId, long feedSourceId) throws DbConnectionException;

	List<CredentialData> getNRecentlyLearnedInProgressCredentials(Long userid, int limit, boolean loadOneMore) 
			throws DbConnectionException;
	
	void updateTargetCredentialLastAction(long userId, long credentialId, Session session) throws DbConnectionException;

	List<Long> getUserIdsForCredential(long credId) throws DbConnectionException;
	
	List<Long> getActiveUserIdsForCredential(long credId) throws DbConnectionException;
	
	long getTargetCredentialNextCompToLearn(long credId, long userId) throws DbConnectionException;
	
	long getNumberOfUsersLearningCredential(long credId) throws DbConnectionException;

	/**
	 * Returns students enrolled in credential, taking into account {@code limit} (max number of students returned) and instructor filter.
	 *
	 * @param credId
	 * @param instructorFilter
	 * @param limit
	 * @return
	 * @throws DbConnectionException
	 */
	List<StudentData> getCredentialStudentsData(long credId, CredentialStudentsInstructorFilter instructorFilter, int limit) throws DbConnectionException;

	StudentData getCredentialStudentsData(long credId, long studentId) throws DbConnectionException;

	/**
	 * Returns filters populated with number of students belonging to each filter/category, taking into
	 * account instructor filter
	 *
	 * @param credId
	 * @param instructorFilter
	 * @return
	 * @throws DbConnectionException
	 */
	CredentialMembersSearchFilter[] getFiltersWithNumberOfStudentsBelongingToEachCategory(long credId, CredentialStudentsInstructorFilter instructorFilter)
			throws DbConnectionException;
	
	List<Credential1> getAllCredentials(long orgId, Session session) throws DbConnectionException;
	
	List<TargetCredential1> getTargetCredentialsForCredential(long credentialId, 
			boolean justUncompleted) throws DbConnectionException;
	
	void updateCredentialVisibility(long credId, List<ResourceVisibilityMember> groups, 
    		List<ResourceVisibilityMember> users, boolean visibleToAll, boolean visibleToAllChanged,
    		UserContextData context) throws DbConnectionException;
	
	EventQueue updateCredentialVisibilityAndGetEvents(long credId, List<ResourceVisibilityMember> groups,
    		List<ResourceVisibilityMember> users, boolean visibleToAll, boolean visibleToAllChanged,
    		UserContextData context) throws DbConnectionException;
	
	boolean isVisibleToAll(long credId) throws DbConnectionException;

	UserData chooseRandomPeer(long credId, long userId);
	
	/**
	 * Returns list of CompetenceData for given credentials.
	 * 
	 * @param credentialId
	 * @return list of simple data types from TargetCompetence
	 * @throws DbConnectionException
	 */
	List<CompetenceData1> getCompetencesForKeywordSearch(long credentialId) throws DbConnectionException;

	/**
	 * Returns tag titles for tags added to competences and activities that are part of the credential
	 * specified by {@code credentialId} id
	 * 
	 * @param credentialId

	 * @throws DbConnectionException
	 */
	List<String> getTagsFromCredentialCompetencesAndActivities(long credentialId) throws DbConnectionException;

	/**
	 * Returns list of ActivityData for given credentials.
	 * 
	 * @param credentialId
	 * @return list of simple data types from TargetActivity(title, id, duration and competenceId)
	 * @throws DbConnectionException
	 */
	List<ActivityData> getActivitiesForKeywordSearch(long credentialId) throws DbConnectionException;
	/**
	 * Returns list of ids of all users that currently do not have instructor assigned for credential
	 * with {@code credId} id, except users which ids are contained in {@code usersToExclude} list.
	 * 
	 * If you do not want to exclude any user, pass null or empty list for {@code usersToExclude} parameter.
	 *
	 * @param credId
	 * @param usersToExclude
	 * @return
	 * @throws DbConnectionException
	 */
	List<Long> getUnassignedCredentialMembersIds(long credId, List<Long> usersToExclude) 
			throws DbConnectionException;

	int getNumberOfTags(long credentialId) throws DbConnectionException;

	CredentialData getTargetCredentialData(long credentialId, long userId, CredentialLoadConfig credentialLoadConfig)
			throws DbConnectionException;
	
	LearningInfo getCredentialLearningInfo(long credId, long userId, boolean loadCompLearningInfo) 
			throws DbConnectionException;
	
	CredentialDeliveriesSummaryData getOngoingDeliveriesSummaryData(long credId) throws DbConnectionException;

	CredentialInStagesDeliveriesSummaryData getOngoingDeliveriesSummaryDataFromAllStages(long firstStageCredentialId) throws DbConnectionException;
	
	RestrictedAccessResult<List<CredentialData>> getCredentialDeliveriesWithAccessRights(long credId, 
			long userId, SortOrder<CredentialDeliverySortOption> sortOrder, CredentialSearchFilterManager filter) throws DbConnectionException;
	
	void archiveCredential(long credId, UserContextData context) throws DbConnectionException;

	Result<Void> archiveCredentialAndGetEvents(long credId, UserContextData context)
			throws DbConnectionException;
	
	void restoreArchivedCredential(long credId, UserContextData context)
			throws DbConnectionException;

	Result<Void> restoreArchivedCredentialAndGetEvents(long credId, UserContextData context)
			throws DbConnectionException;

	PaginatedResult<CredentialData> searchCredentialsForManager(CredentialSearchFilterManager searchFilter, int limit, int page,
																long userId) throws DbConnectionException, NullPointerException;
	
	UserAccessSpecification getUserPrivilegesForCredential(long credId, long userId) throws DbConnectionException;
	
	ResourceAccessData getResourceAccessData(long credId, long userId, ResourceAccessRequirements req) 
			throws DbConnectionException;
	
	List<Long> getIdsOfAllCompetencesInACredential(long credId, Session session) throws DbConnectionException;
	
	Credential1 createCredentialDelivery(long credentialId, long start, long end, UserContextData context)
			throws DbConnectionException, IllegalDataStateException;
	
	Result<Credential1> createCredentialDeliveryAndGetEvents(long credentialId, Date start, Date end, 
			UserContextData context) throws DbConnectionException, IllegalDataStateException;
	
	List<Long> getIdsOfAllCredentialDeliveries(long credId, Session session) throws DbConnectionException;

	Result<Void> updateCredentialCreator(long newCreatorId, long oldCreatorId,
										 UserContextData context) throws DbConnectionException;

	ResourceCreator getCredentialCreator(long credId) throws DbConnectionException;

	List<Tag> getTagsForCredential(long credentialId) throws DbConnectionException;

	List<Tag> getHashtagsForCredential(long credentialId) throws DbConnectionException;

	List<CredentialData> getCredentialDeliveriesForUserWithInstructPrivilege(long userId, SortOrder<CredentialDeliverySortOption> sortOrder)
			throws DbConnectionException;

	long getCredentialIdForDelivery(long deliveryId) throws DbConnectionException;

	List<Long> getDeliveryIdsForCredential(long credId) throws DbConnectionException;

	void changeOwner(long credId, long newOwnerId, UserContextData context) throws DbConnectionException;

	Result<Void> changeOwnerAndGetEvents(long credId, long newOwnerId, UserContextData context) throws DbConnectionException;

	List<Long> getIdsOfUncompletedDeliveries(long userId) throws DbConnectionException;

	List<Long> getIdsOfDeliveriesUserIsLearning(long userId, UserLearningProgress progress)
			throws DbConnectionException;

	List<Long> getIdsOfDeliveriesUserIsLearningContainingCompetence(long userId, long compId)
			throws DbConnectionException;

	boolean isUserEnrolled(long credId, long userId);

	PaginatedResult<CredentialData> searchCredentialsForAdmin(long unitId, CredentialSearchFilterManager searchFilter, int limit, int page)
			throws DbConnectionException, NullPointerException;

	void updateDeliveryStartAndEnd(CredentialData deliveryData, UserContextData context)
			throws StaleDataException, IllegalDataStateException, DbConnectionException;

	Result<Void> updateDeliveryStartAndEndAndGetEvents(CredentialData deliveryData, UserContextData context)
			throws StaleDataException, IllegalDataStateException, DbConnectionException;

	Long getInstructorUserId(long userId, long credId, Session session) throws DbConnectionException;

	Credential1 getCredentialWithCompetences(long credentialId, CredentialType type) throws DbConnectionException;

	List<Long> getUsersLearningDelivery(long deliveryId) throws DbConnectionException;

	public List<Long> getUsersLearningDeliveryAssignedToInstructor(long deliveryId, long instructorUserId);

	Result<Credential1> createCredentialInLearningStageAndGetEvents(long firstStageCredentialId, long learningStageId, boolean copyCompetences, UserContextData context) throws DbConnectionException;

	long createCredentialInLearningStage(long basedOnCredentialId, long learningStageId, boolean copyCompetences, UserContextData context) throws DbConnectionException;

	/**
	 * Disables learning in stages for all original credentials, but not for deliveries
	 *
	 * @param orgId
	 * @param context
	 * @return
	 * @throws DbConnectionException
	 */
	EventQueue disableLearningStagesForOrganizationCredentials(long orgId, UserContextData context) throws DbConnectionException;

	List<AssessmentTypeConfig> getCredentialAssessmentTypesConfig(long credId) throws DbConnectionException;

	long getTargetCredentialId(long credId, long studentId) throws DbConnectionException;

	CredentialCategory getCredentialCategory(long categoryId) throws DbConnectionException;

	/**
	 *
	 * @param credentialId
	 * @param studentId
	 * @param session
	 * @return
	 * @throws DbConnectionException
	 */
	TargetCredential1 getTargetCredentialForStudentAndCredential(long credentialId, long studentId, Session session);

	/**
	 * Returns true if there is at least one competency in credential with evidence learning path enabled
	 *
	 * @param credId
	 * @return
	 * @throws DbConnectionException
	 */
	boolean doesCredentialHaveAtLeastOneEvidenceBasedCompetence(long credId);

	AssessorAssignmentMethod getAssessorAssignmentMethod(long credId);

	/**
	 * Returns basic info (id, title) for credentials user completed and that are not already added
	 * to the profile
	 *
	 * @param userId
	 * @return
	 */
	List<CredentialIdData> getCompletedCredentialsBasicDataForCredentialsNotAddedToProfile(long userId);

}
