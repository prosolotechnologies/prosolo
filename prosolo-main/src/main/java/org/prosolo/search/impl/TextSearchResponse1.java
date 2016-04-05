package org.prosolo.search.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TextSearchResponse1<T> implements Serializable {

	private static final long serialVersionUID = -3012674403522535706L;
	
	private long hitsNumber;
	private List<T> foundNodes = new ArrayList<T>();

	public TextSearchResponse1(List<T> foundUsers, long hits) {
		foundNodes = foundUsers;
		hitsNumber = hits;
	}

	@SuppressWarnings("unchecked")
	public TextSearchResponse1(List<T> nodes) {
		foundNodes = nodes;
	}

	public TextSearchResponse1() { }

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

}
