package org.prosolo.common.domainmodel.lti;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
public class LtiConsumer extends BaseLtiEntity {
	
	private static final long serialVersionUID = -6100293716089700061L;
	
	private String keyLtiOne;
	private String secretLtiOne;
	private String keyLtiTwo;
	private String secretLtiTwo;
	private String capabilities;
	private List<String> capabilitieList;
	private Set<LtiService> services;
	private LtiToolSet toolSet;

	public LtiConsumer(){
		capabilitieList = new ArrayList<>();
		services = new HashSet<>();
	}
	
	public LtiConsumer(long id, String keyLtiOne, String secretLtiOne, String keyLtiTwo, String secretLtiTwo){
		this.setId(id);
		this.keyLtiOne = keyLtiOne;
		this.secretLtiOne = secretLtiOne;
		this.keyLtiTwo = keyLtiTwo;
		this.secretLtiTwo = secretLtiTwo;
	}
	
	@Column(nullable = false)
	public String getKeyLtiOne() {
		return keyLtiOne;
	}

	public void setKeyLtiOne(String keyLtiOne) {
		this.keyLtiOne = keyLtiOne;
	}

	@Column(nullable = false)
	public String getSecretLtiOne() {
		return secretLtiOne;
	}

	public void setSecretLtiOne(String secretLtiOne) {
		this.secretLtiOne = secretLtiOne;
	}

	public String getKeyLtiTwo() {
		return keyLtiTwo;
	}

	public void setKeyLtiTwo(String keyLtiTwo) {
		this.keyLtiTwo = keyLtiTwo;
	}

	public String getSecretLtiTwo() {
		return secretLtiTwo;
	}

	public void setSecretLtiTwo(String secretLtiTwo) {
		this.secretLtiTwo = secretLtiTwo;
	}

	@Column (length = 2000)
	public String getCapabilities() {
		if(capabilitieList != null){
			capabilities = StringUtils.join(capabilitieList, ',');
		}
		return capabilities;
	}
	public void setCapabilities(String capabilities) {
		this.capabilities = capabilities;
		if(capabilities != null){
			capabilitieList = new ArrayList<String>(Arrays.asList(capabilities.split(",")));
		}
	}
	@Transient
	public List<String> getCapabilitieList() {
		return capabilitieList;
	}
	public void setCapabilitieList(List<String> capabilitieList) {
		this.capabilitieList = capabilitieList;
	}

	@OneToMany(mappedBy="consumer")
	@Cascade (CascadeType.ALL)
	public Set<LtiService> getServices() {
		return services;
	}

	public void setServices(Set<LtiService> services) {
		this.services = services;
	}

	@OneToOne(mappedBy = "consumer")
	public LtiToolSet getToolSet() {
		return toolSet;
	}

	public void setToolSet(LtiToolSet toolSet) {
		this.toolSet = toolSet;
	}
	

}