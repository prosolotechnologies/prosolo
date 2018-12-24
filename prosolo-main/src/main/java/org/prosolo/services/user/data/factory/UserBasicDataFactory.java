package org.prosolo.services.user.data.factory;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.user.data.UserBasicData;
import org.prosolo.web.util.AvatarUtils;
import org.springframework.stereotype.Component;

/**
 * @author stefanvuckovic
 * @date 2018-11-21
 * @since 1.2.0
 */
@Component
public class UserBasicDataFactory {

    public UserBasicData getBasicUserData(User user) {
        return new UserBasicData(user.getId(), user.getFullName(), AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size120x120));
    }
}
