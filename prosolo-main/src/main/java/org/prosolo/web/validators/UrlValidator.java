package org.prosolo.web.validators;

import org.prosolo.common.util.string.StringUtil;
import org.prosolo.web.validator.Validator;

public class UrlValidator extends Validator {

	public UrlValidator(Validator v) {
		super(v);
	}

	@Override
	protected Object validate(Object obj, String message) throws Exception {
		org.apache.commons.validator.routines.UrlValidator urlValidator = 
				new org.apache.commons.validator.routines.UrlValidator();
		//String decodeUrl = URLDecoder.decode(obj.toString(),"UTF-8");
		//System.out.println("DECODED URL " + decodeUrl);
		//String decodedWithUri = new URI(obj.toString()).getPath();
		//System.out.println("DECODED WITH URI " + decodedWithUri);
		//URL urlString = new URL(decodeUrl);
		//URI uri = new URI(urlString.getProtocol(), urlString.getUserInfo(), urlString.getHost(), urlString.getPort(), urlString.getPath(), urlString.getQuery(), urlString.getRef());
		//System.out.println("URI " + uri.toString());
		boolean valid = urlValidator.isValid(StringUtil.encodeUrl(obj.toString()));
		if(valid) {
			return obj;
		}
		throw new Exception(getExceptionMessage(message));
	}

	@Override
	protected String getDefaultMessage() {
		return "Url not valid";
	}

}
