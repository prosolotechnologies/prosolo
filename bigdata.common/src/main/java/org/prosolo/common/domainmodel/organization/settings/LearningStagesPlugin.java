package org.prosolo.common.domainmodel.organization.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.prosolo.common.domainmodel.learningStage.LearningStage;

import javax.persistence.*;
import java.util.Set;

/**
 * @author Nikola Milikic
 * @date 2019-05-30
 * @since 1.3.2
 */
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningStagesPlugin extends OrganizationPlugin {

    private Set<LearningStage> learningStages;

    @OneToMany(mappedBy = "learningStagesPlugin", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @OrderBy("order ASC")
    public Set<LearningStage> getLearningStages() {
        return learningStages;
    }

    public void setLearningStages(Set<LearningStage> learningStages) {
        this.learningStages = learningStages;
    }

}
