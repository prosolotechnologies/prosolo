package org.prosolo.services.user.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2019-03-25
 * @since 1.3
 */
@Getter
@Setter
@AllArgsConstructor
public class UserAssessmentTokenData implements Serializable {

    private boolean assessmentTokensEnabled;
    private boolean userAvailableForAssessments;
    private int numberOfTokensAvailable;
}
