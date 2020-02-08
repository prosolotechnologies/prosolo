package org.prosolo.services.authentication.impl;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.interfacesettings.UserSettings;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.core.spring.security.authentication.sessiondata.ProsoloUserDetails;
import org.prosolo.services.authentication.UserAuthenticationService;
import org.prosolo.services.interfaceSettings.InterfaceSettingsManager;
import org.prosolo.services.logging.AccessResolver;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.user.data.UserData;
import org.prosolo.web.administration.data.RoleData;
import org.prosolo.web.util.AvatarUtils;
import org.prosolo.web.util.ResourceBundleUtil;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Service for authenticating user; should be used only in request thread.
 *
 * @author stefanvuckovic
 * @date 2017-08-30
 * @since 1.0.0
 */
@Service("org.prosolo.services.authentication.UserAuthenticationService")
public class UserAuthenticationServiceImpl implements UserAuthenticationService {

    private static final long serialVersionUID = 7461929298489156499L;

    private static Logger logger = Logger.getLogger(UserAuthenticationServiceImpl.class);

    @Inject private RoleManager roleManager;
    @Inject private InterfaceSettingsManager interfaceSettingsManager;
    @Inject private AccessResolver accessResolver;
    @Inject private HttpServletRequest httpServletRequest;

    @Override
    @Transactional
    public ProsoloUserDetails authenticateUser(UserData user) throws UsernameNotFoundException, LockedException, DbConnectionException {
        if (user == null) {
            throw new UsernameNotFoundException("There is no user with this email");
        }
        if (user.isDeleted()) {
            throw new LockedException(ResourceBundleUtil.getSpringMessage("AbstractUserDetailsAuthenticationProvider.locked"));
        }
        try {
            //load other user data needed for principal object
            UserSettings userSettings = interfaceSettingsManager.getOrCreateUserSettings(user.getId());
            String ipAddress = accessResolver.findRemoteIPAddress(httpServletRequest);

            Collection<SimpleGrantedAuthority> userAuthorities = new ArrayList<>();
            for (long role : user.getRoleIds()) {
                List<String> capabilities = roleManager.getNamesOfRoleCapabilities(role);
                if (capabilities != null) {
                    for (String cap : capabilities) {
                        userAuthorities.add(new SimpleGrantedAuthority(cap.toUpperCase()));
                    }
                }
            }

            boolean enabled = true;
            boolean accountNonExpired = true;
            boolean credentialsNonExpired = true;

            logger.debug("Returning user details " + user.getEmail() + ", user.getPassword(): " +
                    user.getPassword() + ", enabled: " + enabled + ", accountNonExpired: " +
                    accountNonExpired + ", credentialsNonExpired: " + credentialsNonExpired + ", accountNonLocked: " +
                    !user.isDeleted() + ", userAuthorities: " + userAuthorities);
            String password = user.getPassword();
            if (password == null) {
                password = "";
            }

            ProsoloUserDetails.Builder userDetailsBuilder = ProsoloUserDetails.builder();
            userDetailsBuilder
                    .email(user.getEmail())
                    .password(password)
                    .authorities(userAuthorities)
                    .enabled(enabled)
                    .accountNonExpired(accountNonExpired)
                    .accountNonLocked(!user.isDeleted())
                    .credentialsNonExpired(credentialsNonExpired)
                    .userId(user.getId())
                    .organizationId(user.getOrganizationId())
                    .firstName(user.getName())
                    .lastName(user.getLastName())
                    .avatar(user.getAvatarUrl())
                    .position(user.getPosition())
                    .ipAddress(ipAddress)
                    .sessionId(httpServletRequest.getSession().getId())
                    .locale(userSettings.getLocaleSettings().createLocale());

            return userDetailsBuilder.create();
        } catch (Exception e) {
            logger.error("error", e);
            throw new DbConnectionException("Error loading the user session data during authentication for user: " + user.getId());
        }
    }
}
