package org.prosolo.search.util;

import java.util.LinkedList;
import java.util.List;

import org.elasticsearch.search.sort.SortOrder;

public class CourseMembersSortOptionTranslator {

	public static ESSortOption getSortOption(CourseMembersSortOption sortOption) {
	    ESSortOption esSortOption = new ESSortOption();
		
		SortOrder sortOrder = null;
		if(sortOption.getSortOption() == null) {
			sortOrder = SortOrder.ASC;
		} else {
			switch (sortOption.getSortOption()) {
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
			case INSTRUCTOR_NAME:
				sortFieldList.add("courses.instructor.first_name");
				sortFieldList.add("courses.instructor.last_name");
				break;
			case PROGRESS:
				sortFieldList.add("courses.progress");
				break;
			case PROFILE_TYPE:
				sortFieldList.add("profile_type");
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
