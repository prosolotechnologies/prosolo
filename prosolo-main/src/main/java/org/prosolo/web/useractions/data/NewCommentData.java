package org.prosolo.web.useractions.data;

import java.io.Serializable;

public class NewCommentData implements Serializable {
	
	private static final long serialVersionUID = -1281455487659394191L;

	private String text;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
