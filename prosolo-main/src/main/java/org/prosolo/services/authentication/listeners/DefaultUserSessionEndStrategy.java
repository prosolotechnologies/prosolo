package org.prosolo.services.authentication.listeners;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.spring.security.authentication.sessiondata.ProsoloUserDetails;
import org.prosolo.services.authentication.AuthenticatedUserService;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.user.UserManager;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author stefanvuckovic
 * @date 2018-11-06
 * @since 1.2.0
 */
@Service("org.prosolo.services.authentication.listeners.UserSessionEndStrategy")
public class DefaultUserSessionEndStrategy implements UserSessionEndStrategy {

    private static Logger logger = Logger.getLogger(DefaultUserSessionEndStrategy.class);

    @Inject private UserManager userManager;
    @Inject private EventFactory eventFactory;
    @Inject private AuthenticatedUserService authenticatedUserService;

    @Override
    public void onUserSessionEnd(HttpSession session) {
        Optional<ProsoloUserDetails> userInfoOpt = authenticatedUserService.getUserDetailsFromSession(session);
        if (userInfoOpt.isPresent()) {
            ProsoloUserDetails userInfo = userInfoOpt.get();
            try {
                //delete all temp files for this user
                FileUtils.deleteDirectory(new File(Settings.getInstance().config.fileManagement.uploadPath +
                        File.separator + userInfo.getUserId()));
            } catch (IOException e) {
                logger.error("error", e);
            }
            userManager.fullCacheClear();

            Map<String, String> parameters = new HashMap<>();
            parameters.put("ip", userInfo.getIpAddress());
            eventFactory.generateEvent(EventType.SESSIONENDED, UserContextData.of(
                    userInfo.getUserId(), userInfo.getOrganizationId(), session.getId(), userInfo.getIpAddress(), null),
                    null, null, null, parameters);

            logger.debug("UserSession unbound; session id: " + session.getId() + "; user:" + userInfo.getUserId());
        }
    }
}
