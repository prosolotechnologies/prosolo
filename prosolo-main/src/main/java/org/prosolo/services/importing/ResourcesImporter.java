package org.prosolo.services.importing;

import org.springframework.stereotype.Service;

/**
 * 
 * @author Zoran Jeremic
 * @deprecated since 0.7
 */
@Deprecated
@Service("org.prosolo.services.importing.ResourcesImporter")
public class ResourcesImporter {  
	
//	private static Logger logger = Logger.getLogger(ResourcesImporter.class);
//	
//	@Autowired CompetenceManager compManager;
//	@Autowired NodeEntityESService nodeEntityESService;
//
//	public ArrayList<Competence> parseCompetencesFromJson() {
//		URL url = Thread.currentThread().getContextClassLoader().getResource("jsonfiles/competences.json");
//		ArrayList<Competence> competences = null;
//		
//		try {
//			JSONParser parser = new JSONParser();
//			Object obj = parser.parse(new FileReader(url.getPath()));
//			JSONArray jsonObject = (JSONArray) obj;
//			Gson gson = new GsonBuilder().setDateFormat("MMM dd, yyyy HH:mm:ss a").create();
//			Type listType = new TypeToken<ArrayList<Competence>>() {}.getType();
//			competences = gson.fromJson(jsonObject.toJSONString(), listType);
//		} catch (FileNotFoundException e) {
//			logger.error(e);
//		} catch (IOException e) {
//			logger.error(e);
//		} catch (ParseException e) {
//			logger.error(e);
//		}
//		return competences;
//	}
//
//	@Transactional
//	public void batchImportExternalCompetences() {
//		ArrayList<Competence> competences = parseCompetencesFromJson();
//		
//		int i = 0;
//		for (Competence competence : competences) {
//			if (competence != null) {
//				compManager.saveEntity(competence);
//				nodeEntityESService.saveNodeToES(competence);
//				
//				if ( i % (20 / 2) == 0 ) { //20, same as the JDBC batch size, 2 because we have saved two instances
//			        //flush a batch of inserts and release memory
//					compManager.flush();
//					compManager.clear();
//			    }
//			}
//			i++;
//		}
//	}

}
