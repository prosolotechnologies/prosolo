package org.prosolo.services.nodes.factory;

import org.prosolo.common.domainmodel.learningStage.LearningStage;
import org.prosolo.services.nodes.data.LearningResourceLearningStage;
import org.prosolo.services.nodes.data.organization.LearningStageData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2017-11-17
 * @since 1.2.0
 */
@Component
public class LearningResourceLearningStageDataFactory {

    /**
     *
     * @param resultSet - each list element is object array where first element is {@link LearningStage} object
     *                  and the second is learning resource id if there is a learning resource created for this learning stage
     * @return
     */
    public List<LearningResourceLearningStage> getLearningResourceLearningStages(List<Object[]> resultSet) {
        List<LearningResourceLearningStage> stages = new ArrayList<>();
        //indicates whether first stage for which learning resource is not created is found or not
        boolean firstNotCreatedStageSet = false;
        for (Object[] row : resultSet) {
            LearningStage ls = (LearningStage) row[0];
            Long lrId = (Long) row[1];
			/*
			learning resource for the given stage can be created if it is not already created and it is the first stage for
			which learning resource is not created
			 */
            boolean canBeCreated = lrId == null && !firstNotCreatedStageSet;
            if (canBeCreated) {
                firstNotCreatedStageSet = true;
            }
            stages.add(new LearningResourceLearningStage(
                    new LearningStageData(ls.getId(), ls.getTitle(), ls.getOrder(), false, false),
                    lrId != null ? lrId.longValue() : 0, canBeCreated));
        }

        return stages;
    }
}
