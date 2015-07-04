package org.prosolo.services.email.notification;

import java.util.ArrayList;
import java.util.Collection;

public class HTMLDocumentMainBody {
	private String bodyTitle="Updates";
	Collection<HTMLDocumentBodyElement> bodyElements=new ArrayList<HTMLDocumentBodyElement>();
	public void setBodyTitle(String bodyTitle) {
		this.bodyTitle = bodyTitle;
	}
	public String produceDocumentMainBody(){
		String mainBody="<tr> <td width='5%'>&nbsp;</td> <td> <table style='width: 100%; height: 100%; background-color: white;' rule='none' class='bordered'> <tbody>";
		mainBody=mainBody+"<tr class='bordered_bottom titled'><td width='5%'>&nbsp;</td><td width='90%' style='font-size:20px'><br>"+bodyTitle+"</td></tr>";
		for(HTMLDocumentBodyElement bodyEl:bodyElements){
			mainBody=mainBody+bodyEl.produceBodyElement();
		}
		mainBody=mainBody+"</tbody></td></tr>";
		
		return mainBody;
	}
	public void addBodyElement(HTMLDocumentBodyElement bElement){
		bodyElements.add(bElement);
	}

}
