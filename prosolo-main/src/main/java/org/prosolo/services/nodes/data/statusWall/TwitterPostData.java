package org.prosolo.services.nodes.data.statusWall;

import lombok.Getter;
import lombok.Setter;

/**
 * Data object storing data for rendering a Twitter post on the Status Wall.
 *
 * @author Nikola Milikic
 * @date 2019-03-29
 * @since 1.3
 */
@Getter @Setter
public class TwitterPostData extends AttachmentPreview {

    private String nickname;
    private String profileUrl;
}
