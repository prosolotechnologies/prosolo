package org.prosolo.services.importing;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.CompetenceActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.event.EventException;
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

public class CompetenceDeserializer  implements JsonDeserializer<Competence> {
	
	private static Logger logger = Logger.getLogger(CompetenceDeserializer.class);

	@Override
	public Competence deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		
		final JsonObject jsonObject = json.getAsJsonObject();
		long date = jsonObject.get("dateCreated").getAsLong();
		Date dateCreated = null;
		
		if (date > 0) {
			dateCreated = new Date(date);
		}
		
		String description = jsonObject.get("description").getAsString();
		String title = jsonObject.get("title").getAsString();
		int duration = jsonObject.get("duration").getAsInt();
		int validity = jsonObject.get("validityPeriod").getAsInt();
		Competence[] cPrerequisites = context.deserialize(jsonObject.get("prerequisites"), Competence[].class);
		List<Competence> prerequisites = new ArrayList<Competence>();
		
		for (int i = 0; i < cPrerequisites.length; i++) {
			prerequisites.add(cPrerequisites[i]);
		}

		Competence[] cCorequisites = context.deserialize(jsonObject.get("corequisites"), Competence[].class);
		List<Competence> corequisites = new ArrayList<Competence>();
		
		for (int i = 0; i < cCorequisites.length; i++) {
			corequisites.add(cCorequisites[i]);
		}
		
		Tag[] annotations = context.deserialize(jsonObject.get("annotations"), Tag[].class);
		Set<Tag> tags = new HashSet<Tag>();
		for (int i = 0; i < annotations.length; i++) {
			tags.add(annotations[i]);
		}

		User maker = null;
		Competence comp = context.deserialize(jsonObject.get("competence"), Competence.class);
		CompetenceActivity[] compActivities = context.deserialize(jsonObject.get("activities"), CompetenceActivity[].class);
		
		try {
			comp = ServiceLocator.getInstance().getService(CompetenceManager.class)
					.createCompetence(maker.getId(), title, description, validity, duration, tags, prerequisites, corequisites, dateCreated);
		} catch (EventException | ResourceCouldNotBeLoadedException e) {
			logger.error(e.getLocalizedMessage());
		}
		for (int i = 0; i < compActivities.length; i++) {
			comp.addActivity(compActivities[i]);
		}
		return comp;
	}

}
