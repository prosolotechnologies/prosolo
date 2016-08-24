/**
 * 
 */
package org.prosolo.web.useractions;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.organization.Visible;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.VisibilityManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.useractions.data.NewPostData;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 *
 */
@ManagedBean(name = "visibilityaction")
@Component("visibilityaction")
@Scope("request")
public class VisibilityActionBean {
	
	private static Logger logger = Logger.getLogger(VisibilityActionBean.class);
	
	@Autowired private VisibilityManager visibilityManager;
	@Autowired private LoggedUserBean loggedUser;

//	public Visible changeNodeVisibility(Visible visibleResource) throws VisibilityChangeError {
//		String visibility = PageUtil.getPostParameter("visType");
//		
//		try {
//			return visibilityManager.setResourceVisibility(loggedUser.getUser(), visibleResource, visibility);
//		} catch (VisibilityCoercionError e) {
//			logger.error(e.getMessage());
//		} catch (EventException e) {
//			logger.error(e.getMessage());
//		}
//		throw new VisibilityChangeError("There was an error changing the visibility of a resource "+visibleResource+" to "+visibility);
//	}
	
	public Visible changeVisibility(long resId, VisibilityType visType, String context) {
		try {
			return visibilityManager.setResourceVisibility(loggedUser.getUserId(), resId, visType, context);
		} catch (EventException e) {
			logger.error(e.getMessage());
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
	public void changeVisibility(NewPostData postData) {
    	String visibility = PageUtil.getPostParameter("visType");
    	
    	if (visibility != null) {
    		VisibilityType vis = VisibilityType.valueOf(visibility);
    		postData.setVisibility(vis);
    	}
    }

}
