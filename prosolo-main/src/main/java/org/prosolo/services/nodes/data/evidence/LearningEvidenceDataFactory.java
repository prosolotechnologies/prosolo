package org.prosolo.services.nodes.data.evidence;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.CompetenceEvidence;
import org.prosolo.common.domainmodel.credential.LearningEvidence;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.nodes.data.BasicObjectInfo;
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

    public LearningEvidenceData getCompetenceLearningEvidenceData(LearningEvidence evidence, CompetenceEvidence compEvidence, Set<Tag> tags) {
        LearningEvidenceData evidenceData = new LearningEvidenceData();
        evidenceData.setId(evidence.getId());
        evidenceData.setUserId(evidence.getUser().getId());
        evidenceData.setTitle(evidence.getTitle());
        evidenceData.setText(evidence.getDescription());
        if (tags != null) {
            evidenceData.setTags(tags.stream().map(Tag::getTitle).collect(Collectors.toSet()));
            evidenceData.setTagsString(AnnotationUtil.getAnnotationsAsSortedCSV(tags));
        }
        evidenceData.setType(evidence.getType());
        evidenceData.setUrl(evidence.getUrl());
        evidenceData.setDateCreated(DateUtil.getMillisFromDate(evidence.getDateCreated()));
        if (compEvidence != null) {
            evidenceData.setCompetenceEvidenceId(compEvidence.getId());
            evidenceData.setDateAttached(DateUtil.getMillisFromDate(compEvidence.getDateCreated()));
        }
        return evidenceData;
    }

    public LearningEvidenceData getLearningEvidenceData(LearningEvidence evidence, Set<Tag> tags, List<BasicObjectInfo> competences) {
        LearningEvidenceData ev = getCompetenceLearningEvidenceData(evidence, null, tags);
        if (competences != null) {
            ev.addCompetences(competences);
        }
        return ev;
    }
}