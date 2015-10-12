package org.prosolo.common.domainmodel.lti;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;

@Entity
public class LtiToolSet extends BaseLtiEntity {

	private static final long serialVersionUID = 4261594093642672687L;
	
	private String productCode;
	private Set<LtiTool> tools;
	private LtiConsumer consumer;
	private String registrationUrl;

	public LtiToolSet(){
		tools = new HashSet<>();
	}
	
	public LtiToolSet(long id, LtiConsumer consumer) {
		setId(id);
		this.consumer = consumer;
	}


	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	@OneToMany(mappedBy="toolSet")
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	public Set<LtiTool> getTools() {
		return tools;
	}

	public void setTools(Set<LtiTool> tools) {
		this.tools = tools;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	@JoinColumn(nullable = false)
	public LtiConsumer getConsumer() {
		return consumer;
	}

	public void setConsumer(LtiConsumer consumer) {
		this.consumer = consumer;
	}


	public String getRegistrationUrl() {
		return registrationUrl;
	}

	public void setRegistrationUrl(String registrationUrl) {
		this.registrationUrl = registrationUrl;
	}

	@Transient
	public String getFullRegistrationURL(){
		return registrationUrl+"?id="+getId();
	}

}