/**
 * 
 */
package org.prosolo.web.communications.evaluation.data;

import org.prosolo.common.domainmodel.portfolio.ExternalCredit;
import org.prosolo.util.date.DateUtil;


/**
 * @author "Nikola Milikic"
 * 
 */
public class EvaluatedExCreditData extends EvaluatedResourceData {

	private static final long serialVersionUID = 6016874063612023041L;
	
	private String description;
	private String certificateLink;
	public String start;
	public String end;

	public EvaluatedExCreditData(ExternalCredit exCredit, EvaluatedResourceType type) {
		super(exCredit.getId(), exCredit.getTitle(), type);
		setResource(exCredit);
		this.description = exCredit.getDescription();
		this.certificateLink = exCredit.getCertificateLink();
		this.start = DateUtil.getPrettyDate(exCredit.getStart());
		this.end = DateUtil.getPrettyDate(exCredit.getEnd());
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCertificateLink() {
		return certificateLink;
	}

	public void setCertificateLink(String certificateLink) {
		this.certificateLink = certificateLink;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

}
