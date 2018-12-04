package org.prosolo.services.user.data.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2018-11-15
 * @since 1.2.0
 */
@AllArgsConstructor
@Getter
public class ProfileSummaryData implements Serializable {

    private static final long serialVersionUID = -3598064489753143720L;

    private final long numberOfCompletedCredentials;
    private final long numberOfCompletedCompetencies;
    private final long numberOfAssessments;
    private final long numberOfPiecesOfEvidence;


}
