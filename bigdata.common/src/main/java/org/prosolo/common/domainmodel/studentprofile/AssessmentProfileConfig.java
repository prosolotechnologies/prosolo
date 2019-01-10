package org.prosolo.common.domainmodel.studentprofile;

import javax.persistence.Embedded;
import javax.persistence.Entity;

/**
 * Represents assessment to be displayed on student's profile
 *
 * @author stefanvuckovic
 * @date 2018-10-11
 * @since 1.2.0
 */
@Entity
public abstract class AssessmentProfileConfig extends StudentProfileConfig {

    private static final long serialVersionUID = 6538772167089269061L;

    @Embedded
    private Grade grade;

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

}
