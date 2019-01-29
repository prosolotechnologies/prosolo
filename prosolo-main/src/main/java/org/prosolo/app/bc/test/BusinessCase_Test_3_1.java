package org.prosolo.app.bc.test;

import org.apache.log4j.Logger;
import org.prosolo.app.bc.BaseBusinessCase5;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.ActivityResultType;
import org.prosolo.services.nodes.data.ActivityType;
import org.prosolo.services.nodes.data.competence.CompetenceData1;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
