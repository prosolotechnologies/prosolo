package org.prosolo.common.domainmodel.credential;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class TextTargetActivity1 extends TargetActivity1 {

	private static final long serialVersionUID = -8665485823516197893L;
	
	private String text;
	
	public TextTargetActivity1() {
		
	}

    @Column(length = 21845, columnDefinition="Text")
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
}
