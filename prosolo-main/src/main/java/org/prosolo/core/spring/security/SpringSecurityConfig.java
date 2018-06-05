/**
 * 
 */
package org.prosolo.core.spring.security;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.saml2.metadata.provider.ResourceBackedMetadataProvider;
import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.saml.*;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.log.SAMLLogger;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.processor.*;
import org.springframework.security.saml.trust.httpclient.TLSProtocolConfigurer;
import org.springframework.security.saml.trust.httpclient.TLSProtocolSocketFactory;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.*;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.inject.Inject;
import java.util.*;

/**
 * @author "Nikola Milikic"
 *
 */
@Configuration
@EnableWebSecurity
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

	@Inject
	private UserDetailsService userDetailsService;
    @Inject
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;
    @Inject
    private SAMLUserDetailsService samlUserDetailsService;
	
	@Override
    protected void configure(HttpSecurity http) throws Exception {
		
		String rememberMeKey = "prosoloremembermekey";
		http
        //.addFilterBefore(metadataGeneratorFilter(), ChannelProcessingFilter.class)
        .addFilterAfter(samlFilter(), BasicAuthenticationFilter.class)
		.authorizeRequests()
				.antMatchers("/favicon.ico").permitAll()
				.antMatchers("/resources/css2/**").permitAll()
				.antMatchers("/resources/images2/**").permitAll()
				.antMatchers("/resources/fonts/**").permitAll()
				.antMatchers("/resources/javascript2/**").permitAll()
				.antMatchers("/resources/javascript/**").permitAll()
				.antMatchers("/javax.faces.resource/**").permitAll()
				.antMatchers("/javax.faces.resource/schedule/**").permitAll()
				.antMatchers("/javax.faces.**").permitAll()
				.antMatchers("/api/lti/**").permitAll()
				//.antMatchers("/prosolo/api/lti/**").permitAll()
				.antMatchers("/about").permitAll()
				.antMatchers("/elb_ping").permitAll()
				.antMatchers("/terms").permitAll()
				.antMatchers("/profile").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/profile/**").permitAll()
				.antMatchers("/maintenance").permitAll()
				.antMatchers("/digest").permitAll()
				.antMatchers("/login").permitAll()
				.antMatchers("/login?**").permitAll()
				.antMatchers("/ltitoolproxyregistration.xhtml").permitAll()
				.antMatchers("/ltitool.xhtml").permitAll()
				.antMatchers("/loginAdmin").permitAll()
				.antMatchers("/ltiproviderlaunch.xhtml").permitAll()
				.antMatchers("/openid.xhtml").permitAll()
				.antMatchers("/register").permitAll()
				.antMatchers("/verify").permitAll()
				.antMatchers("/reset/successful/**").permitAll()
				.antMatchers("/reset").permitAll()
				.antMatchers("/recovery/**").permitAll()
				.antMatchers("/javax.faces.resource/**").permitAll()
				.antMatchers("/saml/**").permitAll()
				//.antMatchers("/notfound").permitAll()

				.antMatchers("/").hasAnyAuthority("BASIC.USER.ACCESS", "BASIC.INSTRUCTOR.ACCESS", "BASIC.MANAGER.ACCESS", "BASIC.ADMIN.ACCESS")
				.antMatchers("/files/**").hasAnyAuthority("BASIC.USER.ACCESS", "BASIC.INSTRUCTOR.ACCESS", "BASIC.MANAGER.ACCESS", "BASIC.ADMIN.ACCESS")
				// we need to allow access to index.jsp as Tomcat by default tries to load this file
				.antMatchers("/index.jsp").hasAnyAuthority("BASIC.USER.ACCESS", "BASIC.INSTRUCTOR.ACCESS", "BASIC.MANAGER.ACCESS", "BASIC.ADMIN.ACCESS")
				.antMatchers("/version.txt").hasAnyAuthority("BASIC.ADMIN.ACCESS")
				.antMatchers("/home").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/people").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/people/followers").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/people/following").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/settings/email").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/settings/password").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/settings/twitterOAuth").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/credentials/*/students").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/credentials/*/students/*").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/credentials/*/keywords").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/profile/*/*").permitAll()
				.antMatchers("/competences/*/edit").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/credentials/*/*").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/credentials/*/assessments").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/credentials/*/assessments/self").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/profile/*/credentials/*/assessments/self").permitAll()
				.antMatchers("/credentials/*/assessments/instructor").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/profile/*/credentials/*/assessments/instructor").permitAll()
				.antMatchers("/credentials/*/assessments/peer").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/profile/*/credentials/*/assessments/peer").permitAll()
				.antMatchers("/credentials/*/assessments/peer/*").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/profile/*/credentials/*/assessments/peer/*").permitAll()
				.antMatchers("/credentials/*/announcements").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/credentials/*/*/*").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/credentials/*/*/*/results").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/competences/new").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/competences/**").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/competences/*/assessments/self").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/profile/*/competences/*/assessments/self").permitAll()
				.antMatchers("/competences/*/assessments/instructor").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/profile/*/competences/*/assessments/instructor").permitAll()
				.antMatchers("/competences/*/assessments/instructor/*").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/profile/*/competences/*/assessments/instructor/*").permitAll()
				.antMatchers("/competences/*/assessments/peer").hasAuthority("BASIC.USER.ACCESS")
                .antMatchers("/profile/*/competences/*/assessments/peer").permitAll()
				.antMatchers("/competences/*/assessments/peer/*").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/profile/*/competences/*/assessments/peer/*").permitAll()
				//.antMatchers("/activities/new").hasAuthority("BASIC.USER.ACCESS")
				//.antMatchers("/activities/**").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/library").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/library/credentials").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/library/competencies").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/notifications").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/posts/*").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/groups/*/join").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/credentials/*/*/*/responses/*").hasAnyAuthority("BASIC.USER.ACCESS")
				.antMatchers("/competences/*/*/responses/*").hasAnyAuthority("BASIC.USER.ACCESS")
				.antMatchers("/evidence").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/evidence/*").permitAll()
				.antMatchers("/evidence/new").hasAuthority("BASIC.USER.ACCESS")
				.antMatchers("/evidence/*/edit").hasAuthority("BASIC.USER.ACCESS")


				// MANAGE
				.antMatchers("/manage").hasAnyAuthority("BASIC.INSTRUCTOR.ACCESS", "BASIC.MANAGER.ACCESS")
				.antMatchers("/manage/").hasAnyAuthority("BASIC.INSTRUCTOR.ACCESS", "BASIC.MANAGER.ACCESS")
				.antMatchers("/manage/css/**").hasAnyAuthority("BASIC.INSTRUCTOR.ACCESS", "BASIC.MANAGER.ACCESS")
				.antMatchers("/manage/js/**").hasAnyAuthority("BASIC.INSTRUCTOR.ACCESS", "BASIC.MANAGER.ACCESS")
				.antMatchers("/manage/images/**").hasAnyAuthority("BASIC.INSTRUCTOR.ACCESS", "BASIC.MANAGER.ACCESS")
				.antMatchers("/manage/notifications").hasAnyAuthority("BASIC.INSTRUCTOR.ACCESS", "BASIC.MANAGER.ACCESS")
				.antMatchers("/manage/messages/*").hasAnyAuthority("BASIC.INSTRUCTOR.ACCESS", "BASIC.MANAGER.ACCESS")
				.antMatchers("/manage/messages").hasAnyAuthority("BASIC.INSTRUCTOR.ACCESS", "BASIC.MANAGER.ACCESS")
				.antMatchers("/manage/settings/password").hasAnyAuthority("BASIC.INSTRUCTOR.ACCESS", "BASIC.MANAGER.ACCESS")
				.antMatchers("/manage/settings/email").hasAnyAuthority("BASIC.INSTRUCTOR.ACCESS", "BASIC.MANAGER.ACCESS")
				.antMatchers("/manage/settings/twitterOAuth").hasAnyAuthority("BASIC.INSTRUCTOR.ACCESS", "BASIC.MANAGER.ACCESS")
				.antMatchers("/manage/settings").hasAnyAuthority("BASIC.INSTRUCTOR.ACCESS", "BASIC.MANAGER.ACCESS")

				//manage competence
				.antMatchers("/manage/competences/*/edit").hasAuthority("COURSE.CREATE")
				.antMatchers("/manage/competences/new").hasAuthority("COURSE.CREATE")
				.antMatchers("/manage/competences/*/tools").hasAuthority("BASIC.MANAGER.ACCESS")
				.antMatchers("/manage/competences/*/who-can-learn").hasAnyAuthority("BASIC.INSTRUCTOR.ACCESS", "BASIC.MANAGER.ACCESS")
				.antMatchers("/manage/competences/*/editors").hasAnyAuthority("BASIC.INSTRUCTOR.ACCESS", "BASIC.MANAGER.ACCESS")
				.antMatchers("/manage/competences/*").hasAnyAuthority("COURSE.VIEW", "COURSE.VIEW.PERSONALIZED")
				.antMatchers("/manage/competences/*/students").hasAnyAuthority("COURSE.CREATE")
				.antMatchers("/manage/competences/*/privacy").hasAnyAuthority("COURSE.CREATE")
				//.antMatchers("/manage/competences/*/activities").hasAnyAuthority("COURSE.VIEW", "COURSE.VIEW.PERSONALIZED")
				.antMatchers("/manage/competences").hasAuthority("COMPETENCES.VIEW")

				// competences with credential id
				.antMatchers("/manage/credentials/*/*/edit").hasAuthority("COURSE.CREATE")
				.antMatchers("/manage/credentials/*/*/who-can-learn").hasAnyAuthority("BASIC.INSTRUCTOR.ACCESS", "BASIC.MANAGER.ACCESS")
				.antMatchers("/manage/credentials/*/*/editors").hasAnyAuthority("BASIC.INSTRUCTOR.ACCESS", "BASIC.MANAGER.ACCESS")
				.antMatchers("/manage/credentials/*/*/students").hasAnyAuthority("COURSE.CREATE")
				.antMatchers("/manage/credentials/*/*/privacy").hasAnyAuthority("COURSE.CREATE")

				.antMatchers("/manage/credentials/*/feeds").hasAnyAuthority("COURSE.VIEW", "COURSE.VIEW.PERSONALIZED")
				.antMatchers("/manage/credentials/*/students").hasAnyAuthority("COURSE.MEMBERS.VIEW", "COURSE.MEMBERS.VIEW.PERSONALIZED")
				.antMatchers("/manage/credentials/*/students/*").hasAnyAuthority("COURSE.MEMBERS.VIEW", "COURSE.MEMBERS.VIEW.PERSONALIZED")
				.antMatchers("/manage/credentials/*/edit").hasAuthority("COURSE.CREATE")
				.antMatchers("/manage/credentials/*/editors").hasAnyAuthority("COURSE.CREATE")
				.antMatchers("/manage/credentials/*/privacy").hasAnyAuthority("COURSE.CREATE")
				.antMatchers("/manage/credentials/*/who-can-learn").hasAnyAuthority("COURSE.CREATE")
				.antMatchers("/manage/credentials/new").hasAnyAuthority("COURSE.CREATE")
				//capability for external tool?
				//for manage competence
				.antMatchers("/manage/credentials/*/*").hasAuthority("COMPETENCES.VIEW")

				.antMatchers("/manage/students/*").hasAnyAuthority("MANAGE.STUDENTPROFILE.VIEW")

				.antMatchers("/manage/credentials/*/tools").hasAuthority("BASIC.MANAGER.ACCESS")
				//.antMatchers("/manage/credentials/*/instructors/*/reassignStudents").hasAuthority("STUDENT.ASSIGN.INSTRUCTOR")
				//.antMatchers("/manage/credentials/*/instructors/*/edit").hasAuthority("COURSE.ASSIGNINSTRUCTOR")
				//.antMatchers("/manage/credentials/*/instructors/new").hasAuthority("COURSE.ASSIGNINSTRUCTOR")
				.antMatchers("/manage/credentials/*/instructors").hasAuthority("COURSE.INSTRUCTORS.VIEW")
				.antMatchers("/manage/credentials/*/assessments/*").hasAnyAuthority("MANAGE.CREDENTIAL.ASSESSMENTS")
				.antMatchers("/manage/credentials/*/assessments/activities/*").hasAnyAuthority("MANAGE.CREDENTIAL.ASSESSMENTS")
				.antMatchers("/manage/credentials/*/assessments/activities/*/*").hasAnyAuthority("MANAGE.CREDENTIAL.ASSESSMENTS")
				.antMatchers("/manage/credentials/*/assessments/competencies/*").hasAuthority("MANAGE.CREDENTIAL.ASSESSMENTS")
				.antMatchers("/manage/credentials/*/rss").hasAnyAuthority("MANAGE.CREDENTIAL.RSSFEEDS")
				.antMatchers("/manage/credentials/*/assessments").hasAnyAuthority("MANAGE.CREDENTIAL.ASSESSMENTS")
				.antMatchers("/manage/credentials/*/announcements").hasAnyAuthority("COURSE.ANNOUNCEMENTS.VIEW")
				.antMatchers("/manage/credentials/*/deliveries").hasAnyAuthority("BASIC.MANAGER.ACCESS")

				//manage activity
				.antMatchers("/manage/competences/*/*/edit").hasAnyAuthority("COURSE.CREATE")
				.antMatchers("/manage/competences/*/newActivity").hasAnyAuthority("COURSE.CREATE")
				.antMatchers("/manage/credentials/*/*/*").hasAnyAuthority("COMPETENCES.VIEW")
				.antMatchers("/manage/credentials/*/*/*/results").hasAuthority("COMPETENCES.VIEW")
				.antMatchers("/manage/competences/*/*").hasAnyAuthority("COMPETENCES.VIEW")

				.antMatchers("/manage/credentials/**").hasAnyAuthority("COURSE.VIEW", "COURSE.VIEW.PERSONALIZED")
				//manage library
				.antMatchers("/manage/library").hasAnyAuthority("MANAGE.LIBRARY.VIEW", "INSTRUCTOR.LIBRARY.VIEW")
				.antMatchers("/manage/library/credentials").hasAuthority("MANAGE.LIBRARY.VIEW")
				.antMatchers("/manage/library/competencies").hasAuthority("MANAGE.LIBRARY.VIEW")
				.antMatchers("/manage/library/instructor/credentials").hasAuthority("INSTRUCTOR.LIBRARY.VIEW")

				.antMatchers("/manage/rubrics").hasAnyAuthority("MANAGE.RUBRICS.VIEW")
				.antMatchers("/manage/rubrics/*/settings").hasAnyAuthority("MANAGE.RUBRICS.VIEW")
				.antMatchers("/manage/rubrics/*/privacy").hasAnyAuthority("MANAGE.RUBRICS.VIEW")
				.antMatchers("/manage/rubrics/*").hasAnyAuthority("MANAGE.RUBRICS.VIEW")

				.antMatchers("/manage/tools/*/*/*/create").hasAuthority("BASIC.MANAGER.ACCESS")
				.antMatchers("/manage/tools/*").hasAuthority("BASIC.MANAGER.ACCESS")
				.antMatchers("/manage/social-interaction*").hasAuthority("BASIC.MANAGER.ACCESS")

				// ADDED
				.antMatchers("/manage/studentProfile.history.xhtml").hasAuthority("BASIC.MANAGER.ACCESS")
				.antMatchers("/manage/reports").hasAuthority("REPORTS.VIEW")

				.antMatchers("/manage/groups").hasAuthority("BASIC.MANAGER.ACCESS")
				.antMatchers("/manage/evidence/*").hasAnyAuthority("BASIC.MANAGER.ACCESS", "BASIC.INSTRUCTOR.ACCESS")


				//admin
				.antMatchers("/admin").hasAuthority("BASIC.ADMIN.ACCESS")
				.antMatchers("/admin/").hasAuthority("BASIC.ADMIN.ACCESS")
				.antMatchers("/admin/roles").hasAuthority("ROLES.VIEW")
				.antMatchers("/admin/dashboard").hasAuthority("ADMINDASHBOARD.VIEW")
				.antMatchers("/admin/settings/password").hasAuthority("BASIC.ADMIN.ACCESS")
				.antMatchers("/admin/settings/twitterOAuth").hasAuthority("BASIC.ADMIN.ACCESS")
				.antMatchers("/admin/settings").hasAuthority("BASIC.ADMIN.ACCESS")
				.antMatchers("/admin/messages").hasAuthority("BASIC.ADMIN.ACCESS")
				.antMatchers("/admin/settings_old").hasAuthority("BASIC.ADMIN.ACCESS")
				.antMatchers("/admin/other").hasAuthority("ADMIN.ADVANCED")
				.antMatchers("/admin/admins").hasAuthority("ADMIN.ADVANCED")
				.antMatchers("/admin/admins/new").hasAuthority("ADMIN.ADVANCED")
				.antMatchers("/admin/organizations/*/users/*/edit/password").hasAuthority("ORGANIZATION.ADMINISTRATION")
				.antMatchers("/admin/organizations/*/users/*/edit").hasAuthority("ORGANIZATION.ADMINISTRATION")
				.antMatchers("/admin/organizations/*/users/new").hasAuthority("ORGANIZATION.ADMINISTRATION")
				.antMatchers("/admin/organizations/*/users").hasAuthority("ORGANIZATION.ADMINISTRATION")
				.antMatchers("/admin/organizations/new").hasAuthority("ADMINS.VIEW")
				.antMatchers("/admin/organizations/*/settings").hasAuthority("ORGANIZATION.ADMINISTRATION")
				.antMatchers("/admin/admins/*/edit").hasAuthority("ORGANIZATION.ADMINISTRATION")
				.antMatchers("/admin/admins/*/edit/password").hasAuthority("ORGANIZATION.ADMINISTRATION")
				.antMatchers("/admin/organizations").hasAuthority("ORGANIZATION.ADMINISTRATION")
				.antMatchers("/admin/organizations/*/units").hasAnyAuthority("ORGANIZATION.UNITS.ADMINISTRATION")
				.antMatchers("/admin/organizations/*/units/*/teachers").hasAnyAuthority("ORGANIZATION.UNITS.ADMINISTRATION")
				.antMatchers("/admin/organizations/*/units/*/students").hasAnyAuthority("ORGANIZATION.UNITS.ADMINISTRATION")
				.antMatchers("/admin/organizations/*/units/*/instructors").hasAnyAuthority("ORGANIZATION.UNITS.ADMINISTRATION")
				.antMatchers("/admin/organizations/*/units/*/settings").hasAuthority("ORGANIZATION.UNITS.ADMINISTRATION")
				.antMatchers("/admin/organizations/*/units/*/credentials").hasAuthority("ORGANIZATION.UNITS.ADMINISTRATION")
				.antMatchers("/admin/organizations/*/units/*/credentials/*").hasAuthority("ORGANIZATION.UNITS.ADMINISTRATION")
				.antMatchers("/admin/organizations/*/units/*/credentials/*/who-can-learn").hasAuthority("ORGANIZATION.UNITS.ADMINISTRATION")
				.antMatchers("/admin/organizations/*/units/*/credentials/*/*").hasAuthority("ORGANIZATION.UNITS.ADMINISTRATION")
				.antMatchers("/admin/organizations/*/units/*/competences/*").hasAuthority("ORGANIZATION.UNITS.ADMINISTRATION")
				.antMatchers("/admin/organizations/*/units/*/credentials/*/*/*").hasAuthority("ORGANIZATION.UNITS.ADMINISTRATION")
				.antMatchers("/admin/organizations/*/units/*/competences/*/*").hasAuthority("ORGANIZATION.UNITS.ADMINISTRATION")
				.antMatchers("/admin/organizations/*/units/*/groups").hasAnyAuthority("ORGANIZATION.UNITS.ADMINISTRATION")
				.antMatchers("/admin/organizations/*/units/*/groups/*/users").hasAnyAuthority("ORGANIZATION.UNITS.ADMINISTRATION")
				.antMatchers("/admin/migrations").hasAnyAuthority("ADMIN.ADVANCED")
				.antMatchers("/manage/**").denyAll()
				.antMatchers("/admin/**").denyAll()
				.antMatchers("/**").hasAnyAuthority("BASIC.USER.ACCESS")
				.and()
				.formLogin().loginPage("/login").loginProcessingUrl("/loginspring")
				.usernameParameter("username")
				.passwordParameter("password")
				.permitAll()
				.successHandler(authenticationSuccessHandler)
				.failureUrl("/login?err=1")
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
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		/*this means security filters will not be applied
		against this pattern and it is not the same as
		permitAll method on httpsecurity object which
		applies filters and requires any 'role' for user
		to be present (at least anonymous)*/
		web.ignoring()
			.antMatchers("/email.xhtml")
			.antMatchers("/notfound")
			.antMatchers("/manage/notfound")
			.antMatchers("/admin/notfound");
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
		dao.setPasswordEncoder(passwordEncoder());
		dao.setHideUserNotFoundExceptions(false);
		return dao;
	}

	@Bean
	public ProviderManager authenticationManager() {
		List<AuthenticationProvider> providers = new ArrayList<>();
		providers.add(daoAuthenticationProvider());
		providers.add(samlAuthenticationProvider());
		
		return new ProviderManager(providers);
	}
	
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
	}

	@Bean
	public AccessDeniedHandler accessDeniedHandler(){
		CustomAccessDeniedHandler adh = new CustomAccessDeniedHandler();
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

	//SAML config
	     
    @Bean
    public VelocityEngine velocityEngine() {
        return VelocityFactory.getEngine();
    }
 
    // XML parser pool needed for OpenSAML parsing
    @Bean(initMethod = "initialize")
    public StaticBasicParserPool parserPool() {
        return new StaticBasicParserPool();
    }
 
    @Bean(name = "parserPoolHolder")
    public ParserPoolHolder parserPoolHolder() {
        return new ParserPoolHolder();
    }
 
    // Bindings, encoders and decoders used for creating and parsing messages
    @Bean
    public MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager() {
        return new MultiThreadedHttpConnectionManager();
    }
 
    @Bean
    public HttpClient httpClient() {
        return new HttpClient(multiThreadedHttpConnectionManager());
    }
 
    @Bean
    public SAMLAuthenticationProvider samlAuthenticationProvider() {
        SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
        samlAuthenticationProvider.setUserDetails(samlUserDetailsService);
        samlAuthenticationProvider.setForcePrincipalAsString(false);
        return samlAuthenticationProvider;
    }
 
    // Provider of default SAML Context
    @Bean
    public SAMLContextProviderImpl contextProvider() {
        return new SAMLContextProviderImpl();
    }
 
    // Initialization of OpenSAML library
    @Bean
    public static SAMLBootstrap sAMLBootstrap() {
        return new SAMLBootstrap();
    }
 
    // Logger for SAML messages and events
	@Bean
	public SAMLLogger samlLogger() {
		SAMLDefaultLogger samlLogger = new SAMLDefaultLogger();
		samlLogger.setLogMessages(true);
		return samlLogger;
	}

	// SAML 2.0 WebSSO Assertion Consumer
    @Bean
    public WebSSOProfileConsumer webSSOprofileConsumer() {
    	WebSSOProfileConsumerImpl consumer = new WebSSOProfileConsumerImpl();
    	consumer.setMaxAuthenticationAge(604800);
    	return consumer;
    }
 
    // SAML 2.0 Holder-of-Key WebSSO Assertion Consumer
    @Bean
    public WebSSOProfileConsumerHoKImpl hokWebSSOprofileConsumer() {
        return new WebSSOProfileConsumerHoKImpl();
    }
 
    // SAML 2.0 Web SSO profile
    @Bean
    public WebSSOProfile webSSOprofile() {
        return new WebSSOProfileImpl();
    }
 
    // SAML 2.0 Holder-of-Key Web SSO profile
//    @Bean
//    public WebSSOProfileConsumerHoKImpl hokWebSSOProfile() {
//        return new WebSSOProfileConsumerHoKImpl();
//    }
 
    // SAML 2.0 ECP profile
    @Bean
    public WebSSOProfileECPImpl ecpprofile() {
        return new WebSSOProfileECPImpl();
    }
 
    @Bean
    public SingleLogoutProfile logoutprofile() {
        return new SingleLogoutProfileImpl();
    }
 
    // Central storage of cryptographic keys
    @Bean
    public KeyManager keyManager() {
    	DefaultResourceLoader loader = new DefaultResourceLoader();
        Resource storeFile = loader
                .getResource("classpath:security/keystore.jks");
        String storePass = "prosolopass";
        Map<String, String> passwords = new HashMap<String, String>();
        passwords.put("prosoloalias", "prosolopass");
        String defaultKey = "prosoloalias";
        return new JKSKeyManager(storeFile, storePass, passwords, defaultKey);
    }
 
    // Setup TLS Socket Factory
    @Bean
    public TLSProtocolConfigurer tlsProtocolConfigurer() {
    	return new TLSProtocolConfigurer();
    }
    
    @Bean
    public ProtocolSocketFactory socketFactory() {
        return new TLSProtocolSocketFactory(keyManager(), null, "default");
    } 

    @Bean
    public Protocol socketFactoryProtocol() {
        return new Protocol("https", socketFactory(), 443);
    }

    @Bean
    public MethodInvokingFactoryBean socketFactoryInitialization() {
        MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
        methodInvokingFactoryBean.setTargetClass(Protocol.class);
        methodInvokingFactoryBean.setTargetMethod("registerProtocol");
        Object[] args = {"https", socketFactoryProtocol()};
        methodInvokingFactoryBean.setArguments(args);
        return methodInvokingFactoryBean;
    }
    
    @Bean
    public WebSSOProfileOptions defaultWebSSOProfileOptions() {
        WebSSOProfileOptions webSSOProfileOptions = new WebSSOProfileOptions();
        webSSOProfileOptions.setIncludeScoping(false);
        /*
         * to avoid credentialsexpiredexception - it always forces to enter login credentials
         * on IDP login page
         */
        //webSSOProfileOptions.setForceAuthN(true);
        return webSSOProfileOptions;
    }
 
    // Entry point to initialize authentication, default values taken from
    // properties file
    @Bean
    public SAMLEntryPoint samlEntryPoint() {
        SAMLEntryPoint samlEntryPoint = new SAMLEntryPoint();
        samlEntryPoint.setDefaultProfileOptions(defaultWebSSOProfileOptions());
      //  samlEntryPoint.setFilterProcessesUrl("/prosolo/saml/login");
        return samlEntryPoint;
    }
    
    // Setup advanced info about metadata
    @Bean
    public ExtendedMetadata extendedMetadata() {
    	ExtendedMetadata extendedMetadata = new ExtendedMetadata();
    	extendedMetadata.setIdpDiscoveryEnabled(true); 
    	extendedMetadata.setSignMetadata(false);
    	//extendedMetadata.setSslHostnameVerification("allowAll");
    	return extendedMetadata;
    }
    
    // IDP Discovery Service
//	    @Bean
//	    public SAMLDiscovery samlIDPDiscovery() {
//	        //SAMLDiscovery idpDiscovery = new SAMLDiscovery();
//	        //idpDiscovery.setIdpSelectionPath("/saml/idpSelection");
//	        //return idpDiscovery;
//	    }
    
//	@Bean
//	@Qualifier("idp-ssocircle")
//	public ExtendedMetadataDelegate ssoCircleExtendedMetadataProvider()
//			throws MetadataProviderException {
//		String idpSSOCircleMetadataURL = "https://idp.ssocircle.com/idp-meta.xml";
//		Timer backgroundTaskTimer = new Timer(true);
//		HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(
//				backgroundTaskTimer, httpClient(), idpSSOCircleMetadataURL);
//		httpMetadataProvider.setParserPool(parserPool());
//		ExtendedMetadataDelegate extendedMetadataDelegate =
//				new ExtendedMetadataDelegate(httpMetadataProvider, extendedMetadata());
//		extendedMetadataDelegate.setMetadataTrustCheck(true);
//		extendedMetadataDelegate.setMetadataRequireSignature(false);
//		return extendedMetadataDelegate;
//	}

	@Bean
	@Qualifier("idp-testutaedu")
	public ExtendedMetadataDelegate ssoUtaTestExtendedMetadataProvider()
			throws MetadataProviderException {
		String idpSSOCircleMetadataURL = "https://idp-test.uta.edu/idp/shibboleth";
		Timer backgroundTaskTimer = new Timer(true);
		HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(
				backgroundTaskTimer, httpClient(), idpSSOCircleMetadataURL);
		httpMetadataProvider.setParserPool(parserPool());
		ExtendedMetadataDelegate extendedMetadataDelegate =
				new ExtendedMetadataDelegate(httpMetadataProvider, extendedMetadata());
		extendedMetadataDelegate.setMetadataTrustCheck(true);
		extendedMetadataDelegate.setMetadataRequireSignature(false);
		return extendedMetadataDelegate;
	}

	@Bean
	@Qualifier("idp-produtaedu")
	public ExtendedMetadataDelegate ssoUtaProdExtendedMetadataProvider()
			throws MetadataProviderException {
		String idpSSOCircleMetadataURL = "https://idp.uta.edu/idp/shibboleth";
		Timer backgroundTaskTimer = new Timer(true);
		HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(
				backgroundTaskTimer, httpClient(), idpSSOCircleMetadataURL);
		httpMetadataProvider.setParserPool(parserPool());
		ExtendedMetadataDelegate extendedMetadataDelegate =
				new ExtendedMetadataDelegate(httpMetadataProvider, extendedMetadata());
		extendedMetadataDelegate.setMetadataTrustCheck(true);
		extendedMetadataDelegate.setMetadataRequireSignature(false);
		return extendedMetadataDelegate;
	}
	
	@Bean
	@Qualifier("idp-simplesaml")
	public ExtendedMetadataDelegate simpleSamlProvider()
			throws MetadataProviderException {
		String idpSSOCircleMetadataURL = "http://simplesaml.com/simplesaml/saml2/idp/metadata.php";
		Timer backgroundTaskTimer = new Timer(true);
		HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(
				backgroundTaskTimer, httpClient(), idpSSOCircleMetadataURL);
		httpMetadataProvider.setParserPool(parserPool());
		ExtendedMetadataDelegate extendedMetadataDelegate = 
				new ExtendedMetadataDelegate(httpMetadataProvider, extendedMetadata());
		extendedMetadataDelegate.setMetadataTrustCheck(true);
		extendedMetadataDelegate.setMetadataRequireSignature(false);
		return extendedMetadataDelegate;
	}
//	
//	@Bean
//	@Qualifier("idp-simplesaml-shib")
//	public ExtendedMetadataDelegate simpleSamlShibProvider()
//			throws MetadataProviderException {
//		String idpSSOCircleMetadataURL = "http://simplesaml.com/simplesaml/shib13/idp/metadata.php";
//		Timer backgroundTaskTimer = new Timer(true);
//		HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(
//				backgroundTaskTimer, httpClient(), idpSSOCircleMetadataURL);
//		httpMetadataProvider.setParserPool(parserPool());
//		ExtendedMetadataDelegate extendedMetadataDelegate = 
//				new ExtendedMetadataDelegate(httpMetadataProvider, extendedMetadata());
//		extendedMetadataDelegate.setMetadataTrustCheck(true);
//		extendedMetadataDelegate.setMetadataRequireSignature(false);
//		return extendedMetadataDelegate;
//	}
 
    // IDP Metadata configuration + sp metadata configuration
    @Bean
    @Qualifier("metadata")
    public CachingMetadataManager metadata() throws MetadataProviderException, ResourceException {
    	List<MetadataProvider> providers = new ArrayList<MetadataProvider>();
      //  providers.add(ssoCircleExtendedMetadataProvider());
		providers.add(ssoUtaTestExtendedMetadataProvider());
		providers.add(ssoUtaProdExtendedMetadataProvider());
        //providers.add(simpleSamlProvider());
        //providers.add(simpleSamlShibProvider());
        //our metadata
        providers.add(prosoloSPMetadata());
        return new CachingMetadataManager(providers);
    }
    
    @Bean 
    public ClasspathResource classPathResource() throws ResourceException {
    	return new ClasspathResource("/saml/prosolosamlspmetadata.xml");
    }
    
    @Bean
    public ResourceBackedMetadataProvider resourceBackedProvider() throws MetadataProviderException, ResourceException {
    	Timer timer = new Timer(true);
    	ResourceBackedMetadataProvider rbmp = new ResourceBackedMetadataProvider(timer, classPathResource());
    	rbmp.setParserPool(parserPool());
    	return rbmp;
    }
    
    @Bean
    public ExtendedMetadata prosoloExtendedMetadata() {
    	ExtendedMetadata extendedMetadata = new ExtendedMetadata();
    	extendedMetadata.setIdpDiscoveryEnabled(true); 
    	extendedMetadata.setSignMetadata(false);
    	extendedMetadata.setLocal(true);
    	//extendedMetadata.setSslHostnameVerification("allowAll");
    	return extendedMetadata;
    }
    
    @Bean
	public ExtendedMetadataDelegate prosoloSPMetadata() throws MetadataProviderException, ResourceException {
		return new ExtendedMetadataDelegate(resourceBackedProvider(), prosoloExtendedMetadata());
	}
 
    // Filter automatically generates default SP metadata
//    @Bean
//    public MetadataGenerator metadataGenerator() {
//        MetadataGenerator metadataGenerator = new MetadataGenerator();
//        metadataGenerator.setEntityId("ca.prosolo");
//        metadataGenerator.setExtendedMetadata(extendedMetadata());
//        metadataGenerator.setIncludeDiscoveryExtension(false);
//        metadataGenerator.setKeyManager(keyManager()); 
//        return metadataGenerator;
//    }
 
    // The filter is waiting for connections on URL suffixed with filterSuffix
    // and presents SP metadata there
    @Bean
    public MetadataDisplayFilter metadataDisplayFilter() {
        return new MetadataDisplayFilter();
    }
    
	// Handler deciding where to redirect user after failed login
    @Bean
    public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
    	SimpleUrlAuthenticationFailureHandler failureHandler =
    			new SimpleUrlAuthenticationFailureHandler();
    	failureHandler.setUseForward(true);
    	failureHandler.setDefaultFailureUrl("/login?err=1");
    	return failureHandler;
    }
     
//	    @Bean
//	    public SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter() throws Exception {
//	        SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter = new SAMLWebSSOHoKProcessingFilter();
//	        samlWebSSOHoKProcessingFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
//	        samlWebSSOHoKProcessingFilter.setAuthenticationManager(authenticationManager());
//	        samlWebSSOHoKProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
//	        return samlWebSSOHoKProcessingFilter;
//	    }
    
    // Processing filter for WebSSO profile messages
    @Bean
    public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {
        SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();
        samlWebSSOProcessingFilter.setAuthenticationManager(authenticationManager());
        samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
        samlWebSSOProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return samlWebSSOProcessingFilter;
    }
     
//    @Bean
//    public MetadataGeneratorFilter metadataGeneratorFilter() {
//        return new MetadataGeneratorFilter(metadataGenerator());
//    }
     
    // Handler for successful logout
    @Bean
    public SimpleUrlLogoutSuccessHandler successLogoutHandler() {
        SimpleUrlLogoutSuccessHandler successLogoutHandler = new SimpleUrlLogoutSuccessHandler();
        successLogoutHandler.setDefaultTargetUrl("/login");
        return successLogoutHandler;
    }
     
    // Logout handler terminating local session
    @Bean
    public SecurityContextLogoutHandler logoutHandler() {
        SecurityContextLogoutHandler logoutHandler = 
        		new SecurityContextLogoutHandler();
        logoutHandler.setInvalidateHttpSession(true);
        logoutHandler.setClearAuthentication(true);
        return logoutHandler;
    }
 
    // Filter processing incoming logout messages
    // First argument determines URL user will be redirected to after successful
    // global logout
    @Bean
    public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
        return new SAMLLogoutProcessingFilter(successLogoutHandler(),
                logoutHandler());
    }
     
    // Overrides default logout processing filter with the one processing SAML
    // messages
    @Bean
    public SAMLLogoutFilter samlLogoutFilter() {
        return new SAMLLogoutFilter(successLogoutHandler(),
                new LogoutHandler[] { logoutHandler() },
                new LogoutHandler[] { logoutHandler() });
    }
	
    // Bindings
    private ArtifactResolutionProfile artifactResolutionProfile() {
        final ArtifactResolutionProfileImpl artifactResolutionProfile = 
        		new ArtifactResolutionProfileImpl(httpClient());
        artifactResolutionProfile.setProcessor(new SAMLProcessorImpl(soapBinding()));
        return artifactResolutionProfile;
    }
    
    @Bean
    public HTTPArtifactBinding artifactBinding(ParserPool parserPool, VelocityEngine velocityEngine) {
        return new HTTPArtifactBinding(parserPool, velocityEngine, artifactResolutionProfile());
    }
 
    @Bean
    public HTTPSOAP11Binding soapBinding() {
        return new HTTPSOAP11Binding(parserPool());
    }
    
    @Bean
    public HTTPPostBinding httpPostBinding() {
    	return new HTTPPostBinding(parserPool(), velocityEngine());
    }
    
    @Bean
    public HTTPRedirectDeflateBinding httpRedirectDeflateBinding() {
    	return new HTTPRedirectDeflateBinding(parserPool());
    }
    
    @Bean
    public HTTPSOAP11Binding httpSOAP11Binding() {
    	return new HTTPSOAP11Binding(parserPool());
    }
    
    @Bean
    public HTTPPAOS11Binding httpPAOS11Binding() {
    	return new HTTPPAOS11Binding(parserPool());
    }
    
    // Processor
	@Bean
	public SAMLProcessorImpl processor() {
		Collection<SAMLBinding> bindings = new ArrayList<SAMLBinding>();
		bindings.add(httpRedirectDeflateBinding());
		bindings.add(httpPostBinding());
		bindings.add(artifactBinding(parserPool(), velocityEngine()));
		bindings.add(httpSOAP11Binding());
		bindings.add(httpPAOS11Binding());
		return new SAMLProcessorImpl(bindings);
	}
    
	/**
	 * Define the security filter chain in order to support SSO Auth by using SAML 2.0
	 * 
	 * @return Filter chain proxy
	 * @throws Exception
	 */
    @Bean
    public FilterChainProxy samlFilter() throws Exception {
        List<SecurityFilterChain> chains = new ArrayList<SecurityFilterChain>();
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/login/**"),
                samlEntryPoint()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/logout/**"),
                samlLogoutFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/metadata/**"),
                metadataDisplayFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSO/**"),
                samlWebSSOProcessingFilter()));
//	        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSOHoK/**"),
//	                samlWebSSOHoKProcessingFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SingleLogout/**"),
                samlLogoutProcessingFilter()));
        //chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/discovery/**"),
                //samlIDPDiscovery()));
        return new FilterChainProxy(chains);
    }

//	    /**
//	     * Returns the authentication manager currently used by Spring.
//	     * It represents a bean definition with the aim allow wiring from
//	     * other classes performing the Inversion of Control (IoC).
//	     * 
//	     * @throws  Exception 
//	     */
//	    @Bean
//	    @Override
//	    public AuthenticationManager authenticationManagerBean() throws Exception {
//	        return super.authenticationManagerBean();
//	    }
 
//    /**
//     * Sets a custom authentication provider.
//     *
//     * @param   auth SecurityBuilder used to create an AuthenticationManager.
//     * @throws  Exception
//     */
//	    @Override
//	    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//	        auth
//	            .authenticationProvider(samlAuthenticationProvider());
//	    }
//

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}


}
