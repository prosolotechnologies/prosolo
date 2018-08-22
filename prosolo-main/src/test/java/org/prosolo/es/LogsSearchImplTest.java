package org.prosolo.es;/**
 * Created by zoran on 10/10/16.
 */

import com.google.gson.Gson;
import org.junit.BeforeClass;
import org.junit.Test;
import org.prosolo.bigdata.common.dal.pojo.LogsFilter;
import org.prosolo.bigdata.common.dal.pojo.LogsRecord;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.core.stress.TestContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * zoran 10/10/16
 */
public class LogsSearchImplTest  extends TestContext {

    @Autowired private LogsSearch logsSearch;

    private static LogsFilter logsFilter;

    @BeforeClass
    public static void init(){
        logsFilter=new LogsFilter();
        logsFilter.setSize(1000);
        logsFilter.addCredential(3l);
        logsFilter.addEventType(EventType.ChangeProgress);

    }

    @Test
    public void testLogsSearch(){

       List<LogsRecord> logs= logsSearch.findLogsByFilter(logsFilter);
        System.out.println("FOUND LOGS:"+logs.size());
        Gson gson=new Gson();
        for(LogsRecord log:logs){
            System.out.println("LOG:"+gson.toJson(log));
        }

    }
}
