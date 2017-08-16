package org.prosolo.bigdata.common.events.serialization;/**
 * Created by zoran on 26/07/16.
 */

import com.google.gson.*;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.common.event.context.LearningContext;

import java.lang.reflect.Type;

/**
 * zoran 26/07/16
 */
public class ContextDeserializer implements JsonDeserializer<Context> {
    private static JsonParser parser = new JsonParser();


    @Override
    public Context deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final JsonObject jsonObject = jsonElement.getAsJsonObject();
        Context lcontext=new Context();
        if(jsonObject.has("object_type")){
            lcontext.setObjectType(jsonObject.get("object_type").getAsString());
        }
        if(jsonObject.has("name")){
            String name=jsonObject.get("name").getAsString();
            lcontext.setName(ContextName.valueOf(name));
        }
        if(jsonObject.has("id")){
            lcontext.setId(jsonObject.get("id").getAsLong());

        }
        if(jsonObject.has("context")){
            Context childContext= jsonDeserializationContext.deserialize(jsonObject.get("context"),Context.class);
            lcontext.setContext(childContext);
        }
        return lcontext;
    }
}
