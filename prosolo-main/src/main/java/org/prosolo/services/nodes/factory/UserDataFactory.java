package org.prosolo.services.nodes.factory;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.nodes.data.ResourceCreator;
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
}
