package org.prosolo.search.util.credential;

/**
 * @author stefanvuckovic
 * @date 2018-10-05
 * @since 1.2.0
 */
public class CredentialStudentsInstructorFilter {

    private long instructorId;
    private String label;
    private SearchFilter filter;

    public CredentialStudentsInstructorFilter(long instructorId, String label, SearchFilter filter) {
        this.instructorId = instructorId;
        this.label = label;
        this.filter = filter;
    }

    public long getInstructorId() {
        return instructorId;
    }

    public String getLabel() {
        return label;
    }

    public SearchFilter getFilter() {
        return filter;
    }

    public enum SearchFilter {
        ALL,
        NO_INSTRUCTOR,
        INSTRUCTOR
    }
}
