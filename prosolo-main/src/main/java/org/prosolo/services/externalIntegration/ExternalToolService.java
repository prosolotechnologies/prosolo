package org.prosolo.services.externalIntegration;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import net.oauth.OAuthException;

import org.w3c.dom.Document;

/**
@author Zoran Jeremic Dec 26, 2014
 *
 */

public interface ExternalToolService {

	BasicLTIResponse processReplaceResultOutcome(Document w3cDoc)
			throws TransformerException;
 

	//boolean checkAuthorization(String authorization, String url, String method)
	//		throws IOException, OAuthException, URISyntaxException;


	BasicLTIResponse processFailureResponse(Document w3cDoc,
			String failureMessage);


	String retrieveConsumerSecret(Document w3cDoc) throws TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException;


	boolean checkAuthorization(String authorization, String url, String method,
			String consumerSecret) throws IOException, OAuthException,
			URISyntaxException;

}

