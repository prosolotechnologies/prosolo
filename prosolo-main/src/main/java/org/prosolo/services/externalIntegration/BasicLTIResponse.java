package org.prosolo.services.externalIntegration;

import java.io.Serializable;

/**
 * @author Zoran Jeremic Dec 29, 2014
 *
 */

public class BasicLTIResponse implements Serializable{
	private boolean ok = false;
	private String action = null;
	private String codeMajor = null;
	private String description = null;
	private String consumerRef = null;
	private String providerRef = null;
	private String data = null;

	public BasicLTIResponse() {
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getCodeMajor() {
		return codeMajor;
	}

	public void setCodeMajor(String codeMajor) {
		this.codeMajor = codeMajor;
	}

	public String getConsumerRef() {
		return consumerRef;
	}

	public void setConsumerRef(String consumerRef) {
		this.consumerRef = consumerRef;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProviderRef() {
		return providerRef;
	}

	public void setProviderRef(String providerRef) {
		this.providerRef = providerRef;
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public String toXML() {
		if (ok) {
			this.codeMajor = "success";
		} else if (this.codeMajor == null) {
			this.codeMajor = "failure";
		}
		StringBuilder xml = new StringBuilder();
		xml.append("<imsx_POXEnvelopeResponse xmlns=\"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">\n");
		xml.append(" <imsx_POXHeader>\n");
		xml.append(" <imsx_POXResponseHeaderInfo>\n");
		xml.append(" <imsx_version>V1.0</imsx_version>\n");
		xml.append(" <imsx_messageIdentifier>").append(this.consumerRef)
				.append("</imsx_messageIdentifier>\n");
		xml.append(" <imsx_statusInfo>\n");
		xml.append(" <imsx_codeMajor>").append(this.codeMajor)
				.append("</imsx_codeMajor>\n");
		xml.append(" <imsx_severity>status</imsx_severity>\n");
		xml.append(" <imsx_description>").append(this.description)
				.append("</imsx_description>\n");
		xml.append(" <imsx_messageRefIdentifier>").append(this.providerRef)
				.append("</imsx_messageRefIdentifier>\n");
		if (this.action.length() > 0) {
			xml.append(" <imsx_operationRefIdentifier>").append(this.action)
					.append("</imsx_operationRefIdentifier>\n");
		}
		xml.append(" </imsx_statusInfo>\n");
		xml.append(" </imsx_POXResponseHeaderInfo>\n");
		xml.append(" </imsx_POXHeader>\n");
		xml.append(" <imsx_POXBody>\n");
		if (this.data != null) {
			xml.append(" <").append(this.action).append("Response>\n");
			xml.append(this.data);
			xml.append(" </").append(this.action).append("Response>\n");
		} else {
			xml.append(" <").append(this.action).append("Response />\n");
		}
		xml.append(" </imsx_POXBody>\n");
		xml.append("</imsx_POXEnvelopeResponse>");
		return xml.toString();
	}
}
