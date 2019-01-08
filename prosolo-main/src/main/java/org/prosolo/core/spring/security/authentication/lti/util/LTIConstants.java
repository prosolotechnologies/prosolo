package org.prosolo.core.spring.security.authentication.lti.util;

public class LTIConstants {
	
	//required parameters
	public static final String TOOL_SET_ID = "id";
	public static final String TOOL_ID = "id";
	public static final String MESSAGE_TYPE = "lti_message_type";
	public static final String LTI_VERSION = "lti_version";
	public static final String RESOURCE_LINK_ID = "resource_link_id";
	public static final String REG_KEY = "reg_key";
	public static final String REG_PASSWORD = "reg_password";
	public static final String TC_PROFILE_URL = "tc_profile_url";
	public static final String LAUNCH_PRESENTATION_RETURN_URL = "launch_presentation_return_url";
	
	//optionalparameters
	public static final String RESOURCE_LINK_TITLE = "resource_link_title";
	public static final String CUSTOM_RESOURCE_LINK_TITLE = "custom_resource_link_title";
	public static final String RESOURCE_LINK_DESCRIPTION = "resource_link_description";
	public static final String CUSTOM_RESOURCE_LINK_DESCRIPTION = "custom_resource_link_description";
	public static final String USER_ID = "user_id";
	public static final String USER_IMAGE = "user_image";
	public static final String CUSTOM_USER_IMAGE = "custom_user_image";
	public static final String ROLES = "roles";
	public static final String LIS_PERSON_SOURCEDID = "lis_person_sourcedid";
	public static final String CUSTOM_LIS_PERSON_SOURCEDID = "custom_lis_person_sourcedid";
	public static final String LIS_PERSON_NAME_GIVEN = "lis_person_name_given";
	public static final String CUSTOM_LIS_PERSON_NAME_GIVEN = "custom_lis_person_name_given";
	public static final String LIS_PERSON_NAME_FAMILY = "lis_person_name_family";
	public static final String CUSTOM_LIS_PERSON_NAME_FAMILY = "custom_lis_person_name_family";
	public static final String LIS_PERSON_NAME_FULL = "lis_person_name_full";
	public static final String CUSTOM_LIS_PERSON_NAME_FULL = "custom_lis_person_name_full";
	public static final String LIS_PERSON_CONTACT_EMAIL_PRIMARY = "lis_person_contact_email_primary";
	public static final String CUSTOM_LIS_PERSON_CONTACT_EMAIL_PRIMARY = "custom_lis_person_contact_email_primary";
	public static final String LIS_COURSE_OFFERING_SOURCEDID = "lis_course_offering_sourcedid";
	public static final String CUSTOM_LIS_COURSE_OFFERING_SOURCEDID = "custom_lis_course_offering_sourcedid";
	public static final String LIS_COURSE_SECTION_SOURCEDID = "lis_course_section_sourcedid";
	public static final String CUSTOM_LIS_COURSE_SECTION_SOURCEDID = "custom_lis_course_section_sourcedid";
	public static final String LIS_RESULT_SOURCEDID = "lis_result_sourcedid";
	public static final String CONTEXT_ID = "context_id";
	public static final String CONTEXT_TYPE = "context_type";
	public static final String CONTEXT_TITLE = "context_title";
	public static final String CUSTOM_CONTEXT_TITLE = "custom_context_title";
	public static final String CONTEXT_LABEL = "context_label";
	public static final String CUSTOM_CONTEXT_LABEL = "custom_context_label";
	public static final String LAUNCH_PRESENTATION_LOCALE = "launch_presentation_locale";
	public static final String LAUNCH_PRESENTATION_CSS_URL = "launch_presentation_css_url";
	public static final String LAUNCH_PRESENTATION_DOCUMENT_TARGET = "launch_presentation_document_target";
	public static final String LAUNCH_PRESENTATION_WIDTH = "launch_presentation_width";
	public static final String LAUNCH_PRESENTATION_HEIGHT = "launch_presentation_height";
	public static final String TOOL_CONSUMER_INSTANCE_GUID = "tool_consumer_instance_guid";
	public static final String TOOL_CONSUMER_INSTANCE_NAME = "tool_consumer_instance_name";
	public static final String CUSTOM_TOOL_CONSUMER_INSTANCE_NAME = "custom_tool_consumer_instance_name";
	public static final String TOOL_CONSUMER_INSTANCE_DESCRIPTION = "tool_consumer_instance_description";
	public static final String CUSTOM_TOOL_CONSUMER_INSTANCE_DESCRIPTION = "custom_tool_consumer_instance_description";
	public static final String TOOL_CONSUMER_INSTANCE_URL = "tool_consumer_instance_url";
	public static final String CUSTOM_TOOL_CONSUMER_INSTANCE_URL = "custom_tool_consumer_instance_url";
	public static final String TOOL_CONSUMER_INSTANCE_CONTACT_EMAIL = "tool_consumer_instance_contact_email";
	public static final String CUSTOM_TOOL_CONSUMER_INSTANCE_CONTACT_EMAIL = "custom_tool_consumer_instance_contact_email";
	public static final String ROLE_SCOPE_MENTOR = "role_scope_mentor";
	public static final String CUSTOM = "custom_";
	//lti 2 custom params
	public static final String LTI2_PERSON_FIST_NAME = "Person.name.given";
	public static final String LTI2_PERSON_LAST_NAME = "Person.name.family";
	public static final String LTI2_PERSON_EMAIL = "Person.email.primary";
	public static final String LTI2_PERSON_USER_ID = "User.id";
	public static final String LTI2_RESULT_SOURCED_ID = "Result.sourcedId";
	public static final String LTI2_RESULT_URL = "Result.url";

	
	
	//security
	public static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
	public static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
	public static final String OAUTH_TIMESTAMP = "oauth_timestamp";
	public static final String OAUTH_NONCE = "oauth_nonce";
	public static final String OAUTH_VERSION = "oauth_version";
	public static final String OAUTH_SIGNATURE = "oauth_signature";
	public static final String OAUTH_CALLBACK = "oauth_callback";
	
	
	//values
	public static final String MESSAGE_TYPE_TPREGISTRATION = "ToolProxyRegistrationRequest";
	public static final String MESSAGE_TYPE_LTILAUNCH = "basic-lti-launch-request";
	public static final String LTI_VERSION_ONE = "LTI-1p0";
	public static final String LTI_VERSION_TWO = "LTI-2p0";
	public static final String OAUTH_SIGNATURE_METHOD_HMAC_SHA1 = "HMAC-SHA1";
	public static final String OAUTH_VERSION_ONE = "1.0";
	public static final String OAUTH_CALLBACK_VALUE = "about:blank";
	public static final String TOOL_PROXY_REGISTRATION_RESPONSE_STATUS_SUCCESS = "success";
	public static final String TOOL_PROXY_REGISTRATION_RESPONSE_STATUS_FAILURE = "failure";
	
	//HTTP Requests
	public static final String POST_REQUEST = "POST";
	public static final String GET_REQUEST = "GET";
	public static final String PUT_REQUEST = "PUT";
	public static final String DELETE_REQUEST = "DELETE";
	
	//contexts
	public static final String TOOL_PROXY_CONTEXT = "http://www.imsglobal.org/imspurl/lti/v2/ctx/ToolProxy";
	
	//types
	public static final String TOOL_PROXY_TYPE = "ToolProxy";
	
	//Message Handler message type
	public static final String RES_HANDLER_MESSAGE_TYPE_VARIABLE = "variable";
	public static final String RES_HANDLER_MESSAGE_TYPE_FIXED = "fixed";
	
	//toolservic type
	public static final String REST_SERVICE_PROFILE = "RestServiceProfile";
	
	//formats
	public static final String FORMAT_TOOL_SETTINGS = "application/vnd.ims.lti.v2.toolsettings+json";
	public static final String FORMAT_TOOL_SETTINGS_SIMPLE = "application/vnd.ims.lti.v2.toolsettings.simple+json";
	public static final String FORMAT_TOOL_PROXY = "application/vnd.ims.lti.v2.toolproxy+json";
	public static final String FORMAT_RESULT = "application/vnd.ims.lis.v2.result+json";
	public static final String FORMAT_TOOL_PROXY_REGISTRATION_RESPONSE = "application/vnd.ims.lti.v2.ToolProxy.id+json";
	
	//query params
	public static final String PARAM_STATUS = "status";
	public static final String PARAM_TOOL_GUID = "tool_guid";
	public static final String PARAM_LTI_ERRORMSG = "lti_errormsg";
	public static final String PARAM_LTI_ERRORLOG = "lti_errorlog";
	public static final String PARAM_LTI_MSG = "lti_msg";
	public static final String PARAM_LTI_LOG = "lti_log";
	
	public static final String TOOL_URL = "ltitool.xhtml";
	public static final String TOOL_LAUNCH_ENDPOINT = "ltiproviderlaunch.xhtml";
	
	
}
