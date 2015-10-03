package org.prosolo.web.lti;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.prosolo.services.oauth.OauthService;
import org.prosolo.web.lti.json.MessageParameterTypeAdapterFactory;
import org.prosolo.web.lti.json.data.BaseURL;
import org.prosolo.web.lti.json.data.Contact;
import org.prosolo.web.lti.json.data.Description;
import org.prosolo.web.lti.json.data.ExtendedMessageHandler;
import org.prosolo.web.lti.json.data.InlineContext;
import org.prosolo.web.lti.json.data.MessageParameter;
import org.prosolo.web.lti.json.data.ProductFamily;
import org.prosolo.web.lti.json.data.ProductInfo;
import org.prosolo.web.lti.json.data.ProductInstance;
import org.prosolo.web.lti.json.data.ResourceHandler;
import org.prosolo.web.lti.json.data.ResourceType;
import org.prosolo.web.lti.json.data.SecurityContract;
import org.prosolo.web.lti.json.data.Service;
import org.prosolo.web.lti.json.data.ToolProfile;
import org.prosolo.web.lti.json.data.ToolProxy;
import org.prosolo.web.lti.json.data.ToolService;
import org.prosolo.web.lti.json.data.Vendor;
import org.prosolo.web.lti.message.ToolProxyRegistrationMessage;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

@ManagedBean(name = "ltitoolproxyregistrationbean")
@Component("ltitoolproxyregistrationbean")
@Scope("request")
public class LTIToolProxyRegistrationBean implements Serializable {

	private static final long serialVersionUID = 9095250834224496207L;

	private static Logger logger = Logger.getLogger(LTIToolProxyRegistrationBean.class);

	@Inject
	private OauthService oAuthService;

	public LTIToolProxyRegistrationBean() {
		logger.info("LTIProviderLaunchBean initialized");
	}
	//called when Tool Consumer submits request
	public void processPOSTRequest() {
		try {
			ToolProxy toolP = LTIConfigLoader.getInstance().loadToolProxy();
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			ToolProxyRegistrationMessage msg = validateRequest();
			TCProfile tcProfile = getTCProfile(msg.getTcProfileURL());
			String tpGuid = registerToolProxy(tcProfile, msg);
			String returnURL = formReturnURL(tpGuid, msg.getLaunchPresentationReturnURL());
			try {
				externalContext.redirect(returnURL);
			} catch (Exception e) {
				logger.error(e);
			}
		} catch (Exception e) {
			logger.error(e);
			// show error page with error message - Request validation error
		}

	}
	//request validation (parameter validation, oauth validation) and wrapping parameters in ToolProxyRegistrationMessage object
	private ToolProxyRegistrationMessage validateRequest() throws Exception {
		if (!LTIConstants.POST_REQUEST.equalsIgnoreCase(
				((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
						.getMethod())) {
			throw new Exception("Not POST Request!");
		}
		ToolProxyRegistrationMessage msg = createToolProxyRegistrationMessage();
		validateParameters(msg);
		return msg;
	}
	//parameter validtion
	private void validateParameters(ToolProxyRegistrationMessage msg) throws Exception {
		// TODO Auto-generated method stub

	}
	//get Tool Consumer Profile from Tool Consumer
	public TCProfile getTCProfile(String url) throws Exception{
		String param = "?" + LTIConstants.LTI_VERSION + "=" + LTIConstants.LTI_VERSION_TWO;
		String finalUrl = url;
		if (url.indexOf(param) == -1) {
			finalUrl += param;
		}
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet getRequest = new HttpGet(finalUrl);
		// getRequest.addHeader(HttpHeaders.ACCEPT,
		// "application/vnd.ims.lti.v2.toolconsumerprofile+json");
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(getRequest);
			HttpEntity entity = response.getEntity();
			String jsonString = EntityUtils.toString(entity);
			logger.info("TC PROFILE RESPONSE: " + jsonString);
			TCProfile tcProfile = parseJsonTCProfile(jsonString);
			return tcProfile;
		} catch (Exception e) {
			logger.error(e);
			throw new Exception("Error while getting Tool Consumer Profile");
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}

	}
	//parse needed data from JSON
	private TCProfile parseJsonTCProfile(String jsonString) throws Exception {
		try {
			JsonParser parser = new JsonParser();
			JsonObject tcProfile = (JsonObject) parser.parse(jsonString);
			JsonPrimitive id = tcProfile.getAsJsonPrimitive("@id");
			List<InlineContext> contexts = new ArrayList<>();
			JsonElement con = tcProfile.get("@context");
			if (con instanceof JsonArray) {
				for (JsonElement el : con.getAsJsonArray()) {
					if (el instanceof JsonObject) {
						Set<Entry<String, JsonElement>> set = el.getAsJsonObject().entrySet();
						for (Entry<String, JsonElement> entry : set) {
							InlineContext ic = new InlineContext();
							ic.setPrefix(entry.getKey());
							ic.setURI(entry.getValue().getAsJsonPrimitive().getAsString());
							contexts.add(ic);
						}
					}
				}
			}
			JsonArray capabilitiesJson = tcProfile.getAsJsonArray("capability_offered");
			JsonArray servicesOfferedJson = tcProfile.getAsJsonArray("service_offered");

			Gson gson = new Gson();
			List capabilities = gson.fromJson(capabilitiesJson, ArrayList.class);
			Type listType = new TypeToken<List<Service>>() {
			}.getType();
			List<Service> servicesOffered = gson.fromJson(servicesOfferedJson, listType);

			TCProfile toolConsumerProfile = new TCProfile();
			toolConsumerProfile.setId(id.getAsString());
			toolConsumerProfile.setContexts(contexts);
			toolConsumerProfile.setCapabilities(capabilities);
			toolConsumerProfile.setServices(servicesOffered);
			return toolConsumerProfile;
		} catch (Exception e) {
			logger.error(e);
			logger.info("Tool Consumer Profile not formatted properly");
			throw new Exception("Error while parsing Tool Consumer Profile");
		}

	}
	// register Tool Proxy object
	private String registerToolProxy(TCProfile tcProfile, ToolProxyRegistrationMessage msg) throws Exception {
		String postURL = findToolProxyRegistrationEndpoint(tcProfile);
		if (postURL == null) {
			throw new Exception("Tool Proxy Registration Service Not Found!");
		}
		ToolProxy tp = createToolProxy(tcProfile);
		String json = transformToolProxyToJson(tp);
		logger.info("TOOL PROXY "+json);
		return registerToolProxyWithToolConsumer(json, msg.getRegKey(), msg.getRegPassword(), postURL);
	}
	//find endpoint of Tool Proxy Registration REST Service if Tool Consumer offered it
	private String findToolProxyRegistrationEndpoint(TCProfile tcProfile) {
		for (Service s : tcProfile.getServices()) {
			if (LTIConstants.FORMAT_TOOL_PROXY.equals(s.getFormats().get(0))) {
				boolean supported = checkIfSupported(LTIConstants.POST_REQUEST, s.getActions());
				if (supported) {
					return s.getEndpoint();
				}
			}
		}
		return null;
	}
	//create ToolProxy object to be sent to Tool Consumer
	public ToolProxy createToolProxy(TCProfile tcp) {
		ToolProxy tp = createToolProxyPredefined(tcp.getId());
		tp.getToolProfile()
				.setResourceHandler(createResourceHandlers(
						tp.getToolProfile().getProductInstance().getProductInfo().getProductFamily().getCode(),
						tcp.getCapabilities()));
		tp.setSecurityContract(createSecurityContract(
				tp.getToolProfile().getProductInstance().getProductInfo().getProductFamily().getCode(),
				tcp.getServices(), tcp.getContexts()));
		return tp;
	}
	//populate ToolProxy with data that do not depend on Tool Consumer
	private ToolProxy createToolProxyPredefined(String tcProfileId) {
		ToolProxy tp = new ToolProxy();
		tp.setContext(LTIConstants.TOOL_PROXY_CONTEXT);
		tp.setId("http://lms.example.com/ToolProxy/869e5ce5-214c-4e85-86c6-b99e8458a592");
		tp.setType(LTIConstants.TOOL_PROXY_TYPE);
		tp.setLtiVersion(LTIConstants.LTI_VERSION_TWO);
		tp.setToolConsumerProfile(tcProfileId);

		// toolprofile
		ToolProfile toolProfile = new ToolProfile();
		toolProfile.setLtiVersion(LTIConstants.LTI_VERSION_TWO);

		// productinstance
		ProductInstance productInstance = new ProductInstance();
		productInstance.setGuid(UUID.randomUUID().toString());

		// productinfo
		ProductInfo productInfo = new ProductInfo();

		Description productName = new Description();
		productName.setDefaultValue("ProSolo");
		productName.setKey("tool.name");

		Description productDescription = new Description();
		productDescription.setDefaultValue("ProSolo is a Learning Management System");
		productDescription.setKey("tool.description");
		
		Description technicalDescription = new Description();
		technicalDescription.setDefaultValue("Implemented Tool Provider supports LTI 2.0 as well as LTI 1.1 version of specification");
		technicalDescription.setKey("tool.technical");

		productInfo.setProductName(productName);
		productInfo.setDescription(productDescription);
		productInfo.setProductVersion("1.1");
		productInfo.setTechnicalDescription(technicalDescription);

		// productfamily
		ProductFamily productFamily = new ProductFamily();
		productFamily.setId(UUID.randomUUID().toString());
		productFamily.setCode("prosolo");

		// vendor
		Vendor vendor = new Vendor();
		vendor.setCode("prosolo.ca");
		vendor.setTimestamp(getCurrentTimestamp("yyyy-MM-dd'T'HH:mm:ss"));

		Description vendorName = new Description();
		vendorName.setDefaultValue("ProSolo Inc");
		vendorName.setKey("tool.vendor.name");
		
		Description vendorDescription = new Description();
		vendorDescription.setDefaultValue("ProSolo is a ...");
		vendorDescription.setKey("tool.vendor.description");
		
		vendor.setVendorName(vendorName);
		vendor.setDescription(vendorDescription);
		vendor.setWebsite("http://www.prosolo.ca");

		Contact vendorContact = new Contact();
		vendorContact.setEmail("prosolo@prosolo.ca");

		vendor.setContact(vendorContact);

		productFamily.setVendor(vendor);

		productInfo.setProductFamily(productFamily);

		productInstance.setProductInfo(productInfo);

		toolProfile.setProductInstance(productInstance);

		List<BaseURL> baseURLs = new ArrayList();
		BaseURL baseURL = new BaseURL();
		baseURL.setSelector("DefaultSelector");
		baseURL.setDefaultBaseURL("http://localhost:8080/prosolo/");
		baseURL.setSecureBaseURL("http://localhost:8080/prosolo/");
		baseURLs.add(baseURL);

		toolProfile.setBaseURLChoice(baseURLs);

		tp.setToolProfile(toolProfile);
		
		Map<String,String> customParameters = new HashMap<>();
		customParameters.put("testproxysettingscustom", "12345");
		tp.setCustom(customParameters);
		
		return tp;
	}
	//create Resource Handlers based on capabilities offered in Tool Consumer Profile
	private List<ResourceHandler> createResourceHandlers(String toolID, List<String> capabilities) {
		List<ResourceHandler> resHandlers = getResourcesHandlersForTheTool(toolID);
		List<ResourceHandler> resHandlersFinal = new ArrayList<>();
		for (ResourceHandler rh : resHandlers) {
			ResourceHandler rHandler = new ResourceHandler();
			Description name = new Description();
			name.setDefaultValue(rh.getName().getDefaultValue());
			name.setKey(rh.getName().getKey());
			Description description = new Description();
			description.setDefaultValue(rh.getDescription().getDefaultValue());
			description.setKey(rh.getDescription().getKey());
			rHandler.setName(name);
			rHandler.setDescription(description);
			rHandler.setResourceType(rh.getResourceType());

			List<ExtendedMessageHandler> mHandlers = new ArrayList<>();
			for (ExtendedMessageHandler emh : rh.getMessage()) {
				ExtendedMessageHandler mh = new ExtendedMessageHandler();
				mh.setPath(emh.getPath());
				mh.setMessageType(emh.getMessageType());
				List<MessageParameter> parameters = new ArrayList();
				for (MessageParameter mp : emh.getParameter()) {
					boolean isSupported = true;
					if (LTIConstants.RES_HANDLER_MESSAGE_TYPE_VARIABLE.equals(mp.getParameterType())) {
						isSupported = checkIfSupported(mp.getParameterValue(), capabilities);
					}
					if (isSupported) {
						MessageParameter mp1 = new MessageParameter();
						mp1.setName(mp.getName());
						mp1.setParameterType(mp.getParameterType());
						mp1.setParameterValue(mp.getParameterValue());
						parameters.add(mp1);
					}
				}
				mh.setParameter(parameters);
				List<String> caps = new ArrayList<>();
				for (String c : emh.getEnabledCapability()) {
					boolean isOffered = checkIfSupported(c, capabilities);
					if (isOffered) {
						caps.add(c);
					}
				}
				mh.setEnabledCapability(caps);
				mHandlers.add(mh);
			}
			rHandler.setMessage(mHandlers);
			resHandlersFinal.add(rHandler);
		}
		return resHandlersFinal;
	}

	//create return url with query parameters
	private String formReturnURL(String tpGuid, String url) {
		Map<String, String> tpResponse = new HashMap<>();
		if (tpGuid != null) {
			tpResponse.put(LTIConstants.PARAM_STATUS, LTIConstants.TOOL_PROXY_REGISTRATION_RESPONSE_STATUS_SUCCESS);
			tpResponse.put(LTIConstants.PARAM_TOOL_GUID, tpGuid);
		} else {
			tpResponse.put(LTIConstants.PARAM_STATUS, LTIConstants.TOOL_PROXY_REGISTRATION_RESPONSE_STATUS_FAILURE);
		}

		return Util.formURLWithParams(url, tpResponse);
	}
	//wrap POST parameters in ToolProxyRegistrationMessage
	private ToolProxyRegistrationMessage createToolProxyRegistrationMessage() {
		ToolProxyRegistrationMessage msg = new ToolProxyRegistrationMessage();
		msg.setMessageType(PageUtil.getPostParameter(LTIConstants.MESSAGE_TYPE));
		msg.setLtiVersion(PageUtil.getPostParameter(LTIConstants.LTI_VERSION));
		msg.setUserID(PageUtil.getPostParameter(LTIConstants.USER_ID));
		msg.setLaunchPresentationReturnURL(PageUtil.getPostParameter(LTIConstants.LAUNCH_PRESENTATION_RETURN_URL));
		msg.getRoles().add(PageUtil.getPostParameter(LTIConstants.ROLES));
		msg.setRegKey(PageUtil.getPostParameter(LTIConstants.REG_KEY));
		msg.setRegPassword(PageUtil.getPostParameter(LTIConstants.REG_PASSWORD));
		msg.setTcProfileURL(PageUtil.getPostParameter(LTIConstants.TC_PROFILE_URL));

		logger.info("Message type: " + msg.getMessageType());
		logger.info("LTI version: " + msg.getLtiVersion());
		logger.info("User ID: " + msg.getUserID());
		logger.info("Roles: " + msg.getRoles().toString());
		logger.info("Launch presentation Return URL: " + msg.getLaunchPresentationReturnURL());
		logger.info("Reg key: " + msg.getRegKey());
		logger.info("Reg password: " + msg.getRegPassword());
		logger.info("Tool consumer profile URL: " + msg.getTcProfileURL());

		return msg;
	}

	//sign message and register Tool Proxy with Tool Consumer
	public String registerToolProxyWithToolConsumer(String msg, String key, String password, String url)
			throws Exception {
		try {
			String authorizationHeader = oAuthService.bodySignMessage(msg, key, password, url);
			return sendToolProxyRegistrationRequest(url, msg, authorizationHeader);
		} catch (Exception e) {
			throw new Exception("Tool Proxy Registration Failed!");
		}
	}
	//register Tool Proxy by POST request sent to REST Service
	public String sendToolProxyRegistrationRequest(String url, String msg, String authorizationHeader)
			throws Exception {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost post = new HttpPost(url);
		CloseableHttpResponse response = null;
		try {
			// String authorizationHeader =
			// oauthMsg.getAuthorizationHeader(null);
			StringEntity postBody = new StringEntity(msg);
			post.setEntity(postBody);
			post.addHeader("Content-Type", LTIConstants.FORMAT_TOOL_PROXY);
			post.addHeader("Authorization", authorizationHeader);
			response = httpClient.execute(post);

			HttpEntity entity = response.getEntity();

			String jsonString = EntityUtils.toString(entity);

			logger.info("Tool Proxy Registration response: " + jsonString);

			if (response.getStatusLine().getStatusCode() == 201) {
				JsonParser parser = new JsonParser();
				JsonObject tcProfile = (JsonObject) parser.parse(jsonString);
				JsonPrimitive toolProxyGuid = tcProfile.getAsJsonPrimitive("tool_proxy_guid");
				String tpGuid = toolProxyGuid.getAsString();
				if (tpGuid == null) {
					throw new Exception();
				}
				return tpGuid;
			} else {
				throw new Exception();
			}

		} catch (Exception e) {
			logger.error(e);
			throw new Exception("Error while trying to register Tool Proxy");
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}

	}
	//create Security Contract based on offered services in Tool Consumer Profile
	private SecurityContract createSecurityContract(String toolID, List<Service> services,
			List<InlineContext> contexts) {
		SecurityContract sc = new SecurityContract();
		String sharedSecret = UUID.randomUUID().toString();
		sc.setSharedSecret(sharedSecret);

		List<Service> supportedServices = getWantedServicesForTheTool(toolID);
		List<ToolService> toolServices = new ArrayList<>();
		for (Service so : services) {
			Service wantedService = findWantedService(so, supportedServices);
			if (wantedService != null) {
				ToolService toolService = createToolService(so, wantedService, contexts);
				if (toolService != null) {
					toolServices.add(toolService);
				}
			}
		}
		sc.setToolService(toolServices);
		return sc;
	}
	//match Service from Tool Consumer Profile with Service we want to use
	private Service findWantedService(Service serviceOffered, List<Service> wantedServices) {
		for (String f : serviceOffered.getFormats()) {
			Service serviceWanted = findServiceWithFormat(f, wantedServices);
			if (serviceWanted != null) {
				return serviceWanted;
			}
		}
		return null;
	}
	//find service with desired format
	private Service findServiceWithFormat(String format, List<Service> services) {
		for (Service so : services) {
			boolean exists = checkIfSupported(format, so.getFormats());
			if (exists) {
				return so;
			}
		}
		return null;
	}
	//create ToolService based on Service from Tool Consumer Profile and service functionalities we want to use
	private ToolService createToolService(Service serviceOffered, Service serviceWanted,
			List<InlineContext> contexts) {
		ToolService ts = new ToolService();
		ts.setType(LTIConstants.REST_SERVICE_PROFILE);
		ts.setAction(getWantedActions(serviceOffered, serviceWanted));
		ts.setService(getFullServiceId(serviceOffered, contexts));
		return ts;
	}
	//get full service id if prefixes used
	private String getFullServiceId(Service serviceOffered, List<InlineContext> contexts) {
		String id = serviceOffered.getId();
		String prefix = null;
		String suffix = null;
		int delimiterPosition = id.indexOf(":");
		if (delimiterPosition != -1) {
			prefix = id.substring(0, delimiterPosition);
			suffix = id.substring(delimiterPosition + 1, id.length());
			String fullURI = getFullURIForPrefix(prefix, contexts);
			if (fullURI != null) {
				id = fullURI + suffix;
			}
		}
		return id;
	}
	
	private String getFullURIForPrefix(String prefix, List<InlineContext> contexts) {
		for (InlineContext ic : contexts) {
			if (prefix.equals(ic.getPrefix())) {
				return ic.getURI();
			}
		}
		return null;
	}

	private List<String> getWantedActions(Service serviceOffered, Service serviceWanted) {
		List<String> actions = new ArrayList<>();
		for (String s : serviceOffered.getActions()) {
			boolean wanted = checkIfSupported(s, serviceWanted.getActions());
			if (wanted) {
				actions.add(s);
			}
		}
		return actions;
	}
	//get services we want to use for specific tool (probably from conf file)
	private List<Service> getWantedServicesForTheTool(String code) {
		List<Service> services = new ArrayList<>();

		Service soResultService = new Service();
		List<String> actionsResultService = new ArrayList<>();
		actionsResultService.add(LTIConstants.GET_REQUEST);
		actionsResultService.add(LTIConstants.PUT_REQUEST);
		soResultService.setActions(actionsResultService);
		List<String> formatsResultService = new ArrayList<>();
		formatsResultService.add(LTIConstants.FORMAT_RESULT);
		soResultService.setFormats(formatsResultService);

		Service soToolProxyService = new Service();
		List<String> actionsToolProxyService = new ArrayList<>();
		actionsToolProxyService.add(LTIConstants.GET_REQUEST);
		actionsToolProxyService.add(LTIConstants.PUT_REQUEST);
		actionsToolProxyService.add(LTIConstants.POST_REQUEST);
		soToolProxyService.setActions(actionsToolProxyService);
		List<String> formatsToolProxyService = new ArrayList<>();
		formatsToolProxyService.add(LTIConstants.FORMAT_TOOL_PROXY);
		soToolProxyService.setFormats(formatsToolProxyService);

		Service soToolSettingsService = new Service();
		List<String> actionsToolSettingsService = new ArrayList<>();
		actionsToolSettingsService.add(LTIConstants.GET_REQUEST);
		actionsToolSettingsService.add(LTIConstants.PUT_REQUEST);
		soToolSettingsService.setActions(actionsToolSettingsService);
		List<String> formatsToolSettingsService = new ArrayList<>();
		formatsToolSettingsService.add(LTIConstants.FORMAT_TOOL_SETTINGS);
		formatsToolSettingsService.add(LTIConstants.FORMAT_TOOL_SETTINGS_SIMPLE);
		soToolSettingsService.setFormats(formatsToolSettingsService);

		services.add(soResultService);
		services.add(soToolProxyService);
		services.add(soToolSettingsService);

		return services;
	}
	
	private boolean checkIfSupported(String value, List<String> list) {
		return list.contains(value);
	}
	//get resource handlers for specific tool (probably from conf file)
	private List<ResourceHandler> getResourcesHandlersForTheTool(String code) {
		List<ResourceHandler> resHandlers = new ArrayList();
		ResourceHandler res = new ResourceHandler();

		Description name = new Description();
		name.setDefaultValue("Learning goal");
		name.setKey("learninggoal.resource.name");
		
		Description description = new Description();
		description.setDefaultValue("Learning goal description");
		description.setKey("learninggoal.resource.description");
		
		res.setName(name);
		res.setDescription(description);

		ResourceType rt = new ResourceType();
		rt.setCode("learninggoaltool");
		res.setResourceType(rt);

		List<ExtendedMessageHandler> mHandlers = new ArrayList<>();
		ExtendedMessageHandler mh = new ExtendedMessageHandler();
		mh.setPath("ltiproviderlaunch.xhtml");
		mh.setMessageType(LTIConstants.MESSAGE_TYPE_LTILAUNCH);

		List<MessageParameter> parameters = new ArrayList();
		MessageParameter mp1 = new MessageParameter();
		mp1.setName("lis_person_name_given");
		mp1.setParameterType(LTIConstants.RES_HANDLER_MESSAGE_TYPE_VARIABLE);
		mp1.setParameterValue("Person.name.given");
		MessageParameter mp2 = new MessageParameter();
		mp2.setName("lis_person_email_primary");
		mp2.setParameterType(LTIConstants.RES_HANDLER_MESSAGE_TYPE_VARIABLE);
		mp2.setParameterValue("Person.email.primary");
		MessageParameter mp3 = new MessageParameter();
		mp3.setName("result_sourcedid");
		mp3.setParameterType(LTIConstants.RES_HANDLER_MESSAGE_TYPE_VARIABLE);
		mp3.setParameterValue("Result.sourcedId");
		MessageParameter mp4 = new MessageParameter();
		mp4.setName("result_url");
		mp4.setParameterType(LTIConstants.RES_HANDLER_MESSAGE_TYPE_VARIABLE);
		mp4.setParameterValue("Result.url");
		MessageParameter mp5 = new MessageParameter();
		mp5.setName("test");
		mp5.setParameterType(LTIConstants.RES_HANDLER_MESSAGE_TYPE_FIXED);
		mp5.setParameterValue("test");
		MessageParameter mp6 = new MessageParameter();
		mp6.setName("user_id");
		mp6.setParameterType(LTIConstants.RES_HANDLER_MESSAGE_TYPE_VARIABLE);
		mp6.setParameterValue("User.id");
		MessageParameter mp7 = new MessageParameter();
		mp7.setName("settings_toolProxy");
		mp7.setParameterType(LTIConstants.RES_HANDLER_MESSAGE_TYPE_VARIABLE);
		mp7.setParameterValue("ToolProxy.custom.url");
		MessageParameter mp8 = new MessageParameter();
		mp8.setName("settings_context");
		mp8.setParameterType(LTIConstants.RES_HANDLER_MESSAGE_TYPE_VARIABLE);
		mp8.setParameterValue("ToolProxyBinding.custom.url");
		MessageParameter mp9 = new MessageParameter();
		mp9.setName("settings_link");
		mp9.setParameterType(LTIConstants.RES_HANDLER_MESSAGE_TYPE_VARIABLE);
		mp9.setParameterValue("LtiLink.custom.url");
		parameters.add(mp1);
		parameters.add(mp2);
		parameters.add(mp3);
		parameters.add(mp4);
		parameters.add(mp5);
		parameters.add(mp6);
		parameters.add(mp7);
		parameters.add(mp8);
		parameters.add(mp9);
		mh.setParameter(parameters);
		List<String> capabilities = new ArrayList<>();
		capabilities.add("Result.autocreate");
		mh.setEnabledCapability(capabilities);
		mHandlers.add(mh);
		res.setMessage(mHandlers);
		resHandlers.add(res);
		return resHandlers;

	}

	private String getCurrentTimestamp(String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date date = new Date();
		return sdf.format(date);
	}

	public String transformToolProxyToJson(ToolProxy tp) {
		Gson gson = new GsonBuilder().registerTypeAdapterFactory(new MessageParameterTypeAdapterFactory())
				.setPrettyPrinting().create();
		return gson.toJson(tp);
	}

}
