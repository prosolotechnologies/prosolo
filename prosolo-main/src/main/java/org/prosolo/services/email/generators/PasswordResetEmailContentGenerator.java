/**
 * 
 */
package org.prosolo.services.email.generators;

import org.prosolo.common.email.generators.EmailContentGenerator;

/**
 * @author "Nikola Milikic"
 *
 */
public class PasswordResetEmailContentGenerator extends EmailContentGenerator {
	
	@Override
	public String getTemplateName() {
		return "passreset";
	}
	
	@Override
	public String getSubject() {
		return "Reset your password on ProSolo";
	}
	
	private String name;
	private String link;
	
	public PasswordResetEmailContentGenerator(String name, String link) {
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
