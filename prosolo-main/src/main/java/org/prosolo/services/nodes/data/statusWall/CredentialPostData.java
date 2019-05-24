package org.prosolo.services.nodes.data.statusWall;

import lombok.Getter;
import lombok.Setter;

/**
 * Data object storing data for rendering a credential on the Status Wall.
 *
 * @author Nikola Milikic
 * @date 2019-03-29
 * @since 1.3
 */
@Getter @Setter
public class CredentialPostData extends AttachmentPreview {

    private long credentialId;
    private boolean universityCreated;
    private String creatorName;
    private String duration;
}
