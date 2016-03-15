package org.prosolo.bigdata.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonUtil {

	public static String getAsString(JsonObject object, String name) {
		JsonElement el = object.get(name);
		return el.getAsString();
	}
}
