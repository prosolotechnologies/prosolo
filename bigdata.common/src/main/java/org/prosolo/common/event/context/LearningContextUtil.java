package org.prosolo.common.event.context;

/**
 * @author stefanvuckovic
 * @date 2017-08-02
 * @since 0.7
 */
public class LearningContextUtil {

    public static long getIdFromContext(Context context, ContextName ctx) {
        if (context == null) {
            return 0;
        }
        if (context.getName() == ctx) {
            return context.getId();
        }
        return getIdFromContext(context.getContext(), ctx);
    }
}
