package org.prosolo.services.indexing.impl;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.util.ElasticsearchUtil;
import org.prosolo.services.indexing.AbstractBaseEntityESServiceImpl;
import org.prosolo.services.indexing.RubricsESService;
import org.prosolo.services.nodes.RubricManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-25
 * @since 1.0.0
 */

@Service("org.prosolo.services.indexing.RubricsESService")
public class RubricsESServiceImpl extends AbstractBaseEntityESServiceImpl implements RubricsESService{

    private static Logger logger = Logger.getLogger(RubricsESServiceImpl.class);

    @Inject
    private RubricManager rubricManager;

    @Override
    @Transactional
    public void saveRubric(long rubricId) {
        try{
            XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
            builder.field("id",rubricId);
            builder.field("name",rubricManager.getRubricName(rubricId));
            builder.endObject();

            System.out.println("JSON: " + builder.prettyPrint().string());

            String fullIndexName = ESIndexNames.INDEX_RUBRIC_NAME +
                    ElasticsearchUtil.getRubricIndexSuffix(rubricId);

            indexNode(builder, String.valueOf(rubricId), fullIndexName,
                    ESIndexTypes.RUBRIC);
        }catch (IOException e){
            logger.error(e);
            e.printStackTrace();
        }
    }


}
