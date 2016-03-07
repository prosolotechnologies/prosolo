package org.prosolo.web.lti.message;

import org.prosolo.web.lti.validator.EmptyValidator;
import org.prosolo.web.lti.validator.LongValidator;
import org.prosolo.web.lti.validator.NullValidator;

public class LTIMessage {

	private LtiMessageParameter ltiVersion;
	private LtiMessageParameter id;
	//private List<String> roles;
	
	
	public LTIMessage(){
		ltiVersion = new LtiMessageParameter(new NullValidator (new EmptyValidator(null)));
		id = new LtiMessageParameter(new NullValidator(new EmptyValidator(new LongValidator(null))));
	}

	public String getLtiVersion() {
		return ltiVersion.getParameter();
	}

	public void setLtiVersion(String ltiVersion) throws Exception {
		this.ltiVersion.setParameter(ltiVersion);
	}

	public long getId() {
		return Long.parseLong(id.getParameter());
	}

	public void setId(String id) throws Exception {
		this.id.setParameter(id);
	}

}
