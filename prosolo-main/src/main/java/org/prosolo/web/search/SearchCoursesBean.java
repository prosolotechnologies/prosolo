package org.prosolo.web.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CreatorType;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.data.CourseData;
import org.prosolo.web.courses.util.CourseDataConverter;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.search.data.SortingOption;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "searchCoursesBean")
@Component("searchCoursesBean")
@Scope("view")
public class SearchCoursesBean implements Serializable {

	private static final long serialVersionUID = -795112624657629753L;

	private static Logger logger = Logger.getLogger(SearchCoursesBean.class);
	
	@Inject private TextSearch textSearch;
	@Inject private CourseManager courseManager;
	@Inject private LoggingNavigationBean loggingNavigationBean;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private UrlIdEncoder idEncoder;
	
	private String query;
	private CreatorType creatorType;
	private List<CourseData> courses;
	private int size;
	private int page = 0;
	private int limit = 7;
	private boolean moreToLoad;
	
	private List<Tag> filterTags = new ArrayList<Tag>();
	
	// sorting
	private SortingOption sortTitleAsc = SortingOption.ASC;
	private SortingOption sortDateAsc = SortingOption.NONE;
	
	private List<Long> personalizedCourseIds;
	
	public SearchCoursesBean() {
		courses = new ArrayList<CourseData>();
	}
	
	@PostConstruct
	public void init() {
		
	}
//	public void searchListener(ValueChangeEvent event) {
//		this.limit = 3;
//		search(event.getNewValue().toString(), null, false);
//	}
	
	public void searchAllCourses() {
		String role = PageUtil.getPostParameter("role");
		
		if(role.equals("MANAGER") && personalizedCourseIds == null) {
			boolean showAll = loggedUserBean.hasCapability("COURSE.VIEW");
			if(!showAll) {
				long personalizedForUserId = loggedUserBean.getUserId();
				List<Long> ids = textSearch.getInstructorCourseIds(personalizedForUserId);
				personalizedCourseIds = (ids == null ? new ArrayList<>() : ids);
			} else {
				personalizedCourseIds = new ArrayList<>();
			}
		}
		
		boolean published = false;
		
		if (role != null && role.equals("USER")) {
			published = true;
		}
		
		search(query, null, true, published);
	}
	
//	public void search(String searchQuery) {
//		search(searchQuery, null, false);
//	}

	public void search(String searchQuery, Collection<Course> objToExclude, boolean loadOneMore, boolean published) {
		this.courses.clear();
		this.size = 0;
		
		
		
		fetchCourses(searchQuery, creatorType, objToExclude, this.limit, loadOneMore, published);
		
		if (searchQuery != null && searchQuery.length() > 0) {
			loggingNavigationBean.logServiceUse(
					ComponentName.SEARCH_CREDENTIALS, 
					"query", searchQuery,
					"context", "plan.browse");
		}
	}
	
	public void loadMore() {
		page++;
		
		String role = PageUtil.getPostParameter("role");
		
		boolean published = false;
		
		if (role != null && role.equals("USER")) {
			published = true;
		}
		
		fetchCourses(query, creatorType, null, this.limit, true, published);
	}

	public void fetchCourses(String searchQuery, CreatorType creatorType, Collection<Course> coursesToExclude, 
			int limit, boolean loadOneMore, boolean published) {
		
		TextSearchResponse searchResponse = textSearch.searchCourses(
				searchQuery,
				creatorType,
				this.page, 
				limit,
				loadOneMore,
				coursesToExclude,
				published,
				filterTags,
				personalizedCourseIds,
				this.sortTitleAsc,
				this.sortDateAsc);
		
		@SuppressWarnings("unchecked")
		List<Course> foundCourses = (List<Course>) searchResponse.getFoundNodes();
		size = (int) searchResponse.getHitsNumber();
		// if there is more than limit, set moreToLoad to true
		if (loadOneMore && foundCourses.size() == limit+1) {
			foundCourses = foundCourses.subList(0, foundCourses.size()-1);
			moreToLoad = true;
		} else {
			moreToLoad = false;
		}

		courses.addAll(CourseDataConverter.convertToCoursesData(foundCourses));
		
		if(foundCourses != null && !foundCourses.isEmpty()) {
			Map<Long, List<Long>> counts = courseManager.getCoursesParticipants(foundCourses);
			
			if (counts != null) {
				for (CourseData courseData : this.courses) {
					List<Long> memberIds = counts.get(courseData.getId());
					
					if (memberIds != null) {
						courseData.setMemberIds(memberIds);
					}
				}
			}
		}
	}

	public boolean hasMore() {
		return size > limit + 1;
	}
	
	public void addFilterTag(Tag tag) {
		if (tag != null && !filterTags.contains(tag)) {
			filterTags.add(tag);
		}
	}
	
	public void removeFilterTag(Tag tag) {
		if (tag != null && filterTags.contains(tag)) {
			Iterator<Tag> iterator = filterTags.iterator();
			
			while (iterator.hasNext()) {
				Tag ann = (Tag) iterator.next();
				
				if (ann.equals(tag)) {
					iterator.remove();
					break;
				}
			}
		}
	}
	
	/*
	 * Sorting
	 */
	public void changeDateSorting(boolean ascending) {
		resetSorting();

		this.sortDateAsc = ascending ? SortingOption.ASC : SortingOption.DESC;
		
		searchAllCourses();
	}
	
	public void changeTitleSorting(boolean ascending) {
		resetSorting();
		
		this.sortTitleAsc = ascending ? SortingOption.ASC : SortingOption.DESC;
		
		searchAllCourses();
	}
	
	private void resetSorting() {
		this.sortTitleAsc = SortingOption.NONE;
		this.sortDateAsc = SortingOption.NONE;
	}
	
	public String createNewCourseAndNavigate(String url) {
		try {
			Course course = courseManager.createNewUntitledCourse(loggedUserBean.getUserId(), CreatorType.MANAGER);
			String encodedId = idEncoder.encodeId(course.getId());
			String redirectUrl = url + "?faces-redirect=true&id=" + encodedId;
			return redirectUrl;
		} catch(DbConnectionException e) {
			e.printStackTrace();
			logger.error(e);
			PageUtil.fireErrorMessage("Error while navigating, please try again");
		}
		return null;
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public String getQuery() {
		return query;
	}

	public List<CourseData> getCourses() {
		return courses;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
	public CreatorType getCreatorType() {
		return creatorType;
	}

	public void setCreatorType(CreatorType creatorType) {
		this.creatorType = creatorType;
	}

	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public void setPage(int page) {
		this.page = page;
	}

	public boolean isMoreToLoad() {
		return moreToLoad;
	}

	public List<Tag> getFilterTags() {
		return filterTags;
	}

	public boolean isSortDateAsc() {
		return sortDateAsc.equals(SortingOption.ASC);
	}

	public boolean isSortTitleAsc() {
		return sortTitleAsc.equals(SortingOption.ASC);
	}
	
}
