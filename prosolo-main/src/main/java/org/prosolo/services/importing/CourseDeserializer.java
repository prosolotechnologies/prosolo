package org.prosolo.services.importing;

/**
 * 
 * @author Zoran Jeremic
 * @deprecated since 0.7
 */
@Deprecated
public class CourseDeserializer 
//implements JsonDeserializer<Course> 
{
	
//	private static Logger logger = Logger.getLogger(CourseDeserializer.class);
//
//	@Override
//	public Course deserialize(JsonElement json, Type arg1,
//		JsonDeserializationContext context) throws JsonParseException {
//		final JsonObject jsonObject = json.getAsJsonObject();
//	    final String title = jsonObject.get("title").getAsString();
//	    final String description = jsonObject.get("description").getAsString();
//	    final CreatorType creatorType = CreatorType.valueOf(jsonObject.get("creatorType").getAsString());
//		final boolean studentsCanAddNewCompetences = jsonObject.get("published").getAsBoolean();
//	    final boolean published = jsonObject.get("studentsCanAddNewCompetences").getAsBoolean();
// 
//		Tag[] tagsArray = context.deserialize(jsonObject.get("tags"), Tag[].class);
//		Tag[] hashtagsArray = context.deserialize(jsonObject.get("hashtags"), Tag[].class);
//		CourseCompetence[] competences = context.deserialize(jsonObject.get("competences"), CourseCompetence[].class);
//		User maker = context.deserialize(jsonObject.get("maker"), User.class);
//		Course restoredCourse = null;
//		Course basedOn = null;
//		List<CourseCompetence> courseCompetences = new ArrayList<CourseCompetence>();
//		
//		for (int i = 0; i < competences.length; i++) {
//			courseCompetences.add(competences[i]);
//		}
//		
//		Set<Tag> tags = new HashSet<Tag>();
//		for (int i = 0; i < tagsArray.length; i++) {
//			tags.add(tagsArray[i]);
//		}
//		
//		Set<Tag> hashtags = new HashSet<Tag>();
//		for (int i = 0; i < hashtagsArray.length; i++) {
//			hashtags.add(hashtagsArray[i]);
//		}
//		
//	    try {
//			restoredCourse = ServiceLocator.getInstance().getService(CourseManager.class).saveNewCourse(
//					title,
//					description, 
//					basedOn, 
//					courseCompetences, 
//					tags,
//					hashtags,
//					maker, 
//					creatorType, 
//					studentsCanAddNewCompetences,
//					published);
//		} catch (EventException | ResourceCouldNotBeLoadedException e) {
//			logger.error(e);
//		}
//		return restoredCourse;
//	}

}
