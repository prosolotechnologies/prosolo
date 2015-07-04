/**
 * 
 */
package org.prosolo.core.spring.security;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * @author "Nikola Milikic"
 *
 */
@Configuration
@ComponentScan
@EnableWebSecurity
@ImportResource({"classpath:core/security/context.xml"})
public class SpringSecurityConfig 
//extends WebSecurityConfigurerAdapter 
{
	
//	@Autowired private UserDetailsService userDetailsService;
//	@Autowired private PasswordEncrypter passwordEncrypter;
//	
//	protected void configure(HttpSecurity http) throws Exception {
//		http
//	        .authorizeRequests()
//	            .antMatchers("/resources/**").permitAll()
//	            .anyRequest().authenticated()
//	            .and()
////            .authorizeRequests()
////	            .antMatchers("/login.xhtml").permitAll()
////	            .and()
//            .authorizeRequests()
//	            .antMatchers("/register.xhtml").permitAll()
//	            .and()
//            .authorizeRequests()
//	            .antMatchers("/verify.xhtml").permitAll()
//	            .and()
//            .authorizeRequests()
//	            .antMatchers("/passwordReset.xhtml").permitAll()
//	            .and()
//            .authorizeRequests()
//	            .antMatchers("/recovery.xhtml").permitAll()
//	            .and()
//            .authorizeRequests()
//	            .antMatchers("/terms.xhtml").permitAll()
//	            .and()
//            .authorizeRequests()
//	            .antMatchers("/javax.faces.resource/**").permitAll()
//	            .and()
//            .authorizeRequests()
//	            .antMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
//	            .and()
//            .authorizeRequests()
//	            .antMatchers("/manage/**").hasAuthority("ROLE_MANAGER")
//	            .and()
//            .authorizeRequests()
//	            .antMatchers("/**").hasAuthority("ROLE_USER,ROLE_MANAGER,ROLE_ADMIN")
//	            .and()
//	        .formLogin()
//	            .loginPage("/login.xhtml").failureUrl("/accessDenied.xhtml")
//	        	.permitAll()
//	            .and()
//		    .logout()
//		    	.permitAll()
//		    	.logoutSuccessUrl("/login.xhtml")
//		    	.and()
//	    	.csrf()
//	    		.disable()
//	        // Example Remember Me Configuration
//	        .rememberMe()
//	        	.key("...verylonganduniquekey...")
//	        ;
//		
//    }
//
//	@Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//		super.configure(auth);
//        auth
//	        .authenticationProvider(daoAuthenticationProvider())
//            .userDetailsService(userDetailsService)
//            ;
//    }
//	
//	@Bean
//    public DaoAuthenticationProvider daoAuthenticationProvider() {
//        DaoAuthenticationProvider dao = new DaoAuthenticationProvider();
//        dao.setUserDetailsService(userDetailsService);
//        dao.setPasswordEncoder(passwordEncrypter);
//        return dao;
//    }
//
//	@Bean
//	public ProviderManager authenticationManager() {
//		List<AuthenticationProvider> providers = new ArrayList<AuthenticationProvider>();
//		providers.add(daoAuthenticationProvider());
//
//		ProviderManager serializableProviderManager = new ProviderManager(providers);
//		
//		return serializableProviderManager;
//	}
}
