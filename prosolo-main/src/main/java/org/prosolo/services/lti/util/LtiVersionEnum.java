package org.prosolo.services.lti.util;

public enum LtiVersionEnum {
	LTI1("LTI-1p0"), LTI2("LTI-2p0");
	
	private final String val;
	
	private LtiVersionEnum(String val){
		this.val = val;
	}

	public String getVal() {
		return val;
	}
	
}
