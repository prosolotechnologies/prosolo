/**
 * 
 */
package org.prosolo.core.spring.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.prosolo.services.authentication.PasswordEncrypter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.header.writers.frameoptions.WhiteListedAllowFromStrategy;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter.XFrameOptionsMode;

import com.amazonaws.services.s3.Headers;

/**
 * @author "Nikola Milikic"
 *
 */
/*@Configuration
// @ComponentScan
@EnableWebSecurity
//@ImportResource({"classpath:core/security/context.xml"})
public class SpringSecurityConfig2 extends WebSecurityConfigurerAdapter {

	@Inject
	private UserDetailsService userDetailsService;
	@Inject
	private PasswordEncrypter passwordEncrypter;
	
	@Override
    protected void configure(HttpSecurity http) throws Exception {
		
		http.authorizeRequests()
		   .antMatchers("/test.html").permitAll()
		   .antMatchers("/favicon.ico").permitAll()
		   .antMatchers("/resources/css/**").permitAll()
		   .antMatchers("/resources/images/**").permitAll()
		   .antMatchers("/resources/javascript/**").permitAll()
		   .antMatchers("/javax.faces.resource/**").permitAll()
		   .antMatchers("/javax.faces.resource/schedule/**").permitAll()
		   .antMatchers("/javax.faces.**").permitAll()
		   .antMatchers("/api/lti/**").permitAll()
		   .antMatchers("/prosolo/api/lti/**").permitAll()
		   .antMatchers("/about").permitAll()
		   .antMatchers("/elb_ping").permitAll()
		   .antMatchers("/terms").permitAll()
		   .antMatchers("/profile/**").permitAll()
		   .antMatchers("/maintenance").permitAll()
		   .antMatchers("/digest").permitAll()
		   .antMatchers("/ltitoolproxyregistration.xhtml").permitAll()
		   .antMatchers("/ltitool.xhtml").permitAll()
		   .antMatchers("/login").permitAll()
		   .antMatchers("/loginAdmin").permitAll()
		   .antMatchers("/ltiproviderlaunch.xhtml").permitAll()
		   .antMatchers("/openid.xhtml").permitAll()
		   .antMatchers("/register").permitAll()
		   .antMatchers("/verify").permitAll()
	       .antMatchers("/passwordReset").permitAll()
		   .antMatchers("/recovery").permitAll()
		   .antMatchers("/javax.faces.resource/**").permitAll()
		   .antMatchers("/manage/competences").hasAuthority("COMPETENCES.VIEW")
		   .antMatchers("/manage/reports").hasAuthority("REPORTS.VIEW")
		   .antMatchers("/admin/users").hasAuthority("USERS.VIEW")
		   .antMatchers("/admin/roles").hasAuthority("ROLES.VIEW")
		   .antMatchers("/admin/dashboard").hasAuthority("ADMINDASHBOARD.VIEW")
		   .antMatchers("/manage/**").hasAnyAuthority("BASIC.MANAGER.ACCESS")
		   .antMatchers("/admin/**").hasAnyAuthority("BASIC.ADMIN.ACCESS")
		   .antMatchers("/**").hasAnyAuthority("BASIC.USER.ACCESS")
		   .and()
           .csrf().disable()
           .rememberMe().rememberMeServices(rememberMeService())
           .and().exceptionHandling().authenticationEntryPoint(authenticationEntryPoint())
           .and().exceptionHandling().accessDeniedHandler(accessDeniedHandler())
           .and().headers()
   		   .frameOptions().disable();
		
    }
	@Inject
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		//super.configure(auth);
		auth.authenticationProvider(daoAuthenticationProvider());
	}
	

	@Bean
	public DaoAuthenticationProvider daoAuthenticationProvider() {
		DaoAuthenticationProvider dao = new DaoAuthenticationProvider();
		dao.setUserDetailsService(userDetailsService);
		dao.setPasswordEncoder(passwordEncrypter);
		return dao;
	}

	@Bean
	public ProviderManager authenticationManager() {
		List<AuthenticationProvider> providers = new ArrayList<AuthenticationProvider>();
		providers.add(daoAuthenticationProvider());
		
		return new ProviderManager(providers);
		
	}
	*/
	
	/*@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncrypter);
	}
	
	@Bean 
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		//return super.authenticationManagerBean();
		return authenticationManager();
	}*/
	
	
	/*@Bean
	public AuthenticationEntryPoint authenticationEntryPoint(){
		LoginUrlAuthenticationEntryPoint auth = new LoginUrlAuthenticationEntryPoint("/login");
		return auth;
	}
	
	@Bean
	public AccessDeniedHandler accessDeniedHandler(){
		AccessDeniedHandlerImpl adh = new AccessDeniedHandlerImpl();
		adh.setErrorPage("/accessDenied");
		return adh;
	}
	
	@Bean
	public TokenBasedRememberMeServices rememberMeService(){
		TokenBasedRememberMeServices service = new TokenBasedRememberMeServices("verylongkey", userDetailsService);
		
		service.setCookieName("ProSolo");
		service.setParameter("_spring_security_remember_me");
		//service.setAlwaysRemember(true);
		
		return service;
	}
	
	
}*/
