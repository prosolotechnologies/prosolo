package org.prosolo.bigdata.dal.cassandra;/**
 * Created by zoran on 20/07/16.
 */

/**
 * zoran 20/07/16
 */
public interface UserRecommendationsDBManager {
    public void insertStudentPreference(Long student, String resourcetype, Long resourceid, Double preference, Long timestamp);
}
