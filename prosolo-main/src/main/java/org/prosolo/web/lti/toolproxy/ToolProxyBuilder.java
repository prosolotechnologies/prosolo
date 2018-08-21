package org.prosolo.web.lti.toolproxy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.lti.LtiToolManager;
import org.prosolo.web.lti.LTIConfigLoader;
import org.prosolo.web.lti.LTIConstants;
import org.prosolo.web.lti.TCProfile;
import org.prosolo.web.lti.json.data.BaseURL;
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

public class ToolProxyBuilder {

	public ToolProxy buildToolProxy(TCProfile tcProfile, long toolSetId) throws Exception {
		try {
			ToolProxy tp = createToolProxyPredefined(tcProfile.getId());
			tp.getToolProfile().setResourceHandler(createResourceHandlers(tp.getToolProfile().getResourceHandler(),
					tcProfile.getCapabilities(), toolSetId));
			tp.setSecurityContract(
					createSecurityContract(tp.getWantedServices(), tcProfile.getServices(), tcProfile.getContexts()));
			tp.setWantedServices(null);
			return tp;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error while building the ToolProxy");
		}
	}

	private ToolProxy createToolProxyPredefined(String tcProfileId) throws Exception {
		String domain = CommonSettings.getInstance().config.appConfig.domain;

		ToolProxy tp = LTIConfigLoader.getInstance().getToolProxy();
		tp.setContext(LTIConstants.TOOL_PROXY_CONTEXT);
		tp.setId(domain + "ToolProxy/" + UUID.randomUUID().toString());
		tp.setType(LTIConstants.TOOL_PROXY_TYPE);
		tp.setLtiVersion(LTIConstants.LTI_VERSION_TWO);
		tp.setToolConsumerProfile(tcProfileId);
		ToolProfile toolProfile = tp.getToolProfile();
		toolProfile.setLtiVersion(LTIConstants.LTI_VERSION_TWO);
		ProductInstance productInstance = toolProfile.getProductInstance();
		productInstance.setGuid(UUID.randomUUID().toString());
		ProductInfo productInfo = productInstance.getProductInfo();
		Description productName = productInfo.getProductName();
		productName.setKey("tool.name");
		Description productDescription = productInfo.getDescription();
		productDescription.setKey("tool.description");
		Description technicalDescription = productInfo.getTechnicalDescription();
		technicalDescription.setKey("tool.technical");
		ProductFamily productFamily = productInfo.getProductFamily();
		productFamily.setCode(UUID.randomUUID().toString());
		productFamily.setId(domain + productFamily.getCode());
		Vendor vendor = productFamily.getVendor();
		vendor.setTimestamp(getCurrentTimestamp("yyyy-MM-dd'T'HH:mm:ss"));
		Description vendorName = vendor.getVendorName();
		vendorName.setKey("tool.vendor.name");
		Description vendorDescription = vendor.getDescription();
		vendorDescription.setKey("tool.vendor.description");
		List<BaseURL> baseURLs = new ArrayList();
		BaseURL baseURL = new BaseURL();
		baseURL.setSelector("DefaultSelector");
		baseURL.setDefaultBaseURL(domain);
		baseURL.setSecureBaseURL(domain);
		baseURLs.add(baseURL);
		toolProfile.setBaseURLChoice(baseURLs);

		return tp;
	}

	private List<ResourceHandler> createResourceHandlers(List<ResourceHandler> resourceHandlers,
			List<String> capabilities, long id) throws Exception{

		List<ResourceHandler> resHandlers = new ArrayList<>();
		ResourceHandler res = resourceHandlers.get(0);

		List<LtiTool> tools = ServiceLocator.getInstance().getService(LtiToolManager.class).getToolsForToolProxy(id);

		List<MessageParameter> parameters = new ArrayList<>();
		List<String> enabledCapabilities = new ArrayList<>();

		ExtendedMessageHandler emh = res.getMessage().get(0);
		for (MessageParameter mp : emh.getParameter()) {
			boolean isSupported = true;
			if (LTIConstants.RES_HANDLER_MESSAGE_TYPE_VARIABLE.equals(mp.getParameterType())) {
				isSupported = capabilities.contains(mp.getParameterValue());
			}
			if (isSupported) {
				parameters.add(mp);
			}
		}
		for (String c : emh.getEnabledCapability()) {
			boolean isOffered = capabilities.contains(c);
			if (isOffered) {
				enabledCapabilities.add(c);
			}
		}

		for (LtiTool tool : tools) {
			ResourceHandler resH = new ResourceHandler();

			Description name = new Description();
			name.setDefaultValue(tool.getName());
			name.setKey(UUID.randomUUID().toString());
			resH.setName(name);

			Description description = new Description();
			description.setDefaultValue(tool.getDescription());
			description.setKey(UUID.randomUUID().toString());
			resH.setDescription(description);

			ResourceType rt = new ResourceType();
			rt.setCode(UUID.randomUUID().toString());
			resH.setResourceType(rt);

			List<ExtendedMessageHandler> mHandlers = new ArrayList<>();
			ExtendedMessageHandler mh = new ExtendedMessageHandler();
			String path = tool.getFullLaunchURL().substring(CommonSettings.getInstance().config.appConfig.domain.length());
			mh.setPath(path);

			mh.setMessageType(LTIConstants.MESSAGE_TYPE_LTILAUNCH);
			mh.setEnabledCapability(enabledCapabilities);
			mh.setParameter(parameters);
			mHandlers.add(mh);
			resH.setMessage(mHandlers);
			resHandlers.add(resH);
		}

		return resHandlers;
	}

	// create Security Contract based on offered services in Tool Consumer
	// Profile
	private SecurityContract createSecurityContract(List<Service> wantedServices, List<Service> offeredServices,
			List<InlineContext> contexts) {
		SecurityContract sc = new SecurityContract();
		String sharedSecret = UUID.randomUUID().toString();
		sc.setSharedSecret(sharedSecret);

		List<ToolService> toolServices = new ArrayList<>();
		for (Service so : offeredServices) {
			Service wantedService = findWantedService(so, wantedServices);
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

	// match Service from Tool Consumer Profile with Service we want to use
	private Service findWantedService(Service serviceOffered, List<Service> wantedServices) {
		for (String f : serviceOffered.getFormats()) {
			Service serviceWanted = findServiceWithFormat(f, wantedServices);
			if (serviceWanted != null) {
				return serviceWanted;
			}
		}
		return null;
	}

	// find service with desired format
	private Service findServiceWithFormat(String format, List<Service> services) {
		if (services != null) {
			for (Service so : services) {
				boolean exists = so.getFormats().contains(format);
				if (exists) {
					return so;
				}
			}
		}
		return null;
	}

	// create ToolService based on Service from Tool Consumer Profile and
	// service functionalities we want to use
	private ToolService createToolService(Service serviceOffered, Service serviceWanted, List<InlineContext> contexts) {
		ToolService ts = new ToolService();
		ts.setType(LTIConstants.REST_SERVICE_PROFILE);
		ts.setAction(getWantedActions(serviceOffered, serviceWanted));
		ts.setService(getFullServiceId(serviceOffered, contexts));
		return ts;
	}

	// get full service id if prefixes used
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
			boolean wanted = serviceWanted.getActions().contains(s);
			if (wanted) {
				actions.add(s);
			}
		}
		return actions;
	}

	private String getCurrentTimestamp(String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date date = new Date();
		return sdf.format(date);
	}

}
