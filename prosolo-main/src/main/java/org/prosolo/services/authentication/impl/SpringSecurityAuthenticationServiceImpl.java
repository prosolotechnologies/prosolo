package org.prosolo.services.authentication.impl;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.services.authentication.AuthenticationService;
import org.prosolo.services.authentication.exceptions.AuthenticationException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Service("org.prosolo.services.authentication.AuthenticationService")
public class SpringSecurityAuthenticationServiceImpl implements AuthenticationService, Serializable {
	
	private static final long serialVersionUID = 221859676151839996L;

	private static Logger logger = Logger.getLogger(SpringSecurityAuthenticationServiceImpl.class);

	@Autowired private AuthenticationManager authenticationManager; // specific for Spring Security
	@Autowired private UserManager userManager;
	@Autowired private RoleManager roleManager;
	@Inject
	private PasswordEncoder passwordEncoder;
	@Inject
	private AuthenticationSuccessHandler authSuccessHandler;
	@Inject
	private UserDetailsService userDetailsService;
	@Inject private EventFactory eventFactory;
	
	//@Inject private TokenBasedRememberMeServices rememberMeService;

	/*@Override
	public boolean login(String email, String password) throws AuthenticationException {
		email = email.toLowerCase();
		logger.debug("email: " + email);

		try {
			Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

			logger.debug("User with email "+email+" is authenticated: " + authenticate.isAuthenticated());
			if (authenticate.isAuthenticated()) {
				SecurityContextHolder.getContext().setAuthentication(authenticate);
				//HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
				//HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
				//rememberMeService.loginSuccess(request, response, authenticate);

				logger.debug("Returning true");
				return true;
			}
		} catch (org.springframework.security.core.AuthenticationException e) {
			logger.debug("Authentication exception:"+email,e);
			throw new AuthenticationException(e.getMessage());
		}catch (Exception ex){
			logger.error("Excetion during authentication",ex);
			ex.printStackTrace();
		}
		logger.debug("Returning false");
		return false;
	}*/

	@Override
	public void login(HttpServletRequest req, HttpServletResponse resp, String email) 
			throws AuthenticationException {
		email = email.toLowerCase();
		logger.debug("email: " + email);
		
		try {
			UserDetails user = userDetailsService.loadUserByUsername(email);
			Authentication authentication = new UsernamePasswordAuthenticationToken(user, null,
				   user.getAuthorities());
			//SecurityContextHolder.getContext().setAuthentication(authentication);
			if (authentication.isAuthenticated()) {
				logger.info("AUTHENTICATED");
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
			authSuccessHandler.onAuthenticationSuccess(req, resp, authentication);
		} catch (Exception e) {
			logger.error("Error while trying to login as user with email " + email + ";" + e);
			throw new AuthenticationException("Error while trying to authentication user");
		}
	}
	
	/*@Override
	public boolean loginOpenId(String email) throws AuthenticationException {
		email = email.toLowerCase();
		System.out.println("login open id for:"+email);
		try {
			Authentication authenticate =null;
			boolean existingUser=userManager.checkIfUserExists(email);
			//Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
			Collection grantedAuthorities=new LinkedList<GrantedAuthority>();
			grantedAuthorities.add(new GrantedAuthorityImpl("ROLE_USER"));
			logger.debug("Granted authority ROLE USER");
			if(!existingUser){
				System.out.println("NOT EXISTING USER> CREATE NEW ONE");
				
			// authenticate = new UsernamePasswordAuthenticationToken(email, null,AuthorityUtils.createAuthorityList("ROLE_USER"));
			}else{
				System.out.println("Existing user");
				List<Role> roles=roleManager.getUserRoles(email);
				//String[] roleNames=new String[3];
				if(roles!=null){
				for(int i=0;i<roles.size();i++){
					//roleNames[i]=new String("ROLE_"+roles.get(i).getTitle().toUpperCase());
					String role="ROLE_"+roles.get(i).getTitle().toUpperCase();
					if(!role.toUpperCase().equals("ROLE_USER")){
						logger.debug("Granted authority:"+role);
						grantedAuthorities.add(new GrantedAuthorityImpl(role));
					}
				}
				}
//				if(roles==null || roles.size()==0){
//					grantedAuthorities.add(new GrantedAuthorityImpl("ROLE_USER"));
//					//authenticate = new UsernamePasswordAuthenticationToken(email, null,AuthorityUtils.createAuthorityList("ROLE_USER"));
//				}
			}
			
			authenticate = new UsernamePasswordAuthenticationToken(email, null,grantedAuthorities);
			logger.debug("Authentication token created for:"+email);
			//Authentication authenticate = new UsernamePasswordAuthenticationToken(email, null);
			SecurityContextHolder.getContext().setAuthentication(authenticate);
			 if (authenticate.isAuthenticated()) {
				 logger.info("Authentication was successful");
 				// SecurityContextHolder.getContext().setAuthentication(authenticate);
				 return true;
			 }else{
				 logger.info("Authentication was not successful");
				 return false;
			 }
				
			//	SecurityContextHolder.getContext().setAuthentication(authenticate);		
				
			//}
		} catch (org.springframework.security.core.AuthenticationException e) {
			logger.error(e.getMessage());
			throw new AuthenticationException(e.getMessage());
		}
	}*/
	
	@Override
	public boolean loginUser(String email) throws AuthenticationException {
		email = email.toLowerCase();
		System.out.println("login user with email: "+ email);
		try {
			Authentication authenticate =null;
			boolean existingUser=userManager.checkIfUserExists(email);
			//Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
			Collection grantedAuthorities=new LinkedList<GrantedAuthority>();
			//grantedAuthorities.add(new GrantedAuthorityImpl("ROLE_USER"));
			//logger.debug("Granted authority ROLE USER");
			if (!existingUser) {
				logger.debug("NOT EXISTING USER");
				return false;
			// authenticate = new UsernamePasswordAuthenticationToken(email, null,AuthorityUtils.createAuthorityList("ROLE_USER"));
			} else {
				System.out.println("Existing user");
				List<Role> roles=roleManager.getUserRoles(email);
				//String[] roleNames=new String[3];
				for (Role role : roles) {
					List<String> capabilities = roleManager.getNamesOfRoleCapabilities(role.getId());
					//userAuthorities.add(new SimpleGrantedAuthority(addRolePrefix(role.getTitle())));
					if (capabilities != null) {
						for (String cap:capabilities) {
							grantedAuthorities.add(new SimpleGrantedAuthority(cap.toUpperCase()));
						}
					}
				}

			}
			
			authenticate = new UsernamePasswordAuthenticationToken(email, null,grantedAuthorities);
			logger.debug("Authentication token created for:"+email);
			//Authentication authenticate = new UsernamePasswordAuthenticationToken(email, null);
			//SecurityContextHolder.getContext().setAuthentication(authenticate);
			 if (authenticate.isAuthenticated()) {
				 logger.info("Authentication was successful");
 				 SecurityContextHolder.getContext().setAuthentication(authenticate);
				 return true;
			 } else {
				 logger.info("Authentication was not successful");
				 return false;
			 }
				
			//	SecurityContextHolder.getContext().setAuthentication(authenticate);		
				
			//}
		} catch (org.springframework.security.core.AuthenticationException e) {
			logger.error(e.getMessage());
			throw new AuthenticationException(e.getMessage());
		}
	}

	@Override
	public void logout() {
		// tell to Spring Security that user is not authorized any more
		SecurityContextHolder.getContext().setAuthentication(null);
		SecurityContextHolder.clearContext();
		
		// invalidate JSF session
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();

		final HttpServletRequest request = (HttpServletRequest) ec.getRequest();
		request.getSession(false).invalidate();
	}

	@Override
	public boolean checkPassword(String rawPassword, String encodedPassword) {
		return passwordEncoder.matches(rawPassword, encodedPassword);
	}
}
