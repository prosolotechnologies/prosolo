package org.prosolo.services.externalIntegration.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import net.oauth.OAuth.Parameter;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.activities.ExternalToolActivity;
import org.prosolo.common.domainmodel.outcomes.Outcome;
import org.prosolo.common.domainmodel.outcomes.SimpleOutcome;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.authentication.OAuthValidator;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.externalIntegration.BasicLTIResponse;
import org.prosolo.services.externalIntegration.ExternalToolService;
import org.prosolo.services.interfaceSettings.LearnActivityCacheUpdater;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.messaging.data.ServiceType;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.util.XMLUtils;
import org.prosolo.web.ApplicationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Zoran Jeremic Dec 26, 2014
 *
 */
@Service("org.prosolo.services.externalIntegration.ExternalToolService")
public class ExternalToolServiceImpl implements ExternalToolService {
	
	private static Logger logger = Logger.getLogger(ExternalToolServiceImpl.class.getName());

	@Autowired private ActivityManager activityManager;
	@Autowired private ResourceFactory resourceFactory;
	@Autowired private ApplicationBean applicationBean;
	@Autowired private SessionMessageDistributer messageDistributer;
	@Autowired private OAuthValidator oauthValidator;
	
	@Override
	public boolean checkAuthorization(String authorization, String url, String method, String consumerSecret) throws IOException, OAuthException, URISyntaxException{
		List<Parameter> parameters=new ArrayList<Parameter>();
		for(Parameter param : OAuthMessage.decodeAuthorization(authorization)){
			if(!"realm".equalsIgnoreCase(param.getKey())){
				parameters.add(param);
			}
			} 
		
		OAuthMessage oauthMessage=new OAuthMessage(method,url,parameters);
		//oauthMessage.addParameter("oauth_consumer_secret", "secret");
		OAuthServiceProvider serviceProvider = new OAuthServiceProvider(null, null, null);
		// try to load from local cache if not throw exception
		String consumerKey = oauthMessage.getConsumerKey();
		//String consumerSecret="";
		OAuthConsumer consumer = new OAuthConsumer(null, consumerKey, consumerSecret, serviceProvider);
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		accessor.tokenSecret = "";
		oauthValidator.validateMessage(oauthMessage, accessor);
			return true;
	}
	
	@Override
	public String retrieveConsumerSecret(org.w3c.dom.Document w3cDoc) throws TransformerFactoryConfigurationError, TransformerException{
		//Transformer tf = TransformerFactory.newInstance().newTransformer();
		//tf.transform(new DOMSource(w3cDoc), new StreamResult(System.out));
		Document doc = XMLUtils.convertW3CDocument(w3cDoc);
		String sourceId="";
		String secret="";
		try {
			 sourceId = XMLUtils.getXMLElementByPath(
						doc.getRootElement(), "//*[local-name()='sourcedId']")
						.getText();
			 String[] parts = sourceId.split("\\::");
			 long activityId = Long.valueOf(parts[1]);
			 ExternalToolActivity activity;
			try {
				activity = activityManager.loadResource(ExternalToolActivity.class, activityId);
				secret=activity.getSharedSecret();
			} catch (ResourceCouldNotBeLoadedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
			 
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return secret;
	}
 
	@Override
	public BasicLTIResponse processReplaceResultOutcome(org.w3c.dom.Document w3cDoc)
			throws TransformerException {
		//Transformer tf = TransformerFactory.newInstance().newTransformer();
		//tf.transform(new DOMSource(w3cDoc), new StreamResult(System.out));
		Document doc = XMLUtils.convertW3CDocument(w3cDoc);
		BasicLTIResponse response=null;
		String messageIdentifier="";
		String sourceId="";
		try {
			 messageIdentifier=XMLUtils.getXMLElementByPath(
					doc.getRootElement(), "//*[local-name()='imsx_messageIdentifier']").getText();
			 sourceId = XMLUtils.getXMLElementByPath(
					doc.getRootElement(), "//*[local-name()='sourcedId']")
					.getText();
			Element result = XMLUtils.getXMLElementByPath(doc.getRootElement(),
					"//*[local-name()='result']");
			if (result != null) {
				String textString = XMLUtils.getXMLElementByPath(result,
						"//*[local-name()='textString']").getText();
				Double score = Double.parseDouble(textString);
				String[] parts = sourceId.split("\\::");
				long userId = Long.valueOf(parts[0]);
				long activityId = Long.valueOf(parts[1]);
				long targetActivityId = Long.valueOf(parts[2]);
				Session session = (Session) activityManager.getPersistence().openSession();
				SimpleOutcome outcome=resourceFactory.createSimpleOutcome(score);
				activityManager.replaceTargetActivityOutcome(targetActivityId, outcome, session);
				this.updateTargetActivityOutcomeInformation(targetActivityId, activityId, outcome.getId(), userId, session);
				session.flush();
				session.close();

				System.out.println("USER ID:" + parts[0] + " activity id:"
						+ parts[1] + " target activity id:" + parts[2]+" SCORE:"+score);

			} else {
				System.out.println("DOESNT HAVE RESULT YET");
			}
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response=this.createLTIResponse(sourceId, messageIdentifier, false, e.getLocalizedMessage());
			return response;
		}
		response=this.createLTIResponse(sourceId, messageIdentifier, true, "Score updated successfully");
		return response;
	}
	
	private BasicLTIResponse createLTIResponse(String consumerRef, String providerRef, boolean success, String description){
		BasicLTIResponse response=new BasicLTIResponse();
		response.setConsumerRef(consumerRef);
		response.setProviderRef(providerRef);
		response.setAction("");
		response.setDescription(description);
		
		if (success) {
			response.setCodeMajor("success");
		} else {
			response.setCodeMajor("failure");
		}
		return response;
	}
	
	private void updateTargetActivityOutcomeInformation(long targetActivityId, long activityId, long outcomeId, long userId, Session session){
		HttpSession userSession = applicationBean.getUserSession(userId);
			
		if (Settings.getInstance().config.rabbitmq.distributed) {
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("targetActivityId", String.valueOf(targetActivityId));
			parameters.put("activityId", String.valueOf(activityId));
			parameters.put("outcomeId", String.valueOf(outcomeId));
			parameters.put("userId", String.valueOf(userId));
			
			messageDistributer.distributeMessage(
					ServiceType.UPDATETARGETACTIVITYOUTCOME,
					userId,
					outcomeId,
					null,
					parameters);
		} else if (userSession != null) {
			try {
				Outcome outcome = activityManager.loadResource(Outcome.class, outcomeId, true, session);

				LearnActivityCacheUpdater learnActivityCacheUpdater = ServiceLocator.getInstance().getService(LearnActivityCacheUpdater.class);
				
				learnActivityCacheUpdater.updateActivityOutcome(targetActivityId, outcome, userSession, session);
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
			
		}
	}
	

	@Override
	public BasicLTIResponse processFailureResponse(org.w3c.dom.Document w3cDoc,
			String failureMessage) {
		BasicLTIResponse response=null;
		String messageIdentifier="";
		String sourceId="";
		Document doc = XMLUtils.convertW3CDocument(w3cDoc);
			 try {
				messageIdentifier=XMLUtils.getXMLElementByPath(
						doc.getRootElement(), "//*[local-name()='imsx_messageIdentifier']").getText();
				 sourceId = XMLUtils.getXMLElementByPath(
							doc.getRootElement(), "//*[local-name()='sourcedId']")
							.getText();
			} catch (JDOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		response=this.createLTIResponse(sourceId, messageIdentifier, false, failureMessage);
		return response;
	 
	}

}
