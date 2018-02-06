package org.prosolo.services.nodes.data.assessments;

import org.prosolo.services.nodes.data.AssessmentDiscussionMessageData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2017-10-26
 * @since 1.1.0
 */
public class CompetenceAssessmentsSummaryData implements Serializable {

    private static final long serialVersionUID = 8332256225462068436L;

    private long id;
    private String title;
    private List<ActivityAssessmentsSummaryData> activitiesAssessmentSummaryData;

    public CompetenceAssessmentsSummaryData() {
        this.activitiesAssessmentSummaryData = new ArrayList<>();
    }

    public void addActivitySummary(ActivityAssessmentsSummaryData actSummary) {
        activitiesAssessmentSummaryData.add(actSummary);
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

    public List<ActivityAssessmentsSummaryData> getActivitiesAssessmentSummaryData() {
        return activitiesAssessmentSummaryData;
    }

    public void setActivitiesAssessmentSummaryData(List<ActivityAssessmentsSummaryData> activitiesAssessmentSummaryData) {
        this.activitiesAssessmentSummaryData = activitiesAssessmentSummaryData;
    }

}
