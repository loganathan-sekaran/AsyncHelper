/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at

 *   http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.vishag.async;

import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The Scheduler class that comprises of an executor service and methods used to
 * submit tasks and suppliers.
 * 
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
class Scheduler implements AutoCloseable {

	/** The default scheduled executor service. */
	private static final ScheduledExecutorService DEFAULT_SCHEDULED_EXECUTOR_SERVICE = Executors
			.newScheduledThreadPool(ForkJoinPool.getCommonPoolParallelism());
	
	/** The default instance of Scheduler. */
	private static final Scheduler DEFAULT_INSTANCE = new Scheduler(DEFAULT_SCHEDULED_EXECUTOR_SERVICE);

	/** The scheduled executor service. */
	private final ScheduledExecutorService scheduledExecutorService;

	/** The closed. */
	private volatile boolean closed;
	
	/**
	 * The Interface SchedulingFunction. This is used internally to schedule
	 * task(s) and Supplier(s).
	 *
	 * @param <T>
	 *            the generic type
	 * @param <R>
	 *            the generic type
	 */
	protected interface SchedulingFunction<T, R> {
	
		/**
		 * Can run.
		 *
		 * @return true, if successful
		 */
		boolean canRun();
	
		/**
		 * Can cancel.
		 *
		 * @return true, if successful
		 */
		boolean canCancel();
	
		/**
		 * Invoke next function.
		 *
		 * @return the r
		 */
		R invokeNextFunction();
	
		/**
		 * Consume result.
		 *
		 * @param r
		 *            the r
		 */
		void consumeResult(R r);
	}
	
	/**
	 * Instantiates a new scheduler.
	 *
	 * @param scheduledExecutorService the scheduled executor service
	 */
	private Scheduler(ScheduledExecutorService scheduledExecutorService) {
		this.scheduledExecutorService = scheduledExecutorService;
	}
	
	/**
	 * Gets the default instance of scheduler.
	 *
	 * @return the default
	 */
	protected static Scheduler getDefault() {
		return DEFAULT_INSTANCE;
	}

	/**
	 * Do schedule function.
	 *
	 * @param <T>
	 *            the generic type
	 * @param <R>
	 *            the generic type
	 * @param initialDelay
	 *            the initial delay
	 * @param delay
	 *            the delay
	 * @param unit
	 *            the unit
	 * @param waitForPreviousFunction
	 *            the wait for previous function
	 * @param schedulingFunction
	 *            the scheduling function
	 * @return the scheduled future
	 */
	protected <T, R> ScheduledFuture<?> doScheduleFunction(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousFunction, Scheduler.SchedulingFunction<T, R> schedulingFunction) {
		final ScheduledFuture<?>[] scheduleFuture = new ScheduledFuture<?>[1];
		Runnable seq = () -> {
			synchronized (schedulingFunction) {
				if (schedulingFunction.canRun()) {
					R res = schedulingFunction.invokeNextFunction();
					schedulingFunction.consumeResult(res);
					if (schedulingFunction.canCancel() && scheduleFuture[0] != null) {
						scheduleFuture[0].cancel(true);
					}
				}
			}
		};
	
		if (waitForPreviousFunction) {
			scheduleFuture[0] = scheduledExecutorService.scheduleWithFixedDelay(seq, initialDelay, delay, unit);
		} else {
			scheduleFuture[0] = scheduledExecutorService.scheduleAtFixedRate(seq, initialDelay, delay, unit);
		}
	
		return scheduleFuture[0];
	}

	/* (non-Javadoc)
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public synchronized void close() {
		if(!closed) {
			scheduledExecutorService.shutdownNow();
			closed = true;
		}
	}

	/**
	 * Of scheduled executor service.
	 *
	 * @param scheduledExecutorService the scheduled executor service
	 * @return the scheduler
	 */
	public static Scheduler ofScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
		return new Scheduler(scheduledExecutorService);
	}

}
