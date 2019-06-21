package org.prosolo.services.datainit.impl;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.data.Result;
import org.prosolo.services.datainit.StaticDataInitManager;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.indexing.impl.NodeChangeObserver;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.user.UserManager;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Arrays;

/**
 * @author stefanvuckovic
 * @date 2019-06-12
 * @since 1.3.2
 */
@Service("org.prosolo.services.datainit.StaticDataInitManager")
public class StaticDataInitManagerImpl implements StaticDataInitManager {

    protected static Logger logger = Logger.getLogger(StaticDataInitManagerImpl.class.getName());

    @Inject private UserManager userManager;
    @Inject private RoleManager roleManager;
    @Inject private EventFactory eventFactory;

    @Override
    public void initStaticData() {
        Long superAdminRoleId = roleManager.getRoleIdByName(SystemRoleNames.SUPER_ADMIN);

        try {
            Result<User> res = userManager.createNewUserAndGetEvents(
                    0,
                    Settings.getInstance().config.init.defaultUser.name,
                    Settings.getInstance().config.init.defaultUser.lastname,
                    Settings.getInstance().config.init.defaultUser.email,
                    true,
                    Settings.getInstance().config.init.defaultUser.pass,
                    null,
                    null,
                    null,
                    Arrays.asList(superAdminRoleId),
                    true);
            eventFactory.generateAndPublishEvents(res.getEventQueue(), new Class[] {NodeChangeObserver.class});
        } catch (Exception e) {
            logger.error("error", e);
            throw new DbConnectionException("Error initializing static data");
        }
    }
}
