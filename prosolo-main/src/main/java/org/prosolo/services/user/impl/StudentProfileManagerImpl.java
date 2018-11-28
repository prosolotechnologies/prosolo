package org.prosolo.services.user.impl;

import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.studentprofile.CompetenceProfileConfig;
import org.prosolo.common.domainmodel.studentprofile.CredentialProfileConfig;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentSortOrder;
import org.prosolo.services.common.data.SortOrder;
import org.prosolo.services.common.data.SortingOption;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.SocialNetworksManager;
import org.prosolo.services.user.StudentProfileManager;
import org.prosolo.services.user.UserManager;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.user.data.parameterobjects.CompetenceAssessmentWithGradeSummaryData;
import org.prosolo.services.user.data.parameterobjects.CompetenceProfileOptionsParam;
import org.prosolo.services.user.data.parameterobjects.CredentialAssessmentWithGradeSummaryData;
import org.prosolo.services.user.data.parameterobjects.CredentialProfileOptionsParam;
import org.prosolo.services.user.data.profile.*;
import org.prosolo.services.user.data.profile.factory.CredentialProfileDataFactory;
import org.prosolo.services.user.data.profile.factory.CredentialProfileOptionsDataFactory;
import org.prosolo.web.profile.data.UserSocialNetworksData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author stefanvuckovic
 * @date 2018-11-15
 * @since 1.2.0
 */
@Service
public class StudentProfileManagerImpl extends AbstractManagerImpl implements StudentProfileManager {

    @Inject private UserManager userManager;
    @Inject private SocialNetworksManager socialNetworksManager;
    @Inject private CredentialProfileDataFactory credentialProfileDataFactory;
    @Inject private CredentialProfileOptionsDataFactory credentialProfileOptionsDataFactory;
    @Inject private Competence1Manager competenceManager;
    @Inject private AssessmentManager assessmentManager;

    @Override
    public Optional<StudentProfileData> getStudentProfileData(long userId) {
        try {
            UserData userData = userManager.getUserData(userId);
            if (userData == null) {
                return Optional.empty();
            } else {
                UserSocialNetworksData userSocialNetworkData = socialNetworksManager.getUserSocialNetworkData(userId);
                return Optional.of(new StudentProfileData(userData, userSocialNetworkData, null, getCredentialProfileData(userId)));
            }
        } catch (DbConnectionException e) {
            logger.error("error", e);
            throw e;
        } catch (Exception e) {
            logger.error("error", e);
            throw new DbConnectionException("Error loading student profile bean");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorizedCredentialsProfileData> getCredentialProfileData(long userId) {
        try {
            String query =
                    "SELECT cpc FROM CredentialProfileConfig cpc " +
                            "INNER JOIN fetch cpc.targetCredential tc " +
                            "INNER JOIN fetch tc.credential c " +
                            "LEFT JOIN fetch c.category cat " +
                            "WHERE cpc.student.id = :userId " +
                            "ORDER BY cat.title";
            List<CredentialProfileConfig> confList = (List<CredentialProfileConfig>) persistence.currentManager()
                    .createQuery(query)
                    .setLong("userId", userId)
                    .list();
            List<CredentialProfileData> credentialProfileData = new ArrayList<>();
            confList.forEach(conf -> credentialProfileData.add(credentialProfileDataFactory.getCredentialProfileData(conf)));
            return credentialProfileDataFactory.groupCredentialsByCategory(credentialProfileData);
        } catch (Exception e) {
            logger.error("error", e);
            throw new DbConnectionException("Error loading credential profile data");
        }
    }

    @Override
    @Transactional
    public void addCredentialsToProfile(long userId, List<Long> idsOfTargetCredentialsToAdd) {
        try {
            for (Long id : idsOfTargetCredentialsToAdd) {
                CredentialProfileConfig config = new CredentialProfileConfig();
                config.setStudent((User) persistence.currentManager().load(User.class, userId));
                TargetCredential1 tc = (TargetCredential1) persistence.currentManager().load(TargetCredential1.class, id);
                config.setTargetCredential(tc);
                config.setCredentialProfileConfigTargetCredential(tc);
                saveEntity(config);
                saveCompetenceProfileConfigForCredentialCompetences(config);
            }
        } catch (Exception e) {
            logger.error("error", e);
            throw new DbConnectionException("Error adding credentials to student profile");
        }
    }

    private void saveCompetenceProfileConfigForCredentialCompetences(CredentialProfileConfig config) {
        List<TargetCompetence1> targetCompetences = competenceManager.getTargetCompetencesForCredentialAndUser(
                config.getTargetCredential().getCredential().getId(),
                config.getStudent().getId());
        for (TargetCompetence1 tc : targetCompetences) {
            CompetenceProfileConfig competenceProfileConfig = new CompetenceProfileConfig();
            competenceProfileConfig.setTargetCredential(config.getTargetCredential());
            competenceProfileConfig.setStudent(config.getStudent());
            competenceProfileConfig.setCredentialProfileConfig(config);
            competenceProfileConfig.setTargetCompetence(tc);
            saveEntity(competenceProfileConfig);
        }
    }

    @Override
    @Transactional
    public void removeCredentialFromProfile(long userId, long targetCredentialId) {
        try {
            /*
            load and lock credential profile config to make sure that profile config is not updated
            at the same time for this or related objects (competence, assessment, evidence configs)
             */
            CredentialProfileConfig credentialProfileConfig = getCredentialProfileConfig(targetCredentialId, true);

            //delete competence evidence from profile
            String query =
                    "DELETE FROM CompetenceEvidenceProfileConfig conf " +
                    "WHERE conf.student.id = :userId " +
                    "AND conf.targetCredential.id = :tCredId";
            int affected = persistence.currentManager().createQuery(query)
                    .setLong("userId", userId)
                    .setLong("tCredId", targetCredentialId)
                    .executeUpdate();
            logger.info("Number of pieces of evidence removed from profile: " + affected);
            //delete competence assessments from profile
            query =
                    "DELETE FROM CompetenceAssessmentProfileConfig conf " +
                    "WHERE conf.student.id = :userId " +
                    "AND conf.targetCredential.id = :tCredId";
            affected = persistence.currentManager().createQuery(query)
                    .setLong("userId", userId)
                    .setLong("tCredId", targetCredentialId)
                    .executeUpdate();
            logger.info("Number of competence assessments removed from profile: " + affected);
            //delete competences from profile
            query =
                    "DELETE FROM CompetenceProfileConfig conf " +
                    "WHERE conf.student.id = :userId " +
                    "AND conf.targetCredential.id = :tCredId";
            affected = persistence.currentManager().createQuery(query)
                    .setLong("userId", userId)
                    .setLong("tCredId", targetCredentialId)
                    .executeUpdate();
            logger.info("Number of competences removed from profile: " + affected);
            //delete credential assessments from profile
            query =
                    "DELETE FROM CredentialAssessmentProfileConfig conf " +
                    "WHERE conf.student.id = :userId " +
                    "AND conf.targetCredential.id = :tCredId";
            affected = persistence.currentManager().createQuery(query)
                    .setLong("userId", userId)
                    .setLong("tCredId", targetCredentialId)
                    .executeUpdate();
            logger.info("Number of credential assessments removed from profile: " + affected);
            //delete credential from profile
            delete(credentialProfileConfig);
        } catch (Exception e) {
            logger.error("error", e);
            throw new DbConnectionException("Error removing credential from student profile");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CredentialProfileOptionsData getCredentialProfileOptions(long targetCredentialId) {
        try {
            TargetCredential1 targetCredential = (TargetCredential1) persistence.currentManager().load(TargetCredential1.class, targetCredentialId);
            CredentialProfileConfig credentialProfileConfig = getCredentialProfileConfig(targetCredentialId, false);
            Optional<CredentialProfileConfig> credProfileConfigOpt = Optional.ofNullable(credentialProfileConfig);
            List<CredentialAssessment> credentialAssessments = assessmentManager
                    .getCredentialAssessments(
                            targetCredentialId,
                            true,
                            SortOrder.<AssessmentSortOrder>builder().addOrder(
                                    AssessmentSortOrder.LAST_ASSESSMENT_DATE,
                                    SortingOption.ASC).build());
            List<CredentialAssessmentWithGradeSummaryData> credentialAssessmentsWithGradeSummaryData =
                    credentialAssessments
                            .stream()
                            .map(ca -> new CredentialAssessmentWithGradeSummaryData(ca, assessmentManager.getCredentialAssessmentGradeSummary(ca.getId())))
                            .collect(Collectors.toList());
            List<TargetCompetence1> targetCompetences = competenceManager.getTargetCompetencesForCredentialAndUser(targetCredential.getCredential().getId(), targetCredential.getUser().getId());
            List<CompetenceProfileOptionsParam> competenceProfileOptionsParams = new ArrayList<>();
            for (TargetCompetence1 tc : targetCompetences) {
                Optional<CompetenceProfileConfig> competenceProfileConfig = credProfileConfigOpt.isPresent()
                        ? credProfileConfigOpt.get().getCompetenceProfileConfigs().stream().filter(conf -> conf.getTargetCompetence().getId() == tc.getId()).findFirst()
                        : Optional.empty();
                List<CompetenceAssessment> competenceAssessments = assessmentManager.getCredentialCompetenceAssessments(
                        targetCredentialId,
                        tc.getCompetence().getId(),
                        tc.getUser().getId(),
                        true,
                        SortOrder.<AssessmentSortOrder>builder().addOrder(
                            AssessmentSortOrder.LAST_ASSESSMENT_DATE,
                            SortingOption.ASC).build());
                List<CompetenceAssessmentWithGradeSummaryData> competenceAssessmentsWithGradeSummaryData =
                        competenceAssessments
                                .stream()
                                .map(ca -> new CompetenceAssessmentWithGradeSummaryData(ca, assessmentManager.getCompetenceAssessmentGradeSummary(ca.getId())))
                                .collect(Collectors.toList());
                competenceProfileOptionsParams.add(new CompetenceProfileOptionsParam(tc, competenceProfileConfig, competenceAssessmentsWithGradeSummaryData));
            }
            return credentialProfileOptionsDataFactory.getCredentialProfileOptionsData(
                    new CredentialProfileOptionsParam(targetCredential, credentialAssessmentsWithGradeSummaryData, credProfileConfigOpt, competenceProfileOptionsParams));
        } catch (Exception e) {
            logger.error("error", e);
            throw new DbConnectionException("Error loading credential profile data");
        }
    }

    public CredentialProfileConfig getCredentialProfileConfig(long targetCredentialId, boolean lockObject) {
        String query =
                "SELECT conf FROM CredentialProfileConfig conf " +
                "WHERE conf.targetCredential.id = :tcId";
        Query q =  persistence.currentManager()
                .createQuery(query)
                .setLong("tcId", targetCredentialId);
        if (lockObject) {
            q.setLockOptions(LockOptions.UPGRADE);
        }
        return (CredentialProfileConfig) q.uniqueResult();
    }
}
