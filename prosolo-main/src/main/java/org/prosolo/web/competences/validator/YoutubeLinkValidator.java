package org.prosolo.web.competences.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.prosolo.web.validator.Validator;

public class YoutubeLinkValidator extends Validator {

	public YoutubeLinkValidator(Validator v) {
		super(v);
	}

	@Override
	protected Object validate(Object obj, String message) throws Exception {
		String id = null;
		//
		//^https?://.*(?:youtu.be/|v/|u/\\w/|embed/|watch?v=)([^#&?]*).*$
		Pattern pattern = Pattern.compile("(?:https?:\\/\\/)?(?:www\\.)?youtu\\.?be(?:\\.com)?\\/?.*(?:watch|embed)?(?:.*v=|v\\/|\\/)([\\w-_]+)(&.*)?",
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(obj.toString());
		if (matcher.matches()) {
			id = matcher.group(1);
			return id;
		} else {
			return null;
		}
	}

	@Override
	protected String getDefaultMessage() {
		return "Youtube link not valid";
	}

}
