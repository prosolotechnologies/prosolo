package org.prosolo.services.importing;

import java.lang.reflect.Type;

import org.apache.log4j.Logger;
import org.prosolo.domainmodel.competences.Competence;
import org.prosolo.domainmodel.course.CourseCompetence;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.nodes.CompetenceManager;

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

public class CourseCompetenceDeserializer  implements JsonDeserializer<CourseCompetence> {
	private static Logger logger = Logger.getLogger(CourseCompetenceDeserializer.class);
	
	@Override
	public CourseCompetence deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		final JsonObject jsonObject = json.getAsJsonObject();
	    final long order = jsonObject.get("order").getAsLong(); 
		final long daysOffset= jsonObject.get("daysOffset").getAsLong();
		final long duration= jsonObject.get("duration").getAsLong();
		Competence competence=context.deserialize(jsonObject.get("competence"), Competence.class);
		CourseCompetence cCompetence=null;
		if(competence!=null){
			cCompetence=new CourseCompetence(competence);
			cCompetence.setDaysOffset(daysOffset);
			cCompetence.setDuration(duration);
			cCompetence.setOrder(order);
			try{
			ServiceLocator.getInstance().getService(CompetenceManager.class).saveEntity(cCompetence);
			}catch(Exception ex){
				logger.error("Exception during the serialization",ex);
			}
		}
		return cCompetence;
	}

}
