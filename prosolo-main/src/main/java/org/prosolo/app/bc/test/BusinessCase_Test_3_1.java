package org.prosolo.app.bc.test;

import org.apache.log4j.Logger;
import org.prosolo.services.event.EventQueue;

/**
 * @author Nikola Milikic
 * @date 2019-01-29
 * @since 1.3
 */
public class BusinessCase_Test_3_1 extends BusinessCase_Test_3 {

    private static Logger logger = Logger.getLogger(BusinessCase_Test_3_1.class.getName());

    @Override
    protected void createAdditionalDataTest3(EventQueue events) {

    }

    @Override
    protected String getBusinessCaseInitLog() {
        return "Initializing business case - test 3.1";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
