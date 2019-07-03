package org.prosolo.services.nodes.data.evidence;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.CompetenceEvidence;
import org.prosolo.common.domainmodel.credential.LearningEvidence;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.nodes.data.CompetencyBasicObjectInfo;
import org.prosolo.util.nodes.AnnotationUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author stefanvuckovic
 * @date 2017-12-05
 * @since 1.2.0
 */
@Component
public class LearningEvidenceDataFactory {

    public LearningEvidenceData getCompetenceLearningEvidenceData(LearningEvidence evidence, CompetenceEvidence competenceEvidence, Set<Tag> tags, List<CompetencyBasicObjectInfo> competences, LearningEvidenceLoadConfig loadConfig) {
        LearningEvidenceData evidenceData = new LearningEvidenceData();
        evidenceData.setId(evidence.getId());
        evidenceData.setUserId(evidence.getUser().getId());
        if (loadConfig.isLoadUserName()) {
            evidenceData.setUserFullName(evidence.getUser().getFullName());
        }
        evidenceData.setTitle(evidence.getTitle());
        evidenceData.setText(evidence.getDescription());
        if (loadConfig.isLoadTags() && tags != null) {
            evidenceData.setTags(tags.stream().map(Tag::getTitle).collect(Collectors.toSet()));
            evidenceData.setTagsString(AnnotationUtil.getAnnotationsAsSortedCSV(tags));
        }
        evidenceData.setType(evidence.getType());
        evidenceData.setUrl(evidence.getUrl());
        evidenceData.setDateCreated(DateUtil.getMillisFromDate(evidence.getDateCreated()));

        if (competenceEvidence != null) {
            evidenceData.setCompetenceEvidenceId(competenceEvidence.getId());
            evidenceData.setDateAttached(DateUtil.getMillisFromDate(competenceEvidence.getDateCreated()));
            evidenceData.setRelationToCompetence(competenceEvidence.getDescription());
        }

        if (loadConfig.isLoadCompetenceTitle()) {
            evidenceData.setCompetenceTitle(competenceEvidence.getCompetence().getCompetence().getTitle());
        }

        if (loadConfig.isLoadCompetences() && competences != null) {
            evidenceData.addCompetences(competences);
        }

        return evidenceData;
    }
}
