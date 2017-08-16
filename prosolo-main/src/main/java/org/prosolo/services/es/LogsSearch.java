package org.prosolo.services.es;/**
 * Created by zoran on 09/10/16.
 */

import org.prosolo.bigdata.common.dal.pojo.LogsFilter;
import org.prosolo.bigdata.common.dal.pojo.LogsRecord;

import java.util.List;

/**
 * zoran 09/10/16
 */
public interface LogsSearch {
    List<LogsRecord> findLogsByFilter(LogsFilter logsFilter);
}
