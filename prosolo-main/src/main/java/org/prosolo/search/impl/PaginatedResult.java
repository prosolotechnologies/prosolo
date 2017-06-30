package org.prosolo.search.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PaginatedResult<T> implements Serializable {

	private static final long serialVersionUID = -3012674403522535706L;
	
	private long hitsNumber;
	private List<T> foundNodes = new ArrayList<T>();
	private Map<String, Object> additionalInfo;

	public PaginatedResult(List<T> foundUsers, long hits) {
		foundNodes = foundUsers;
		hitsNumber = hits;
	}

	public PaginatedResult(List<T> nodes) {
		foundNodes = nodes;
	}

	public PaginatedResult() { }

	public long getHitsNumber() {
		return hitsNumber;
	}

	public void setHitsNumber(long hitsNumber) {
		this.hitsNumber = hitsNumber;
	}

	public List<T> getFoundNodes() {
		return foundNodes;
	}

	public void setFoundNodes(List<T> foundNodes) {
		this.foundNodes = foundNodes;
	}

	public void addFoundNode(T node) {
		if (!this.foundNodes.contains(node)) {
			this.foundNodes.add(node);
		}
	}

	public Map<String, Object> getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(Map<String, Object> additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

}
