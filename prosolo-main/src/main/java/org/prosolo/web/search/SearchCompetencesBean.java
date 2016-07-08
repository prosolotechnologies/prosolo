package org.prosolo.web.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.component.UIInput;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.course.CreatorType;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.search.data.CompetenceData;
import org.prosolo.web.search.data.SortingOption;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "searchCompetencesBean")
@Component("searchCompetencesBean")
@Scope("view")
public class SearchCompetencesBean implements Serializable {

	private static final long serialVersionUID = 7425398428736202443L;
	
	private static Logger logger = Logger.getLogger(SearchCompetencesBean.class);
	
	@Inject private LoggedUserBean loggedUser;
	@Inject private TextSearch textSearch;
	@Inject private CompetenceManager competenceManager;
	@Inject private LoggingNavigationBean loggingNavigationBean;
	@Inject private UrlIdEncoder idEncoder;
	
	private String query;
	private List<CompetenceData> competences;
	private List<CompetenceData> pageCompetences;

	private int compsSize;
	private int page = 0;
	private int navPage = 0;
	private int limit = 5;
	private boolean moreToLoad;
	
	private long[] toExclude;
	
	// sorting
	private SortingOption sortTitleAsc = SortingOption.ASC;
	
//	private Collection<Competence> competencesToExclude = new ArrayList<Competence>();
	private List<Tag> filterTags = new ArrayList<Tag>();
	 
	public SearchCompetencesBean() {
		competences = new ArrayList<CompetenceData>();
		pageCompetences = new ArrayList<CompetenceData>();
	}
	
	public void executeSearch(String toExcludeString, String context){
		this.toExclude = StringUtil.fromStringToLong(toExcludeString);
		this.limit = 3;
		this.page = 0;
		this.navPage = 0;
	 	searchCompetences(this.query, false, context);
	}
	
	@Deprecated
	public void searchCompetencesListener(ValueChangeEvent event) {
		String toExcludeString = (String) ((UIInput) event.getSource()).getAttributes().get("toExclude");
		this.toExclude = StringUtil.fromStringToLong(toExcludeString);
		this.limit = 3;
		this.page=0;
		this.navPage = 0;
	 	searchCompetences(event.getNewValue().toString(), false, "searchCompetencesListener");
	}
	
	public void searchAllCompetences() {
		if (this.query == null) {
			setQuery("");
		}
		searchCompetences(query, true, "search.allCompetences");
	}
	
	public void searchCompetences(String searchQuery) {
		searchCompetences(searchQuery, false, "search.competences");
	}
	
	public void searchCompetences(String searchQuery, 
			boolean loadOneMore,
			String context) {
		this.competences.clear();
		this.compsSize = 0;
		
 		fetchCompetences(searchQuery != null ? searchQuery : "", this.toExclude, this.limit, loadOneMore);
		
 		if (searchQuery != null && searchQuery.length() > 0) {
			loggingNavigationBean.logServiceUse(
				ComponentName.SEARCH_COMPETENCES, 
				"query", searchQuery,
				"context", context);
 		}
	}
	
	
	public void loadMoreCompetences() {
		page++;
		fetchCompetences(query, toExclude, this.limit, true);
	}

	public void fetchCompetences(String searchQuery, long[] compsToExclude, int limit, 
			boolean loadOneMore) {
		//loadOneMore=false;
	 	TextSearchResponse searchResponse = textSearch.searchCompetences(
				searchQuery,
				this.page, 
				limit,
				loadOneMore,
				compsToExclude,
				filterTags,
				this.sortTitleAsc);
		
		@SuppressWarnings("unchecked")
		List<Competence> foundCompetences = (List<Competence>) searchResponse.getFoundNodes();
		compsSize = (int) searchResponse.getHitsNumber();
		// if there is more than limit, set moreToLoad to true
		if (loadOneMore && foundCompetences.size() == limit+1) {
			foundCompetences = foundCompetences.subList(0, foundCompetences.size()-1);
			moreToLoad = true;
		} else {
			moreToLoad = false;
		}
		
		competences.addAll(SearchCompetencesBean.convertToCompetenceData(foundCompetences));
	}

	public boolean hasMoreCompetences() {
		return compsSize > (navPage + 1) * limit;
	}

	public boolean hasPrevCompetences() {
		if (this.navPage > 0) {
			return true;
		} else
			return false;
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
	
	public String createNewCompetenceAndNavigate(String origin) {
		try {
			Competence competence = competenceManager.createNewUntitledCompetence(loggedUser.getUserId(), CreatorType.MANAGER);
			String encodedId = idEncoder.encodeId(competence.getId());
			String redirectUrl = "/manage/competence-overall?faces-redirect=true&compId=" + encodedId + "&origin=" + origin;
			return redirectUrl;
		} catch(DbConnectionException e) {
			e.printStackTrace();
			logger.error(e);
			PageUtil.fireErrorMessage("Error while navigating, please try again");
		}
		return null;
	}
//	
//	public void addCompetenceToExclude(Competence competence){
//		if (!competencesToExclude.contains(competence))
//			competencesToExclude.add(competence);
//	}
	
	/*
	 * Utility
	 */
	public static List<CompetenceData> convertToCompetenceData(List<Competence> comps) {
		List<CompetenceData> compsData = new ArrayList<CompetenceData>();
		if (comps != null && !comps.isEmpty()) {
			for (Competence comp : comps) {
				CompetenceData competenceData = new CompetenceData(comp);
				compsData.add(competenceData);
			}
		}
		return compsData;
	}
	
	public String getPageProgressTitle(){
		int first = navPage * limit + 1;
		int last = first + limit - 1;
		
		if (last > compsSize) {
			last = compsSize;
		}
		String pageProgressTitle = first + "-" + last + " of " + compsSize;
		return pageProgressTitle;
	}

	public void nextPage(){
		navPage++;
		initPageToShow();
	}

	public void prevPage(){
		navPage--;
		initPageToShow();
	}
	
	public void showAllCompetences() {
  	if (this.query == null) {
			setQuery("");
		 }
		this.limit = 5;
		this.page = 0;
		this.navPage = 0;
		searchCompetences(query, true, "showAllCompetences");
		initPageToShow();
	}
	
	private void initPageToShow() {
		pageCompetences.clear();
		int startInd = navPage * limit;
		int endInd = startInd + limit;
		if (endInd > compsSize) {
			endInd = compsSize;
		}
		
		if ((competences.size() < endInd + 1) && (endInd < compsSize)) {
			this.loadMoreCompetences();
		}
		for (int i = startInd; i < endInd; i++) {
			CompetenceData compData = competences.get(i);
			pageCompetences.add(compData);
		}
	}
	
	/*
	 * Sorting
	 */
	public void changeTitleSorting(boolean ascending) {
		resetSorting();
		
		this.sortTitleAsc = ascending ? SortingOption.ASC : SortingOption.DESC;
		
		searchAllCompetences();
	}
	
	private void resetSorting() {
		this.sortTitleAsc = SortingOption.NONE;
	}

	/*
	 * GETTERS / SETTERS
	 */
	public String getQuery() {
		return query;
	}

	public List<CompetenceData> getCompetences() {
		return competences;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public int getCompsSize() {
		return compsSize;
	}
	
	public void setCompsSize(int compsSize) {
		this.compsSize = compsSize;
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

//	public Collection<Competence> getCompetencesToExclude() {
//		return competencesToExclude;
//	}
	
	public List<CompetenceData> getPageCompetences() {
		return pageCompetences;
	}

	public void setPageCompetences(List<CompetenceData> pageCompetences) {
		this.pageCompetences = pageCompetences;
	}
	
	public boolean isSortTitleAsc() {
		return sortTitleAsc.equals(SortingOption.ASC);
	}
	
}
