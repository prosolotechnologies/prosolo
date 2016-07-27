package org.prosolo.bigdata.events.serialization;

import java.lang.reflect.Type;

import org.prosolo.bigdata.events.pojo.LogEvent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.prosolo.common.event.context.LearningContext;

/**
 * @author Zoran Jeremic Apr 6, 2015
 *
 */

public class LogEventDeserializer implements JsonDeserializer<LogEvent> {
	private static JsonParser parser = new JsonParser();

	@Override
	public LogEvent deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject wrappedMessage = json.getAsJsonObject();
		JsonObject message = (JsonObject) wrappedMessage.get("message");
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		LogEvent event = gson.fromJson(message.toString(), LogEvent.class);
		String messageParameters = message.get("parameters").toString();
		JsonObject parameters = (JsonObject) parser.parse(messageParameters);
		event.setParameters(parameters);
		if(message.has("learningContext")){
			String learningContext = message.get("learningContext").toString();

			System.out.println("LEARNING CONTEXT");
			JsonObject learningContextObject = (JsonObject) parser.parse(learningContext);
			event.setLearningContextJson(learningContextObject);
			LearningContext lContext=context.deserialize(message.get("learningContext"),LearningContext.class);
		}
		return event;
	}

}
