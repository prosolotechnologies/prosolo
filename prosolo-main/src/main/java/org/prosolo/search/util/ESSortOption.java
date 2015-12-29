package org.prosolo.search.util;

import java.util.List;

import org.elasticsearch.search.sort.SortOrder;

public class ESSortOption {

	private List<String> fields;
	private SortOrder sortOrder;
	
	public ESSortOption() {
		
	}

	public ESSortOption(List<String> fields, SortOrder sortOrder) {
		this.fields = fields;
		this.sortOrder = sortOrder;
	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}

	
	
}
