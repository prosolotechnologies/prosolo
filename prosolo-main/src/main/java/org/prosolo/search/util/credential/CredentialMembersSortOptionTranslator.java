package org.prosolo.search.util.credential;

import java.util.LinkedList;
import java.util.List;

import org.elasticsearch.search.sort.SortOrder;
import org.prosolo.search.util.ESSortOption;
import org.prosolo.search.util.ESSortOrderTranslator;

@Deprecated
public class CredentialMembersSortOptionTranslator {

//	public static ESSortOption getSortOption(CredentialMembersSortOption sortOption) {
//	    ESSortOption esSortOption = new ESSortOption();
//		
//		SortOrder sortOrder = ESSortOrderTranslator.getSortOrder(sortOption.getSortOption());
//		
//		esSortOption.setSortOrder(sortOrder);
//		
//		List<String> sortFieldList = new LinkedList<>();
//		
//		if(sortOption.getSortField() == null) {
//			sortFieldList.add("name");
//			sortFieldList.add("lastname");
//		} else {
//			switch (sortOption.getSortField()) {
//			case STUDENT_NAME:
//				sortFieldList.add("name");
//				sortFieldList.add("lastname");
//				break;
//			case PROGRESS:
//				sortFieldList.add("courses.progress");
//				break;
//			case PROFILE_TYPE:
//				sortFieldList.add("courses.profile.profileType");
//				break;
//			default:
//				sortFieldList.add("name");
//				sortFieldList.add("lastname");
//				break;
//			}
//		}
//		
//		esSortOption.setFields(sortFieldList);
//		return esSortOption;
//	}
}
