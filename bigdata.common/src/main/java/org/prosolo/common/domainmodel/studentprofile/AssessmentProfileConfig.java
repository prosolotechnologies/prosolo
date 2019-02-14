package org.prosolo.common.domainmodel.studentprofile;

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

    /*
        the following two values are normalized on agreed scale because we have different grading methods and
        we need to be able to threat them the same and compare them
         */
    //grade student received
    private int grade;
    //max grade for the resource
    private int maxGrade;

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public int getMaxGrade() {
        return maxGrade;
    }

    public void setMaxGrade(int maxGrade) {
        this.maxGrade = maxGrade;
    }
}
