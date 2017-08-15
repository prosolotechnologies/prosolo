package org.prosolo.bigdata.common.events.serialization;/**
 * Created by zoran on 26/07/16.
 */

import com.google.gson.*;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.common.event.context.LearningContext;
import org.prosolo.common.event.context.Service;

import java.lang.reflect.Type;

/**
 * zoran 26/07/16
 */
public class ServiceDeserializer implements JsonDeserializer<Service> {
    private static JsonParser parser = new JsonParser();

    @Override
    public Service deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final JsonObject jsonObject = jsonElement.getAsJsonObject();
        Service service=new Service();
        if(jsonObject.has("object_type")){
            service.setObjectType(jsonObject.get("object_type").getAsString());
        }
        if(jsonObject.has("name")){
            String name=jsonObject.get("name").getAsString();
            service.setName(ContextName.valueOf(name));
        }
        if(jsonObject.has("id")){
            service.setId(jsonObject.get("id").getAsLong());
            Context childContext= jsonDeserializationContext.deserialize(jsonObject.get("context"),Context.class);
        }
        return service;
    }
}
