package org.prosolo.services.context.impl;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.context.util.CustomContextNameDeserializer;
import org.prosolo.services.event.context.Context;
import org.prosolo.services.event.context.ContextName;
import org.prosolo.services.event.context.LearningContext;
import org.prosolo.services.event.context.LearningContextInfo;
import org.prosolo.services.event.context.Service;
import org.prosolo.web.ApplicationPage;
import org.prosolo.web.ApplicationPagesBean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@org.springframework.stereotype.Service("org.prosolo.services.context.ContextJsonParserService")
public class ContextJsonParserServiceImpl implements ContextJsonParserService {

	private static Logger logger = Logger.getLogger(ContextJsonParserServiceImpl.class);
	
	@Inject private ApplicationPagesBean applicationPagesBean;
	
	@Override
	public LearningContext parseCustomContextString(String page, String context, String service) {
		try {
			LearningContext lContext = new LearningContext();
			
			ApplicationPage appPage = page != null ? applicationPagesBean.getPageForURI(page) : null;
			
			GsonBuilder builder = new GsonBuilder();
			builder.registerTypeAdapter(ContextName.class, new CustomContextNameDeserializer());	
			Gson gson = builder.create();
			
			Context c = parseContext(context, gson);
			
			Service s = null;
			if(service != null && !service.isEmpty()) {
				Map<String, Object> srv = parseString(service, "service");
				
				String jsonService = gson.toJson(srv);
				//System.out.println(jsonService);
				s = gson.fromJson(jsonService, Service.class);
				addObjectTypeInfoForContext(s);
			}
			
			lContext.setPage(appPage);
			lContext.setContext(c);
			lContext.setService(s);
			
			return lContext;
			
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public Context parseContext(String context) {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(ContextName.class, new CustomContextNameDeserializer());	
		Gson gson = builder.create();
		return parseContext(context, gson);
	}
	
	private Context parseContext(String context, Gson gson) {
		Context c = null;
		if(context != null && !context.isEmpty()) {
			Map<String, Object> ctx = parseString(context, "context");
			
			String jsonContext = gson.toJson(ctx);
			//System.out.println(jsonContext);
			c = gson.fromJson(jsonContext, Context.class);
			addObjectTypeInfoForContext(c);
		}
		return c;
	}
	
	private void addObjectTypeInfoForContext(LearningContextInfo lci) {
		if(lci.getObjectType() == null || lci.getObjectType().isEmpty()) {
			lci.setObjectType(lci.getName().getObjectType());
		}
		
		if(lci instanceof Context) {
			Context c = (Context) lci;
			if(c.getContext() != null) {
				addObjectTypeInfoForContext(c.getContext());
			}
		} else {
			Service s = (Service) lci;
			if(s.getService() != null) {
				addObjectTypeInfoForContext(s.getService());
			}
		}
	}

	private boolean isNumeric(String string) {
		return string.matches("^[1-9]\\d*$");
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> parseString(String str, String type) {
		//case when base context isn't specified with context special keyword
		//System.out.println(str.length());
		str = str.replaceAll("^\\|*(?=" + type + ":)|\\|*$", "");
		//System.out.println(str.length());
		if(!str.startsWith(type + ":") || !(str.charAt(str.length() - 1) == '/')) {
			str = type + ":/" + str + "/";
		}
		int length = str.length();
		Map<String, Object> resMap = parseString(str, 0, length, type);
		if(resMap != null) {
			return (Map<String, Object>) ((Map<String, Object>) resMap.get("jsonMap")).get(type);
		}
		return null;
	}
	
	private Map<String, Object> parseString(String str, int startPos, int length, String type) {
	    int pos = startPos;
	    Map<String, Object> resultMap = new HashMap<>();
	    Map<String, Object> map = new HashMap<>();
	    resultMap.put("jsonMap", map);
	    StringBuilder result = new StringBuilder();
	    String key = null;
	    while(pos < length) {
	    	boolean alreadyIncremented = false;
	    	char c = str.charAt(pos);
	    	switch (c) {
			case '|':
				if(key != null) {
					map.put(key, getValueForMap(result));
					result.setLength(0);
					key = null;
				}
				break;
			case ':':
				if(!type.equals(result.toString())) {
					key = result.toString();
					result.setLength(0);
				} else {
					result.append(c);
				}
				break;
			case '/':
				 if ((type+":").equals(result.toString())) {
					Map<String, Object> resMap = parseString(str, pos + 1, length, type);
					pos = (int) resMap.get("currentPos");
					alreadyIncremented = true;
	            	map.put(type, resMap.get("jsonMap"));
	            	key = null;
	            	result.setLength(0);
	            	break;
		         } else {
		        	if(key != null) {
		        		map.put(key, getValueForMap(result));
		        	}
		        	resultMap.put("currentPos", pos + 1);
					return resultMap;
		         }
			default:
				result.append(c);
				break;
			}
	    	if(!alreadyIncremented) {
	    		pos++;
	    	}
	    }
	    
	    if(key != null) {
	    	map.put(key, getValueForMap(result));
	    }
  	    return resultMap;
	}
	
	private Object getValueForMap(StringBuilder value) {
		String s = value.toString();
		if(isNumeric(s)) {
			return Long.parseLong(s);
		}
		return s;
	}
	
	@Override
	public String addSubContext(String base, String subcontext) {
		return addNestedDoc(base, subcontext, "context");
	}
	
	@Override
	public String addSubService(String base, String subservice) {
		return addNestedDoc(base, subservice, "service");
	}

	private String addNestedDoc(String base, String subdoc, String type) {
		if(base != null && !base.isEmpty()) {
			subdoc = type + ":/" + subdoc + "/|";
			int index = base.lastIndexOf(type + ":/");
			if(index != -1) {
				int ind = index + (type + ":/").length();
				String firstPart = base.substring(0, ind);
				String lastPart = base.substring(ind);
				return firstPart + subdoc + lastPart;
			} else {
				return subdoc + base;
			}
		}
		return subdoc;
	}

//	public static void main(String[] args) {
//		ContextJsonParserServiceImpl c = new ContextJsonParserServiceImpl();
//		//c.parseCustomContextString("/learn.xhtml", "name:CREDENTIAL.id:123.context:/name:COMPETENCE_WALL.id:21123.context:/name:stef.id:2.context:/name:blabla.id:1///", "name:CREDENTIAL.id:123.service:/name:COMPETENCE_WALL.id:21123.service:/name:stef.id:2.service:/name:blabla.id:1///");
//		c.parseCustomContextString("/learn.xhtml", "||||||||||||||||||context:/name:goal_wall|id:2|context:/context:/name:activity_wall|id:12/|name:competence_wall|id:11//|||||", "name:CREDENTIAL&id:123&service:/name:COMPETENCE_WALL&id:21123&service:/name:stef&id:2&service:/name:blabla&id:1///");
//		
////		String str = c.addSubContext("name:CREDENTIAL.id:123.context:/name:COMPETENCE_WALL.id:21123.context:/name:stef.id:2.context:/name:blabla.id:1///", 
////				"name:test.id:555");
//		String str = c.addSubContext("name:CREDENTIAL.id:123", 
//				"name:test.id:555");
//		System.out.println("----------------------");
//		c.parseCustomContextString("/learn.xhtml", str, null);
//		
//	}
}
