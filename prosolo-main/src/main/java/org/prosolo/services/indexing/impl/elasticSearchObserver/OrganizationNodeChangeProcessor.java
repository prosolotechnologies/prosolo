package org.prosolo.services.indexing.impl.elasticSearchObserver;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.Event;
import org.prosolo.services.indexing.ESAdministration;
import org.prosolo.services.indexing.UserEntityESService;
import org.prosolo.services.nodes.UserManager;

import java.util.List;

/**
 * @author Stefan Vuckovic
 * @date 2017-06-21
 * @since 0.7
 */
public class OrganizationNodeChangeProcessor implements NodeChangeProcessor {

	private static Logger logger = Logger.getLogger(OrganizationNodeChangeProcessor.class);

	private ESAdministration esAdministration;
	private UserEntityESService userEntityESService;
	private UserManager userManager;
	private Event event;
	private Session session;

	public OrganizationNodeChangeProcessor(ESAdministration esAdministration,
										   UserEntityESService userEntityESService,
										   UserManager userManager, Event event, Session session) {
		this.esAdministration = esAdministration;
		this.userEntityESService = userEntityESService;
		this.userManager = userManager;
		this.event = event;
		this.session = session;
	}
	
	@Override
	public void process() {
		try {
			long orgId = event.getObject().getId();
			//create indexes for organization
			esAdministration.createOrganizationIndexes(orgId);
			//index organization users - those users will be only admins added when organization is created
			List<User> orgUsers = userManager.getOrganizationUsers(orgId, false, session);
			for (User user : orgUsers) {
				userEntityESService.saveUserNode(user, orgId, session);
			}
		} catch (Exception e) {
			//TODO handle es exceptions somehow
			logger.error("Error", e);
		}
	}

}
