/**
 * 
 */
package org.prosolo.web.portfolio.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.workflow.evaluation.Evaluation;

/**
 * @author "Nikola Milikic"
 * 
 */
public class CompletedResEvaluationData implements Serializable {

	private static final long serialVersionUID = 4448964510871032661L;

	private String creationDate;
	private String uri;
	private long id;
	private String text;
	private String makerName;
	private String makerUri;
	private long makerId;
	private Evaluation evaluation;

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getMakerName() {
		return makerName;
	}

	public void setMakerName(String makerName) {
		this.makerName = makerName;
	}

	public String getMakerUri() {
		return makerUri;
	}

	public void setMakerUri(String makerUri) {
		this.makerUri = makerUri;
	}
	
	public Evaluation getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(Evaluation evaluation) {
		this.evaluation = evaluation;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getMakerId() {
		return makerId;
	}

	public void setMakerId(long makerId) {
		this.makerId = makerId;
	}

}
