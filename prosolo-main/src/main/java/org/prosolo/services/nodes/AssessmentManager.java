package org.prosolo.services.nodes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.nodes.data.AssessmentData;
import org.prosolo.services.nodes.data.AssessmentRequestData;
import org.prosolo.services.nodes.data.FullAssessmentData;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public interface AssessmentManager {

	long requestAssessment(AssessmentRequestData assessmentRequestData);

	long createDefaultAssessment(TargetCredential1 targetCredential, long assessorId) 
			throws DbConnectionException;
	
	List<AssessmentData> getAllAssessmentsForCredential(long credentialId, long assessorId,
			boolean searchForPending, boolean searchForApproved, UrlIdEncoder idEncoder, DateFormat simpleDateFormat);

	FullAssessmentData getFullAssessmentData(long id, UrlIdEncoder encoder, long userId, DateFormat dateFormat);

	Long countAssessmentsForUserAndCredential(long userId, long credentialId);

	void approveCredential(long credentialAssessmentId, long targetCredentialId, String reviewText);

	ActivityAssessment createActivityDiscussion(long targetActivityId, long competenceAssessmentId, List<Long> participantIds,
			long senderId, boolean isDefault, Integer grade) throws ResourceCouldNotBeLoadedException;
	
	ActivityAssessment createActivityDiscussion(long targetActivityId, long competenceAssessmentId, List<Long> participantIds,
			long senderId, boolean isDefault, Integer grade, Session session) throws ResourceCouldNotBeLoadedException;

	ActivityDiscussionMessageData addCommentToDiscussion(long actualDiscussionId, long senderId, String comment)
			throws ResourceCouldNotBeLoadedException;

	void editCommentContent(long activityMessageId, long userId, String newContent)
			throws ResourceCouldNotBeLoadedException;

	void approveCompetence(long decodedCompetenceAssessmentId);

	void markDiscussionAsSeen(long userId, long discussionId);

	Long getAssessmentIdForUser(long userId, long targetCredentialId);

	List<AssessmentData> getAllAssessmentsForStudent(long id, boolean searchForPending,
			boolean searchForApproved, UrlIdEncoder idEncoder, SimpleDateFormat simpleDateFormat, int page,
			int numberPerPage);

	int countAssessmentsForUser(long id, boolean searchForPending, boolean searchForApproved);

	int countAssessmentsForAssessorAndCredential(long decodedCredentialId, long assessorId, boolean searchForPending,
			boolean searchForApproved);
	
	List<ActivityDiscussionMessageData> getActivityDiscussionMessages(long activityDiscussionId,
				long assessorId) throws DbConnectionException;
	
	Long getAssessorIdForActivityDiscussion(long activityDiscussionId) 
			throws DbConnectionException;
	
	CompetenceAssessment getDefaultCompetenceAssessment(long credId, long compId, long userId) 
			throws DbConnectionException;
	
	CompetenceAssessment getDefaultCompetenceAssessment(long credId, long compId, long userId, Session session) 
			throws DbConnectionException;
	
	long getAssessorIdForCompAssessment(long compAssessmentId) throws DbConnectionException;
	
	void updateDefaultAssessmentAssessor(long targetCredId, long assessorId) throws DbConnectionException;
	
	void updateDefaultAssessmentsAssessor(List<Long> targetCredIds, long assessorId) 
			throws DbConnectionException;
	
	void updateGradeForActivityAssessment(long activityDiscussionId, Integer value) 
			throws DbConnectionException;
	
	Optional<Long> getDefaultCredentialAssessmentId(long credId, long userId) throws DbConnectionException;

	int recalculateScoreForCompetenceAssessment(long compAssessmentId);
	
	int recalculateScoreForCompetenceAssessment(long compAssessmentId, Session session);

	int recalculateScoreForCredentialAssessment(long credAssessmentId);
	
	int recalculateScoreForCredentialAssessment(long credAssessmentId, Session session);
	
	ActivityAssessment getDefaultActivityDiscussion(long targetActId, Session session) throws DbConnectionException;
	
	void createOrUpdateActivityAssessmentsForExistingCompetenceAssessments(long userId, long senderId, 
			long targetCompId, long targetActId, int score, Session session) throws DbConnectionException;

}
