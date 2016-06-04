package org.prosolo.web.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.event.RowEditEvent;
import org.prosolo.common.domainmodel.observations.Suggestion;
import org.prosolo.common.domainmodel.observations.Symptom;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.studentProfile.observations.SuggestionManager;
import org.prosolo.services.studentProfile.observations.SymptomManager;
import org.prosolo.web.manage.students.data.observantions.SuggestionData;
import org.prosolo.web.manage.students.data.observantions.SymptomData;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="analyticsSettingsBean")
@Component("analyticsSettingsBean")
@Scope("view")
public class AnalyticsSettingsBean implements Serializable {

	private static final long serialVersionUID = -747140356305370777L;
	
	protected static Logger logger = Logger.getLogger(AnalyticsSettingsBean.class);
	
	@Inject
	private SymptomManager symptomManager;
	@Inject
	private SuggestionManager suggestionManager;
	
	private List<SymptomData> symptoms;
	private List<SuggestionData> suggestions;
	
	private SymptomData newSymptom;
	private SymptomData symptomForEdit;
	private SuggestionData newSuggestion;
	private SuggestionData suggestionForEdit;
	
	public void init() {
		try {
			newSymptom = new SymptomData();
			newSuggestion = new SuggestionData();
			loadSymptoms();
			loadSuggestions();
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	public void loadSymptoms() throws DbConnectionException {
		try {
			List<Symptom> symptomList = symptomManager.getAllSymptoms();
			symptoms = new ArrayList<>();
			
			for (Symptom s : symptomList) {
				symptoms.add(new SymptomData(s));
			}
		} catch (DbConnectionException e) {
			throw e;
		}
	}
	
	public void loadSuggestions() throws DbConnectionException {
		try {
			List<Suggestion> suggestionList = suggestionManager.getAllSuggestions();
			suggestions = new ArrayList<>();
			
			for (Suggestion s : suggestionList) {
				suggestions.add(new SuggestionData(s));
			}
		} catch (DbConnectionException e) {
			throw e;
		}
	}
	
	public void prepareSymptomEdit(SymptomData symptom) {
		symptomForEdit = symptom;
	}
	
	public void prepareSuggestionEdit(SuggestionData suggestion){
		suggestionForEdit = suggestion;
	}
	
	public void saveSymptom() {
		try {
			saveSymptom(newSymptom.getId(), newSymptom.getDescription());
			newSymptom = new SymptomData();
		}catch(Exception e){
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	public void updateSymptom() {
		try {
			saveSymptom(symptomForEdit.getId(), symptomForEdit.getDescription());
			PageUtil.fireSuccessfulInfoMessage("Symptom updated");
		} catch (Exception e) {
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	public void saveSymptom(long id, String descripton) {
		try {
			symptomManager.saveSymptom(id, descripton);
			loadSymptoms();
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	public void saveSuggestion() {
		try {
			saveSuggestion(newSuggestion.getId(), newSuggestion.getDescription());
			newSuggestion = new SuggestionData();
		}catch(Exception e){
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	public void updateSuggestion() {
		try {
			saveSuggestion(suggestionForEdit.getId(), suggestionForEdit.getDescription());
			PageUtil.fireSuccessfulInfoMessage("Suggestion updated");
		}catch(Exception e){
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	public void saveSuggestion(long id, String descripton) {
		try{
			suggestionManager.saveSuggestion(id, descripton);
			loadSuggestions();
		}catch(DbConnectionException e){
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	public void deleteSymptom() {
		try{
			symptomManager.deleteSymptom(symptomForEdit.getId());
			symptomForEdit = null;
			loadSymptoms();
			PageUtil.fireSuccessfulInfoMessage("Symptom deleted");
		}catch(DbConnectionException e){
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	public void deleteSuggestion() {
		try{
			suggestionManager.deleteSuggestion(suggestionForEdit.getId());
			suggestionForEdit = null;
			loadSuggestions();
			PageUtil.fireSuccessfulInfoMessage("Suggestion deleted");
		}catch(DbConnectionException e){
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	public void validateNewSymptomName(FacesContext context, UIComponent validate, Object value) {
		validateSymptomName(value, true);
	}
	
	public void validateExistingSymptomName(FacesContext context, UIComponent validate, Object value) {
		validateSymptomName(value, false);	
	}
	
	public void validateSymptomName(Object value, boolean isNew) throws DbConnectionException {
		try {
			boolean valid = false;
			List<Long> ids = null;
			
			if (value != null) {
				ids = symptomManager.getSymptomIdsWithName(value.toString());
				if (ids.isEmpty() || (!isNew && ids.size() == 1 && ids.get(0) == symptomForEdit.getId())) {
					valid = true;
				}
			}

			if (!valid) {
				FacesMessage message = new FacesMessage("The name: '" + value.toString() + "' is taken!");
				throw new ValidatorException(message);
			}
			// return valid;
		} catch (DbConnectionException e) {
			FacesMessage message = new FacesMessage(e.getMessage());
			throw new ValidatorException(message);
		}
	}
	
	public void validateNewSuggestionName(FacesContext context, UIComponent validate, Object value) {
		validateSuggestionName(value, true);
	}
	
	public void validateExistingSuggestionName(FacesContext context, UIComponent validate, Object value) {
		validateSuggestionName(value, false);
	}
	
	public void validateSuggestionName(Object value, boolean isNew) {
		try {
			boolean valid = false;
			List<Long> ids = null;
			if (value != null) {
				ids = suggestionManager.getSuggestionIdsWithName(value.toString());
				if (ids.isEmpty() || (!isNew && ids.size() == 1 && ids.get(0) == suggestionForEdit.getId())) {
					valid = true;
				}
			}

			if (!valid) {
				FacesMessage message = new FacesMessage("The name: '" + value.toString() + "' is taken!");
				throw new ValidatorException(message);
			}
		} catch (DbConnectionException e) {
			FacesMessage message = new FacesMessage(e.getMessage());
			throw new ValidatorException(message);
		}
	}
	
	public boolean isSymptomUsed(long id) {
		try {
			return symptomManager.isSymptomUsed(id);
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage(e.getMessage());
			return true;
		}
	}

	public boolean isSuggestionUsed(long id) {
		try {
			return suggestionManager.isSuggestionUsed(id);
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage(e.getMessage());
			return true;
		}
	}

//	public void prepareSymptomEdit(RowEditEvent event) {
//		SymptomData sd = (SymptomData) event.getObject();
//		prepareSymptomEdit(sd);
//	}

	public void onSymptomEdit(RowEditEvent event) {
		SymptomData sd = (SymptomData) event.getObject();
		try {
			saveSymptom(sd.getId(), sd.getDescription());
			PageUtil.fireSuccessfulInfoMessage("Symptom successfully updated");
		} catch (DbConnectionException e) {
			sd.setDescription(symptomForEdit.getDescription());
			PageUtil.fireErrorMessage("Error while updating symptom");
		}
		setSymptomForEdit(null);
	}

//	public void prepareSuggestionEdit(RowEditEvent event) {
//		SuggestionData sd = (SuggestionData) event.getObject();
//		prepareSuggestionEdit(sd);
//	}

	public void onSuggestionEdit(RowEditEvent event) {
		SuggestionData sd = (SuggestionData) event.getObject();
		try {
			saveSuggestion(sd.getId(), sd.getDescription());
			PageUtil.fireSuccessfulInfoMessage("Suggestion successfully updated");
		} catch (DbConnectionException e) {
			sd.setDescription(suggestionForEdit.getDescription());
			PageUtil.fireErrorMessage("Error while updating suggestion");
		}
		setSuggestionForEdit(null);
	}
	 
	
	//GETTERS AND SETTERS
	
	public List<SymptomData> getSymptoms() {
		return symptoms;
	}

	public void setSymptoms(List<SymptomData> symptoms) {
		this.symptoms = symptoms;
	}

	public List<SuggestionData> getSuggestions() {
		return suggestions;
	}

	public void setSuggestions(List<SuggestionData> suggestions) {
		this.suggestions = suggestions;
	}

	public SymptomData getNewSymptom() {
		return newSymptom;
	}

	public void setNewSymptom(SymptomData newSymptom) {
		this.newSymptom = newSymptom;
	}

	public SymptomData getSymptomForEdit() {
		return symptomForEdit;
	}

	public void setSymptomForEdit(SymptomData symptomForEdit) {
		this.symptomForEdit = symptomForEdit;
	}

	public SuggestionData getNewSuggestion() {
		return newSuggestion;
	}

	public void setNewSuggestion(SuggestionData newSuggestion) {
		this.newSuggestion = newSuggestion;
	}

	public SuggestionData getSuggestionForEdit() {
		return suggestionForEdit;
	}

	public void setSuggestionForEdit(SuggestionData suggestionForEdit) {
		this.suggestionForEdit = suggestionForEdit;
	}
	
}
