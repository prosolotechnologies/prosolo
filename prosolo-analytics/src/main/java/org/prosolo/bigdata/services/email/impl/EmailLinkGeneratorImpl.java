package org.prosolo.bigdata.services.email.impl;

import org.prosolo.bigdata.services.email.EmailLinkGenerator;
import org.prosolo.bigdata.services.urlencoding.UrlIdEncoder;
import org.prosolo.bigdata.services.urlencoding.impl.HashidsUrlIdEncoderImpl;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.general.BaseEntity;

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
	public <T extends BaseEntity> String getLink(T entity, long userId, String context) {
		String baseUrl = CommonSettings.getInstance().config.appConfig.domain + getPage();
		String id = idEncoder.encodeId(entity.getId());
		String user = idEncoder.encodeId(userId);
		String params = String.format("?id=%1$s&user=%2$s&context=%3$s", id, user, context);
		return baseUrl + params;
	}

	private String getPage() {
		return "email.xhtml";
	}
	
}
