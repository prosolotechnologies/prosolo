/**
 * 
 */
package org.prosolo.core.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @author "Nikola Milikic"
 *
 */
@Configuration
@ComponentScan
({
	"org.prosolo.core", 
	"org.prosolo.services", 
	"org.prosolo.similarity", 
	"org.prosolo.similarity.impl", 
	"org.prosolo.services.aspects", 
	"org.prosolo.recommendation", 
	"org.prosolo.reminders", 
	"org.prosolo.news", 
	"org.prosolo.recommendation.dal.impl", 
	"org.prosolo.recommendation.util", 
	"org.prosolo.app.bc", 
	"org.prosolo.web", 
	"org.prosolo.search", 
	"org.prosolo.web.home", 
	"org.prosolo.services.annotation", 
	"org.prosolo.services.annotation.impl"})
@ImportResource({"classpath:core/spring/context.xml"})
public class SpringConfig {
	
//	@Autowired private SessionFactory sessionFactory;
//	@Autowired @Qualifier(value= "dataSource") private ComboPooledDataSource dataSource;
//
//	@Bean
//	public ServiceLocator contextApplicationContextProvider() {
//		return new ServiceLocator();
//	}
//	
//	@Bean
//	public CustomScopeConfigurer customScopeConfigurer() {
//		CustomScopeConfigurer customScopeConfigurer =  new CustomScopeConfigurer();
//		
//		Map<String, Object> scopes = new HashMap<String, Object>();
//		scopes.put("view", viewScope());
//		
//		customScopeConfigurer.setScopes(scopes);
//		
//		return customScopeConfigurer;
//	}
//	
//	@Bean
//	public ViewScope viewScope() {
//		return new ViewScope();
//	}
//
//	@Bean
//	public ThreadPoolTaskExecutor taskExecutor() {
//		return new ThreadPoolTaskExecutor();
//	}
	
//	@Bean
//	public HibernateTransactionManager transactionManager() {
//		HibernateTransactionManager transactionManager = new HibernateTransactionManager();
//		
//		transactionManager.setSessionFactory(sessionFactory);
//		transactionManager.setDataSource(dataSource);
//		
//		return transactionManager;
//	}
//	
//	@Bean
//	public TransactionTemplate transactionTemplate() {
//		TransactionTemplate transactionTemplate = new TransactionTemplate();
//		
//		transactionTemplate.setTransactionManager(transactionManager());
//		
//		return transactionTemplate;
//	}
}
