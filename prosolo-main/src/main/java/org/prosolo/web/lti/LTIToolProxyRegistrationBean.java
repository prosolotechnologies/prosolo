package org.prosolo.web.lti;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.prosolo.core.spring.security.authentication.lti.util.LTIConstants;
import org.prosolo.services.lti.LtiConsumerManager;
import org.prosolo.services.lti.ToolSetManager;
import org.prosolo.services.oauth.OauthService;
import org.prosolo.web.lti.json.MessageParameterTypeAdapterFactory;
import org.prosolo.web.lti.json.data.InlineContext;
import org.prosolo.web.lti.json.data.Service;
import org.prosolo.web.lti.json.data.ToolProxy;
import org.prosolo.web.lti.message.ToolProxyRegistrationMessage;
import org.prosolo.web.lti.message.extract.LtiMessageBuilder;
import org.prosolo.web.lti.message.extract.LtiMessageBuilderFactory;
import org.prosolo.web.lti.toolproxy.ToolProxyBuilder;
import org.prosolo.web.util.page.PageUtil;
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
@Scope("view")
public class LTIToolProxyRegistrationBean implements Serializable {

	private static final long serialVersionUID = 9095250834224496207L;

	private static Logger logger = Logger.getLogger(LTIToolProxyRegistrationBean.class);

	@Inject
	private OauthService oAuthService;
	@Inject
	private ToolSetManager tsManager;
	@Inject
	private LtiConsumerManager consumerManager;
	
	private String returnUrl;

	public LTIToolProxyRegistrationBean() {
		logger.info("LTIProviderLaunchBean initialized");
	}

	// called when Tool Consumer submits request
	public void processPOSTRequest() {
		//ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		try {
			logger.info("New LTI registration request");
			ToolProxyRegistrationMessage msg = validateRequest();
			logger.info("LTI registration request valid");
			TCProfile tcProfile = getTCProfile(msg.getTcProfileURL());
			logger.info("LTI TC Profile");
			ToolProxy tp = registerToolProxy(tcProfile, msg);
			logger.info("LTI Register Tool Proxy");
			consumerManager.registerLTIConsumer(msg.getId(), tp.getToolProxyGuid(), 
					tp.getSecurityContract().getSharedSecret(), tcProfile.getCapabilities(), tcProfile.getServices());
			logger.info("LTI Register LTI Consumer");
			redirectUser(tp.getToolProxyGuid());
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			// show error page with error message - Request validation error
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	public void redirectUser(String param) {
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		if (returnUrl != null) {
			String url = buildReturnURL(param, returnUrl);
			try {
				logger.info("Redirecting to LTI tool consumer");
				externalContext.redirect(url);
			} catch (IOException ex) {
				logger.error(ex);
			}
		}
	}

	// request validation (parameter validation, oauth validation) and wrapping
	// parameters in ToolProxyRegistrationMessage object
	private ToolProxyRegistrationMessage validateRequest() throws Exception {
		if (!LTIConstants.POST_REQUEST.equalsIgnoreCase(
				((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
						.getMethod())) {
			throw new Exception("Not POST Request!");
		}
		returnUrl = PageUtil.getPostParameter(LTIConstants.LAUNCH_PRESENTATION_RETURN_URL);
		ToolProxyRegistrationMessage msg = createToolProxyRegistrationMessage();
		checkIfToolExists(msg);
		return msg;
	}

	// parameter validation
	private void checkIfToolExists(ToolProxyRegistrationMessage msg) throws Exception {
		boolean exists = tsManager.checkIfToolSetExists(msg.getId());
		if (!exists) {
			throw new Exception("This tool does not exists");
		}
	}

	// get Tool Consumer Profile from Tool Consumer
	public TCProfile getTCProfile(String url) throws Exception {
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
			logger.info("LTI tool consumer profile retrieved");
			TCProfile tcProfile = parseJsonTCProfile(jsonString);
			return tcProfile;
		} catch (Exception e) {
			logger.error(e);
			throw new Exception("Error while retrieving Tool Consumer Profile");
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}

	}

	// parse needed data from JSON
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
			throw new Exception("Error while parsing Tool Consumer Profile");
		}

	}

	// register Tool Proxy object
	private ToolProxy registerToolProxy(TCProfile tcProfile, ToolProxyRegistrationMessage msg) throws Exception {
		String postURL = findToolProxyRegistrationEndpoint(tcProfile);
		if (postURL == null) {
			throw new Exception("Tool Proxy Registration Service Not Found!");
		}
		ToolProxy tp = new ToolProxyBuilder().buildToolProxy(tcProfile, msg.getId());
		String json = transformToolProxyToJson(tp);
		String tpGuid = registerToolProxyWithToolConsumer(json, msg.getRegKey(), msg.getRegPassword(), postURL);
		tp.setToolProxyGuid(tpGuid);
		return tp;
	}

	// find endpoint of Tool Proxy Registration REST Service if Tool Consumer
	// offered it
	private String findToolProxyRegistrationEndpoint(TCProfile tcProfile) {
		for (Service s : tcProfile.getServices()) {
			if (LTIConstants.FORMAT_TOOL_PROXY.equals(s.getFormats().get(0))) {
				boolean supported = s.getActions().contains(LTIConstants.POST_REQUEST);
				if (supported) {
					return s.getEndpoint();
				}
			}
		}
		return null;
	}

	// create return url with query parameters
	private String buildReturnURL(String tpGuid, String url) {
		Map<String, String> tpResponse = new HashMap<>();
		if (tpGuid != null && !tpGuid.isEmpty()) {
			tpResponse.put(LTIConstants.PARAM_STATUS, LTIConstants.TOOL_PROXY_REGISTRATION_RESPONSE_STATUS_SUCCESS);
			tpResponse.put(LTIConstants.PARAM_TOOL_GUID, tpGuid);
		} else {
			tpResponse.put(LTIConstants.PARAM_STATUS, LTIConstants.TOOL_PROXY_REGISTRATION_RESPONSE_STATUS_FAILURE);
		}

		return Util.buildURLWithParams(url, tpResponse);
	}

	// wrap POST parameters in ToolProxyRegistrationMessage
	private ToolProxyRegistrationMessage createToolProxyRegistrationMessage() throws Exception {
		try {
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			LtiMessageBuilder msgE = LtiMessageBuilderFactory.createMessageExtractor(request);
			ToolProxyRegistrationMessage msg = (ToolProxyRegistrationMessage) msgE.getLtiMessage(request);
			return msg;
		} catch(Exception e) {
			logger.error("Error", e);
			throw new Exception("Required parameters missing or not valid");
		}
	}

	// sign message and register Tool Proxy with Tool Consumer
	public String registerToolProxyWithToolConsumer(String msg, String key, String password, String url)
			throws Exception {
		try {
			String authorizationHeader = oAuthService.bodySignMessage(msg, key, password, url);
			return sendToolProxyRegistrationRequest(url, msg, authorizationHeader);
		} catch (Exception e) {
			logger.error("Error", e);
			throw new Exception("Tool Proxy Registration Failed!");
		}
	}

	// register Tool Proxy by POST request sent to REST Service
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
			logger.error("Error", e);
			throw new Exception("Error while trying to register Tool Proxy");
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}

	}

	public String transformToolProxyToJson(ToolProxy tp) {
		Gson gson = new GsonBuilder().registerTypeAdapterFactory(new MessageParameterTypeAdapterFactory())
				.setPrettyPrinting().create();
		return gson.toJson(tp);
	}

}
