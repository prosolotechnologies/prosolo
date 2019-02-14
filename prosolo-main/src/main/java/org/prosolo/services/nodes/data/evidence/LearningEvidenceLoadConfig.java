package org.prosolo.services.nodes.data.evidence;

import lombok.Builder;
import lombok.Data;

/**
 * @author stefanvuckovic
 * @date 2018-12-05
 * @since 1.2.0
 */
@Builder
@Data
public class LearningEvidenceLoadConfig {

    private final boolean loadTags;
    private final boolean loadCompetences;
    private final boolean loadCompetenceTitle;
    private final boolean loadUserName;
}
