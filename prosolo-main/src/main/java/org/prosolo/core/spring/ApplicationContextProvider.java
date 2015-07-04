package org.prosolo.core.spring;

import org.springframework.context.ApplicationContext;

interface ApplicationContextProvider {

	ApplicationContext getContext();
	
	ApplicationContext createContext();
	
	String[] getContextLocations();
}
