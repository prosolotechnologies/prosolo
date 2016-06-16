package org.prosolo.bigdata.dal.persistence;/**
 * Created by zoran on 09/01/16.
 */

import java.util.List;

/**
 * zoran 09/01/16
 */
public interface ClusteringDAO {
    @SuppressWarnings({ "unchecked" })
    List<Long> getAllCoursesIds();
    List<Long> getAllCredentialsIds();
}
