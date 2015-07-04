package org.prosolo.services.email.notification;

public class HTMLDocument {
	private String title="ProSolo automatic notification";
	private String cssStyles=".bordered { border: 1px solid; border-color: #D7D7D7; frame='body'; } " +
			".bordered_bottom td { border-bottom: 1px solid; border-color: #D7D7D7; frame='bottom'; } " +
			".border tr td{ border: medium none; } " +
			".titled{ font-color: grey; height: 40px; } " +
			".subtitled{ height: 15px; } " +
			".notifelement{ height: 50px;}"+
			".header{height: 60px}"+
			".footer{height: 60px}"+
			
".atcImages {overflow: hidden;display: inline;}"+
".feedContent {padding: 0 0 10px 0;}"+
".imgInfoContainer20 {overflow: hidden;	float: left;	padding: 0 10px 0 0;}"
+ ".textWrapper {width: 600px;}"
+ ".atcTitle {font-size: 13px;	display: block;	font-weight: bold;	word-wrap: break-word;	white-space: normal;}"
+ ".atcUrl {font-size: 10px;	display: block;	word-break: break-all;}"
+ ".atcDesc {	font-size: 12px;	width: 100%;}"
+ ".date {	float: right;	margin-right: 5px;	color: #a1a1a1;	font-size: 10px;}";
			
	private String metadata="<meta content='text/html; charset=ISO-8859-1' http-equiv='content-type'>";
	private HTMLDocumentFooter docFooter;
	private HTMLDocumentHeader docHeader;
	private HTMLDocumentMainBody docMainBody;

	public void setDocMainBody(HTMLDocumentMainBody docMainBody) {
		this.docMainBody = docMainBody;
	}

	public void setDocFooter(HTMLDocumentFooter docFooter) {
		this.docFooter = docFooter;
	}

	public void setDocHeader(HTMLDocumentHeader docHeader) {
		this.docHeader = docHeader;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	public String produceDocumentHead(){
		String docHead="<head>"+metadata+"<style type='text/css'>"+cssStyles+"</style>"+"<title>"+title+"</title></head>";
		
		return docHead;
	}
	public String produceHTMLDocument(){
		String htmlDocument="<html>"+produceDocumentHead()+
				getMainDocumentBody()+
				"</html>";
		
		return htmlDocument;
	}
	public String getBodyHeader(){
		String bHeader="";
		if (docHeader!=null){
			bHeader=docHeader.produceDocumentHeader();
		}
		return bHeader;
	}
	public String getMainBody(){
		String mainBody="";
		if(this.docMainBody!=null){
			mainBody=docMainBody.produceDocumentMainBody();
		}
		return mainBody;
	}
	public String getBodyFooter(){
		String bFooter="";
		if(docFooter!=null){
			bFooter=docFooter.produceDocumentFooter();
		}
		return bFooter;
	}
	public String getMainDocumentBody(){
		String docBody="<body><table style='background-color: rgb(239, 239, 239); width: 1202px;'><tbody>"+
	getBodyHeader()+
	getMainBody()+
	getBodyFooter()+
	 "</tbody></table></body>";
		return docBody;
	}

}
