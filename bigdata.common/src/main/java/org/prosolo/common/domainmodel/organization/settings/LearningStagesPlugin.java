package org.prosolo.common.domainmodel.organization.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.prosolo.common.domainmodel.learningStage.LearningStage;
import org.prosolo.common.domainmodel.organization.Organization;

import javax.persistence.*;
import java.util.Set;

/**
 * @author Nikola Milikic
 * @date 2019-05-30
 * @since 1.3.2
 */
@Entity
@NoArgsConstructor
public class LearningStagesPlugin extends OrganizationPlugin {

    private Set<LearningStage> learningStages;

    @Builder
    public LearningStagesPlugin(long id, boolean enabled, OrganizationPluginType type, Organization organization,
                                    Set<LearningStage> learningStages) {
        super(id, enabled, type, organization);
        this.learningStages = learningStages;
    }

    @OneToMany(mappedBy = "learningStagesPlugin", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @OrderBy("order ASC")
    public Set<LearningStage> getLearningStages() {
        return learningStages;
    }

    public void setLearningStages(Set<LearningStage> learningStages) {
        this.learningStages = learningStages;
    }

}
