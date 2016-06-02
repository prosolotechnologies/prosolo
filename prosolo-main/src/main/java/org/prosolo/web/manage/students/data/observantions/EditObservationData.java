package org.prosolo.web.manage.students.data.observantions;

import java.util.List;

public class EditObservationData {

	private ObservationData editObservation;
	private List<Long> selectedSymptoms;
	private List<Long> selectedSuggestions;
	
	public ObservationData getEditObservation() {
		return editObservation;
	}
	public void setEditObservation(ObservationData editObservation) {
		this.editObservation = editObservation;
	}
	public List<Long> getSelectedSymptoms() {
		return selectedSymptoms;
	}
	public void setSelectedSymptoms(List<Long> selectedSymptoms) {
		this.selectedSymptoms = selectedSymptoms;
	}
	public List<Long> getSelectedSuggestions() {
		return selectedSuggestions;
	}
	public void setSelectedSuggestions(List<Long> selectedSuggestions) {
		this.selectedSuggestions = selectedSuggestions;
	}
}
