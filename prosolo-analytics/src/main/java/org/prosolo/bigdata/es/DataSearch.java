package org.prosolo.bigdata.es;/**
 * Created by zoran on 31/07/16.
 */

import java.util.List;

/**
 * zoran 31/07/16
 */
public interface DataSearch {
    public List<Long> findCredentialMembers(long credId, int page, int limit);
}
