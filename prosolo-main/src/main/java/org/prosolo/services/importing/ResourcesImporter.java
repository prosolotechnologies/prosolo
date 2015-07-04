package org.prosolo.services.importing;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.prosolo.domainmodel.competences.Competence;
import org.prosolo.services.indexing.NodeEntityESService;
import org.prosolo.services.nodes.CompetenceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * @author Zoran Jeremic 2013-07-14
 * 
 */
@Service("org.prosolo.services.importing.ResourcesImporter")
public class ResourcesImporter {  
	
	private static Logger logger = Logger.getLogger(ResourcesImporter.class);
	
	@Autowired CompetenceManager compManager;
	@Autowired NodeEntityESService nodeEntityESService;

	public ArrayList<Competence> parseCompetencesFromJson() {
		URL url = Thread.currentThread().getContextClassLoader().getResource("jsonfiles/competences.json");
		ArrayList<Competence> competences = null;
		
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader(url.getPath()));
			JSONArray jsonObject = (JSONArray) obj;
			Gson gson = new GsonBuilder().setDateFormat("MMM dd, yyyy HH:mm:ss a").create();
			Type listType = new TypeToken<ArrayList<Competence>>() {}.getType();
			competences = gson.fromJson(jsonObject.toJSONString(), listType);
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (ParseException e) {
			logger.error(e);
		}
		return competences;
	}

	@Transactional
	public void batchImportExternalCompetences() {
		ArrayList<Competence> competences = parseCompetencesFromJson();
		
		int i = 0;
		for (Competence competence : competences) {
			if (competence != null) {
				compManager.saveEntity(competence);
				nodeEntityESService.saveNodeToES(competence);
				
				if ( i % (20 / 2) == 0 ) { //20, same as the JDBC batch size, 2 because we have saved two instances
			        //flush a batch of inserts and release memory
					compManager.flush();
					compManager.clear();
			    }
			}
			i++;
		}
	}

}
