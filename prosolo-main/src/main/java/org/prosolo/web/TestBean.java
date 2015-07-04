package org.prosolo.web;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.services.authentication.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "testbean")
@Component("testbean")
@Scope("view")
public class TestBean implements Serializable {
	
	private static Logger logger = Logger.getLogger(TestBean.class);

	private static final long serialVersionUID = -2760060999550263904L;

	@Autowired private AuthenticationService authenticationService;

	public void throwRuntimeException() {
		throw new RuntimeException("peek-a-boo");
	}

	public void throwSQLException() throws SQLException {
		throw new SQLException("DB fail");
	}
	
	public void invalidate() {
		authenticationService.logout();
		//((HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false)).invalidate();
	}
	
	public String redirect() {
		return "index";
	}
	
	public void test() {
		logger.debug("test55767");
	}
	
	private String message;
	private String menuSelected = "Tab1";
	
	public void init() {
		if (menuSelected.equals("Tab1")) {
			message = "Tab1";
		}
		if (menuSelected.equals("Tab2")) {
			message = "Tab2";
		}
	}

	public String getMessage() {
		return message;
	}

	public String getMenuSelected() {
		return menuSelected;
	}

	public void setMenuSelected(String menuSelected) {
		this.menuSelected = menuSelected;
	}
	
	private String msg;
	
	public void method1() {

	}

	public String getMsg() {
		return msg;
	}
	
	private Date testDate;

	public Date getTestDate() {
		return testDate;
	}

	public void setTestDate(Date testDate) {
		this.testDate = testDate;
	}
	
}
