package org.prosolo.services.nodes.data.organization;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.prosolo.common.domainmodel.organization.settings.AssessmentsPlugin;

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
    private boolean assessmentTokensEnabled;
    private boolean privateDiscussionEnabled;
    private int initialNumberOfTokensGiven;
    private int numberOfSpentTokensPerRequest;
    private int numberOfEarnedTokensPerAssessment;

    public AssessmentTokensPluginData(AssessmentsPlugin assessmentTokensPlugin) {
        this.pluginId = assessmentTokensPlugin.getId();
        this.assessmentTokensEnabled = assessmentTokensPlugin.isAssessmentTokensEnabled();
        this.privateDiscussionEnabled = assessmentTokensPlugin.isPrivateDiscussionEnabled();
        this.initialNumberOfTokensGiven = assessmentTokensPlugin.getInitialNumberOfTokensGiven();
        this.numberOfEarnedTokensPerAssessment = assessmentTokensPlugin.getNumberOfEarnedTokensPerAssessment();
        this.numberOfSpentTokensPerRequest = assessmentTokensPlugin.getNumberOfSpentTokensPerRequest();
    }
}
