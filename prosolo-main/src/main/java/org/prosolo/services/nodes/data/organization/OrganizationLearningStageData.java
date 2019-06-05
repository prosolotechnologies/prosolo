package org.prosolo.services.nodes.data.organization;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2019-03-19
 * @since 1.3
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationLearningStageData implements Serializable {

    private boolean learningInStagesEnabled;
    private List<LearningStageData> learningStages = new ArrayList<>();
    //storing learning stages marked for removal in a separate collection
    private List<LearningStageData> learningStagesForDeletion = new ArrayList<>();

    public void addLearningStage(LearningStageData lStage) {
        learningStages.add(lStage);
    }

    public void addAllLearningStages(Collection<LearningStageData> learningStages) {
        this.learningStages.addAll(learningStages);
    }

    public void addLearningStageForDeletion(LearningStageData learningStageForDeletion) {
        learningStagesForDeletion.add(learningStageForDeletion);
    }

    public void resetLearningStages(List<LearningStageData> learningStages) {
        learningStagesForDeletion.clear();
        this.learningStages.clear();
        this.learningStages.addAll(learningStages);
    }
}
