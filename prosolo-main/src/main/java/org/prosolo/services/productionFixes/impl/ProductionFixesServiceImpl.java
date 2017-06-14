package org.prosolo.services.productionFixes.impl;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivityConfig;
import org.prosolo.common.domainmodel.annotation.AnnotatedResource;
import org.prosolo.common.domainmodel.annotation.Annotation1;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.outcomes.SimpleOutcome;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.productionFixes.ProductionFixesService;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * Created by stefanvuckovic on 6/14/17.
 */

@Service("org.prosolo.services.productionFixes.ProductionFixesService")
public class ProductionFixesServiceImpl extends AbstractManagerImpl implements ProductionFixesService {

    private static final long serialVersionUID = 3153996561406682298L;

    private static Logger logger = Logger.getLogger(ProductionFixesServiceImpl.class);

    @Inject private CredentialManager credManager;
    @Inject private UrlIdEncoder idEncoder;


    @Override
    @Transactional
    public void deleteUsersCredentials() throws DbConnectionException {
        List<String> users = Arrays.asList(
                "ydPD2EXd",
                "QmDPLyYm",
                "VmEEL9am",
                "bd9EB41d",
                "X7JZ2bb7",
                "Gm6oLxjd",
                "Q70alE8R",
                "vRLx2K0R",
                "37jwWbKR",
                "lmGOLnkm",
                "odxrxL3R",
                "lmqOWjV7");
        deleteUsersCredentials("QmD6A6y7", users);
    }

    @Override
    @Transactional
    public void deleteUsersCredentials(String credId, List<String> userIds) throws DbConnectionException {
        logger.info("Custom fix: Deleting user credentials started");
        try {
            for (String userId : userIds) {
                TargetCredential1 tc = credManager.getTargetCredential(
                        idEncoder.decodeId(credId), idEncoder.decodeId(userId), false, false);
                if (tc != null) {
                    //delete all assessments
                    deleteAssessments(tc.getId());

                    for (TargetCompetence1 tComp : tc.getTargetCompetences()) {
                        //delete all social activities referencing target competence
                        deleteSocialActivitiesReferencingUserCompetence(tComp.getId());
                        for (TargetActivity1 ta : tComp.getTargetActivities()) {
                            //delete social activities referencing target activity
                            deleteSocialActivitiesReferencingUserActivity(ta.getId());

                            //delete activity results comments
                            deleteActivityResultCommentsForUserActivity(ta.getId());

                            //delete outcomes if exist for target activity
                            deleteUserActivityOutcomes(ta.getId());
                        }
                        //delete target competence
                        delete(tComp);
                    }
                    //delete target credential
                    delete(tc);

                    logger.info("User credential for user " + idEncoder.decodeId(userId) + " deleted");
                } else {
                    logger.info("User credential for user " + idEncoder.decodeId(userId) + " does not exist");
                }
            }
            logger.info("Custom fix: Deleting user credentials finished");
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            throw new DbConnectionException();
        }
    }

    private void deleteUserActivityOutcomes(long taId) {
        List<SimpleOutcome> outcomes = getOutcomesForUserActivity(taId);
        for (SimpleOutcome o : outcomes) {
            delete(o);
        }
    }

    private void deleteAssessments(long tcId) {
        List<CredentialAssessment> assessments = getAllAssessmentsForUserCredential(tcId);
        for (CredentialAssessment ca : assessments) {
            delete(ca);
        }
    }

    private List<CredentialAssessment> getAllAssessmentsForUserCredential(long targetCredId) {
        String query = "SELECT a FROM CredentialAssessment  a " +
                "WHERE a.targetCredential.id = :tCredId";

        return persistence.currentManager()
                .createQuery(query)
                .setLong("tCredId", targetCredId)
                .list();
    }

    private void deleteSocialActivitiesReferencingUserCompetence(long targetCompId) {
        List<SocialActivity1> sActivities = getSocialActivitiesReferencingUserCompetence(
                targetCompId);
        deleteSocialActivities(sActivities);
    }

    private void deleteSocialActivitiesReferencingUserActivity(long targetActivityId) {
        List<SocialActivity1> sActivitiesReferencingActivity =
                getSocialActivitiesReferencingUserActivity(targetActivityId);
        deleteSocialActivities(sActivitiesReferencingActivity);
    }

    private void deleteSocialActivities(List<SocialActivity1> activities) {
        for (SocialActivity1 sa : activities) {
            //delete social activity configs
            for (SocialActivityConfig c : getSocialActivityConfigs(sa.getId())) {
                delete(c);
            }
            //delete social activity comments
            List<Comment1> comments = getSocialActivityComments(sa.getId());
            deleteComments(comments);
            //delete social activity annotations
            List<Annotation1> annotations = getAnnotations(sa.getId(), AnnotatedResource.SocialActivity);
            for (Annotation1 a : annotations) {
                delete(a);
            }
            delete(sa);
        }
    }

    private List<SocialActivityConfig> getSocialActivityConfigs(long socialActivityId) {
        String query = "SELECT c FROM SocialActivityConfig c " +
                "WHERE c.socialActivity.id = :saId";

        return persistence.currentManager()
                .createQuery(query)
                .setLong("saId", socialActivityId)
                .list();
    }

    private List<SocialActivity1> getSocialActivitiesReferencingUserCompetence(long targetCompId) {
        String query = "SELECT sa FROM CompetenceCompleteSocialActivity sa " +
                "WHERE sa.targetCompetenceObject.id = :tCompId";

        return persistence.currentManager()
                .createQuery(query)
                .setLong("tCompId", targetCompId)
                .list();
    }

    private List<SocialActivity1> getSocialActivitiesReferencingUserActivity(long targetActivityId) {
        String query = "SELECT sa FROM ActivityCompleteSocialActivity sa " +
                "WHERE sa.targetActivityObject.id = :tActId";

        return persistence.currentManager()
                .createQuery(query)
                .setLong("tActId", targetActivityId)
                .list();
    }

    private List<SimpleOutcome> getOutcomesForUserActivity(long targetActivityId) {
        String query = "SELECT o FROM SimpleOutcome o " +
                "WHERE o.activity.id = :tActId";

        return persistence.currentManager()
                .createQuery(query)
                .setLong("tActId", targetActivityId)
                .list();
    }

    private void deleteActivityResultCommentsForUserActivity(long targetActivityId) {
        List<Comment1> comments = getActivityResultCommentsForUserActivity(targetActivityId);
        deleteComments(comments);
    }

    private void deleteComments(List<Comment1> comments) {
        for (Comment1 c : comments) {
            List<Annotation1> annotations = getAnnotations(c.getId(), AnnotatedResource.Comment);
            for (Annotation1 a : annotations) {
                delete(a);
            }
            delete(c);
        }
    }

    private List<Comment1> getSocialActivityComments(long saId) {
        return getComments(saId, CommentedResourceType.SocialActivity);
    }

    private List<Comment1> getActivityResultCommentsForUserActivity(long targetActivityId) {
        return getComments(targetActivityId, CommentedResourceType.ActivityResult);
    }

    private List<Comment1> getComments(long resId, CommentedResourceType type) {
        String query =
                "SELECT comment " +
                        "FROM Comment1 comment " +
                        "WHERE comment.resourceType = :resType " +
                        "AND comment.commentedResourceId = :resourceId";

        return persistence.currentManager()
                .createQuery(query)
                .setString("resType", type.name())
                .setLong("resourceId", resId)
                .list();
    }

    private List<Annotation1> getAnnotations(long resId, AnnotatedResource res) {
        String query =
                "SELECT a FROM Annotation1 a " +
                        "WHERE a.annotatedResource = :res " +
                        "AND a.annotatedResourceId = :resId";

        return persistence.currentManager()
                .createQuery(query)
                .setString("res", res.name())
                .setLong("resId", resId)
                .list();
    }

}
