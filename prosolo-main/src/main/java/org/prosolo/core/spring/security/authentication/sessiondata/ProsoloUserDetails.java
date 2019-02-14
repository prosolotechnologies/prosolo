package org.prosolo.core.spring.security.authentication.sessiondata;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Locale;

/**
 * @author stefanvuckovic
 * @date 2018-10-29
 * @since 1.2.0
 */
public class ProsoloUserDetails extends User {

    private static final long serialVersionUID = 1535776685165795835L;

    private final long userId;
    private final long organizationId;
    private final String name;
    private final String lastName;
    private final String avatar;
    private final String position;
    private final String ipAddress;
    private final String sessionId;
    private final Locale locale;

    private ProsoloUserDetails(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities, long userId, long organizationId, String name, String lastName, String avatar, String position, String ipAddress, String sessionId, Locale locale) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.userId = userId;
        this.organizationId = organizationId;
        this.name = name;
        this.lastName = lastName;
        this.avatar = avatar;
        this.position = position;
        this.ipAddress = ipAddress;
        this.sessionId = sessionId;
        this.locale = locale;
    }

    public static Builder builder() {
        return new Builder();
    }

    public long getUserId() {
        return userId;
    }

    public String getEmail() {
        return getUsername();
    }

    public String getName() {
        return name;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPosition() {
        return position;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getOrganizationId() {
        return organizationId;
    }

    public Locale getLocale() {
        return locale;
    }

    public static class Builder {

        private long userId;
        private long organizationId;
        private String name;
        private String lastName;
        private String email;
        private String password;
        private String avatar;
        private String position;
        private String ipAddress;
        private String sessionId;
        private Locale locale;
        private Collection<? extends GrantedAuthority> authorities;
        private boolean enabled;
        private boolean accountNonExpired;
        private boolean credentialsNonExpired;
        private boolean accountNonLocked;

        private Builder() {}

        public Builder userId(long userId) {
            this.userId = userId;
            return this;
        }

        public Builder organizationId(long organizationId) {
            this.organizationId = organizationId;
            return this;
        }

        public Builder firstName(String firstName) {
            this.name = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public Builder position(String position) {
            this.position = position;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder locale(Locale locale) {
            this.locale = locale;
            return this;
        }

        public Builder authorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities = authorities;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder accountNonExpired(boolean accountNonExpired) {
            this.accountNonExpired = accountNonExpired;
            return this;
        }

        public Builder credentialsNonExpired(boolean credentialsNonExpired) {
            this.credentialsNonExpired = credentialsNonExpired;
            return this;
        }

        public Builder accountNonLocked(boolean accountNonLocked) {
            this.accountNonLocked = accountNonLocked;
            return this;
        }

        public ProsoloUserDetails create() {
            return new ProsoloUserDetails(email, password, enabled, accountNonExpired, credentialsNonExpired,
                    accountNonLocked, authorities, userId, organizationId, name, lastName, avatar, position, ipAddress,
                    sessionId, locale);
        }
    }

}
