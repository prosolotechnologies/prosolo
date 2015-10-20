/**
 * 
 */
package org.prosolo.core.spring.security;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.prosolo.services.authentication.PasswordEncrypter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author "Nikola Milikic"
 *
 */
@Configuration
// @ComponentScan
@EnableWebSecurity
@ImportResource({"classpath:core/security/context.xml"})
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

/*	@Inject
	private UserDetailsService userDetailsService;
	@Inject
	private PasswordEncrypter passwordEncrypter;
	
	@Override
    protected void configure(HttpSecurity http) throws Exception {
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
		   .antMatchers("/admin/**").hasRole("ADMIN")
		   .antMatchers("/manage/**").hasRole("MANAGER")
		   .antMatchers("/**").hasRole("USER,MANAGER,ADMIN")
		   .antMatchers("/**").hasRole("USER,MANAGER,ADMIN").and()
        .csrf().disable()
        .formLogin()
            .loginPage("/login").usernameParameter("j_username").passwordParameter("j_password")
            .failureUrl("/accessDenied.xhtml").permitAll()
            .and()
        .logout()
            .logoutUrl("/j_spring_security_logout").invalidateHttpSession(true).deleteCookies("JSESSIONID")
            .logoutSuccessUrl("/login").permitAll()
            .and().rememberMe().key("...verylonganduniquekey...");;
    }
	@Inject
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		//super.configure(auth);
		auth.authenticationProvider(daoAuthenticationProvider())
		        .userDetailsService(userDetailsService)
		        .passwordEncoder(passwordEncrypter);
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

		SerializableProviderManager serializableProviderManager = new SerializableProviderManager();
		serializableProviderManager.setProviders(providers);
		return serializableProviderManager;
	}*/
	
/*	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncrypter);
	}
	
	@Bean 
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		//return super.authenticationManagerBean();
		return authenticationManager();
	}*/
	
}
