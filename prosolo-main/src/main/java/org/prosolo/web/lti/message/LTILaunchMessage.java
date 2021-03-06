package org.prosolo.web.lti.message;

import org.prosolo.core.spring.security.authentication.lti.util.LTIConstants;
import org.prosolo.web.lti.validator.EmptyValidator;
import org.prosolo.web.lti.validator.EqualValuesValidator;
import org.prosolo.web.lti.validator.NullValidator;

public class LTILaunchMessage extends LTIMessage {

    private LtiMessageParameter messageType;
    private LtiMessageParameter consumerKey;
    private LtiMessageParameter userFirstName;
    private LtiMessageParameter userLastName;
    private LtiMessageParameter userEmail;
    private LtiMessageParameter resultUrl;
    private LtiMessageParameter resultSourcedId;
    private LtiMessageParameter userID;
    private LtiMessageParameter roles;
    private LtiMessageParameter launchPresentationReturnURL;
    //private String contextID;
    //private String contextType;
    //private String resourceLinkID;
    //private List<String> roleScopeMentorList;
    //private String toolConsumerInstanceGUID;
    //private String contextTitle;
    //private String contextLabel;
    //private String resourceLinkTitle;
    //private String resourceLinkDescription;
    //private String userFullName;
    //private String userImage;
    //private String toolConsumerInstanceName;

    public LTILaunchMessage() {
        messageType = new LtiMessageParameter("Message type", new NullValidator(new EmptyValidator(
                new EqualValuesValidator(null, LTIConstants.MESSAGE_TYPE_LTILAUNCH))));
        consumerKey = new LtiMessageParameter("Consumer key", new NullValidator(new EmptyValidator(null)));
        userFirstName = new LtiMessageParameter("First name",null);
        userLastName = new LtiMessageParameter("Last name", null);
        userEmail = new LtiMessageParameter("Email", new NullValidator(new EmptyValidator(null)));
        userID = new LtiMessageParameter("User id", new NullValidator(new EmptyValidator(null)));
        resultUrl = new LtiMessageParameter("Result URL", null);
        resultSourcedId = new LtiMessageParameter("Result sourced id", null);
        launchPresentationReturnURL = new LtiMessageParameter("Launch presentation return URL", null);
        roles=new LtiMessageParameter("Roles", null);
    }

    public String getMessageType() {
        return messageType.getParameter();
    }

    public void setMessageType(String messageType) throws Exception {
        this.messageType.setParameter(messageType);
    }

    public String getConsumerKey() {
        return consumerKey.getParameter();
    }

    public void setConsumerKey(String consumerKey) throws Exception {
        this.consumerKey.setParameter(consumerKey);
    }

    public String getUserFirstName() {
        return userFirstName.getParameter();
    }

    public void setUserFirstName(String userFirstName) throws Exception {
        this.userFirstName.setParameter(userFirstName);
    }

    public String getUserLastName() {
        return userLastName.getParameter();
    }

    public void setUserLastName(String userLastName) throws Exception {
        this.userLastName.setParameter(userLastName);
    }

    public String getUserEmail() {
        return userEmail.getParameter();
    }

    public void setUserEmail(String userEmail) throws Exception {
        this.userEmail.setParameter(userEmail);
    }

    public String getResultUrl() {
        return resultUrl.getParameter();
    }

    public void setResultUrl(String resultUrl) throws Exception {
        this.resultUrl.setParameter(resultUrl);
    }

    public String getResultSourcedId() {
        return resultSourcedId.getParameter();
    }

    public void setResultSourcedId(String resultSourcedId) throws Exception {
        this.resultSourcedId.setParameter(resultSourcedId);
    }

    public String getUserID() {
        return userID.getParameter();
    }

    public void setUserID(String userID) throws Exception {
        this.userID.setParameter(userID);
    }

    public String getRoles() {
        return roles.getParameter();
    }

    public void setRoles(String roles) throws Exception {
        this.roles.setParameter(roles);
    }

    public String getLaunchPresentationReturnURL() {
        return launchPresentationReturnURL.getParameter();
    }

    public void setLaunchPresentationReturnURL(String launchPresentationReturnURL) throws Exception {
        this.launchPresentationReturnURL.setParameter(launchPresentationReturnURL);
    }


}
