package org.prosolo.services.nodes.data;

import org.prosolo.common.domainmodel.user.User;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2017-08-08
 * @since 0.7
 */
public class UserCreationData implements Serializable {

    private static final long serialVersionUID = 4552700742197806011L;

    private User user;
    private boolean isNewAccount;

    public UserCreationData(User user, boolean isNewAccount) {
        this.user = user;
        this.isNewAccount = isNewAccount;
    }

    public User getUser() {
        return user;
    }

    public boolean isNewAccount() {
        return isNewAccount;
    }
}
