package org.prosolo.services.email.notification;

/**
 * @author Zoran Jeremic Sep 21, 2014
 *
 */

public class HTMLDocumentBodySimpleSubElement extends HTMLDocumentBodySubElement{

	String link;
	String linkTitle;
	
	public HTMLDocumentBodySimpleSubElement(String link, String linkTitle){
		this.link=link;
		this.linkTitle=linkTitle;
	}
	@Override
	public String produceBodyElement() {
		String content="<div style='text-align:right;float:rigth; margin-right:250px; color:#a1a1a1;font-size:14px;'><a href='"+this.link+"'>"+this.linkTitle+"</a></div><br>";
		return content;
	}
	
}
