package org.prosolo.bigdata.common.events.serialization;/**
 * Created by zoran on 26/07/16.
 */

import com.google.gson.*;
//import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.LearningContext;
import org.prosolo.common.event.context.Service;
import org.prosolo.common.web.ApplicationPage;

import java.lang.reflect.Type;

/**
 * zoran 26/07/16
 */
public class LearningContextDeserializer  implements JsonDeserializer<LearningContext> {
    private static JsonParser parser = new JsonParser();
    @Override
    public LearningContext deserialize(JsonElement json, Type typeOfT,
                                JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        LearningContext learningContext=new LearningContext();
       // ApplicationPage page=context.deserialize(jsonObject.get("page"),ApplicationPage.class);


        if(jsonObject.has("page")){
            ApplicationPage  page= ApplicationPage.valueOf(jsonObject.get("page").getAsString());
            learningContext.setPage(page);
        }
        if(jsonObject.has("context")){
            Context ccontext=context.deserialize(jsonObject.get("context"),Context.class);
            learningContext.setContext(ccontext);
        }
        if(jsonObject.has("service")){
            Service service=context.deserialize(jsonObject.get("service"),Service.class);
            learningContext.setService(service);
        }




        return learningContext;

    }
}
