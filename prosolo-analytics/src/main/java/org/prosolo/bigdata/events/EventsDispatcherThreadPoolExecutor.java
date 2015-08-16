package org.prosolo.bigdata.events;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

/**
 * @author Zoran Jeremic Apr 5, 2015
 *
 */

public class EventsDispatcherThreadPoolExecutor {
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private ThreadPoolExecutor executorService;

	public static class EventsDispatcherThreadPoolExecutorHolder {
		public static final EventsDispatcherThreadPoolExecutor INSTANCE = new EventsDispatcherThreadPoolExecutor();
	}

	public static EventsDispatcherThreadPoolExecutor getInstance() {
		return EventsDispatcherThreadPoolExecutorHolder.INSTANCE;
	}

	/* Custom methods */
	private EventsDispatcherThreadPoolExecutor() {
		if (this.executorService == null) {
			this.executorService = (ThreadPoolExecutor) Executors
					.newCachedThreadPool();
		}
	}

	public void shutDown() {
		this.executorService.shutdown();
		this.executorService = null;
	}

	public void submit(Runnable runnable) {
		this.executorService.submit(runnable);
	}
}
