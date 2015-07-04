package org.prosolo.services.email.generators;

public class AccountCreatedEmailGenerator extends EmailContentGenerator {
	
	@Override
	String getTemplateName() {
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
