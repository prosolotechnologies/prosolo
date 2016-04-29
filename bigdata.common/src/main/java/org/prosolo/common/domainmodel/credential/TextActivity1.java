package org.prosolo.common.domainmodel.credential;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class TextActivity1 extends Activity1 {

	private static final long serialVersionUID = 841699877167562858L;
	
	private String text;
	
	public TextActivity1() {
		
	}

    @Column(length = 21845, columnDefinition="Text")
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
}
