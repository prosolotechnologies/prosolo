package org.prosolo.services.productionFixes.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.productionFixes.ProductionFixesService;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                            
                            //delete target activity
                            delete(ta);
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
    
	@Inject private CredentialManager credentialManager;
	@Inject private Activity1Manager activityManager;
	@Inject private UserManager userManager;

	@Override
	public void fixUtaStudentCourses() throws Exception {
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
		String oldCredentialId = "QmD6A6y7";
		String newCredentialId = "ydPpl19d";
		
		fixCourses(oldCredentialId, newCredentialId, users, getActivitiyDataMap());
		
		// in order to open a transaction, call via service name
        ServiceLocator.getInstance().getService(ProductionFixesService.class).deleteUsersCredentials(oldCredentialId, users);
	}
	
	public void fixCourses(String sourceCredId, String destCredId, List<String> userIds, Map<String, ActivityMappingData> activityIdsMap) throws Exception {
		long sourceCredIdDecoded = idEncoder.decodeId(sourceCredId);
		long destCredIdDecoded = idEncoder.decodeId(destCredId);
		
		for (String userIdStr : userIds) {
			long userId = idEncoder.decodeId(userIdStr);
			User user = ServiceLocator.getInstance().getService(ProductionFixesService.class).loadUser(userId);
			
			// loading source target credential
			TargetCredential1 sourceCred = credentialManager.getTargetCredential(sourceCredIdDecoded, userId, false, false);
			// loading destination target credential
			TargetCredential1 destinationCred = credentialManager.getTargetCredential(destCredIdDecoded, userId, false, false);
			
			// if destination target credential is null, this means user has not enrolled yet. Enroll him/her.
			if (destinationCred == null) {
				credentialManager.enrollInCredential(destCredIdDecoded, userId, null);
				logger.info("Enrolled student " + user.getName() + " " + user.getLastname());
			}
			
			// traversing through all source target credential resources to compare them with the destination ones
			for (TargetCompetence1 tCompetence : sourceCred.getTargetCompetences()) {
				for (TargetActivity1 sourceTargetActivity : tCompetence.getTargetActivities()) {
					ActivityMappingData dataMapping = activityIdsMap.get(idEncoder.encodeId(sourceTargetActivity.getActivity().getId()));
					
					long destinationActivityId = idEncoder.decodeId(dataMapping.destinationActivityId);
					TargetActivity1 destinationTargetActivity = activityManager.getTargetActivity(destinationActivityId, userId);
					
					// check if source and destination activities has the same name, just to be sure we have copied ids properly
					
					if (destinationTargetActivity == null) {
						System.out.println();
					}
					if (!sourceTargetActivity.getTitle().equals(destinationTargetActivity.getTitle())) {
						throw new Exception("Mappings for source and destination activities with hashed ids are not good");
					}
					
					// if there is no response in destination target activity, copy it from the source target activity
					if (sourceTargetActivity.getResult() != null && destinationTargetActivity.getResult() == null) {
						destinationTargetActivity.setResult(sourceTargetActivity.getResult());
						destinationTargetActivity.setResultPostDate(sourceTargetActivity.getResultPostDate());
						saveEntity(destinationTargetActivity);
						
						logger.info("User " + user.getName() + " " + user.getLastname() + ": copied response for activity " + sourceTargetActivity.getTitle());
					}
					
					// if source activity is checked, mark destination activity to be checked
					if (sourceTargetActivity.isCompleted() && !destinationTargetActivity.isCompleted()) {
						activityManager.completeActivity(destinationTargetActivity.getId(), destinationTargetActivity.getTargetCompetence().getId(), destCredIdDecoded, userId, null);
					}
				}
			}
		}
	}

	@Override
	@Transactional (readOnly = false)
	public User loadUser(long userId) throws ResourceCouldNotBeLoadedException {
		return userManager.get(User.class, userId);
	}

	private Map<String, ActivityMappingData> getActivitiyDataMap() {
		Map<String, ActivityMappingData> activityIdsMap = new HashMap<>();
		activityIdsMap.put("ZmyNvqWd", new ActivityMappingData("ZmyNvqWd", "ZmyyOwQm", "Background"));
		activityIdsMap.put("bd9v5Gl7", new ActivityMappingData("bd9v5Gl7", "Jm42YKD7", "Micro-Learning Activity: Credential 1 Goals"));
		activityIdsMap.put("0Rz9K0N7", new ActivityMappingData("0Rz9K0N7", "b7Qp1xqd", "Micro-Learning Activity: Credential 1 Thinking About Mapping"));
		activityIdsMap.put("l7W9PbXm", new ActivityMappingData("l7W9PbXm", "Ndg9pr6R", "Micro-Learning Activity: Credential 1 Thinking About How You Will Earn Engagement Points"));
		activityIdsMap.put("e72D5MZm", new ActivityMappingData("e72D5MZm", "r7ODk1ZR", "Sleuthing the History of Early Civilizations"));
		activityIdsMap.put("w71q5gY7", new ActivityMappingData("w71q5gY7", "4Ropky6m", "Early Human History"));
		activityIdsMap.put("omNX93y7", new ActivityMappingData("omNX93y7", "wmwK0OLm", "Readings and Other Materials"));
		activityIdsMap.put("WdVj8VMd", new ActivityMappingData("WdVj8VMd", "8mrBgw8m", "Micro-Learning Check Quiz"));
		activityIdsMap.put("qd515pzd", new ActivityMappingData("qd515pzd", "NmM1zgjm", "Reilly Reading"));
		activityIdsMap.put("g7a0LeQd", new ActivityMappingData("g7a0LeQd", "vmXE0PAd", "Geography"));
		activityIdsMap.put("lRA85ZJ7", new ActivityMappingData("lRA85ZJ7", "KmknQrVR", "Mesopotamian Religion"));
		activityIdsMap.put("LmYeELDm", new ActivityMappingData("LmYeELDm", "l7pp94Q7", "Sargon and Akkad"));
		activityIdsMap.put("Jm4r5k37", new ActivityMappingData("Jm4r5k37", "r7ODk1xR", "Old Babylon"));
		activityIdsMap.put("b7QOqe5m", new ActivityMappingData("b7QOqe5m", "4RopkVkm", "Assyria"));
		activityIdsMap.put("NdgX40J7", new ActivityMappingData("NdgX40J7", "wmwK0nOm", "New Babylonia"));
		activityIdsMap.put("NmMxy02R", new ActivityMappingData("NmMxy02R", "8mrBgEEm", "Readings and Documents"));
		activityIdsMap.put("vmXz63r7", new ActivityMappingData("vmXz63r7", "ZmyyOXNm", "Micro-Learning Activity: Check Quiz"));
		activityIdsMap.put("KmkZXBpR", new ActivityMappingData("KmkZXBpR", "bd92ly37", "The Gift of the Nile"));
		activityIdsMap.put("l7pBYz2m", new ActivityMappingData("l7pBYz2m", "0Rzo1kom", "Egyptian Political History"));
		activityIdsMap.put("r7OXzLaR", new ActivityMappingData("r7OXzLaR", "KmnBYXKm", "Egyptian Society"));
		activityIdsMap.put("4Ro9oBjd", new ActivityMappingData("4Ro9oBjd", "lmqzQoed", "Egyptian Religious Development"));
		activityIdsMap.put("wmwND13R", new ActivityMappingData("wmwND13R", "QmD52Ded", "Readings and other materials"));
		activityIdsMap.put("8mroyqNd", new ActivityMappingData("8mroyqNd", "oRbeKlq7", "Micro-learning Activity: Egyptian Check Quiz"));
		activityIdsMap.put("ZmyNvqAd", new ActivityMappingData("ZmyNvqAd", "LmvVKpom", "Historical Geography"));
		activityIdsMap.put("Jm4r5kW7", new ActivityMappingData("Jm4r5kW7", "a7Kxy4eR", "IRV Political history"));
		activityIdsMap.put("b7QOqe0m", new ActivityMappingData("b7QOqe0m", "em8br9G7", "IRV society and culture"));
		activityIdsMap.put("NdgX40w7", new ActivityMappingData("NdgX40w7", "ydPpkeGd", "Chinese Historical Geography"));
		activityIdsMap.put("NmMxy0WR", new ActivityMappingData("NmMxy0WR", "Gm6b91qm", "Yangshao and Longshan Cultures"));
		activityIdsMap.put("vmXz63D7", new ActivityMappingData("vmXz63D7", "VmEeV6Wm", "Early Chinese Dynasties"));
		activityIdsMap.put("KmkZXBXR", new ActivityMappingData("KmkZXBXR", "37j2Oxg7", "Shang Political history"));
		activityIdsMap.put("l7pBYzLm", new ActivityMappingData("l7pBYzLm", "ndlqgx97", "Shang Social and Cultural history"));
		activityIdsMap.put("r7OXzL0R", new ActivityMappingData("r7OXzL0R", "Q70pqnL7", "Early Chinese readings and materials"));
		activityIdsMap.put("4Ro9oBad", new ActivityMappingData("4Ro9oBad", "l7WDGjnR", "Micro-learning Activity: Early Chinese Check Quiz"));
		activityIdsMap.put("wmwND1rR", new ActivityMappingData("wmwND1rR", "X7J301zR", "Early Andean Civilization"));
		activityIdsMap.put("8mroyqMd", new ActivityMappingData("8mroyqMd", "odxon8Gm", "Caral-Supe"));
		activityIdsMap.put("KmnyO1XR", new ActivityMappingData("KmnyO1XR", "GmZDeqJ7", "Olmec"));
		activityIdsMap.put("lmqnr1nd", new ActivityMappingData("lmqnr1nd", "vRLjeQ2d", "Maya"));
		activityIdsMap.put("QmDGgE6R", new ActivityMappingData("QmDGgE6R", "wR3wnB3R", "Zapotec and Isthmian"));
		activityIdsMap.put("oRbk3Oem", new ActivityMappingData("oRbk3Oem", "lmGGZE8m", "Cuello and Monte Alban"));
		activityIdsMap.put("LmvBqzXm", new ActivityMappingData("LmvBqzXm", "pmBng8rm", "Maya Religious Belief and Practice"));
		activityIdsMap.put("a7KQwGLm", new ActivityMappingData("a7KQwGLm", "Wme0L6pR", "Nomadic Societies"));
		activityIdsMap.put("em8D5GKm", new ActivityMappingData("em8D5GKm", "e72vGox7", "Hebrew Society"));
		activityIdsMap.put("ydPxnbrd", new ActivityMappingData("ydPxnbrd", "w7123OvR", "Of Trade and Resources"));
		activityIdsMap.put("VmEkM2P7", new ActivityMappingData("VmEkM2P7", "WdVq93pm", "Journal 1 - Key Similarities and Differences"));
		activityIdsMap.put("Gm6r5Y2m", new ActivityMappingData("Gm6r5Y2m", "omNZ0M8m", "Journal 2 - Monumental Architecture"));
		activityIdsMap.put("37jeMYYd", new ActivityMappingData("37jeMYYd", "qd5w2KJR", "Journal 3 - Family Choices"));
		activityIdsMap.put("ndlwk1jm", new ActivityMappingData("ndlwk1jm", "g7aXyjwd", "Discussion 1: Patriarchy and Matriarchy"));
		activityIdsMap.put("Q70153wR", new ActivityMappingData("Q70153wR", "lRAzY3km", "Discussion 2: Document Driven Discussion"));
		return activityIdsMap;
	}

	private class ActivityMappingData {
		String sourceActivityId;
		String destinationActivityId;
		String title;

		public ActivityMappingData(String sourceActivityId, String destinationActivityId, String title) {
			this.sourceActivityId = sourceActivityId;
			this.destinationActivityId = destinationActivityId;
			this.title = title;
		}
	}

}
