package org.prosolo.common.event.context;

import lombok.Getter;
import lombok.Setter;

public class Context extends LearningContextInfo {

    @Getter @Setter
    private Context context;

    public static long getIdFromSubContextWithName(Context ctx, ContextName contextName) {
        if (ctx == null) {
            return 0;
        }
        if (ctx.getName() == contextName) {
            return ctx.getId();
        }
        return getIdFromSubContextWithName(ctx.getContext(), contextName);
    }
}
