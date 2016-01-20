package org.prosolo.web.lti.json;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.prosolo.web.lti.json.data.MessageParameter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class MessageParameterTypeAdapterFactory extends CustomizedTypeAdapterFactory<MessageParameter>{
  
	public MessageParameterTypeAdapterFactory() {
		    super(MessageParameter.class);
	}

	@Override protected void beforeWrite(MessageParameter source, JsonElement toSerialize) {
		/*JsonObject toolProfile = toSerialize.getAsJsonObject().get("tool_profile").getAsJsonObject();
		JsonArray messages = null;
		if(toolProfile.has("message")){
			messages = toolProfile.get("message").getAsJsonArray();	
			int i,j = 0;
			for(JsonElement obj: messages){
				JsonArray parameters = obj.getAsJsonObject().get("parameter").getAsJsonArray();
				for(JsonElement param:parameters){
					String parameterType = source.
					param.getAsJsonObject().add(property, value);
					j++;
				}
				i++;
			}
		}*/
		toSerialize.getAsJsonObject().add(source.getParameterType(), new JsonPrimitive(source.getParameterValue()));
		
	}

	@Override protected void afterRead(JsonElement deserialized, MessageParameter m) {
		  //JsonObject custom = deserialized.getAsJsonObject();
		  //custom.remove("size");
		JsonObject custom = deserialized.getAsJsonObject();
		Set<Entry<String, JsonElement>> set = custom.entrySet();
		Iterator<Entry<String, JsonElement>> it = set.iterator();
		while(it.hasNext()){
			Entry<String, JsonElement> ent = it.next();
			if(!"name".equals(ent.getKey())){
				m.setParameterType(ent.getKey());
				m.setParameterValue(ent.getValue().getAsString());
				break;
			}
		}
		
	}
		
}
