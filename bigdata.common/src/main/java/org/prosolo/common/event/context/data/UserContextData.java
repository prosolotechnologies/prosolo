package org.prosolo.common.event.context.data;

/**
 * @author stefanvuckovic
 * @date 2017-08-13
 * @since 1.0.0
 */
public class UserContextData {

    private long actorId;
    private long organizationId;
    private String sessionId;
    private PageContextData context;

    private UserContextData(long actorId, long organizationId, String sessionId,
                            PageContextData context) {
        this.actorId = actorId;
        this.organizationId = organizationId;
        this.sessionId = sessionId;
        this.context = context;
    }

    public static UserContextData empty() {
        return new UserContextData(0, 0, null, null);
    }

    public static UserContextData of(long actorId, long organizationId, String sessionId,
                                     PageContextData context) {
        return new UserContextData(actorId, organizationId, sessionId, context);
    }

    public static UserContextData ofActor(long actorId) {
        return new UserContextData(actorId, 0, null, null);
    }

    public static UserContextData ofOrganization(long organizationId) {
        return new UserContextData(0, organizationId, null, null);
    }

    public static UserContextData ofLearningContext(PageContextData lcd) {
        return new UserContextData(0, 0, null, lcd);
    }

    public long getActorId() {
        return actorId;
    }

    public long getOrganizationId() {
        return organizationId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public PageContextData getContext() {
        return context;
    }
}
