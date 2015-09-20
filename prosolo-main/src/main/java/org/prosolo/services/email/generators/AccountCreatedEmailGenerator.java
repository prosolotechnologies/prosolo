package org.prosolo.services.email.generators;

import org.prosolo.common.email.generators.EmailContentGenerator;

public class AccountCreatedEmailGenerator extends EmailContentGenerator {
	
	@Override
	public String getTemplateName() {
		return "accountCreated";
	}
	
	private String name;
	private String link;
	
	public AccountCreatedEmailGenerator(String name, String link) {
		this.name = name;
		this.link = link;
	}

	public String getName() {
		return name;
	}

	public String getLink() {
		return link;
	}

}
