/**
 * 
 */
package org.prosolo.services.email.generators;

/**
 * @author "Nikola Milikic"
 *
 */
public class PasswordRecoveryEmailContentGenerator extends EmailContentGenerator {
	
	@Override
	String getTemplateName() {
		return "passwordReset";
	}
	
	private String name;
	private String link;
	
	public PasswordRecoveryEmailContentGenerator(String name, String link) {
		this.name = name;
		this.link = link;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

}
