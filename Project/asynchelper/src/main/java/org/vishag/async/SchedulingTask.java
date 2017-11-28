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
 * The SchedulingTask Helper class with methods for scheduling {@link Runnable}
 * to invoke them as asynchronous tasks.
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
	 * Schedules multiple tasks to be invoked sequentially (as per the
	 * <code>initialDelay</code>, <code>delay</code> and
	 * <code>waitForPreviousTask</code> arguments).
	 *
	 * @param initialDelay
	 *            the initial delay for the first task invocation
	 * @param delay
	 *            if<code>waitForPreviousTask</code> argument is
	 *            <code>true</code> this is the delay between the completion of
	 *            the predecessor task code execution and its succeeding task
	 *            code start. Otherwise, the delay will be periodic from the
	 *            start of the initial task (not related to the completion of
	 *            the tasks' code execution).
	 * @param unit
	 *            the {@link TimeUnit} for which the <code>initialDelay</code>
	 *            and <code>delay</code> arguments are to be used.
	 * @param waitForPreviousTask
	 *            Set it to <code>true</code> argument is.... <code>true</code>
	 *            this is the delay between the completion of the predecessor
	 *            task code execution and its succeeding task code start.
	 *            Otherwise, the delay will be periodic from the start of the
	 *            initial task (not related to the completion of the tasks' code
	 *            execution).
	 * @param runnables
	 *            the tasks to be scheduled sequentially
	 */
	static public void scheduleTasks(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			Runnable... runnables) {
		doScheduleTasks(initialDelay, delay, unit, waitForPreviousTask, runnables);
	}

	/**
	 * Schedules multiple tasks to be invoked sequentially (as per the
	 * <code>initialDelay</code>, <code>delay</code> and
	 * <code>waitForPreviousTask</code> arguments), and this scheduling tasks
	 * will be rotated until a flag is notified using
	 * {@link Async#notifyFlag(String...)} or
	 * {@link Async#notifyAllFlag(String...)} invocation with the same flag, in
	 * same thread or different thread.
	 *
	 * @param initialDelay
	 *            the initial delay for the first task invocation
	 * @param delay
	 *            if<code>waitForPreviousTask</code> argument is
	 *            <code>true</code> this is the delay between the completion of
	 *            the predecessor task code execution and its succeeding task
	 *            code start. Otherwise, the delay will be periodic from the
	 *            start of the initial task (not related to the completion of
	 *            the tasks' code execution).
	 * @param unit
	 *            the {@link TimeUnit} for which the <code>initialDelay</code>
	 *            and <code>delay</code> arguments are to be used.
	 * @param waitForPreviousTask
	 *            Set it to <code>true</code> argument is.... <code>true</code>
	 *            this is the delay between the completion of the predecessor
	 *            task code execution and its succeeding task code start.
	 *            Otherwise, the delay will be periodic from the start of the
	 *            initial task (not related to the completion of the tasks' code
	 *            execution).
	 * @param flag
	 *            the flag with which the tasks will be rotated for scheduling,
	 *            until notified using {@link Async#notifyFlag(String...)} or
	 *            {@link Async#notifyAllFlag(String...)}
	 * @param runnables
	 *            the tasks to be scheduled sequentially and rotated until
	 *            notified using {@link Async#notifyFlag(String...)} or
	 *            {@link Async#notifyAllFlag(String...)}
	 */
	static public void scheduleTasksUntilFlag(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			String flag, Runnable... runnables) {
		doScheduleTasksUntilFlag(initialDelay, delay, unit, waitForPreviousTask, runnables, flag);
	}

	/**
	 * Schedules a single task to be invoked repeatedly (as per the
	 * <code>initialDelay</code>, <code>delay</code> and
	 * <code>waitForPreviousTask</code> arguments), and this scheduling tasks
	 * will be repeated until a flag is notified using
	 * {@link Async#notifyFlag(String...)} or
	 * {@link Async#notifyAllFlag(String...)} invocation with the same flag, in
	 * same thread or different thread.
	 *
	 * @param initialDelay
	 *            the initial delay for the first task invocation
	 * @param delay
	 *            if<code>waitForPreviousTask</code> argument is
	 *            <code>true</code> this is the delay between the completion of
	 *            the predecessor task code execution and its succeeding task
	 *            code start. Otherwise, the delay will be periodic from the
	 *            start of the initial task (not related to the completion of
	 *            the tasks' code execution).
	 * @param unit
	 *            the {@link TimeUnit} for which the <code>initialDelay</code>
	 *            and <code>delay</code> arguments are to be used.
	 * @param waitForPreviousTask
	 *            Set it to <code>true</code> argument is.... <code>true</code>
	 *            this is the delay between the completion of the predecessor
	 *            task code execution and its succeeding task code start.
	 *            Otherwise, the delay will be periodic from the start of the
	 *            initial task (not related to the completion of the tasks' code
	 *            execution).
	 * @param flag
	 *            the flag with which the tasks will be rotated for scheduling,
	 *            until notified using {@link Async#notifyFlag(String...)} or
	 *            {@link Async#notifyAllFlag(String...)}
	 * @param runnable
	 *            the task to be scheduled repeatedly until notified using
	 *            {@link Async#notifyFlag(String...)} or
	 *            {@link Async#notifyAllFlag(String...)}
	 */
	static public void scheduleTaskUntilFlag(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			String flag, Runnable runnable) {
		scheduleTasksUntilFlag(initialDelay, delay, unit, waitForPreviousTask, flag, runnable);
	}

	/**
	 * Schedules multiple tasks to be invoked sequentially (as per the
	 * <code>initialDelay</code>, <code>delay</code> and
	 * <code>waitForPreviousTask</code> arguments), and this scheduling tasks
	 * will be rotated until a flag is notified using
	 * {@link Async#notifyFlag(String...)} or
	 * {@link Async#notifyAllFlag(String...)} invocation with the same flag, in
	 * same thread or different thread. This will wait until the completion of
	 * the execution of the scheduled tasks code.
	 *
	 * @param initialDelay
	 *            the initial delay for the first task invocation
	 * @param delay
	 *            if<code>waitForPreviousTask</code> argument is
	 *            <code>true</code> this is the delay between the completion of
	 *            the predecessor task code execution and its succeeding task
	 *            code start. Otherwise, the delay will be periodic from the
	 *            start of the initial task (not related to the completion of
	 *            the tasks' code execution).
	 * @param unit
	 *            the {@link TimeUnit} for which the <code>initialDelay</code>
	 *            and <code>delay</code> arguments are to be used.
	 * @param waitForPreviousTask
	 *            Set it to <code>true</code> argument is.... <code>true</code>
	 *            this is the delay between the completion of the predecessor
	 *            task code execution and its succeeding task code start.
	 *            Otherwise, the delay will be periodic from the start of the
	 *            initial task (not related to the completion of the tasks' code
	 *            execution).
	 * @param runnables
	 *            the tasks to be scheduled sequentially
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
	 * Schedules a single task to be invoked multiple times (as per the
	 * <code>initialDelay</code>, <code>delay</code>,
	 * <code>waitForPreviousTask</code> and <code>times</code> arguments) and
	 * gets an array of result tasks handles.
	 *
	 * @param initialDelay
	 *            the initial delay for the first task invocation
	 * @param delay
	 *            if<code>waitForPreviousTask</code> argument is
	 *            <code>true</code> this is the delay between the completion of
	 *            the predecessor task code execution and its succeeding task
	 *            code start. Otherwise, the delay will be periodic from the
	 *            start of the initial task (not related to the completion of
	 *            the tasks' code execution).
	 * @param unit
	 *            the {@link TimeUnit} for which the <code>initialDelay</code>
	 *            and <code>delay</code> arguments are to be used.
	 * @param waitForPreviousTask
	 *            Set it to <code>true</code> argument is.... <code>true</code>
	 *            this is the delay between the completion of the predecessor
	 *            task code execution and its succeeding task code start.
	 *            Otherwise, the delay will be periodic from the start of the
	 *            initial task (not related to the completion of the tasks' code
	 *            execution).
	 * @param times
	 *            the number of times the scheduling should be done for the task
	 * @param runnable
	 *            the task to be scheduled
	 */
	static public void scheduleTask(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			Runnable runnable, int times) {
		scheduleTasks(initialDelay, delay, unit, waitForPreviousTask, Async.arrayOfTimes(runnable, times));
	}

	/**
	 * Schedules a single task to be invoked multiple times (as per the
	 * <code>initialDelay</code>, <code>delay</code>,
	 * <code>waitForPreviousTask</code> and <code>times</code> arguments) and
	 * gets an array of result tasks handles. This will wait until the
	 * completion of the execution of the scheduled tasks code.
	 *
	 * @param initialDelay
	 *            the initial delay for the first task invocation
	 * @param delay
	 *            if<code>waitForPreviousTask</code> argument is
	 *            <code>true</code> this is the delay between the completion of
	 *            the predecessor task code execution and its succeeding task
	 *            code start. Otherwise, the delay will be periodic from the
	 *            start of the initial task (not related to the completion of
	 *            the tasks' code execution).
	 * @param unit
	 *            the {@link TimeUnit} for which the <code>initialDelay</code>
	 *            and <code>delay</code> arguments are to be used.
	 * @param waitForPreviousTask
	 *            Set it to <code>true</code> argument is.... <code>true</code>
	 *            this is the delay between the completion of the predecessor
	 *            task code execution and its succeeding task code start.
	 *            Otherwise, the delay will be periodic from the start of the
	 *            initial task (not related to the completion of the tasks' code
	 *            execution).
	 * @param times
	 *            the number of times the scheduling should be done for the task
	 * @param runnable
	 *            the task to be scheduled
	 */
	static public void scheduleTaskAndWait(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			Runnable runnable, int times) {
		scheduleTasksAndWait(initialDelay, delay, unit, waitForPreviousTask, Async.arrayOfTimes(runnable, times));
	}

	/**
	 * Schedules a single task to be invoked one time (as per the
	 * <code>initialDelay</code> argument).
	 *
	 * @param initialDelay
	 *            the initial delay for the first task invocation
	 * @param unit
	 *            the {@link TimeUnit} for which the <code>initialDelay</code>
	 *            argument is used.
	 * @param runnable
	 *            the task to be scheduled
	 */
	static public void scheduleTask(int initialDelay, TimeUnit unit, Runnable runnable) {
		scheduleTask(initialDelay, 1, unit, false, runnable, 1);
	}

	/**
	 * Schedules a single task to be invoked one time (as per the
	 * <code>initialDelay</code> argument). This will wait until the completion
	 * of the execution of the scheduled task code.
	 *
	 * @param initialDelay
	 *            the initial delay for the first task invocation
	 * @param unit
	 *            the {@link TimeUnit} for which the <code>initialDelay</code>
	 *            argument is used.
	 * @param runnable
	 *            the task to be scheduled
	 */
	static public void scheduleTaskAndWait(int initialDelay, TimeUnit unit, Runnable runnable) {
		scheduleTaskAndWait(initialDelay, 1, unit, false, runnable, 1);
	}

}
