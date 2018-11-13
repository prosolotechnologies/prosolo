package org.prosolo.web;

import org.prosolo.app.Settings;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.config.Config;
import org.prosolo.services.logging.AccessResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/*
 * @author Zoran Jeremic 2013-05-18
 */
@ManagedBean(name="applicationbean")
@Component("applicationbean")
@Scope("singleton")
public class ApplicationBean implements Serializable {

	private static final long serialVersionUID = 6323655674786060348L;
	
	@Autowired AccessResolver accessResolver;

	private String serverIP = null;
	private long loginTime;
	
	public ApplicationBean(){
		loginTime = new Date().getTime();
	}
	
	public Config getConfig() {
		return Settings.getInstance().config;
	}
	
	public org.prosolo.common.config.Config getCommonConfig() {
		return CommonSettings.getInstance().config;
	}
	
	public String getServerIp() {
		if (serverIP == null) {
			this.serverIP = accessResolver.findServerIPAddress();
		}
		return serverIP;
	}
	
	public String getDomain(){
		return CommonSettings.getInstance().config.appConfig.domain;
	}
	
	public String getYear() {
		return String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
	}

	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

}

