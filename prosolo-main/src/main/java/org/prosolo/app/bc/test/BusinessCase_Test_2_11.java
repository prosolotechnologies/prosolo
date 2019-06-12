package org.prosolo.app.bc.test;

import org.apache.log4j.Logger;
import org.prosolo.app.bc.BaseBusinessCase5;
import org.prosolo.common.event.EventQueue;

/**
 * @author stefanvuckovic
 * @date 2019-06-12
 * @since 1.3.2
 */
public class BusinessCase_Test_2_11 extends BaseBusinessCase5 {

    private static Logger logger = Logger.getLogger(BusinessCase_Test_2_11.class.getName());


    @Override
    protected void createAdditionalDataBC5(EventQueue events) throws Exception {

    }

    @Override
    protected String getBusinessCaseInitLog() {
        return "Initializing business case - test 2.10";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
