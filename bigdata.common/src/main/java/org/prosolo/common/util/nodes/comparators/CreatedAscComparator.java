package org.prosolo.common.util.nodes.comparators;

import java.util.Comparator;

import org.prosolo.common.domainmodel.general.BaseEntity;

public class CreatedAscComparator implements Comparator<BaseEntity> {

	@Override
	public int compare(BaseEntity ent1, BaseEntity ent2) {
		if (ent1.getDateCreated() != null && ent2.getDateCreated() != null) {
			if (ent1.getDateCreated().before(ent2.getDateCreated())) {
				return -1;
			} else {
				return 1;
			}
		}
		return ent1.getTitle().compareToIgnoreCase(ent2.getTitle());
	}
	
}