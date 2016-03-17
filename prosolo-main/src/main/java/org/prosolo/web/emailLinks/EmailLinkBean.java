package org.prosolo.web.emailLinks;

import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.email.emailLinks.contextParser.EmailLinkContextParser;
import org.prosolo.services.email.emailLinks.data.LinkObjectContextData;
import org.prosolo.services.email.emailLinks.data.LinkObjectData;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


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
					
					User user = new User();
					user.setId(decodedUserId);
					loggingBean.logEmailNavigation(user, link, lContextData);
					
					ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
				    externalContext.redirect(link);
				    return;
				}
			}
			
			try {
				FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound");
			} catch (IOException ioe) {
				ioe.printStackTrace();
				logger.error(ioe);
			}
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
