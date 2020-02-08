package org.prosolo.services.nodes.data.statusWall;

import lombok.Getter;
import lombok.Setter;
import org.prosolo.services.nodes.data.ActivityType;

/**
 * Data object storing data for rendering an activity on the Status Wall.
 *
 * @author Nikola Milikic
 * @date 2019-03-29
 * @since 1.3
 */
@Getter @Setter
public class ActivityPostData extends AttachmentPreview {

    private long credentialId;
    private long competenceId;
    private long activityId;
    private ActivityType activityType;
    private boolean universityCreated;
    private String creatorName;
    private String duration;
}
