package org.prosolo.bigdata.events.serialization;

import java.lang.reflect.Type;
import org.prosolo.bigdata.events.pojo.AnalyticsEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

//import com.google.gson.JsonParser;

/**
 * @author Zoran Jeremic Apr 12, 2015
 *
 */

public class AnalyticsEventDeserializer implements
		JsonDeserializer<AnalyticsEvent> {
	// private static JsonParser parser = new JsonParser();
	@Override
	public AnalyticsEvent deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject wrappedMessage = json.getAsJsonObject();
		JsonObject message = (JsonObject) wrappedMessage.get("message");
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		AnalyticsEvent event = gson.fromJson(message.toString(),
				AnalyticsEvent.class);
		return event;
	}

}
