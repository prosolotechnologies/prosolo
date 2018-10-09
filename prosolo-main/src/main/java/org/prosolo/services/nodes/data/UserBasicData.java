package org.prosolo.services.nodes.data;

/**
 * @author stefanvuckovic
 * @date 2018-10-08
 * @since 1.2.0
 */
public class UserBasicData {

    private final long id;
    private final String fullName;

    public UserBasicData(long id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public long getId() {
        return id;
    }
}
