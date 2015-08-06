package org.prosolo.common.messaging;

import java.lang.reflect.Type;

import org.prosolo.common.messaging.data.MessageWrapper;
 



import org.prosolo.common.messaging.data.SimpleMessage;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MessageWrapperAdapter implements JsonSerializer<MessageWrapper>, JsonDeserializer<MessageWrapper> {

	@Override
	public MessageWrapper deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		
		MessageWrapper messageWrapper=new MessageWrapper();
		JsonObject jsonObject = json.getAsJsonObject();
		
		if (jsonObject.has("sender")){
			messageWrapper.setSender(jsonObject.get("sender").getAsString());
		}
		
		messageWrapper.setTimecreated(jsonObject.get("timecreated").getAsLong());
		
		if (jsonObject.has("type")) {
			String type = jsonObject.get("type").getAsString();

			JsonElement messageElement = jsonObject.get("message");

			try {
				String thepackage = "org.prosolo.common.messaging.data.";
				SimpleMessage message = context.deserialize(messageElement, Class.forName(thepackage + type));
				messageWrapper.setMessage(message);
				return messageWrapper;
			} catch (ClassNotFoundException cnfe) {
				throw new JsonParseException("Unknown element type: " + type, cnfe);
			}
		} else {
			throw new JsonParseException("Element type was not provided: " + json);
		}
	}

	@Override
	public JsonElement serialize(MessageWrapper src, Type typeOfSrc,
			JsonSerializationContext context) {

		JsonObject result = new JsonObject();
		
		if (src.getSender() != null) {
			result.add("sender", new JsonPrimitive(src.getSender()));
		}
		result.add("timecreated", new JsonPrimitive(src.getTimecreated()));
		result.add("type", new JsonPrimitive(src.getMessage().getClass().getSimpleName()));
		result.add("message", context.serialize(src.getMessage(), src.getMessage().getClass()));
		return result;
	}
}
