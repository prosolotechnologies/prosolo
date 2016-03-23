package org.prosolo.bigdata.services.email.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.prosolo.bigdata.services.email.EmailLinkGenerator;
import org.prosolo.bigdata.services.urlencoding.UrlIdEncoder;
import org.prosolo.bigdata.services.urlencoding.impl.HashidsUrlIdEncoderImpl;
import org.prosolo.common.config.CommonSettings;

public class EmailLinkGeneratorImpl implements EmailLinkGenerator {

	public static class EmailLinkGeneratorHolder {
		public static final EmailLinkGeneratorImpl INSTANCE = new EmailLinkGeneratorImpl();
	}
	
	public static EmailLinkGeneratorImpl getInstance() {
		return EmailLinkGeneratorHolder.INSTANCE;
	}
	
	private UrlIdEncoder idEncoder;
	
	private EmailLinkGeneratorImpl () {
		idEncoder = HashidsUrlIdEncoderImpl.getInstance();
	}
	
	@Override
	public String getLink(long userId, LinkedHashMap<String, Long> contextParams) {
		String baseUrl = CommonSettings.getInstance().config.appConfig.domain + getPage();
		String user = idEncoder.encodeId(userId);
		String context = "";
		for (Map.Entry<String, Long> entry : contextParams.entrySet()) {
			String currentCtx = entry.getKey() + ":" + idEncoder.encodeId(entry.getValue());
		    context = context.isEmpty() ? currentCtx : context + "." + currentCtx;
		}
		String params = String.format("?user=%1$s&context=%2$s", user, context);
		return baseUrl + params;
	}

	private String getPage() {
		return "email.xhtml";
	}
	
}
