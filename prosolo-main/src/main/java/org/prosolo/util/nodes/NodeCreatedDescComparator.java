package org.prosolo.util.nodes;

import java.util.Comparator;

import org.prosolo.common.domainmodel.general.BaseEntity;

 

public class NodeCreatedDescComparator implements Comparator<BaseEntity> {

	@Override
	public int compare(BaseEntity node1, BaseEntity node2) {
		if (node1.getDateCreated() != null && node2.getDateCreated() != null) {
			if (node1.getDateCreated().after(node2.getDateCreated())) {
				return -1;
			} else {
				return 1;
			}
		}
		return node1.getTitle().compareToIgnoreCase(node2.getTitle());
	}
	
}