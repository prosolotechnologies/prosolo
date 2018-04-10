package org.prosolo.services.nodes.data;

/**
 * @author stefanvuckovic
 * @date 2017-12-12
 * @since 1.2.0
 */
public class BasicObjectInfo {

    private final long id;
    private final String title;
    private final String description;

    public BasicObjectInfo(long id, String title) {
        this.id = id;
        this.title = title;
        this.description = null;
    }

    public BasicObjectInfo(long id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
