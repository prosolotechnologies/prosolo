package org.prosolo.services.nodes;

import java.text.DateFormat;
import java.util.List;

import org.prosolo.services.nodes.data.AssessmentData;
import org.prosolo.services.nodes.data.AssessmentRequestData;
import org.prosolo.services.nodes.data.FullAssessmentData;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public interface AssessmentManager {

	public void requestAssessment(AssessmentRequestData assessmentRequestData);

	public List<AssessmentData> getAllAssessmentsForCredential(long credentialId, long assessorId, boolean searchForPending,
			boolean searchForApproved, UrlIdEncoder idEncoder, DateFormat simpleDateFormat);
	
	public FullAssessmentData getFullAssessmentData(long id, UrlIdEncoder encoder, DateFormat dateFormat);

	public Long countAssessmentsForUserAndCredential(long userId, long credentialId);

	public void approveCredential(long credentialAssessmentId,  long targetCredentialId, String reviewText);

}
