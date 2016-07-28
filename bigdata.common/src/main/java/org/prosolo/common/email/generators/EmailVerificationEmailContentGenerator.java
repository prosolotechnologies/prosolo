package org.prosolo.common.email.generators;



/**
 * @author Zoran Jeremic 2013-10-25
 *
 */

public class EmailVerificationEmailContentGenerator extends EmailContentGenerator {
	
	@Override
	public String getTemplateName() {
		return "verifyEmail";
	}
	
	@Override
	public String getSubject() {
		return "Verify email";
	}
	
	private String name;
	private String link;
	
	public EmailVerificationEmailContentGenerator(String name, String link) {
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
