package org.prosolo.services.email.notification;

public class HTMLDocumentHeader {
	private String subtitle = "Prosolo - your daily updates!";
	private String title = "";
	private String period = "";

	public HTMLDocumentHeader() {

	}
	
	public HTMLDocumentHeader(String subtitle, String title, String period) {
		this.subtitle = subtitle;
		this.title = title;
		this.period = period;
	}
	
	public String produceDocumentHeader(){
		//String header="<tr class='header'><td width='10%'>&nbsp;</td><td>"+subtitle+"<br><big>"+title;
		String header="<tr>"
				+ "<td width='5%'>&nbsp;</td>"
				+ "<td width='90%' style='margin:0;padding:20px 15px;color:white;background:2e7ab4;background:-moz-linear-gradient(top,#539fda 0%,#2e7ab4 100%);background:-webkit-linear-gradient(top,#539fda 0%,#2e7ab4 100%);background:-o-linear-gradient(top,#539fda 0%,#2e7ab4 100%);background:-ms-linear-gradient(top,#539fda 0%,#2e7ab4 100%);background:linear-gradient(top,#539fda 0%,#2e7ab4 100%)'>"
				+ "<h2>"
				+subtitle+ "</h2>";
		if (!title.equals("")) {
			header = header + "<h3>" + title + "</h3>";
		}

		if (!period.equals("")) {
			header = header + " " + period;
		}
		header = header + "</td><td width='5%'>&nbsp;</td></tr>";

		return header;
	}
	
	/* 
	 * GETTERS / SETTERS
	 */
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

}
