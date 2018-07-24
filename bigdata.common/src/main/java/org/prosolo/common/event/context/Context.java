package org.prosolo.common.event.context;

public class Context extends LearningContextInfo {
	
	private Context context;
	
	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public static long getIdFromSubContextWithName(Context ctx, ContextName contextName) {
		if(ctx == null) {
			return 0;
		}
		if(ctx.getName() == contextName) {
			return ctx.getId();
		}
		return getIdFromSubContextWithName(ctx.getContext(), contextName);
	}
}
