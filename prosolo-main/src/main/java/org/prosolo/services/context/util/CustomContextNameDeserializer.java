package org.prosolo.services.context.util;

import org.prosolo.common.event.context.ContextName;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class CustomContextNameDeserializer implements JsonDeserializer<ContextName>{

	    @Override
	    public ContextName deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext context)
	            throws JsonParseException {
	        try {
	            if(type instanceof Class && ((Class<?>) type).isEnum())
	                return ContextName.valueOf(json.getAsString().toUpperCase());
	            return null;
	        } catch (Exception e) {
	            e.printStackTrace();
	            return null;
	        }
	    }
}
