package org.prosolo.services.importing;

/*/**
 * 
 * @author Zoran Jeremic
 * @deprecated since 0.7
 */
@Deprecated
public class CourseCompetenceDeserializer 
//implements JsonDeserializer<CourseCompetence> 
{
//	private static Logger logger = Logger.getLogger(CourseCompetenceDeserializer.class);
//	
//	@Override
//	public CourseCompetence deserialize(JsonElement json, Type typeOfT,
//			JsonDeserializationContext context) throws JsonParseException {
//		final JsonObject jsonObject = json.getAsJsonObject();
//	    final long order = jsonObject.get("order").getAsLong(); 
//		final long daysOffset= jsonObject.get("daysOffset").getAsLong();
//		final long duration= jsonObject.get("duration").getAsLong();
//		Competence competence=context.deserialize(jsonObject.get("competence"), Competence.class);
//		CourseCompetence cCompetence=null;
//		if(competence!=null){
//			cCompetence=new CourseCompetence(competence);
//			cCompetence.setDaysOffset(daysOffset);
//			cCompetence.setDuration(duration);
//			cCompetence.setOrder(order);
//			try{
//			ServiceLocator.getInstance().getService(CompetenceManager.class).saveEntity(cCompetence);
//			}catch(Exception ex){
//				logger.error("Exception during the serialization",ex);
//			}
//		}
//		return cCompetence;
//	}

}
