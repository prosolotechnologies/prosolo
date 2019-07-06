package org.prosolo.services.nodes.data.statusWall;

import lombok.Getter;
import lombok.Setter;
import org.prosolo.services.nodes.data.ActivityType;

/**
 * Data object storing data for rendering a credential on the Status Wall.
 *
 * @author Nikola Milikic
 * @date 2019-03-29
 * @since 1.3
 */
@Getter @Setter
public class CommentPostData extends AttachmentPreview {

    private long credentialId;
    private long competenceId;

    // if comment on activity
    private long activityId;
    private ActivityType activityType;
}
