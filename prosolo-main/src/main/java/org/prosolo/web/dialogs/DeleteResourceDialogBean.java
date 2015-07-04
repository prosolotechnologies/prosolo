package org.prosolo.web.dialogs;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.LoggedUserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "deleteResourceDialog")
@Component("deleteResourceDialog")
@Scope("view")
public class DeleteResourceDialogBean {

	private static Logger logger = Logger.getLogger(DeleteResourceDialogBean.class);

	@Autowired private DefaultManager defaultManager;
	@Autowired private LoggedUserBean loggedUser;

	private Node resource;
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}

	/*
	 * ACTIONS
	 */

	public void deleteResource() {
		if (resource != null) {
			logger.debug("User "+loggedUser.getUser()+" is deleting a resource: "+resource);
			defaultManager.delete(resource);
		}
	}

	/*
	 * GETTERS / SETTERS
	 */
	
	public Node getResource() {
		return resource;
	}

	public void setResource(Node resource) {
		this.resource = resource;
	}

}
