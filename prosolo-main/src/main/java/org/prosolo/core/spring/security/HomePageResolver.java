package org.prosolo.core.spring.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class HomePageResolver {

    public static final int ADMIN = 1;
    public static final int MANAGE = 2;
    public static final int USER = 3;

    public String getHomeUrl(Collection<GrantedAuthority> authorities) {
        switch (getHighestPriorityCapability(authorities)) {
            case "basic.admin.access":
                return "/admin/users";
            case "basic.manager.access":
            case "basic.instructor.access":
                return "/manage";
            case "basic.user.access":
                return "/";
            default:
                return "/terms";
        }
    }

    public String getHomeUrl(long organizationId) {
        String cap = getHighestPriorityCapability(getGrantedAuthorities());
        if (cap != null) {
            switch (cap) {
                case "admin.advanced":
                    return "/admin";
                case "basic.admin.access":
                    if (organizationId != 0) {
                        String decodedId = ServiceLocator.getInstance().getService(UrlIdEncoder.class).encodeId(organizationId);
                        return "/admin/organizations/" + decodedId + "/units";
                    }
                case "basic.manager.access":
                case "basic.instructor.access":
                    return "/manage";
                case "basic.user.access":
                    return "/";
                default:
                    return "/terms";
            }
        } else {
            return "/terms";
        }
    }


    private String getHighestPriorityCapability(Collection<GrantedAuthority> authorities) {
        String current = null;
        int priority = -1;
        Iterator<GrantedAuthority> it = authorities.iterator();
        while (it.hasNext()) {
            GrantedAuthority ga = it.next();
            switch (ga.getAuthority().toLowerCase()) {
                case "admin.advanced":
                    return ga.getAuthority().toLowerCase();
                case "basic.admin.access":
                    if (isHigherPriority(current, ADMIN, priority)) {
                        current = ga.getAuthority().toLowerCase();
                        priority = ADMIN;
                    }
                    break;
                case "basic.manager.access":
                case "basic.instructor.access":
                    if (isHigherPriority(current, MANAGE, priority)) {
                        current = ga.getAuthority().toLowerCase();
                        priority = MANAGE;
                    }
                    break;
                case "basic.user.access":
                    if (isHigherPriority(current, USER, priority)) {
                        current = ga.getAuthority().toLowerCase();
                        priority = USER;
                    }
                    break;
                default:
                    continue;
            }
        }
        return current;
    }

    private boolean isHigherPriority(String authority, int priority, int currentPriority) {
        if (authority == null || currentPriority > priority) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private List<GrantedAuthority> getGrantedAuthorities() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        List<GrantedAuthority> grantedAuthorities = (List<GrantedAuthority>) authentication.getAuthorities();
        return grantedAuthorities == null ? new ArrayList<GrantedAuthority>() : grantedAuthorities;
    }
}
