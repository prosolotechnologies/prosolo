package org.prosolo.search.util;

import org.elasticsearch.search.sort.SortOrder;
import org.prosolo.search.data.SortingOption;

public class ESSortOrderTranslator {
	
	public static SortOrder getSortOrder(SortingOption sortingOption) {
		SortOrder sortOrder = null;
		if(sortingOption == null) {
			sortOrder = SortOrder.ASC;
		} else {
			switch (sortingOption) {
			case ASC:
				sortOrder = SortOrder.ASC;
				break;
			case DESC:
				sortOrder = SortOrder.DESC;
				break;
			default:
				sortOrder = SortOrder.ASC;
				break;
			}
		}
		return sortOrder;
	}
	
}
