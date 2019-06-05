package org.prosolo.services.nodes.data.organization;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.prosolo.common.domainmodel.organization.settings.AssessmentTokensPlugin;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2019-03-19
 * @since 1.3
 */
@Getter
@Setter
@NoArgsConstructor
public class AssessmentTokensPluginData implements Serializable {

    private long pluginId;
    private boolean enabled;
    private int initialNumberOfTokensGiven;
    private int numberOfSpentTokensPerRequest;
    private int numberOfEarnedTokensPerAssessment;

    public AssessmentTokensPluginData(AssessmentTokensPlugin assessmentTokensPlugin) {
        this.pluginId = assessmentTokensPlugin.getId();
        this.enabled = assessmentTokensPlugin.isEnabled();
        this.initialNumberOfTokensGiven = assessmentTokensPlugin.getInitialNumberOfTokensGiven();
        this.numberOfEarnedTokensPerAssessment = assessmentTokensPlugin.getNumberOfEarnedTokensPerAssessment();
        this.numberOfSpentTokensPerRequest = assessmentTokensPlugin.getNumberOfSpentTokensPerRequest();
    }
}
