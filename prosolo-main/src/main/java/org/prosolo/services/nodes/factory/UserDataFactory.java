package org.prosolo.services.nodes.factory;

import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.nodes.data.ResourceCreator;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.web.util.AvatarUtils;
import org.springframework.stereotype.Component;


/**
 * Created by stefanvuckovic on 5/12/17.
 */
@Component
public class UserDataFactory {

    public ResourceCreator getResourceCreator(User user) {
        return new ResourceCreator(user.getId(),
                getFullName(user.getName(), user.getLastname()),
                AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size120x120),
                user.getPosition());
    }

    private String getFullName(String name, String lastName) {
        return name + (lastName != null ? " " + lastName : "");
    }

    public User getUser(UserData userData, Organization organization){
        User user = new User();
        user.setId(userData.getId());
        user.setName(userData.getName());
        user.setLastname(userData.getLastName());
        user.setAvatarUrl(userData.getAvatarUrl());
        user.setOrganization(organization);
        user.setPosition(userData.getPosition());
        user.setEmail(userData.getEmail());
        user.setPassword(userData.getPassword());
        user.setLocationName(userData.getLocationName());
        user.setLatitude(userData.getLatitude());
        user.setLongitude(userData.getLongitude());
        return user;
    }

    public org.prosolo.common.web.activitywall.data.UserData getUserData(User user){
        org.prosolo.common.web.activitywall.data.UserData userData = new org.prosolo.common.web.activitywall.data.UserData();
        userData.setId(user.getId());
        userData.setAvatarUrl(user.getAvatarUrl());
        userData.setFirstName(user.getName());
        userData.setLastName(user.getLastname());
        userData.setPosition(user.getPosition());
        userData.setName(user.getFullName());
        return userData;
    }

}
