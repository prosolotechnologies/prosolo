package org.prosolo.bigdata.common.dal.pojo;

import java.util.List;

/**
 * Created by zoran on 27/04/16.
 */
public class UserProfileFeatures {
    public Long getStudent() {
        return student;
    }

    public void setStudent(Long student) {
        this.student = student;
    }

    private Long student;

    public Long getCourse() {
        return course;
    }

    public void setCourse(Long course) {
        this.course = course;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    private Long course;

    private String profile;

    private List<String> features;

    public UserProfileFeatures(Long course, Long student, String profile, List<String> features){
        this.course=course;
        this.student=student;
        this.profile=profile;
        this.features=features;
    }

}
