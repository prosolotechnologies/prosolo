package org.prosolo.core.spring.security.authentication.lti;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.spring.security.authentication.lti.authenticationtoken.LTIAuthenticationToken;
import org.prosolo.services.lti.LtiUserManager;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.prosolo.web.lti.LTIToProSoloRoleMapper;
import org.prosolo.web.lti.message.LTILaunchMessage;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Expects authentication token of type #{@link LTIAuthenticationToken} with populated preauthentication token
 * and authenticates user.
 *
 * If authentication is successful it returns  authentication token of the same type but with populated
 * principal and authorities
 *
 * @author stefanvuckovic
 * @date 2018-10-16
 * @since 1.2.0
 */
@Service ("org.prosolo.core.spring.security.authentication.lti.LTIAuthenticationProvider")
public class LTIAuthenticationProviderImpl implements LTIAuthenticationProvider {

    private static Logger logger = Logger.getLogger(LTIAuthenticationProviderImpl.class);

    @Inject private LtiUserManager ltiUserManager;
    @Inject private UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        LTIAuthenticationToken.LTIPreauthenticationToken token = ((LTIAuthenticationToken) authentication).getPreauthenticationToken();
        if (token == null || token.getLaunchMessage() == null || token.getLtiTool() == null) {
            throw new BadCredentialsException("Authentication info not provided in the right format");
        }
        try {
            // fetching or creating a user
            User user = getUserForLaunch(token.getLtiTool(), token.getLaunchMessage());

            if (user == null) {
                logger.info("LTI launch: user is null");
                throw new UsernameNotFoundException("User with given credentials not found");
            }
            return retrieveAuthentication(user.getEmail(), token);
        } catch (AuthenticationException e) {
            logger.error("Error", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new AuthenticationServiceException("Error occured during LTI authentication", e);
        }
    }

    private Authentication retrieveAuthentication(String email, LTIAuthenticationToken.LTIPreauthenticationToken preauthenticationToken) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new LTIAuthenticationToken(userDetails, preauthenticationToken, userDetails.getAuthorities());
    }

    private User getUserForLaunch(LtiTool tool, LTILaunchMessage msg) throws Exception {
        UserContextData contextData = UserContextData.ofOrganization(tool.getOrganization().getId());

        // get role from the LTI message if present
        String roles = msg.getRoles();	// it more roles are present, fetch only the first one (for now)
        String roleName = roles != null ? roles.split(",")[0] : SystemRoleNames.USER;
        return ltiUserManager.getUserForLaunch(
                tool.getToolSet().getConsumer().getId(),
                msg.getUserID(),
                msg.getUserFirstName(),
                msg.getUserLastName(),
                msg.getUserEmail(),
                tool.getUnit() != null ? tool.getUnit().getId() : 0,
                LTIToProSoloRoleMapper.getRole(roleName),
                tool.getUserGroup() != null ? tool.getUserGroup().getId() : 0,
                contextData);
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(LTIAuthenticationToken.class);
    }
}
