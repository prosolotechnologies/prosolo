package org.prosolo.services.context;

import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.LearningContext;

public interface ContextJsonParserService {

 	LearningContext parseCustomContextString(String page, String context, String service);
	
	String addSubContext(String base, String subcontext);
	
	String addSubService(String base, String subservice);
	
	Context parseContext(String context);
}