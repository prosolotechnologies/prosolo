package org.prosolo.services.importing;

import java.lang.reflect.Type;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.annotation.TagManager;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 *
 * @author Zoran Jeremic Apr 21, 2014
 *
 */

public class TagDeserializer implements JsonDeserializer<Tag> {
	
	@Override
	public Tag deserialize(JsonElement json, Type arg1, JsonDeserializationContext context) throws JsonParseException {
		final JsonObject jsonObject = json.getAsJsonObject();
		final String title = jsonObject.get("title").getAsString();
		Tag tag = ServiceLocator.getInstance().getService(TagManager.class).getOrCreateTag(title);
		return tag;
	}
	
}
