package org.prosolo.web.util;

import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.context.ContextJsonParserService;

public class LearningContextUtil {

	public static String addSubContext(String base, String subcontext) {
		return ServiceLocator.getInstance().getService(ContextJsonParserService.class).addSubContext(base, subcontext);
	}
	
	public static String addSubService(String base, String subservice) {
		return ServiceLocator.getInstance().getService(ContextJsonParserService.class).addSubService(base, subservice);
	}
}
