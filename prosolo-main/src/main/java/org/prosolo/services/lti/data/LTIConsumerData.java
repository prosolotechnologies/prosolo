package org.prosolo.services.lti.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author stefanvuckovic
 * @date 2019-05-31
 * @since 1.3.2
 */
@AllArgsConstructor
@Getter
@Setter
public class LTIConsumerData {

    private long id;
    private String keyLtiOne;
    private String secretLtiOne;
    private String keyLtiTwo;
    private String secretLtiTwo;
}
