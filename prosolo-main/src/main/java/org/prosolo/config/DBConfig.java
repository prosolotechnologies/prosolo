package org.prosolo.config;

import org.prosolo.util.StringUtils;
import org.simpleframework.xml.Element;

public class DBConfig {

	@Element(name = "dialect")
	public String dialect;

	@Element(name = "driver")
	public String driver;

	@Element(name = "url")
	public String url;

	@Element(name = "user")
	public String user;

	@Element(name = "password")
	public String password;

	@Override
	public String toString() {
		return StringUtils.toStringByReflection(this);
	}

}