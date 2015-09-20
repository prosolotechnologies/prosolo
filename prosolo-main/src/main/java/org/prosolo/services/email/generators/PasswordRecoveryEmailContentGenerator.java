/**
 * 
 */
package org.prosolo.services.email.generators;

import org.prosolo.common.email.generators.EmailContentGenerator;

/**
 * @author "Nikola Milikic"
 *
 */
public class PasswordRecoveryEmailContentGenerator extends EmailContentGenerator {
	
	@Override
	public String getTemplateName() {
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
