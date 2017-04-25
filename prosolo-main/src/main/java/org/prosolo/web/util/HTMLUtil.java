package org.prosolo.web.util;

import javax.faces.application.FacesMessage;
import javax.faces.validator.ValidatorException;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;

public class HTMLUtil {

	public static String cleanHTMLTagsExceptBrA(String text) {
		Whitelist whitelist = Whitelist.none();
        whitelist.addTags(new String[]{"br", "a"});
        whitelist.addAttributes("a", "data-id");

        String safe = Jsoup.clean(text, whitelist);
        return safe;
	}
	
	public static String cleanHTMLTags(String text) {
		String noTags = Jsoup.clean(text, Whitelist.none());
		return Parser.unescapeEntities(noTags, false);
	}

	public static void cleanHTMLTagsAndRemoveWhiteSpaces(String inputText,String inputTextForValidation,FacesMessage msg){
		
		if(inputText == null || inputText.trim().isEmpty()){
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}else{
			inputTextForValidation = cleanHTMLTags(inputText);
			inputTextForValidation = inputTextForValidation.replaceAll("[\u00A0|\\s+]", "").trim();
		}
		if(inputTextForValidation.isEmpty()){
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
	}
}
