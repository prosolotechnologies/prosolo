package org.prosolo.web.manage.students;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.observations.Observation;
import org.prosolo.common.domainmodel.observations.Suggestion;
import org.prosolo.common.domainmodel.observations.Symptom;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.studentProfile.observations.ObservationManager;
import org.prosolo.services.studentProfile.observations.SuggestionManager;
import org.prosolo.services.studentProfile.observations.SymptomManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.manage.students.data.observantions.EditObservationData;
import org.prosolo.web.manage.students.data.observantions.ObservationData;
import org.prosolo.web.manage.students.data.observantions.SuggestionData;
import org.prosolo.web.manage.students.data.observantions.SymptomData;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;

@ManagedBean(name = "observationBean")
@Component("observationBean")
@Scope("view")
public class ObservationBean implements Serializable {

	private static final long serialVersionUID = 5660853851402473378L;

	private static Logger logger = Logger.getLogger(ObservationBean.class);
	
	@Inject
	private ObservationManager observationManager;
	@Inject
	private LoggedUserBean loggedUserBean;
	@Inject
	private SymptomManager symptomManager;
	@Inject
	private SuggestionManager suggestionManager;
	@Inject
	@Qualifier("taskExecutor") 
	private ThreadPoolTaskExecutor taskExecutor;
	@Inject
	private EventFactory eventFactory;

	private long studentId;
	private String studentName;
	//private long targetCredentialId;

	private ObservationData lastObservation;

	private EditObservationData editObservation;
	private boolean isNew;

	private SelectItem[] allSymptoms;
	private SelectItem[] allSuggestions;
	
	private List<ObservationData> observationHistory;

	public void initializeObservationData() {
		try {
			Observation observation = observationManager.getLastObservationForUser(studentId);
			if (observation != null) {
				lastObservation = new ObservationData(observation);
			}
		} catch (DbConnectionException e) {
			throw e;
		}
	}
	
	public void loadObservationHistory() {
		if(observationHistory == null) {
			List<Observation> observations = observationManager.getObservations(studentId);
			observationHistory = new ArrayList<>();
			for(Observation ob:observations){
				observationHistory.add(new ObservationData(ob));
			}
		}
	}
	
	public void removeObservationHistory() {
		observationHistory = null;
	}

	public void saveObservation() {
		try {
			Date date = isNew ? new Date() : editObservation.getEditObservation().getDateCreated();
			Map<String, Object> result = observationManager.saveObservation(editObservation.getEditObservation().getId(),
					date, editObservation.getEditObservation().getMessage(), editObservation.getEditObservation().getNote(),
					editObservation.getSelectedSymptoms(), editObservation.getSelectedSuggestions(), loggedUserBean.getUserContext(),
					studentId);
			
			logger.info("User with id "+ loggedUserBean.getUserId() + " created observation for student with id "+studentId);
			
			Object msg = result.get("message");
			
			if(msg != null){
				final String context = "studentProfile.observation." + Long.parseLong(result.get("observationId").toString());

				final Message message1 = (Message) msg;
				UserContextData userContext = loggedUserBean.getUserContext();
				taskExecutor.execute(() -> {
					try {
						Map<String, String> parameters = new HashMap<String, String>();
						parameters.put("context", context);
						parameters.put("user", String.valueOf(studentId));
						parameters.put("message", String.valueOf(message1.getId()));
						eventFactory.generateEvent(EventType.SEND_MESSAGE, userContext,
								message1, null, null, parameters);
					} catch (EventException e) {
						logger.error(e);
					}
				});
			}
			
			editObservation = null;
			Observation observation = observationManager.getLastObservationForUser(studentId);
			if (observation != null) {
				lastObservation = new ObservationData(observation);
				if(observationHistory != null) {
					if(isNew) {
						observationHistory.add(0, lastObservation);
					} else {
						Optional<ObservationData> obsData = observationHistory.stream().filter(obs -> 
							obs.getId() == lastObservation.getId())
							.findFirst();
						if(obsData.isPresent()) {
							ObservationData obs = obsData.get();
							obs.setNote(lastObservation.getNote());
							obs.removeCurrentAndAddSymptomsFromList(lastObservation.getSymptoms());
							obs.removeCurrentAndAddSuggestionsFromList(lastObservation.getSuggestions());
						}
						
					}
				}
			}
			
			PageUtil.fireSuccessfulInfoMessage("The observation has been saved");
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}

	public void resetObservationData() {
		editObservation = new EditObservationData();
		editObservation.setEditObservation(new ObservationData());
	}

	public void prepareNewObservation() {
		isNew = true;
		resetObservationData();
		prepareSymptomList();
		prepareSuggestionList();
	}

	public void prepareEditObservation(ObservationData observation) {
		isNew = false;
		editObservation = new EditObservationData();
		editObservation.setEditObservation(observation);
		prepareSymptomList();
		prepareSuggestionList();
//		List<Long> symps = editObservation.getSelectedSymptoms();
	}

	private void prepareSymptomList() {
		try {
			List<SymptomData> symptoms = null;
			if (!isNew) {
				symptoms = editObservation.getEditObservation().getSymptoms();
			}
			editObservation.setSelectedSymptoms(new ArrayList<Long>());

			if (allSymptoms == null) {
				List<Symptom> symps = symptomManager.getAllSymptoms();
				if (symps != null && !symps.isEmpty()) {
					allSymptoms = new SelectItem[symps.size()];
					for (int i = 0; i < symps.size(); i++) {
						Symptom s = symps.get(i);
						SelectItem selectItem = new SelectItem(s.getId(), s.getDescription());
						allSymptoms[i] = selectItem;
						if (!isNew) {
							if (symptoms != null) {
								boolean exists = checkIfSymptomExists(s.getId(), symptoms);
								if (exists) {
									editObservation.getSelectedSymptoms().add(s.getId());
								}
							}
						}
					}
				}
			} else {
				if (!isNew) {
					for (SelectItem si : allSymptoms) {
						long symptomId = (long) si.getValue();
						if (symptoms != null) {
							boolean exists = checkIfSymptomExists(symptomId, symptoms);
							if (exists) {
								editObservation.getSelectedSymptoms().add(symptomId);
							}
						}
					}
				}
			}
		} catch (DbConnectionException e) {
			logger.error(e);
		}

	}

	private void prepareSuggestionList() {
		try {
			List<SuggestionData> suggestions = null;
			if(!isNew){
				suggestions = editObservation.getEditObservation().getSuggestions();
			}
			editObservation.setSelectedSuggestions(new ArrayList<Long>());

			if (allSuggestions == null) {
				List<Suggestion> suggs = suggestionManager.getAllSuggestions();
				if (suggs != null && !suggs.isEmpty()) {
					allSuggestions = new SelectItem[suggs.size()];
					for (int i = 0; i < suggs.size(); i++) {
						Suggestion s = suggs.get(i);
						SelectItem selectItem = new SelectItem(s.getId(), s.getDescription());
						allSuggestions[i] = selectItem;
						if (!isNew) {
							if(suggestions != null){
								boolean exists = checkIfSuggestionExists(s.getId(), suggestions);
								if (exists) {
									editObservation.getSelectedSuggestions().add(s.getId());
								}
							}
						}
					}
				}
			} else {
				if (!isNew) {
					for (SelectItem si : allSuggestions) {
						long suggestionId = (long) si.getValue();
						if(suggestions != null){
							boolean exists = checkIfSuggestionExists(suggestionId, suggestions);
							if (exists) {
								editObservation.getSelectedSuggestions().add(suggestionId);
							}
						}
					}
				}
			}
		} catch (DbConnectionException e) {
			logger.error(e);
		}

	}

	private boolean checkIfSymptomExists(long symptomId, List<SymptomData> symptoms) {
		for (SymptomData s : symptoms) {
			if (s.getId() == symptomId) {
				return true;
			}
		}
		return false;
	}

	private boolean checkIfSuggestionExists(long suggestionId, List<SuggestionData> suggestions) {
		for (SuggestionData s : suggestions) {
			if (s.getId() == suggestionId) {
				return true;
			}
		}
		return false;
	}
	
	public List<String> getFirstTwoSymptoms() {
		List<String> s = new ArrayList<>();
		List<SymptomData> symptoms = lastObservation.getSymptoms();
		for (int i  = 0; i < 2; i++) {
			if(symptoms != null && symptoms.size() != i) {
				s.add(symptoms.get(i).getDescription());
			} else {
				break;
			}
		}
		
		return s;
	}
	
	public List<String> getFirstTwoSuggestions() {
		List<String> s = new ArrayList<>();
		List<SuggestionData> suggestions = lastObservation.getSuggestions();
		for (int i  = 0; i < 2; i++) {
			if(suggestions != null && suggestions.size() != i) {
				s.add(suggestions.get(i).getDescription());
			} else {
				break;
			}
		}
		
		return s;
	}
	
//	public void resetObservationData(long targetCredentialId) {
//		editObservation = null;
//		lastObservation = null;
//		setTargetCredentialId(targetCredentialId);
//		initializeObservationData();
//	}

	public ObservationData getLastObservation() {
		return lastObservation;
	}

	public void setLastObservation(ObservationData lastObservation) {
		this.lastObservation = lastObservation;
	}

	public long getStudentId() {
		return studentId;
	}

	public void setStudentId(long studentId) {
		this.studentId = studentId;
	}

	public EditObservationData getEditObservation() {
		return editObservation;
	}

	public void setEditObservation(EditObservationData editObservation) {
		this.editObservation = editObservation;
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public SelectItem[] getAllSymptoms() {
		return allSymptoms;
	}

	public void setAllSymptoms(SelectItem[] allSymptoms) {
		this.allSymptoms = allSymptoms;
	}

	public SelectItem[] getAllSuggestions() {
		return allSuggestions;
	}

	public void setAllSuggestions(SelectItem[] allSuggestions) {
		this.allSuggestions = allSuggestions;
	}

	public String getStudentName() {
		return studentName;
	}

	public void setStudentName(String studentName) {
		this.studentName = studentName;
	}

	public List<ObservationData> getObservationHistory() {
		return observationHistory;
	}

	public void setObservationHistory(List<ObservationData> observationHistory) {
		this.observationHistory = observationHistory;
	}

//	public long getTargetCredentialId() {
//		return targetCredentialId;
//	}
//
//	public void setTargetCredentialId(long targetCredentialId) {
//		this.targetCredentialId = targetCredentialId;
//	}
	
	
}
