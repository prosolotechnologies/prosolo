package org.prosolo.services.nodes.data.statusWall;

import lombok.Getter;
import lombok.Setter;

/**
 * Data object storing data for rendering a competence on the Status Wall.
 *
 * @author Nikola Milikic
 * @date 2019-03-29
 * @since 1.3
 */
@Getter @Setter
public class CompetencePostData extends AttachmentPreview {

    private long credentialId;
    private long competenceId;
    private boolean universityCreated;
    private String creatorName;
    private String duration;
}
