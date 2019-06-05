package org.prosolo.web.reports;

import org.prosolo.common.util.string.StringUtil;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.user.data.UserData;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Zoran Jeremic Feb 1, 2014
 */
@Deprecated
public class LogsFilterBean implements Serializable {

    private static final long serialVersionUID = -5452146059271122076L;

    @Inject
    private UserTextSearch userTextSearch;
    //@Autowired private LoggingDBManager loggingDBManager;

    private List<UserData> usersList;
    private String searchText;
    private boolean twitterPosts;
    private List<UserData> userSearchResults;
    private List<String> eventTypes;
    private List<String> selectedEventTypes;
    private List<String> objectTypes;
    private List<String> selectedObjectTypes;
    private Date startDate;
    private Date endDate;

    @PostConstruct
    public void init() {
        System.out.println("Logs filter bean...init");
        usersList = new ArrayList<UserData>();
        userSearchResults = new ArrayList<UserData>();
        //setEventTypes(loggingDBManager.getAllDistinctValuesOfEventField("eventType"));
        //setObjectTypes(loggingDBManager.getAllDistinctValuesOfEventField("objectType"));
        selectedEventTypes = new ArrayList<String>();
        selectedObjectTypes = new ArrayList<String>();
        startDate = null;
        endDate = null;
    }

    public void executeTextSearch(String toExcludeString) {
        long[] moreToExclude = StringUtil.fromStringToLong(toExcludeString);

        List<Long> totalListToExclude = new ArrayList<Long>();

        if (moreToExclude != null) {
            for (long l : moreToExclude) {
                totalListToExclude.add(l);
            }
        }

        userSearchResults.clear();
        PaginatedResult<UserData> usersResponse = userTextSearch.searchUsers(0, searchText, 0, 4, false, null, totalListToExclude);

        userSearchResults = usersResponse.getFoundNodes();
    }

    public void addUser(UserData userData) {
        userSearchResults.clear();
        searchText = "";

        if (!usersList.contains(userData)) {
            usersList.add(userData);
        }
    }

    public void removeUser(UserData userData) {
        if (usersList.contains(userData)) {
            usersList.remove(userData);
        }
    }

    public String getToExcludeIds() {
        return usersList.stream().map(u -> u.getId()).collect(Collectors.toList()).toString();
    }

    /*
     * GETTERS / SETTERS
     */

    public List<UserData> getUsersList() {
        return usersList;
    }

    public void setUsersList(List<UserData> usersList) {
        this.usersList = usersList;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public List<UserData> getUserSearchResults() {
        return userSearchResults;
    }

    public void setUserSearchResults(List<UserData> userSearchResults) {
        this.userSearchResults = userSearchResults;
    }

    public boolean isTwitterPosts() {
        return twitterPosts;
    }

    public void setTwitterPosts(boolean twitterPosts) {
        this.twitterPosts = twitterPosts;
    }

    public List<String> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(List<String> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public List<String> getSelectedEventTypes() {
        return selectedEventTypes;
    }

    public void setSelectedEventTypes(List<String> selectedEventTypes) {
        this.selectedEventTypes = selectedEventTypes;
    }

    public List<String> getObjectTypes() {
        return objectTypes;
    }

    public void setObjectTypes(List<String> objectTypes) {
        this.objectTypes = objectTypes;
    }

    public List<String> getSelectedObjectTypes() {
        return selectedObjectTypes;
    }

    public void setSelectedObjectTypes(List<String> selectedObjectTypes) {
        this.selectedObjectTypes = selectedObjectTypes;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

}
