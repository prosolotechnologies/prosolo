package org.prosolo.util.nodes;

import java.util.Comparator;

import org.prosolo.common.domainmodel.general.BaseEntity;

 

public class NodeTitleComparator implements Comparator<BaseEntity> {

	@Override
	public int compare(BaseEntity entity1, BaseEntity entity2) {
		return entity1.getTitle().compareToIgnoreCase(entity2.getTitle());
	}
	
}