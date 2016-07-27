package org.prosolo.bigdata.dal.cassandra;/**
 * Created by zoran on 20/07/16.
 */

/**
 * zoran 20/07/16
 */
public interface UserRecommendationsDBManager {
   // void insertStudentPreferenceRecord(Long student, String resourcetype, Long resourceid, Double preference, Long timestamp);
    void insertStudentPreferenceForDate(Long student, String resourcetype, Long resourceid, Double preference, Long dateEpoch);

    Double getStudentPreferenceForDate(Long student, String resourcetype, Long resourceid, Long dateEpoch);
}
