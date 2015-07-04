package org.prosolo.core.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ServiceLocator implements ApplicationContextProvider, ApplicationContextAware {

	private static ApplicationContext context;
	//private static GenericApplicationContext context;
	
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		//context = (AnnotationConfigWebApplicationContext) ctx;
	
		context =  ctx;
	} 
	
	private static class SingletonHolder {
		public static final ServiceLocator INSTANCE = new ServiceLocator();
	}

	public static ServiceLocator getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public ApplicationContext createContext() {
    	return context;
	}

	public String[] getContextLocations() {
		String[] contextLocations = { 
				"core/mysqldb/context.xml",
				"core/spring/context.xml",
				//"core/wicket/context.xml",
				//"core/hibernate/context.xml" 
				};
		return contextLocations;
	}

	public ApplicationContext getContext() {
		return context;
	}
	
	public <T> T getService(Class<T> clazz) {
		return getContext().getBean(clazz);
	}
	
}
