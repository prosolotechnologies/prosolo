/**
 * 
 */
package org.prosolo.web.errorhandling;

import org.apache.log4j.Logger;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

@ManagedBean(name = "errorHandlerBean")
@Component("errorHandlerBean")
@Scope("request")
public class ErrorHandlerBean implements Serializable {

	private static final long serialVersionUID = 6322575893739870464L;

	private static Logger logger = Logger.getLogger(ErrorHandlerBean.class);

	public void init() {
		ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
		//retrieve original uri that led to 404 error
		String uri = (String) ((HttpServletRequest) extContext.getRequest()).getAttribute("javax.servlet.error.request_uri");
		uri = uri.substring(extContext.getRequestContextPath().length());
		logger.info("404 for URI: " + uri);
		PageUtil.notFound(uri);
	}

}
