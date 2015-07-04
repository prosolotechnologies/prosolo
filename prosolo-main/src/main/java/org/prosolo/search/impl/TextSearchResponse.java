package org.prosolo.search.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.prosolo.domainmodel.general.BaseEntity;

/**
 * @author Zoran Jeremic 2013-06-30
 * 
 */
public class TextSearchResponse implements Serializable {

	private static final long serialVersionUID = -1258945002936704459L;
	
	private long hitsNumber;
	private Collection<BaseEntity> foundNodes = new LinkedList<BaseEntity>();

	public TextSearchResponse(List<BaseEntity> foundUsers, long hits) {
		foundNodes = foundUsers;
		hitsNumber = hits;
	}

	@SuppressWarnings("unchecked")
	public TextSearchResponse(List<? extends BaseEntity> nodes) {
		foundNodes = (Collection<BaseEntity>) nodes;
	}

	public TextSearchResponse() { }

	public long getHitsNumber() {
		return hitsNumber;
	}

	public void setHitsNumber(long hitsNumber) {
		this.hitsNumber = hitsNumber;
	}

	public Collection<? extends BaseEntity> getFoundNodes() {
		return foundNodes;
	}

	@SuppressWarnings("unchecked")
	public void setFoundNodes(Collection<? extends BaseEntity> foundNodes) {
		this.foundNodes = (Collection<BaseEntity>) foundNodes;
	}

	public void addFoundNode(BaseEntity node) {
		if (!this.foundNodes.contains(node)) {
			this.foundNodes.add(node);
		}
	}

}
