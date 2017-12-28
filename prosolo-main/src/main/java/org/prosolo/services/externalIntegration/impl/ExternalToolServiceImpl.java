package org.prosolo.services.externalIntegration.impl;

import com.jcabi.xml.XMLDocument;
import net.oauth.OAuth.Parameter;
import net.oauth.*;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.prosolo.common.domainmodel.credential.ExternalToolActivity1;
import org.prosolo.common.domainmodel.credential.ScoreCalculation;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.authentication.OAuthValidator;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.externalIntegration.BasicLTIResponse;
import org.prosolo.services.externalIntegration.ExternalToolService;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.util.XMLUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zoran Jeremic Dec 26, 2014
 *
 */
@Service("org.prosolo.services.externalIntegration.ExternalToolService")
public class ExternalToolServiceImpl implements ExternalToolService {
	
	private static Logger logger = Logger.getLogger(ExternalToolServiceImpl.class.getName());

	@Autowired private Activity1Manager activityManager;
	@Autowired private ResourceFactory resourceFactory;
	@Autowired private OAuthValidator oauthValidator;
	@Inject private AssessmentManager assessmentManager;
	@Inject private EventFactory eventFactory;
	
	@Override
	public boolean checkAuthorization(String authorization, String url, String method, String consumerSecret) throws IOException, OAuthException, URISyntaxException{
		List<Parameter> parameters = new ArrayList<Parameter>();
		for (Parameter param : OAuthMessage.decodeAuthorization(authorization)) {
			if (!"realm".equalsIgnoreCase(param.getKey())) {
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
			 ExternalToolActivity1 activity;
			try {
				activity = (ExternalToolActivity1) activityManager.loadResource(ExternalToolActivity1.class, activityId);
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
		String xml=new XMLDocument(w3cDoc).toString();
		System.out.println("INPUT:"+xml);
		BasicLTIResponse response=null;
		String messageIdentifier="";
		String sourceId="";

		Result<Void> res = new Result<>();

		try {
			System.out.println("SCORE DOC:"+doc.toString());
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
				//SimpleOutcome outcome=resourceFactory.createSimpleOutcome(score);
				//activityManager.replaceTargetActivityOutcome(targetActivityId, outcome, session);
				Transaction transaction = null;
				try {
					transaction = session.beginTransaction();
					TargetActivity1 ta = (TargetActivity1) session.get(
							TargetActivity1.class, targetActivityId);
					ExternalToolActivity1 act = (ExternalToolActivity1) session.get(
							ExternalToolActivity1.class, activityId);
					
					if (act.isAcceptGrades()) {
						int maxPoints = act.getMaxPoints();
						int scaledGrade = (int) Math.round(score * maxPoints);
						resourceFactory.createSimpleOutcome(scaledGrade, targetActivityId, session);
						int calculatedScore = calculateScoreBasedOnCalculationType(ta, act.getScoreCalculation(),
							scaledGrade);
						if(calculatedScore >= 0) {
							int prevScore = ta.getCommonScore();
							ta.setCommonScore(calculatedScore);
							ta.setNumberOfAttempts(ta.getNumberOfAttempts() + 1);
							if(calculatedScore != prevScore) {
								PageContextData lcd = new PageContextData();
								lcd.setLearningContext("name:external_activity_grade|id:" + ta.getId());
								//TODO how to include organization id in event here
								res.appendEvents(assessmentManager
									.updateActivityGradeInAllAssessmentsAndGetEvents(
											userId, activityId, calculatedScore, session, UserContextData.ofLearningContext(lcd)).getEventQueue());
							}
						}
					}
				 	transaction.commit();
				} catch(Exception e) {
					e.printStackTrace();
					logger.error(e);
					transaction.rollback();
				} finally {
					HibernateUtil.close(session);
				}

				if (res != null) {
					eventFactory.generateEvents(res.getEventQueue());
				}

				System.out.println("USER ID:" + parts[0] + " activity id:"
						+ parts[1] + " target activity id:" + parts[2]+" SCORE:"+score);

			} else {
				System.out.println("DOESNT HAVE RESULT YET");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response=this.createLTIResponse(sourceId, messageIdentifier, false, e.getLocalizedMessage());
			return response;
		}
		response=this.createLTIResponse(sourceId, messageIdentifier, true, "Score updated successfully");
		return response;
	}
	
	private int calculateScoreBasedOnCalculationType(TargetActivity1 ta, ScoreCalculation scoreCalculation, 
			int newScore) {
		int previousScore = ta.getCommonScore();
		int numberOfAttempts = ta.getNumberOfAttempts();
		if(numberOfAttempts == 0) {
			return newScore;
		}
		if(scoreCalculation == null) {
			return -1;
		}
		switch(scoreCalculation) {
			case BEST_SCORE:
				return newScore > previousScore ? newScore : previousScore;
			case LATEST_SCORE:
				return newScore;
			case AVG:
				return (int) Math.round((previousScore * numberOfAttempts + newScore) / 
					(numberOfAttempts * 1.0 + 1));
		}
		return -1;
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
