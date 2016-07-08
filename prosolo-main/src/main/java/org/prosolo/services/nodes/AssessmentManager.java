package org.prosolo.services.nodes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.nodes.data.AssessmentData;
import org.prosolo.services.nodes.data.AssessmentRequestData;
import org.prosolo.services.nodes.data.FullAssessmentData;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public interface AssessmentManager {

	public long requestAssessment(AssessmentRequestData assessmentRequestData);

	public List<AssessmentData> getAllAssessmentsForCredential(long credentialId, long assessorId,
			boolean searchForPending, boolean searchForApproved, UrlIdEncoder idEncoder, DateFormat simpleDateFormat);

	public FullAssessmentData getFullAssessmentData(long id, UrlIdEncoder encoder, long userId, DateFormat dateFormat);

	public Long countAssessmentsForUserAndCredential(long userId, long credentialId);

	public void approveCredential(long credentialAssessmentId, long targetCredentialId, String reviewText);

	public long createActivityDiscussion(long targetActivityId, long competenceAssessmentId, List<Long> participantIds,
			long senderId);

	public ActivityDiscussionMessageData addCommentToDiscussion(long actualDiscussionId, long senderId, String comment)
			throws ResourceCouldNotBeLoadedException;

	public void editCommentContent(long activityMessageId, long userId, String newContent)
			throws ResourceCouldNotBeLoadedException;

	public void approveCompetence(long decodedCompetenceAssessmentId);

	public void markDiscussionAsSeen(long userId, long discussionId);

	public Long getAssessmentIdForUser(long userId, long targetCredentialId);

	public List<AssessmentData> getAllAssessmentsForStudent(long id, boolean searchForPending,
			boolean searchForApproved, UrlIdEncoder idEncoder, SimpleDateFormat simpleDateFormat, int page,
			int numberPerPage);

	public int countAssessmentsForUser(long id, boolean searchForPending, boolean searchForApproved);

	public int countAssessmentsForAssessorAndCredential(long decodedCredentialId, long assessorId, boolean searchForPending,
			boolean searchForApproved);

}
