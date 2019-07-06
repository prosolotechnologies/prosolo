package org.prosolo.services.lti.data;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.prosolo.common.domainmodel.lti.ResourceType;

/**
 * @author stefanvuckovic
 * @date 2019-05-31
 * @since 1.3.2
 */
@Builder
@Getter
@Setter
public class LTIToolData {

    private long id;
    private String launchUrl;
    private ResourceType toolType;
    private boolean enabled;
    private boolean deleted;
    private long organizationId;
    private long unitId;
    private long userGroupId;
    private LTIConsumerData consumer;

}
