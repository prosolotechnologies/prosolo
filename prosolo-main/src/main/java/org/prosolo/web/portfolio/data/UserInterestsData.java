/**
 * 
 */
package org.prosolo.web.portfolio.data;

import java.io.Serializable;

/**
 * @author "Nikola Milikic"
 *
 */
public class UserInterestsData implements Serializable {

	private static final long serialVersionUID = -4452977745120441507L;

	private String interestsText = "";
	private String interestsTextEdit = "";

	public String getInterestsText() {
		return interestsText;
	}

	public void setInterestsText(String interestsText) {
		this.interestsText = interestsText;
	}

	public String getInterestsTextEdit() {
		return interestsTextEdit;
	}

	public void setInterestsTextEdit(String interestsTextEdit) {
		this.interestsTextEdit = interestsTextEdit;
	}
	
}
