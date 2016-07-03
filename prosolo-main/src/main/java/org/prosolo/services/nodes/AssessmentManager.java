package org.prosolo.services.nodes;

import java.text.DateFormat;
import java.util.List;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.data.ActivityDiscussionMessageData;
import org.prosolo.services.nodes.data.AssessmentData;
import org.prosolo.services.nodes.data.AssessmentRequestData;
import org.prosolo.services.nodes.data.FullAssessmentData;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public interface AssessmentManager {

	public long requestAssessment(AssessmentRequestData assessmentRequestData);

	public List<AssessmentData> getAllAssessmentsForCredential(long credentialId, long assessorId, boolean searchForPending,
			boolean searchForApproved, UrlIdEncoder idEncoder, DateFormat simpleDateFormat);
	
	public FullAssessmentData getFullAssessmentData(long id, UrlIdEncoder encoder, User user, DateFormat dateFormat);

	public Long countAssessmentsForUserAndCredential(long userId, long credentialId);

	public void approveCredential(long credentialAssessmentId,  long targetCredentialId, String reviewText);

	public long createActivityDiscussion(long targetActivityId, long competenceAssessmentId, List<Long> participantIds,long senderId);

	public ActivityDiscussionMessageData addCommentToDiscussion(long actualDiscussionId, long senderId, String comment) throws ResourceCouldNotBeLoadedException;

	public void editCommentContent(long activityMessageId, long userId, String newContent) throws ResourceCouldNotBeLoadedException;

	public void approveCompetence(long decodedCompetenceAssessmentId);

	public void markDiscussionAsSeen(long userId, long discussionId);

	public Long getAssessmentIdForUser(long userId, long targetCredentialId);

}
