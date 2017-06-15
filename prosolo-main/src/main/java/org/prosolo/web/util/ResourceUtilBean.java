package org.prosolo.web.util;

import javax.faces.bean.ManagedBean;

import org.springframework.stereotype.Component;

@ManagedBean(name = "resourceUtilBean")
@Component("resourceUtilBean")
public class ResourceUtilBean {
	
	public static long getCurrentTimestamp(){
		return System.currentTimeMillis();
	}
}
