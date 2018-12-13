package org.prosolo.services.user.data;

/**
 * @author stefanvuckovic
 * @date 2018-10-08
 * @since 1.2.0
 */
public class UserBasicData {

    private final long id;
    private final String fullName;
    private final String avatar;

    public UserBasicData(long id, String fullName) {
        this.id = id;
        this.fullName = fullName;
        this.avatar = null;
    }

    public UserBasicData(long id, String fullName, String avatar) {
        this.id = id;
        this.fullName = fullName;
        this.avatar = avatar;
    }

    public String getFullName() {
        return fullName;
    }

    public long getId() {
        return id;
    }

    public String getAvatar() {
        return avatar;
    }
}
