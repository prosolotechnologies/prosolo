package org.prosolo.services.user.data;

import lombok.Getter;
import lombok.Setter;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.web.administration.data.RoleData;
import org.prosolo.web.util.AvatarUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class UserData implements Serializable {

    private static final long serialVersionUID = 8668238017709751223L;

    private long id;
    @Getter @Setter
    private String fullName;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private String lastName;
    @Getter @Setter
    private String password;
    @Getter @Setter
    private String avatarUrl;
    @Getter @Setter
    private String position;
    @Getter @Setter
    private String email;
    @Getter @Setter
    private boolean followedByCurrentUser;
    @Getter @Setter
    private boolean userSet;
    @Getter @Setter
    private UserType type = UserType.REGULAR_USER;
    @Getter @Setter
    private List<RoleData> roles = new ArrayList<>();
    @Getter @Setter
    private List<Long> roleIds = new ArrayList<>();
    @Getter @Setter
    private ObjectStatus objectStatus;
    @Getter @Setter
    private String locationName;
    @Getter @Setter
    private Double longitude;
    @Getter @Setter
    private Double latitude;
    @Getter @Setter
    private String newPassword;
    @Getter @Setter
    private int numberOfTokens;

    public UserData() {
        this.roles = new LinkedList<>();
    }

    public UserData(User user) {
        this();
        this.id = user.getId();
        this.name = user.getName();
        this.lastName = user.getLastname();
        setFullName(user.getName(), user.getLastname());
        this.avatarUrl = AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size120x120);
        this.position = user.getPosition();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.locationName = user.getLocationName();
        this.latitude = user.getLatitude();
        this.longitude = user.getLongitude();
        this.objectStatus = ObjectStatus.UP_TO_DATE;
        this.numberOfTokens = user.getNumberOfTokens();
        for (Role r : user.getRoles()) {
            this.roleIds.add(r.getId());
        }
    }

    public UserData(User user, Collection<Role> roles) {
        this(user);

        if (roles != null) {
            for (Role role : roles) {
                this.roles.add(new RoleData(role));
            }
        }
    }

    public UserData(long id, String firstName, String lastName, String avatar, String position,
                    String email, boolean isAvatarReady) {
        this(id, getFullName(firstName, lastName), avatar, position, email, isAvatarReady);
        this.setName(firstName);
        this.setLastName(lastName);
    }

    public UserData(long id, String fullName, String avatar, String position,
                    String email, boolean isAvatarReady) {
        this.id = id;
        this.fullName = fullName;
        String readyAvatar = avatar;
        if (avatar != null && !isAvatarReady) {
            readyAvatar = AvatarUtils.getAvatarUrlInFormat(avatar, ImageFormat.size120x120);
        }
        this.avatarUrl = readyAvatar;
        this.position = position;
        this.email = email;
    }

    public UserData(long id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        userSet = true;
        this.id = id;
    }

    public boolean hasRole(long roleId) {
        return roles.stream().anyMatch(r -> r.getId() == roleId);
    }

    public void setFullName(String name, String lastName) {
        this.fullName = getFullName(name, lastName);
    }

    private static String getFullName(String name, String lastName) {
        String fName = name != null ? name + " " : "";
        String lName = lastName != null ? lastName : "";
        return fName + lName;
    }

    public String getCommaSeparatedFullName() {
        String fName = name != null ? name : "";
        String lName = lastName != null ? lastName : "";
        String infix = !fName.isEmpty() && !lName.isEmpty() ? ", " : "";
        return lName + infix + fName;
    }

    public String getRolesCSV() {
        String rolesString = "";
        if (roles != null) {
            for (RoleData rd : roles) {
                if (!rolesString.isEmpty()) {
                    rolesString += ", ";
                }
                rolesString += rd.getName();
            }
        }
        return rolesString;
    }

}