package org.prosolo.services.nodes.data;

/**
 * @author stefanvuckovic
 * @date 2017-12-12
 * @since 1.2.0
 */
public class BasicObjectInfo {

    private long id;
    private String title;

    public BasicObjectInfo(long id, String title) {
        this.id = id;
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
