package org.prosolo.services.email.notification;

import java.util.ArrayList;
import java.util.Collection;

public class HTMLDocumentBodyElement {
	
	private String title = "";
	private Collection<HTMLDocumentBodySubElement> subelements = new ArrayList<HTMLDocumentBodySubElement>();

	public void setTitle(String title) {
		this.title = title;
	}
	
	//Collection<String> notifications=new ArrayList<String>();
	public String produceBodyElement(){
		String bodyElement="<tr class='subtitled'><td width='5%'>&nbsp;</td><td colspan='2'><br><br><strong>" +
				title.toUpperCase() +
				"</strong><br><br></td></tr>";
		int i=0;
		//for(String message:notifications){
		for (HTMLDocumentBodySubElement subElement : subelements) {
			i++;
			bodyElement = bodyElement + "<tr class='notifelement";
			
			if (subelements.size() == i) {
				bodyElement = bodyElement + " bordered_bottom";
			}
			
			bodyElement = bodyElement + "'><td width='5%'>&nbsp;</td>" +
					"<td colspan='2'>" +
					subElement.produceBodyElement() +
					"</td></tr>";		
		}
		return bodyElement;
	}
 
	public void addSubElement(HTMLDocumentBodySubElement subelement){
		this.subelements.add(subelement);
	}

}
