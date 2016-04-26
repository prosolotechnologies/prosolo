package org.prosolo.search.util.course;

import java.util.LinkedList;
import java.util.List;

import org.elasticsearch.search.sort.SortOrder;
import org.prosolo.search.util.ESSortOption;
import org.prosolo.search.util.ESSortOrderTranslator;

public class CourseMembersSortOptionTranslator {

	public static ESSortOption getSortOption(CourseMembersSortOption sortOption) {
	    ESSortOption esSortOption = new ESSortOption();
		
		SortOrder sortOrder = ESSortOrderTranslator.getSortOrder(sortOption.getSortOption());
		
		esSortOption.setSortOrder(sortOrder);
		
		List<String> sortFieldList = new LinkedList<>();
		
		if(sortOption.getSortField() == null) {
			sortFieldList.add("name");
			sortFieldList.add("lastname");
		} else {
			switch (sortOption.getSortField()) {
			case STUDENT_NAME:
				sortFieldList.add("name");
				sortFieldList.add("lastname");
				break;
			case PROGRESS:
				sortFieldList.add("courses.progress");
				break;
			case PROFILE_TYPE:
				sortFieldList.add("courses.profile.profileType");
				break;
			default:
				sortFieldList.add("name");
				sortFieldList.add("lastname");
				break;
			}
		}
		
		esSortOption.setFields(sortFieldList);
		return esSortOption;
	}
}