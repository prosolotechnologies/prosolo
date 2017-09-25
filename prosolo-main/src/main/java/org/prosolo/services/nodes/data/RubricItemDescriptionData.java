package org.prosolo.services.nodes.data;

import org.prosolo.services.common.observable.StandardObservable;

import java.io.Serializable;

public class RubricItemDescriptionData extends StandardObservable implements Serializable {

	private static final long serialVersionUID = -8714207904456117949L;

	private String description;

	public RubricItemDescriptionData(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		observeAttributeChange("description", this.description, description);
		this.description = description;
	}

}
