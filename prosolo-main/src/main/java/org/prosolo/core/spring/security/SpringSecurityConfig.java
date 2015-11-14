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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.amazonaws.services.s3.Headers;

/**
 * @author "Nikola Milikic"
 *
 */
@Configuration
// @ComponentScan
@EnableWebSecurity
//@ImportResource({"classpath:core/security/context.xml"})
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

	@Inject
	private UserDetailsService userDetailsService;
	@Inject
	private PasswordEncrypter passwordEncrypter;
    @Inject
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;
	
	@Override
    protected void configure(HttpSecurity http) throws Exception {
		
		String rememberMeKey = "prosoloremembermekey";
		http.authorizeRequests()
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
		   .antMatchers("/login").permitAll()
		   .antMatchers("/ltitoolproxyregistration.xhtml").permitAll()
		   .antMatchers("/ltitool.xhtml").permitAll()
		   .antMatchers("/loginAdmin").permitAll()
		   .antMatchers("/ltiproviderlaunch.xhtml").permitAll()
		   .antMatchers("/openid.xhtml").permitAll()
		   .antMatchers("/register").permitAll()
		   .antMatchers("/verify").permitAll()
	       .antMatchers("/passwordReset").permitAll()
		   .antMatchers("/recovery").permitAll()
		   .antMatchers("/javax.faces.resource/**").permitAll()
		   .antMatchers("/notfound").permitAll()
		   
		   .antMatchers("/manage/css/**").hasAnyAuthority("BASIC.MANAGER.ACCESS")
		   .antMatchers("/manage/js/**").hasAuthority("BASIC.MANAGER.ACCESS")
		   .antMatchers("/manage/images/**").hasAuthority("BASIC.MANAGER.ACCESS")
		   .antMatchers("/manage/competences/**").hasAuthority("COMPETENCES.VIEW")
		   .antMatchers("/manage/credentials/**").hasAnyAuthority("COURSE.VIEW", "COURSE.VIEW.PERSONALIZED")
		   .antMatchers("/manage/credentials/create/").hasAnyAuthority("COURSE.CREATE")
		   //capability for external tool?
		   .antMatchers("/manage/credentials/*/tools").hasAuthority("BASIC.MANAGER.ACCESS")
		   .antMatchers("/manage/competences/*/tools").hasAuthority("BASIC.MANAGER.ACCESS")
		   .antMatchers("/manage/tools/*/*/*/create").hasAuthority("BASIC.MANAGER.ACCESS")
		   .antMatchers("/manage/tools/*").hasAuthority("BASIC.MANAGER.ACCESS")
		   .antMatchers("/manage/credentials/{id}/students").hasAnyAuthority("COURSE.VIEW", "COURSE.VIEW.PERSONALIZED")
		   .antMatchers("/manage/courseFeeds.xhtml*").hasAuthority("BASIC.MANAGER.ACCESS")
		   .antMatchers("/manage/social-interaction*").hasAuthority("BASIC.MANAGER.ACCESS")
		   .antMatchers("/manage/students/*").hasAuthority("BASIC.MANAGER.ACCESS")
		   .antMatchers("/manage/reports").hasAuthority("REPORTS.VIEW")
		   .antMatchers("/admin/users").hasAuthority("USERS.VIEW")
		   .antMatchers("/admin/roles").hasAuthority("ROLES.VIEW")
		   .antMatchers("/admin/dashboard").hasAuthority("ADMINDASHBOARD.VIEW")
		   .antMatchers("/admin/settings").hasAuthority("BASIC.ADMIN.ACCESS")
		  
		   .antMatchers("/manage/**").denyAll()
		   .antMatchers("/admin/**").denyAll()
		   .antMatchers("/**").hasAuthority("BASIC.USER.ACCESS")
		   .and()
		   .formLogin().loginPage("/login").loginProcessingUrl("/loginspring")
		   .usernameParameter("username")
		   .passwordParameter("password")
		   .permitAll()
		   .successHandler(authenticationSuccessHandler)
		   .failureUrl("/login")
           .and().csrf().disable()
           .rememberMe()
           .rememberMeServices(rememberMeService(rememberMeKey)).key(rememberMeKey)
           		.authenticationSuccessHandler(authenticationSuccessHandler)
           //.key("key").userDetailsService(userDetailsService).authenticationSuccessHandler(authenticationSuccessHandler)
           .and()
           .logout().invalidateHttpSession(true).logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
           		.deleteCookies("JSESSIONID")
           .and()
           .exceptionHandling().accessDeniedHandler(accessDeniedHandler())
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
	}*/
	
	@Bean
	public AccessDeniedHandler accessDeniedHandler(){
		AccessDeniedHandlerImpl adh = new AccessDeniedHandlerImpl();
		adh.setErrorPage("/accessDenied");
		return adh;
	}
	
	@Bean
	public TokenBasedRememberMeServices rememberMeService(String key){
		TokenBasedRememberMeServices service = new TokenBasedRememberMeServices(key, userDetailsService);
		
		service.setCookieName("ProSolo");
		service.setParameter("remember-me");
		//service.setAlwaysRemember(true);
		
		return service;
	}
	
	/*@Bean
	public CustomAuthenticationSuccessHandler successHandler(){
		return new CustomAuthenticationSuccessHandler();
	}*/
	
}
