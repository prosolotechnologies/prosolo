package org.prosolo.services.nodes.data.organization;

import org.prosolo.common.domainmodel.learningStage.LearningStage;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.services.nodes.data.UserData;
import twitter4j.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Bojan on 6/6/2017.
 */
public class OrganizationData implements Serializable {

    private long id;
    private String title;
    private List<UserData> admins;
    private boolean learningInStagesEnabled;
    private List<LearningStageData> learningStages;
    //storing learning stages marked for removal in a separate collection
    private List<LearningStageData> learningStagesForDeletion;

    public OrganizationData(){
        learningStages = new ArrayList<>();
        learningStagesForDeletion = new ArrayList<>();
    }

    public OrganizationData(Organization organization){
        this();
        this.id = organization.getId();
        this.title = organization.getTitle();
        this.learningInStagesEnabled = organization.isLearningInStagesEnabled();
    }

    public OrganizationData(Organization organization, List<UserData> chosenAdmins){
        this(organization);
        this.admins = chosenAdmins;
    }

    public OrganizationData(long id, String title){
        this();
        this.id = id;
        this.title = title;
    }

    public String getAdminsString() {
        String adminsString = "";
        if(admins != null) {
            for(UserData a : admins) {
                if(!adminsString.isEmpty()) {
                    adminsString += ", ";
                }
                adminsString += a.getFullName();
            }
        }
        return adminsString;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<UserData> getAdmins() {
        return admins;
    }

    public void setAdmins(List<UserData> admins) {
        this.admins = admins;
    }

    public List<LearningStageData> getLearningStages() {
        return learningStages;
    }

    public void addLearningStage(LearningStageData lStage) {
        learningStages.add(lStage);
    }

    public void addAllLearningStages(Collection<LearningStageData> learningStages) {
        this.learningStages.addAll(learningStages);
    }

    public List<LearningStageData> getLearningStagesForDeletion() {
        return learningStagesForDeletion;
    }

    public void addLearningStageForDeletion(LearningStageData learningStageForDeletion) {
        learningStagesForDeletion.add(learningStageForDeletion);
    }

    public boolean isLearningInStagesEnabled() {
        return learningInStagesEnabled;
    }

    public void setLearningInStagesEnabled(boolean learningInStagesEnabled) {
        this.learningInStagesEnabled = learningInStagesEnabled;
    }
}
