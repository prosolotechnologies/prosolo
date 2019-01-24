package org.prosolo.common.domainmodel.assessment;

import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

import javax.persistence.*;
import java.util.Date;

/**
 * @author stefanvuckovic
 * @date 2019-01-24
 * @since 1.3
 */
@MappedSuperclass
public class Assessment extends BaseEntity {

    private AssessmentStatus status;

    private boolean approved;
    private Date dateApproved;

    private User student;
    private User assessor;
    private AssessmentType type;
    private int points;
    private Date lastAskedForAssessment;
    private boolean assessorNotified;
    private Date lastAssessment;
    private BlindAssessmentMode blindAssessmentMode = BlindAssessmentMode.OFF;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AssessmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentStatus status) {
        this.status = status;
    }

    @Column(name="approved")
    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public Date getDateApproved() {
        return dateApproved;
    }

    public void setDateApproved(Date dateApproved) {
        this.dateApproved = dateApproved;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public User getAssessor() {
        return assessor;
    }

    public void setAssessor(User assessor) {
        this.assessor = assessor;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AssessmentType getType() {
        return type;
    }

    public void setType(AssessmentType type) {
        this.type = type;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Date getLastAskedForAssessment() {
        return lastAskedForAssessment;
    }

    public void setLastAskedForAssessment(Date lastAskedForAssessment) {
        this.lastAskedForAssessment = lastAskedForAssessment;
    }

    public boolean isAssessorNotified() {
        return assessorNotified;
    }

    public void setAssessorNotified(boolean assessorNotified) {
        this.assessorNotified = assessorNotified;
    }

    public Date getLastAssessment() {
        return lastAssessment;
    }

    public void setLastAssessment(Date lastAssessment) {
        this.lastAssessment = lastAssessment;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public BlindAssessmentMode getBlindAssessmentMode() {
        return blindAssessmentMode;
    }

    public void setBlindAssessmentMode(BlindAssessmentMode blindAssessmentMode) {
        this.blindAssessmentMode = blindAssessmentMode;
    }
}
