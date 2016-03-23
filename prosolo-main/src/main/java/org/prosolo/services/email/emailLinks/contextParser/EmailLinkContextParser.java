package org.prosolo.services.email.emailLinks.contextParser;

import java.util.Optional;

import org.prosolo.services.email.emailLinks.data.LinkObjectContextData;

public interface EmailLinkContextParser {

	/*
	 * return parsed learningContext as well as object info from
	 * context so that we can retrieve the actual link that user
	 * should be redirected to 
	*/
	Optional<LinkObjectContextData> getLearningContext(String ctx);

}