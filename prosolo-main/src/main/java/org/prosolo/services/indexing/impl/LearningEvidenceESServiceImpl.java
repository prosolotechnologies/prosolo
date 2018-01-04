package org.prosolo.services.indexing.impl;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.credential.LearningEvidence;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.util.ElasticsearchUtil;
import org.prosolo.services.indexing.AbstractBaseEntityESServiceImpl;
import org.prosolo.services.indexing.LearningEvidenceESService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * @author stefanvuckovic
 * @date 2017-12-07
 * @since 1.2.0
 */
@Service("org.prosolo.services.indexing.LearningEvidenceESService")
public class LearningEvidenceESServiceImpl extends AbstractBaseEntityESServiceImpl implements LearningEvidenceESService {

    private static Logger logger = Logger.getLogger(LearningEvidenceESServiceImpl.class);

    @Override
    @Transactional
    public void saveEvidence(LearningEvidence evidence) {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
            builder.field("id", evidence.getId());
            builder.field("userId", evidence.getUser().getId());
            builder.field("name", evidence.getTitle());
            builder.endObject();

            System.out.println("JSON: " + builder.prettyPrint().string());

            String fullIndexName = ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_EVIDENCE, evidence.getOrganization().getId());

            indexNode(builder, String.valueOf(evidence.getId()), fullIndexName, ESIndexTypes.EVIDENCE);
        } catch (IOException e) {
            logger.error("Error", e);
        }
    }
}
