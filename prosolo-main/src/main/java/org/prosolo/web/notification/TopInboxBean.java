package org.prosolo.web.notification;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "topInboxBean")
@Component("topInboxBean")
@Scope("session")
public class TopInboxBean implements Serializable {
	
	private static final long serialVersionUID = -6523581537208723654L;



}
