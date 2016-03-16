package org.prosolo.web.emailLinks;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.emailLinks.data.ObjectData;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@ManagedBean(name = "digestLinkBean")
@Component("digestLinkBean")
@Scope("request")
public class DigestLinkBean implements Serializable {

	private static final long serialVersionUID = 3384018318823880899L;
	
	private static Logger logger = Logger.getLogger(DigestLinkBean.class);

	@Inject private UrlIdEncoder idEncoder;
	@Inject private ResourceFactory resourceFactory;
	@Inject private LoggingNavigationBean loggingBean;
	
	private String id;
	private String userId;
	private String contextName;
	
	public void init() {
		try {
			long decodedId = idEncoder.decodeId(id);
			long decodedUserId = idEncoder.decodeId(userId);
			if (decodedId > 0 && decodedUserId > 0) {
				EmailContextToObjectMapper mapper = EmailContextToObjectMapper.valueOf(
						contextName.toUpperCase());
				ObjectData objData = mapper.getObjectData();
				String link = resourceFactory.getLinkForObjectType(objData.getClassName(), decodedId, 
						objData.getLinkField());
				
				String page = FacesContext.getCurrentInstance().getViewRoot().getViewId();
				String context = String.format("name:%1$s|id:%2$s", contextName, decodedId);
				String service = null;
				LearningContextData lContextData = new LearningContextData(page, context, service);
				
				User user = new User();
				user.setId(decodedUserId);
				loggingBean.logEmailNavigation(user, link, lContextData);
				
				ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			    externalContext.redirect(link);
			} else {
				try {
					FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
				} catch (IOException ioe) {
					ioe.printStackTrace();
					logger.error(ioe);
				}
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getContextName() {
		return contextName;
	}

	public void setContextName(String contextName) {
		this.contextName = contextName;
	}
	
}
