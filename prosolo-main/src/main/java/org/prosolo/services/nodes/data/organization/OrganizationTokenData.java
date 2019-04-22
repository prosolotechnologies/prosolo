package org.prosolo.services.nodes.data.organization;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2019-03-19
 * @since 1.3
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationTokenData implements Serializable {

    private boolean assessmentTokensEnabled;
    private int initialNumberOfTokensGiven;
    private int numberOfSpentTokensPerRequest;
    private int numberOfEarnedTokensPerAssessment;
}
