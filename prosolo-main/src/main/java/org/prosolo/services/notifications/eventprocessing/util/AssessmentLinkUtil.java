package org.prosolo.services.notifications.eventprocessing.util;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

/**
 * @author stefanvuckovic
 * @date 2018-03-15
 * @since 1.2.0
 */
public class AssessmentLinkUtil {

    private static Logger logger = Logger.getLogger(AssessmentLinkUtil.class);

    public static String getAssessmentNotificationLink(
            Context context,
            long credId,
            long compId,
            long compAssessmentId,
            AssessmentType assessmentType,
            AssessmentManager assessmentManager,
            UrlIdEncoder idEncoder,
            Session session,
            PageSection section) {
        long credAssessmentId = getCredentialAssessmentId(context, compAssessmentId, assessmentManager, session);
        return getAssessmentNotificationLink(credId, credAssessmentId, compId, compAssessmentId, assessmentType, idEncoder, section);
    }

    public static long getCredentialAssessmentId(Context ctx, long compAssessmentId,
                                                 AssessmentManager assessmentManager, Session session) {
        long credAssessmentId = Context.getIdFromSubContextWithName(ctx, ContextName.CREDENTIAL_ASSESSMENT);
        if (credAssessmentId <= 0) {
            credAssessmentId = assessmentManager
                    .getCredentialAssessmentIdForCompetenceAssessment(compAssessmentId, session);
        }
        return credAssessmentId;
    }

    public static String getAssessmentNotificationLink(
            long credId,
            long credAssessmentId,
            long compId,
            long compAssessmentId,
            AssessmentType assessmentType,
            UrlIdEncoder idEncoder,
            PageSection section) {

        if (credId > 0 && credAssessmentId > 0) {
            String encodedCredAssessmentId = idEncoder.encodeId(credAssessmentId);
            return section.getPrefix()
                    + "/credentials/" +
                    idEncoder.encodeId(credId) +
                    "/assessments/" +
                    (section == PageSection.MANAGE ? encodedCredAssessmentId
                            : (assessmentType == AssessmentType.PEER_ASSESSMENT
                                ? "peer/" + encodedCredAssessmentId
                                : (assessmentType == AssessmentType.INSTRUCTOR_ASSESSMENT
                                    ? "instructor" : "self")));
        }

        //if student section and cred id is not passed or credential assessment does not exist, we create notification for competence assessment page
        if (section == PageSection.STUDENT && compId > 0 && compAssessmentId > 0) {
            String encodedCompAssessmentId = idEncoder.encodeId(compAssessmentId);
            return section.getPrefix() + "/competences/" +
                    idEncoder.encodeId(compId) +
                    "/assessments/" +
                    (assessmentType == AssessmentType.PEER_ASSESSMENT
                        ? "peer/" + encodedCompAssessmentId
                        : (assessmentType == AssessmentType.INSTRUCTOR_ASSESSMENT
                            ? "instructor/" + encodedCompAssessmentId : "self"));
        }

        logger.debug("Assessment notification link can't be created");
        return null;
    }

    public static String getCompetenceAssessmentNotificationLinkForStudent(
            long credId,
            long compId,
            long compAssessmentId,
            AssessmentType assessmentType,
            UrlIdEncoder idEncoder) {
        if (credId > 0 && compId > 0 && compAssessmentId > 0) {
            String encodedCompAssessmentId = idEncoder.encodeId(compAssessmentId);
            return "/competences/" +
                    idEncoder.encodeId(compId) +
                    "/assessments/" +
                    (assessmentType == AssessmentType.PEER_ASSESSMENT
                            ? "peer/" + encodedCompAssessmentId
                            : (assessmentType == AssessmentType.INSTRUCTOR_ASSESSMENT
                            ? "instructor/" + encodedCompAssessmentId : "self")) +
                    "?credId=" + idEncoder.encodeId(credId);
        }

        logger.debug("Assessment notification link can't be created");
        return null;
    }
}
