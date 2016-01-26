package org.prosolo.services.context;

import org.prosolo.services.event.context.LearningContext;

public interface ContextJsonParserService {

	LearningContext parseCustomContextString(String page, String context, String service);
	
	String addSubContext(String base, String subcontext);
	
	String addSubService(String base, String subservice);
}