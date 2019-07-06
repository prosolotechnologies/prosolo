package org.prosolo.services.user.impl;

import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.CompetenceEvidence;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.studentprofile.*;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentSortOrder;
import org.prosolo.services.common.data.SelectableData;
import org.prosolo.services.common.data.SortOrder;
import org.prosolo.services.common.data.SortingOption;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.SocialNetworksManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
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
import org.prosolo.services.user.data.profile.factory.GradeDataFactory;
import org.prosolo.services.user.data.profile.grade.GradeData;
import org.prosolo.util.StringUtils;
import org.prosolo.web.profile.data.UserSocialNetworksData;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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
    @Inject private GradeDataFactory gradeDataFactory;
    @Inject private UrlIdEncoder idEncoder;

    @Override
    @Transactional
    public Optional<StudentProfileData> getStudentProfileData(String customProfileUrl) {
        try {
            Optional<ProfileSettingsData> profileSettingsData = getProfileSettingsData(customProfileUrl);

            if (profileSettingsData.isPresent()) {
                long userId = profileSettingsData.get().getUserId();

                UserData userData = userManager.getUserData(userId);
                UserSocialNetworksData userSocialNetworkData = socialNetworksManager.getUserSocialNetworkData(userId);
                return Optional.of(new StudentProfileData(userData, userSocialNetworkData, getProfileLearningData(userId), profileSettingsData.get()));
            } else {
                return Optional.empty();
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
    public ProfileLearningData getProfileLearningData(long userId) {
        return new ProfileLearningData(
                getProfileSummaryData(userId),
                getCredentialProfileData(userId));
    }

    private ProfileSummaryData getProfileSummaryData(long userId) {
        try {
            String query =
                    "SELECT type(conf), count(conf.id) " +
                    "FROM StudentProfileConfig conf " +
                    "WHERE conf.student.id = :userId " +
                    "GROUP BY type(conf)";
            List<Object[]> counts = (List<Object[]>) persistence.currentManager()
                    .createQuery(query)
                    .setLong("userId", userId)
                    .list();

            long numberOfCompletedCredentials = 0;
            long numberOfCompletedCompetences = 0;
            long numberOfCredentialAssessments = 0;
            long numberOfCompetenceAssessments = 0;
            long numberOfPiecesOfEvidence = 0;
            for (Object[] row : counts) {
                Class<? extends StudentProfileConfig> confClass = (Class<? extends StudentProfileConfig>) row[0];
                if (confClass == CredentialProfileConfig.class) {
                    numberOfCompletedCredentials = (long) row[1];
                } else if (confClass == CompetenceProfileConfig.class) {
                    numberOfCompletedCompetences = (long) row[1];
                } else if (confClass == CredentialAssessmentProfileConfig.class) {
                    numberOfCredentialAssessments = (long) row[1];
                } else if (confClass == CompetenceAssessmentProfileConfig.class) {
                    numberOfCompetenceAssessments = (long) row[1];
                } else if (confClass == CompetenceEvidenceProfileConfig.class) {
                    numberOfPiecesOfEvidence = (long) row[1];
                }
            }
            return new ProfileSummaryData(numberOfCompletedCredentials, numberOfCompletedCompetences, numberOfCredentialAssessments + numberOfCompetenceAssessments, numberOfPiecesOfEvidence);
        } catch (Exception e) {
            logger.error("error", e);
            throw new DbConnectionException("Error loading profile summary data");
        }
    }

    public List<CategorizedCredentialsProfileData> getCredentialProfileData(long userId) {
        try {
            String query =
                    "SELECT cpc, count(assessmentConf.id) FROM CredentialProfileConfig cpc " +
                    "INNER JOIN fetch cpc.targetCredential tc " +
                    "INNER JOIN fetch tc.credential c " +
                    "LEFT JOIN fetch c.category cat " +
                    "LEFT JOIN cpc.credentialAssessmentProfileConfigs assessmentConf " +
                    "WHERE cpc.student.id = :userId " +
                    "GROUP BY cpc " +
                    "ORDER BY cat.title";
            List<Object[]> confList = (List<Object[]>) persistence.currentManager()
                    .createQuery(query)
                    .setLong("userId", userId)
                    .list();
            List<CredentialProfileData> credentialProfileData = new ArrayList<>();
            confList.forEach(row -> credentialProfileData.add(credentialProfileDataFactory.getCredentialProfileData((CredentialProfileConfig) row[0], (long) row[1], 0)));
            return credentialProfileDataFactory.groupCredentialsByCategory(credentialProfileData);
        } catch (Exception e) {
            logger.error("error", e);
            throw new DbConnectionException("Error loading credential profile data");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompetenceProfileData> getCredentialCompetencesProfileData(long credProfileConfigId) {
        try {
            String query =
                    "SELECT conf, count(distinct evidenceConf.id), count(distinct assessmentConf.id) FROM CompetenceProfileConfig conf " +
                    "INNER JOIN fetch conf.credentialProfileConfig tc " +
                    "INNER JOIN fetch conf.targetCompetence tc " +
                    "INNER JOIN fetch tc.competence c " +
                    "LEFT JOIN conf.competenceAssessmentProfileConfigs assessmentConf " +
                    "LEFT JOIN conf.evidenceProfileConfigs evidenceConf " +
                    "WHERE conf.credentialProfileConfig.id = :credProfileConfigId " +
                    "GROUP BY conf";
            List<Object[]> confList = (List<Object[]>) persistence.currentManager()
                    .createQuery(query)
                    .setLong("credProfileConfigId", credProfileConfigId)
                    .list();
            return confList
                    .stream()
                    .map(row -> credentialProfileDataFactory.getCompetenceProfileData((CompetenceProfileConfig) row[0], (long) row[1], (long) row[2]))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("error", e);
            throw new DbConnectionException("Error loading credential competences profile data");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompetenceEvidenceProfileData> getCompetenceEvidenceProfileData(long competenceProfileConfigId) {
        try {
            String query =
                    "SELECT conf FROM CompetenceEvidenceProfileConfig conf " +
                    "INNER JOIN fetch conf.competenceEvidence ce " +
                    "INNER JOIN fetch ce.evidence " +
                    "WHERE conf.competenceProfileConfig.id = :compProfileConfigId";
            List<CompetenceEvidenceProfileConfig> confList = (List<CompetenceEvidenceProfileConfig>) persistence.currentManager()
                    .createQuery(query)
                    .setLong("compProfileConfigId", competenceProfileConfigId)
                    .list();
            return confList
                    .stream()
                    .map(evidenceProfileConfig -> credentialProfileDataFactory.getCompetenceEvidenceProfileData(evidenceProfileConfig))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("error", e);
            throw new DbConnectionException("Error loading evidence profile data");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssessmentByTypeProfileData> getCredentialAssessmentsProfileData(long credentialProfileConfigId) {
        try {
            String query =
                    "SELECT conf FROM CredentialAssessmentProfileConfig conf " +
                    "INNER JOIN fetch conf.credentialAssessment ca " +
                    "INNER JOIN fetch ca.assessor " +
                    "WHERE conf.credentialProfileConfig.id = :credProfileConfigId";
            List<CredentialAssessmentProfileConfig> confList = (List<CredentialAssessmentProfileConfig>) persistence.currentManager()
                    .createQuery(query)
                    .setLong("credProfileConfigId", credentialProfileConfigId)
                    .list();
            return credentialProfileDataFactory.getCredentialAssessmentsProfileData(confList);
        } catch (Exception e) {
            logger.error("error", e);
            throw new DbConnectionException("Error loading credential assessments profile data");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssessmentByTypeProfileData> getCompetenceAssessmentsProfileData(long competenceProfileConfigId) {
        try {
            String query =
                    "SELECT conf FROM CompetenceAssessmentProfileConfig conf " +
                    "INNER JOIN fetch conf.competenceAssessment ca " +
                    "INNER JOIN fetch ca.assessor " +
                    "WHERE conf.competenceProfileConfig.id = :compProfileConfigId";
            List<CompetenceAssessmentProfileConfig> confList = (List<CompetenceAssessmentProfileConfig>) persistence.currentManager()
                    .createQuery(query)
                    .setLong("compProfileConfigId", competenceProfileConfigId)
                    .list();
            return credentialProfileDataFactory.getCompetenceAssessmentsProfileData(confList);
        } catch (Exception e) {
            logger.error("error", e);
            throw new DbConnectionException("Error loading competency assessments profile data");
        }
    }

    @Override
    @Transactional
    public void updateProfileSettings(ProfileSettingsData profileSettings) {
        try {
            ProfileSettings user = loadResource(ProfileSettings.class, profileSettings.getId());
            user.setSummarySidebarEnabled(profileSettings.isSummarySidebarEnabled());
            user.setCustomProfileUrl(profileSettings.getCustomProfileUrl());
        } catch (DataIntegrityViolationException e) {
            logger.error("Error", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error saving profile settings with id " + profileSettings.getId());
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
            if (tc.getProgress() == 100) {
                CompetenceProfileConfig competenceProfileConfig = new CompetenceProfileConfig();
                competenceProfileConfig.setTargetCredential(config.getTargetCredential());
                competenceProfileConfig.setStudent(config.getStudent());
                competenceProfileConfig.setCredentialProfileConfig(config);
                competenceProfileConfig.setTargetCompetence(tc);
                saveEntity(competenceProfileConfig);
            }
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
            Optional<CredentialProfileConfig> credentialProfileConfigOpt = getCredentialProfileConfig(targetCredentialId, true);

            if (credentialProfileConfigOpt.isPresent()) {
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
                delete(credentialProfileConfigOpt.get());
            }
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
            Optional<CredentialProfileConfig> credProfileConfigOpt = getCredentialProfileConfig(targetCredentialId, false);
            List<CredentialAssessment> credentialAssessments = assessmentManager
                    .getSubmittedCredentialAssessments(
                            targetCredentialId,
                            SortOrder.<AssessmentSortOrder>builder()
                                    .addOrder(AssessmentSortOrder.ASSESSMENT_TYPE, SortingOption.ASC)
                                    .addOrder(AssessmentSortOrder.LAST_ASSESSMENT_DATE, SortingOption.ASC).build());
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
                List<CompetenceAssessment> competenceAssessments = assessmentManager.getSubmittedCompetenceAssessments(
                        targetCredentialId,
                        tc.getCompetence().getId(),
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

    private Optional<CredentialProfileConfig> getCredentialProfileConfig(long targetCredentialId, boolean lockObject) {
        String query =
                "SELECT conf FROM CredentialProfileConfig conf " +
                "WHERE conf.targetCredential.id = :tcId";
        Query q =  persistence.currentManager()
                .createQuery(query)
                .setLong("tcId", targetCredentialId);
        if (lockObject) {
            q.setLockOptions(LockOptions.UPGRADE);
        }
        return Optional.ofNullable((CredentialProfileConfig) q.uniqueResult());
    }

    @Override
    @Transactional
    public void updateCredentialProfileOptions(CredentialProfileOptionsBasicData profileOptionsData) {
        try {
            /*
            lock credential profile config to avoid parallel updates or deletes that can lead to
            inconsistent data in db
             */
            Optional<CredentialProfileConfig> credentialProfileConfigOpt = getCredentialProfileConfig(profileOptionsData.getTargetCredentialId(), true);
            if (!credentialProfileConfigOpt.isPresent()) {
                /*
                if credential is removed from profile in the meantime, we do not proceed with the update
                 */
                throw new StaleDataException("Credential removed from the profile in the meantime");
            }
            CredentialProfileConfig credentialProfileConfig = credentialProfileConfigOpt.get();
            updateCredentialAssessmentProfileOptions(profileOptionsData.getAssessments(), credentialProfileConfig.getId(),
                    credentialProfileConfig.getTargetCredential().getId(), credentialProfileConfig.getStudent().getId());
            updateCompetencesProfileOptions(profileOptionsData.getCompetences(), credentialProfileConfig.getId(),
                    credentialProfileConfig.getTargetCredential().getId(), credentialProfileConfig.getStudent().getId());
        } catch (StaleDataException e) {
            logger.error("error", e);
            throw e;
        } catch (Exception e) {
            logger.error("error", e);
            throw new DbConnectionException("Error updating credential profile options");
        }
    }

    private void updateCredentialAssessmentProfileOptions(List<SelectableData<Long>> credentialAssessmentsProfileOptions, long credProfileConfigId, long targetCredentialId, long userId) {
        for (SelectableData<Long> assessment : credentialAssessmentsProfileOptions) {
            Optional<CredentialAssessmentProfileConfig> credentialAssessmentProfileConfig = getCredentialAssessmentProfileConfig(assessment.getData(), credProfileConfigId);
            if (assessment.isSelected()) {
                if (!credentialAssessmentProfileConfig.isPresent()) {
                    //if assessment is not already displayed save config
                    CredentialAssessmentProfileConfig conf = new CredentialAssessmentProfileConfig();
                    conf.setCredentialAssessment((CredentialAssessment) persistence.currentManager().load(CredentialAssessment.class, assessment.getData()));
                    conf.setCredentialProfileConfig((CredentialProfileConfig) persistence.currentManager().load(CredentialProfileConfig.class, credProfileConfigId));
                    GradeData gradeSummary = assessmentManager.getCredentialAssessmentGradeSummary(assessment.getData());
                    setAssessmentConfigCommonData(conf, gradeSummary);
                    setStudentProfileCommonData(conf, targetCredentialId, userId);
                    saveEntity(conf);
                }
            } else {
                if (credentialAssessmentProfileConfig.isPresent()) {
                    //if evidence config exists delete it
                    delete(credentialAssessmentProfileConfig.get());
                }
            }
        }
    }

    /**
     *
     * @param competencesProfileOptions
     * @param credProfileConfigId
     * @param targetCredentialId
     * @param userId
     *
     * @throws StaleDataException when some of the competence configs we try to update does not exist in the database
     */
    private void updateCompetencesProfileOptions(List<CompetenceProfileOptionsBasicData> competencesProfileOptions, long credProfileConfigId, long targetCredentialId, long userId) {
        for (CompetenceProfileOptionsBasicData profileOptions : competencesProfileOptions) {
            Optional<CompetenceProfileConfig> competenceProfileConfig = getCompetenceProfileConfig(profileOptions.getTargetCompetenceId(), credProfileConfigId);
            if (!competenceProfileConfig.isPresent()) {
                /*
                this should never happen because competence profile config is tied to credential profile config
                and neither can exist without the other.
                 */
                throw new StaleDataException("Competence removed from the profile in the meantime");
            }
            updateCompetenceEvidenceProfileOptions(profileOptions.getEvidence(), competenceProfileConfig.get().getId(), targetCredentialId, userId);
            updateCompetenceAssessmentsProfileOptions(profileOptions.getAssessments(), competenceProfileConfig.get().getId(), targetCredentialId, userId);
        }
    }

    private void updateCompetenceEvidenceProfileOptions(List<SelectableData<Long>> evidencesProfileOptions, long competenceProfileConfigId, long targetCredentialId, long userId) {
        for (SelectableData<Long> evidence : evidencesProfileOptions) {
            Optional<CompetenceEvidenceProfileConfig> competenceEvidenceProfileConfig = getCompetenceEvidenceProfileConfig(evidence.getData(), competenceProfileConfigId);
            if (evidence.isSelected()) {
                if (!competenceEvidenceProfileConfig.isPresent()) {
                    //if evidence is not already displayed save config
                    CompetenceEvidenceProfileConfig conf = new CompetenceEvidenceProfileConfig();
                    conf.setCompetenceEvidence((CompetenceEvidence) persistence.currentManager().load(CompetenceEvidence.class, evidence.getData()));
                    conf.setCompetenceProfileConfig((CompetenceProfileConfig) persistence.currentManager().load(CompetenceProfileConfig.class, competenceProfileConfigId));
                    setStudentProfileCommonData(conf, targetCredentialId, userId);
                    saveEntity(conf);
                }
            } else {
                if (competenceEvidenceProfileConfig.isPresent()) {
                    //if evidence config exists delete it
                    delete(competenceEvidenceProfileConfig.get());
                }
            }
        }
    }

    private void updateCompetenceAssessmentsProfileOptions(List<SelectableData<Long>> assessmentsProfileOptions, long competenceProfileConfigId, long targetCredentialId, long userId) {
        for (SelectableData<Long> assessment : assessmentsProfileOptions) {
            Optional<CompetenceAssessmentProfileConfig> competenceAssessmentProfileConfig = getCompetenceAssessmentProfileConfig(assessment.getData(), competenceProfileConfigId);
            if (assessment.isSelected()) {
                if (!competenceAssessmentProfileConfig.isPresent()) {
                    //if assessment is not already displayed save config
                    CompetenceAssessmentProfileConfig conf = new CompetenceAssessmentProfileConfig();
                    conf.setCompetenceAssessment((CompetenceAssessment) persistence.currentManager().load(CompetenceAssessment.class, assessment.getData()));
                    conf.setCompetenceProfileConfig((CompetenceProfileConfig) persistence.currentManager().load(CompetenceProfileConfig.class, competenceProfileConfigId));
                    GradeData gradeData = assessmentManager.getCompetenceAssessmentGradeSummary(assessment.getData());
                    setAssessmentConfigCommonData(conf, gradeData);
                    setStudentProfileCommonData(conf, targetCredentialId, userId);
                    saveEntity(conf);
                }
            } else {
                if (competenceAssessmentProfileConfig.isPresent()) {
                    //if evidence config exists delete it
                    delete(competenceAssessmentProfileConfig.get());
                }
            }
        }
    }

    private void setStudentProfileCommonData(StudentProfileConfig conf, long targetCredentialId, long userId) {
        conf.setTargetCredential((TargetCredential1) persistence.currentManager().load(TargetCredential1.class, targetCredentialId));
        conf.setStudent((User) persistence.currentManager().load(User.class, userId));
    }

    private void setAssessmentConfigCommonData(AssessmentProfileConfig conf, GradeData gradeData) {
        conf.setGrade(gradeDataFactory.getGradeFromGradeData(gradeData));
    }

    private Optional<CredentialAssessmentProfileConfig> getCredentialAssessmentProfileConfig(long credAssessmentId, long credProfileConfigId) {
        String q =
                "SELECT conf FROM CredentialAssessmentProfileConfig conf " +
                "WHERE conf.credentialProfileConfig.id = :credProfileConfigId " +
                "AND conf.credentialAssessment.id = :credAssessmentId";
        CredentialAssessmentProfileConfig res = (CredentialAssessmentProfileConfig) persistence.currentManager()
                .createQuery(q)
                .setLong("credProfileConfigId", credProfileConfigId)
                .setLong("credAssessmentId", credAssessmentId)
                .uniqueResult();
        return Optional.ofNullable(res);
    }

    private Optional<CompetenceProfileConfig> getCompetenceProfileConfig(long targetCompId, long credentialProfileConfigId) {
        String q =
                "SELECT conf FROM CompetenceProfileConfig conf " +
                "WHERE conf.credentialProfileConfig.id = :credProfileConfigId " +
                "AND conf.targetCompetence.id = :tcId";
        CompetenceProfileConfig res = (CompetenceProfileConfig) persistence.currentManager()
                .createQuery(q)
                .setLong("credProfileConfigId", credentialProfileConfigId)
                .setLong("tcId", targetCompId)
                .uniqueResult();
        return Optional.ofNullable(res);
    }

    private Optional<CompetenceEvidenceProfileConfig> getCompetenceEvidenceProfileConfig(long compEvidenceId, long competenceProfileConfigId) {
        String q =
                "SELECT conf FROM CompetenceEvidenceProfileConfig conf " +
                "WHERE conf.competenceProfileConfig.id = :compProfileConfigId " +
                "AND conf.competenceEvidence.id = :compEvidenceId";
        CompetenceEvidenceProfileConfig res = (CompetenceEvidenceProfileConfig) persistence.currentManager()
                .createQuery(q)
                .setLong("compProfileConfigId", competenceProfileConfigId)
                .setLong("compEvidenceId", compEvidenceId)
                .uniqueResult();
        return Optional.ofNullable(res);
    }

    private Optional<CompetenceAssessmentProfileConfig> getCompetenceAssessmentProfileConfig(long compAssessmentId, long competenceProfileConfigId) {
        String q =
                "SELECT conf FROM CompetenceAssessmentProfileConfig conf " +
                "WHERE conf.competenceProfileConfig.id = :compProfileConfigId " +
                "AND conf.competenceAssessment.id = :compAssessmentId";
        CompetenceAssessmentProfileConfig res = (CompetenceAssessmentProfileConfig) persistence.currentManager()
                .createQuery(q)
                .setLong("compProfileConfigId", competenceProfileConfigId)
                .setLong("compAssessmentId", compAssessmentId)
                .uniqueResult();
        return Optional.ofNullable(res);
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<ProfileSettingsData> getProfileSettingsData(String customProfileUrl) {
        String q =
                "SELECT profileSettings " +
                "FROM ProfileSettings profileSettings " +
                "LEFT JOIN FETCH profileSettings.user user " +
                "WHERE profileSettings.customProfileUrl = :customProfileUrl";

        ProfileSettings res = (ProfileSettings) persistence.currentManager()
                .createQuery(q)
                .setString("customProfileUrl", customProfileUrl)
                .uniqueResult();

        if (res != null) {
            return Optional.of(new ProfileSettingsData(res));
        } else {
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProfileSettingsData> getProfileSettingsData(long userId) {
        String q =
                "SELECT profileSettings " +
                "FROM ProfileSettings profileSettings " +
                "LEFT JOIN profileSettings.user user " +
                "WHERE user.id = :userId";

        ProfileSettings res = (ProfileSettings) persistence.currentManager()
                .createQuery(q)
                .setLong("userId", userId)
                .uniqueResult();

        if (res != null) {
            return Optional.of(new ProfileSettingsData(res));
        } else {
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = false)
    public ProfileSettingsData generateProfileSettings(long userId, boolean summarySidebarEnabled, Session session) {
        User user = (User) session.get(User.class, userId);

        String nameLastName = generateCustomProfileURLPrefix(user.getName(), user.getLastname());

        // if profile URL is already occupied, try 10 more time with a different profile URL
        int retryTimes = 0;
        long suffix = userId;

        while (retryTimes < 10) {
            String newProfileUrl = null;
            try {
                newProfileUrl = nameLastName + "-" + idEncoder.encodeId(suffix);
                return saveProfileSettings(user, newProfileUrl, true, session);
            } catch (DataIntegrityViolationException e) {
                logger.debug("Error saving ProfileSettings for the user " + userId + ", the profile URL is already taken: " + newProfileUrl, e);
            }

            // as a new suffix use a generated long between 10000 and 100000
            suffix = 10000 + (int) (new Random().nextFloat() * (100000 - 1));

            retryTimes++;
        }

        if (retryTimes == 10) {
            logger.error("Error saving ProfileSettings for the user " + userId + ", the original profile URL and 10 variations of the same are already occupied.");
        }

        return null;
    }

    public static String generateCustomProfileURLPrefix(String name, String lastname) {
        // remove all non alphabetic characters from the name
        String strippedName = StringUtils.stripNonAlphanumericCharacters(name);

        // remove all non alphabetic characters from the last name
        String strippedLastname = StringUtils.stripNonAlphanumericCharacters(lastname);

        String nameLastName = strippedName + "-" + strippedLastname;

        // limit {name}-{lastName} combination to 40 characters
        if (nameLastName.length() > 40) {
            nameLastName = nameLastName.substring(0, 40);
        }
        return nameLastName;
    }

    private ProfileSettingsData saveProfileSettings(User user, String customProfileUrl, boolean summarySidebarEnabled, Session session) {
        ProfileSettings profileSettings = new ProfileSettings();
        profileSettings.setCustomProfileUrl(customProfileUrl);
        profileSettings.setSummarySidebarEnabled(summarySidebarEnabled);
        profileSettings.setUser(user);
        session.save(profileSettings);

        return new ProfileSettingsData(profileSettings);
    }

}
