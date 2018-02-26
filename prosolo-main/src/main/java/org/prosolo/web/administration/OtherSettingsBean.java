package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.observations.Suggestion;
import org.prosolo.common.domainmodel.observations.Symptom;
import org.prosolo.services.migration.CommonCustomMigrationService;
import org.prosolo.services.migration.DemoCustomMigrationService;
import org.prosolo.services.migration.UTACustomMigrationService;
import org.prosolo.services.studentProfile.observations.SuggestionManager;
import org.prosolo.services.studentProfile.observations.SymptomManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.manage.students.data.observantions.SuggestionData;
import org.prosolo.web.manage.students.data.observantions.SymptomData;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name="otherSettingsBean")
@Component("otherSettingsBean")
@Scope("view")
public class OtherSettingsBean implements Serializable {

	private static final long serialVersionUID = -747140356305370777L;
	
	protected static Logger logger = Logger.getLogger(OtherSettingsBean.class);
	
	@Inject
	private SymptomManager symptomManager;
	@Inject
	private SuggestionManager suggestionManager;
	@Inject
	private UTACustomMigrationService utaCustomMigrationService;
	@Inject
	private LoggedUserBean loggedUser;
	@Inject
	private DemoCustomMigrationService demoCustomMigrationService;
	@Inject
	private CommonCustomMigrationService commonCustomMigrationService;
	
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
		this.symptomForEdit = symptom;
	}
	
	public SymptomData getSymptom(long id){
		for(SymptomData sd : symptoms){
			if(sd.getId()==id){
				return sd;
			}
		}
		return null;
	}
	
	public void prepareSuggestionEdit(SuggestionData suggestion){
		this.suggestionForEdit = suggestion;
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
			PageUtil.fireSuccessfulInfoMessage("The symptom has been updated");
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
			PageUtil.fireSuccessfulInfoMessage("The suggestion has been updated");
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
			PageUtil.fireSuccessfulInfoMessage("The symptom has been deleted");
		}catch(DbConnectionException e){
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	public void deleteSuggestion() {
		try{
			suggestionManager.deleteSuggestion(suggestionForEdit.getId());
			suggestionForEdit = null;
			loadSuggestions();
			PageUtil.fireSuccessfulInfoMessage("The suggestion has been deleted");
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
				PageUtil.fireErrorMessage("Name already exist!");
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

	public void migrateUtaCredentialsTo07() {
		utaCustomMigrationService.migrateCredentialsFrom06To07();
	}

	public void migrateDemoServer() {
		demoCustomMigrationService.migrateDataFrom06To11();
	}

	public void migrateAssessments() {
		try {
			commonCustomMigrationService.migrateAssessments();
			PageUtil.fireSuccessfulInfoMessageAcrossPages("Assessments migrated");
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error migrating assessments");
		}
	}

	public void migrateAssessmentDiscussions() {
		try {
			commonCustomMigrationService.migrateAssessmentDiscussions();
			PageUtil.fireSuccessfulInfoMessageAcrossPages("Assessment discussions migrated");
		} catch (Exception e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error migrating assessment discussions");
		}
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
