package org.prosolo.web.emailLinks;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.email.emailLinks.contextParser.EmailLinkContextParser;
import org.prosolo.services.email.emailLinks.data.LinkObjectContextData;
import org.prosolo.services.email.emailLinks.data.LinkObjectData;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;


@ManagedBean(name = "emailLinkBean")
@Component("emailLinkBean")
@Scope("request")
public class EmailLinkBean implements Serializable {

	private static final long serialVersionUID = 3384018318823880899L;
	
	private static Logger logger = Logger.getLogger(EmailLinkBean.class);

	@Inject private UrlIdEncoder idEncoder;
	@Inject private ResourceFactory resourceFactory;
	@Inject private LoggingNavigationBean loggingBean;
	@Inject private EmailLinkContextParser emailLinkContextParser;
    @Inject private UserManager userManager;

	private String userId;
	private String context;
	
	public void init() {
		try {
			long decodedUserId = idEncoder.decodeId(userId);
			if (decodedUserId > 0) {
				Optional<LinkObjectContextData> optLinkObjCtxData = emailLinkContextParser
						.getLearningContext(context);
				if(optLinkObjCtxData.isPresent()) {
					LinkObjectContextData data = optLinkObjCtxData.get();
					String context = data.getLearningContext();
					LinkObjectData linkObjData = data.getLinkObjectData();
					String link = resourceFactory.getLinkForObjectType(linkObjData
							.getInfo().getClassName(), linkObjData.getId(), 
							linkObjData.getInfo().getLinkField());
					
					String page = FacesContext.getCurrentInstance().getViewRoot().getViewId();
					LearningContextData lContextData = new LearningContextData(page, context, null);

					long organizationId = 0;
					try {
						organizationId = userManager.getUserOrganizationId(decodedUserId);
					} catch (DbConnectionException e) {
						logger.error("Error", e);
					}
					String sessionId = null;
					HttpSession session = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getSession(false);
					if (session != null) {
						sessionId = session.getId();
					}

					loggingBean.logEmailNavigation(UserContextData.of(decodedUserId, organizationId, sessionId, lContextData), link);
					
					ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
				    externalContext.redirect(link);
				    return;
				}
			}

			PageUtil.notFound();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String contextName) {
		this.context = contextName;
	}
	
}
