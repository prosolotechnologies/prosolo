package org.prosolo.app.bc.test;

import org.apache.log4j.Logger;
import org.prosolo.app.bc.BaseBusinessCase5;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.ResourceType;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.lti.ToolSetManager;

/**
 * @author stefanvuckovic
 * @date 2019-01-16
 * @since 1.2.0
 */
public class BusinessCase_Test_1_1 extends BaseBusinessCase5 {

    private static Logger logger = Logger.getLogger(BusinessCase_Test_1_1.class.getName());

    @Override
    protected void createAdditionalDataBC5(EventQueue events) throws Exception {
        //////////////////////////////////
        // Create LTI Global tool
        //////////////////////////////////
        LtiTool tool = new LtiTool();
        tool.setToolType(ResourceType.Global);
        tool.setName("External Tool 1");
        tool.setCreatedBy(userNickPowell);
        tool.setCredentialId(0);
        tool.setCompetenceId(-1);
        tool.setActivityId(-1);
        tool.setEnabled(true);
        tool.setOrganization(organization);
        tool.setUnit(unitSchoolOfEducation);
        tool.setUserGroup(userGroupScienceEducationStudents);

        ServiceLocator.getInstance().getService(ToolSetManager.class).saveToolSet(tool, "daa15cb1-ea0c-4409-ad74-ef01990457a2", "78d21840-ae37-4034-8aa8-e605c06f7ef7");
    }

    @Override
    protected String getBusinessCaseInitLog() {
        return "Initializing business case - test 1.1";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
