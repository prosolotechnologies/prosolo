package org.prosolo.services.user.data;

import lombok.*;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2019-02-04
 * @since 1.3
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudentAssessmentInfo implements Serializable {

    private long assessmentId;
    private boolean sentAssessmentNotification;

}
