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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * The class SchedulingTask.
 * 
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
public final class SchedulingTask {

	/**
	 * {@code Logger} for this class.
	 */
	private static final Logger logger = Logger.getLogger(SchedulingTask.class.getName());

	/**
	 * Instantiates a new schedule.
	 */
	private SchedulingTask() {
	}

	/**
	 * SchedulingTask tasks.
	 *
	 * @param initialDelay
	 *            the initial delay
	 * @param delay
	 *            the delay
	 * @param unit
	 *            the unit
	 * @param waitForPreviousTask
	 *            the wait for previous task
	 * @param runnables
	 *            the runnables
	 */
	static public void scheduleTasks(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			Runnable... runnables) {
		doScheduleTasks(initialDelay, delay, unit, waitForPreviousTask, runnables);
	}

	/**
	 * SchedulingTask tasks until flag. This will schedule the tasks, waits until the flag is notified using 
	 * {@link Async#notifyFlag(String...)} or {@link Async#notifyAllFlag(String...)}
	 *
	 * @param initialDelay
	 *            the initial delay
	 * @param delay
	 *            the delay
	 * @param unit
	 *            the unit
	 * @param waitForPreviousTask
	 *            the wait for previous task
	 * @param flag
	 *            the flag
	 * @param runnables
	 *            the runnables
	 */
	static public void scheduleTasksUntilFlag(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			String flag, Runnable... runnables) {
		doScheduleTasksUntilFlag(initialDelay, delay, unit, waitForPreviousTask, runnables, flag);
	}

	/**
	 * SchedulingTask task until flag. This will schedule the task, waits until the flag is notified using 
	 * {@link Async#notifyFlag(String...)} or {@link Async#notifyAllFlag(String...)}
	 *
	 * @param initialDelay
	 *            the initial delay
	 * @param delay
	 *            the delay
	 * @param unit
	 *            the unit
	 * @param waitForPreviousTask
	 *            the wait for previous task
	 * @param flag
	 *            the flag
	 * @param runnable
	 *            the runnable
	 */
	static public void scheduleTaskUntilFlag(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			String flag, Runnable runnable) {
		scheduleTasksUntilFlag(initialDelay, delay, unit, waitForPreviousTask, flag, runnable);
	}

	/**
	 * SchedulingTask tasks and wait. This will schedule the tasks, waits until it finishes.
	 *
	 * @param initialDelay
	 *            the initial delay
	 * @param delay
	 *            the delay
	 * @param unit
	 *            the unit
	 * @param waitForPreviousTask
	 *            the wait for previous task
	 * @param runnables
	 *            the runnables
	 */
	static public void scheduleTasksAndWait(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			Runnable... runnables) {
		try {
			doScheduleTasks(initialDelay, delay, unit, waitForPreviousTask, runnables).get();
		} catch (InterruptedException | ExecutionException | CancellationException e) {
			logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	/**
	 * Do schedule tasks.
	 *
	 * @param initialDelay
	 *            the initial delay
	 * @param delay
	 *            the delay
	 * @param unit
	 *            the unit
	 * @param waitForPreviousTask
	 *            the wait for previous task
	 * @param runnables
	 *            the runnables
	 * @return the scheduled future
	 */
	static private ScheduledFuture<?> doScheduleTasks(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, Runnable... runnables) {
		Async.SchedulingFunction<Runnable, Void> schedulingRunnables = new Async.SchedulingFunction<Runnable, Void>() {
			private AtomicInteger index = new AtomicInteger(0);

			@Override
			public boolean canRun() {
				return index.get() < runnables.length;
			}

			@Override
			public boolean canCancel() {
				return index.get() == runnables.length;
			}

			@Override
			public Void invokeNextFunction() {
				runnables[index.getAndIncrement()].run();
				return null;
			}

			@Override
			public void consumeResult(Void v) {
				// Does nothing
			}

		};
		return Async.doScheduleFunction(initialDelay, delay, unit, waitForPreviousTask, schedulingRunnables);
	}

	/**
	 * Do schedule tasks until flag.
	 *
	 * @param initialDelay
	 *            the initial delay
	 * @param delay
	 *            the delay
	 * @param unit
	 *            the unit
	 * @param waitForPreviousTask
	 *            the wait for previous task
	 * @param runnables
	 *            the runnables
	 * @param flag
	 *            the flag
	 * @return the scheduled future
	 */
	private static ScheduledFuture<?> doScheduleTasksUntilFlag(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, Runnable[] runnables, String flag) {
		AtomicBoolean canCancel = new AtomicBoolean(false);
		Async.SchedulingFunction<Runnable, Void> schedulingRunnables = new Async.SchedulingFunction<Runnable, Void>() {
			private AtomicInteger index = new AtomicInteger(0);

			@Override
			public boolean canRun() {
				return !canCancel.get();
			}

			@Override
			public boolean canCancel() {
				return canCancel.get();
			}

			@Override
			public Void invokeNextFunction() {
				if (index.get() == runnables.length) {
					// Cycle again
					index.set(0);
				}
				runnables[index.getAndIncrement()].run();
				return null;
			}

			@Override
			public void consumeResult(Void v) {
				// Does nothing
			}

		};

		AsyncTask.submitTask(() -> {
			try {
				Async.waitForFlag(flag);
			} catch (InterruptedException e) {
				logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
			canCancel.set(true);
		});

		return Async.doScheduleFunction(initialDelay, delay, unit, waitForPreviousTask, schedulingRunnables);
	}

	/**
	 * SchedulingTask task.
	 *
	 * @param initialDelay
	 *            the initial delay
	 * @param delay
	 *            the delay
	 * @param unit
	 *            the unit
	 * @param waitForPreviousTask
	 *            the wait for previous task
	 * @param runnable
	 *            the runnable
	 * @param times
	 *            the times
	 */
	static public void scheduleTask(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			Runnable runnable, int times) {
		scheduleTasks(initialDelay, delay, unit, waitForPreviousTask, Async.arrayOfTimes(runnable, times));
	}

	/**
	 * SchedulingTask task and wait. This will schedule the task, waits until it finishes.
	 *
	 * @param initialDelay
	 *            the initial delay
	 * @param delay
	 *            the delay
	 * @param unit
	 *            the unit
	 * @param waitForPreviousTask
	 *            the wait for previous task
	 * @param runnable
	 *            the runnable
	 * @param times
	 *            the times
	 */
	static public void scheduleTaskAndWait(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			Runnable runnable, int times) {
		scheduleTasksAndWait(initialDelay, delay, unit, waitForPreviousTask, Async.arrayOfTimes(runnable, times));
	}

	/**
	 * SchedulingTask task.
	 *
	 * @param initialDelay
	 *            the initial delay
	 * @param unit
	 *            the unit
	 * @param runnable
	 *            the runnable
	 */
	static public void scheduleTask(int initialDelay, TimeUnit unit, Runnable runnable) {
		scheduleTask(initialDelay, 1, unit, false, runnable, 1);
	}

	/**
	 * SchedulingTask task and wait.
	 *
	 * @param initialDelay
	 *            the initial delay
	 * @param unit
	 *            the unit
	 * @param runnable
	 *            the runnable
	 */
	static public void scheduleTaskAndWait(int initialDelay, TimeUnit unit, Runnable runnable) {
		scheduleTaskAndWait(initialDelay, 1, unit, false, runnable, 1);
	}

}
