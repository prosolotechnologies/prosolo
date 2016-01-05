/**
 * 
 */
package org.prosolo.services.event;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

class EventThreadPoolExecutor {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(EventThreadPoolExecutor.class);

	int poolSize = 10;

	int maxPoolSize = 50;

	long keepAliveTime = 20;

	ThreadPoolExecutor threadPool = null;

	private final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(50, true);

	public EventThreadPoolExecutor() {
		threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize,
				keepAliveTime, TimeUnit.SECONDS, queue, new ThreadPoolExecutor.CallerRunsPolicy());
	}

	void runTask(Runnable task) {
		threadPool.execute(task);
	}
	
	@SuppressWarnings("unchecked")
	Future<EventObserver> submitTask(Runnable task) {
		return (Future<EventObserver>) threadPool.submit(task);
	}

}
