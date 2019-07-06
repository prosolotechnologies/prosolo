package org.prosolo.services.nodes.data.organization;

import lombok.Getter;
import lombok.Setter;
import org.prosolo.common.domainmodel.organization.settings.LearningStagesPlugin;

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
public class LearningStagesPluginData implements Serializable {

    private long pluginId;
    private boolean enabled;
    private List<LearningStageData> learningStages;
    //storing learning stages marked for removal in a separate collection
    private List<LearningStageData> learningStagesForDeletion;

    public LearningStagesPluginData() {
        this.learningStages = new ArrayList<>();
        this.learningStagesForDeletion = new ArrayList<>();
    }

    public LearningStagesPluginData(LearningStagesPlugin learningStagesPlugin) {
        this();
        this.pluginId = learningStagesPlugin.getId();
        this.enabled = learningStagesPlugin.isEnabled();
    }

    public void addLearningStage(LearningStageData lStage) {
        learningStages.add(lStage);
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
