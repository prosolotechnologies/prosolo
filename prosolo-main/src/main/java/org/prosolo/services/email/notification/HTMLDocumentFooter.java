package org.prosolo.services.email.notification;

public class HTMLDocumentFooter {
	private String subtitle="@2014, ProSolo";
	private String title="";
	
	public HTMLDocumentFooter(){
		
	}
	public HTMLDocumentFooter(String title, String subtitle){
		this.title=title;
		this.subtitle=subtitle;
	}
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String produceDocumentFooter(){
		String footer="<tr class='footer'><td width='5%'>&nbsp;</td><td  width='90%'>";
		if(!title.equals("")){
			footer=footer+"<small>"+title+"</small><br>";
		}
		 
				footer=footer+"<small>"+subtitle+"</small></td><td width='5%'>&nbsp;</td>";
		return footer;
	}
	
}
