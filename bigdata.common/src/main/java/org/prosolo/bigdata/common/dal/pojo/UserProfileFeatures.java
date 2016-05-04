package org.prosolo.bigdata.common.dal.pojo;

import java.util.ArrayList;
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



    private Long course;

    private String profile;


    public List<ProfileFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<ProfileFeature> features) {
        this.features = features;
    }

    private List<ProfileFeature> features;

    public UserProfileFeatures(Long course, Long student, String profile, List<ProfileFeature> features){
        this.course=course;
        this.student=student;
        this.profile=profile;
        this.features=features;

    }
    public class ProfileFeature{
        private String featurename;
        private int value;

        public String getQuartile() {
            return quartile;
        }

        public void setQuartile(String quartile) {
            this.quartile = quartile;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public String getFeaturename() {
            return featurename;
        }

        public void setFeaturename(String featurename) {
            this.featurename = featurename;
        }

        private String quartile;

    }

}
