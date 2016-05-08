package org.prosolo.services.rest.clients;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.bigdata.common.rest.RestClient;
import org.prosolo.bigdata.common.rest.exceptions.ConnectException;
import org.prosolo.bigdata.common.rest.pojo.RequestObject;
import org.prosolo.config.AnalyticalServerConfig;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

/**
 * @author Zoran Jeremic Apr 20, 2015
 *
 */
@Service("org.prosolo.services.rest.clients.RecommendationServices")
public class RecommendationServicesRestImpl implements RecommendationServicesRest {
	
	@SuppressWarnings("unused")
	private final static Logger logger = Logger.getLogger(RecommendationServicesRest.class);
	
	private RestClient restClient = new RestClient();
	private Gson gson = new Gson();
	
	@Override
	public String sendPostRequest(String serviceLocalPath, RequestObject paramObject) throws ConnectException {
		AnalyticalServerConfig analyticalConfig = Settings.getInstance().config.analyticalServerConfig;
		String serviceUrl = "http://" + analyticalConfig.apiHost;
		
		if (analyticalConfig.apiPort > 0) {
			serviceUrl = serviceUrl + ":" + analyticalConfig.apiPort;
		}
		
		serviceUrl = serviceUrl + "/" + analyticalConfig.apiServicesPath + serviceLocalPath;
		String response = restClient.sendPostRequest(serviceUrl, gson.toJson(paramObject));
		
		return response;
	}

	
}
