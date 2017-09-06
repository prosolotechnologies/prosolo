package org.prosolo.services.authentication.impl;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.authentication.UserAuthenticationService;
import org.prosolo.services.nodes.RoleManager;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2017-08-30
 * @since 1.0.0
 */
@Service("org.prosolo.services.authentication.UserAuthenticationService")
public class UserAuthenticationServiceImpl implements UserAuthenticationService, MessageSourceAware {

    private static final long serialVersionUID = 7461929298489156499L;

    private static Logger logger = Logger.getLogger(UserAuthenticationServiceImpl.class);

    @Inject private RoleManager roleManager;

    private MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Override
    public UserDetails authenticateUser(User user) throws LockedException {
        Collection<SimpleGrantedAuthority> userAuthorities = new ArrayList<>();
        //this check is added because for some types of login (SAML) spring does not do these checks
        if (user.isDeleted()) {
            throw new LockedException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.locked", "User account is locked"));
        }
        for (Role role : user.getRoles()) {
            List<String> capabilities = roleManager.getNamesOfRoleCapabilities(role.getId());
            if (capabilities != null) {
                for (String cap : capabilities) {
                    userAuthorities.add(new SimpleGrantedAuthority(cap.toUpperCase()));
                }
            }
        }

        boolean enabled = true;
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;

        logger.debug("Returning user details " + user.getEmail() +", user.getPassword(): "+
                user.getPassword()+", enabled: "+ enabled+", accountNonExpired: "+
                accountNonExpired+", credentialsNonExpired: "+credentialsNonExpired+", accountNonLocked: "+
                !user.isDeleted()+", userAuthorities: "+userAuthorities);
        String password=user.getPassword();
        if(password==null){
            password="";
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(), password, enabled,
                accountNonExpired, credentialsNonExpired, !user.isDeleted(), userAuthorities);
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }
}
