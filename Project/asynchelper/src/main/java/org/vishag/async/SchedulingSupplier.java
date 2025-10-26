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

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * The SchedulingSupplier Helper class with methods for scheduling Suppliers and
 * obtaining their results asynchronously.
 * 
 * <br>
 * <br>
 * Note: In most of the cases default instance obtained with
 * ({@link SchedulingSupplier#getDefault()}) is sufficient, which internally
 * creates a {@link ScheduledThreadPoolExecutor} and uses it. But it is possible
 * to use {@link SchedulingSupplier#of(ScheduledExecutorService)}) or
 * {@link SchedulingSupplier#of(ScheduledExecutorService, AsyncContext)} where
 * an instance of {@code ScheduledThreadPoolExecutor} can be passed explicitly
 * if required.
 * 
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
public final class SchedulingSupplier implements AutoCloseable{

	/**
	 * {@code Logger} for this class.
	 */
	private static final Logger logger = Logger.getLogger(SchedulingSupplier.class.getName());
	
	/** The scheduler. */
	private final Scheduler scheduler;
	
	/** The closed flag. */
	private volatile boolean closed;

	/** The async context. */
	private final AsyncContext asyncContext;
	
	/** The default instance of SchedulingSupplier. */
	private static final SchedulingSupplier DEFAULT_INSTANCE = new SchedulingSupplier(Scheduler.getDefault(), AsyncContext.getDefault());
	
	/**
	 * Instantiates a new scheduling supplier.
	 *
	 * @param scheduler the scheduler
	 * @param asyncContext the async context
	 */
	private SchedulingSupplier(Scheduler scheduler, AsyncContext asyncContext) {
		this.scheduler = scheduler;
		this.asyncContext = asyncContext;
	}
	
	/**
	 * Gets the default instance of SchedulingSupplier.
	 *
	 * @return the default
	 */
	public static SchedulingSupplier getDefault() {
		return DEFAULT_INSTANCE;
	}
	
	/**
	 * Gets a new instance of SchedulingSupplier made of the given scheduled executor service.
	 *
	 * @param scheduledExecutorService the scheduled executor service
	 * @return the scheduling supplier
	 */
	public static SchedulingSupplier of(ScheduledExecutorService scheduledExecutorService) {
		return of(scheduledExecutorService, AsyncContext.getDefault());
	}
	
	/**
	 * Gets a new instance of SchedulingSupplier made of the given scheduled executor service and async context.
	 *
	 * @param scheduledExecutorService the scheduled executor service
	 * @param asyncContext the async context
	 * @return the scheduling supplier
	 */
	public static SchedulingSupplier of(ScheduledExecutorService scheduledExecutorService, AsyncContext asyncContext) {
		return new SchedulingSupplier(Scheduler.ofScheduledExecutorService(scheduledExecutorService), asyncContext);
	}

	/**
	 * Schedules multiple suppliers to be invoked sequentially (as per the
	 * <code>initialDelay</code>, <code>delay</code> and
	 * <code>waitForPreviousTask</code> arguments) and gets an array of result
	 * Suppliers handles. The result of each suppliers can be obtained by
	 * calling the {@link Supplier#get()} from the returning suppliers which
	 * will wait until the scheduled Supplier code execution completes.
	 *
	 * @param <T>
	 *            the generic type
	 * @param initialDelay
	 *            the initial delay for the first Supplier invocation
	 * @param delay
	 *            if<code>waitForPreviousTask</code> argument is
	 *            <code>true</code> this is the delay between the completion of
	 *            the predecessor supplier code execution and its succeeding
	 *            supplier code start. Otherwise, the delay will be periodic
	 *            from the start of the initial task (not related to the
	 *            completion of the suppliers' code execution).
	 * @param unit
	 *            the {@link TimeUnit} for which the <code>initialDelay</code>
	 *            and <code>delay</code> arguments are to be used.
	 * @param waitForPreviousTask
	 *            Set it to <code>true</code> argument is.... <code>true</code>
	 *            this is the delay between the completion of the predecessor
	 *            supplier code execution and its succeeding supplier code
	 *            start. Otherwise, the delay will be periodic from the start of
	 *            the initial task (not related to the completion of the
	 *            suppliers' code execution).
	 * @param suppliers
	 *            the suppliers to be scheduled sequentially
	 * @return the array of result supplier, whose result can be obtained using
	 *         {@link Supplier#get()}, which may wait until the completion of
	 *         Supplier code execution.
	 */
	public <T> Supplier<T>[] scheduleSuppliers(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, @SuppressWarnings("unchecked") Supplier<T>... suppliers) {
		return doScheduleSupplier(initialDelay, delay, unit, waitForPreviousTask, suppliers);
	}

	/**
	 * Schedules multiple suppliers to be invoked sequentially (as per the
	 * <code>initialDelay</code>, <code>delay</code> and
	 * <code>waitForPreviousTask</code> arguments), and this scheduling
	 * suppliers will be rotated until a flag is notified using
	 * {@link AsyncContext#notifyAndGetForFlag(Class, String...)} invocation with the
	 * same flag, in same thread or different thread, which will also return a
	 * stream of results.
	 *
	 * @param <T>
	 *            the generic type
	 * @param initialDelay
	 *            the initial delay for the first Supplier invocation
	 * @param delay
	 *            if<code>waitForPreviousTask</code> argument is
	 *            <code>true</code> this is the delay between the completion of
	 *            the predecessor supplier code execution and its succeeding
	 *            supplier code start. Otherwise, the delay will be periodic
	 *            from the start of the initial task (not related to the
	 *            completion of the suppliers' code execution).
	 * @param unit
	 *            the {@link TimeUnit} for which the <code>initialDelay</code>
	 *            and <code>delay</code> arguments are to be used.
	 * @param waitForPreviousTask
	 *            Set it to <code>true</code> argument is.... <code>true</code>
	 *            this is the delay between the completion of the predecessor
	 *            supplier code execution and its succeeding supplier code
	 *            start. Otherwise, the delay will be periodic from the start of
	 *            the initial task (not related to the completion of the
	 *            suppliers' code execution).
	 * @param flag
	 *            the flag with which the suppliers will be rotated for
	 *            scheduling, until notified using
	 *            {@link AsyncContext#notifyAndGetForFlag(Class, String...)}
	 * @param suppliers
	 *            the suppliers to be scheduled sequentially and rotated until
	 *            notified using
	 *            {@link AsyncContext#notifyAndGetForFlag(Class, String...)}
	 */
	public <T> void scheduleSuppliersUntilFlag(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, String flag, @SuppressWarnings("unchecked") Supplier<T>... suppliers) {
		doScheduleSupplierUntilFlag(initialDelay, delay, unit, waitForPreviousTask, suppliers, flag);
	}

	/**
	 * Schedules a single suppliers to be invoked repeatedly (as per the
	 * <code>initialDelay</code>, <code>delay</code> and
	 * <code>waitForPreviousTask</code> arguments), and this scheduling supplier
	 * will be repeatedly until a flag is notified using
	 * {@link AsyncContext#notifyAndGetForFlag(Class, String...)} invocation with the
	 * same flag, in same thread or different thread, which will also return a
	 * stream of results.
	 *
	 * @param <T>
	 *            the generic type
	 * @param initialDelay
	 *            the initial delay for the first Supplier invocation
	 * @param delay
	 *            if<code>waitForPreviousTask</code> argument is
	 *            <code>true</code> this is the delay between the completion of
	 *            the predecessor supplier code execution and its succeeding
	 *            supplier code start. Otherwise, the delay will be periodic
	 *            from the start of the initial task (not related to the
	 *            completion of the suppliers' code execution).
	 * @param unit
	 *            the {@link TimeUnit} for which the <code>initialDelay</code>
	 *            and <code>delay</code> arguments are to be used.
	 * @param waitForPreviousTask
	 *            Set it to <code>true</code> argument is.... <code>true</code>
	 *            this is the delay between the completion of the predecessor
	 *            supplier code execution and its succeeding supplier code
	 *            start. Otherwise, the delay will be periodic from the start of
	 *            the initial task (not related to the completion of the
	 *            suppliers' code execution).
	 * @param flag
	 *            the flag with which the suppliers will be rotated for
	 *            scheduling, until notified using
	 *            {@link AsyncContext#notifyAndGetForFlag(Class, String...)}
	 * @param supplier
	 *            the single supplier to be scheduled sequentially and rotated
	 *            until notified using
	 *            {@link AsyncContext#notifyAndGetForFlag(Class, String...)}
	 */
	@SuppressWarnings("unchecked")
	public <T> void scheduleSupplierUntilFlag(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, String flag, Supplier<T> supplier) {
		scheduleSuppliersUntilFlag(initialDelay, delay, unit, waitForPreviousTask, flag, supplier);
	}

	/**
	 * Schedules multiple suppliers to be invoked sequentially (as per the
	 * <code>initialDelay</code>, <code>delay</code> and
	 * <code>waitForPreviousTask</code> arguments), waits for the completion of
	 * execution of all scheduled Suppliers and gets a {@link Stream} of
	 * results.
	 *
	 * @param <T>
	 *            the generic type
	 * @param initialDelay
	 *            the initial delay for the first Supplier invocation
	 * @param delay
	 *            if<code>waitForPreviousTask</code> argument is
	 *            <code>true</code> this is the delay between the completion of
	 *            the predecessor supplier code execution and its succeeding
	 *            supplier code start. Otherwise, the delay will be periodic
	 *            from the start of the initial task (not related to the
	 *            completion of the suppliers' code execution).
	 * @param unit
	 *            the {@link TimeUnit} for which the <code>initialDelay</code>
	 *            and <code>delay</code> arguments are to be used.
	 * @param waitForPreviousTask
	 *            Set it to <code>true</code> argument is.... <code>true</code>
	 *            this is the delay between the completion of the predecessor
	 *            supplier code execution and its succeeding supplier code
	 *            start. Otherwise, the delay will be periodic from the start of
	 *            the initial task (not related to the completion of the
	 *            suppliers' code execution).
	 * @param suppliers
	 *            the suppliers to be scheduled sequentially
	 * @return the {@link Stream} of results
	 */
	public <T> Stream<T> scheduleSuppliersAndWait(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, @SuppressWarnings("unchecked") Supplier<T>... suppliers) {
		Supplier<T>[] scheduleSupplier = doScheduleSupplier(initialDelay, delay, unit, waitForPreviousTask, suppliers);
		return Stream.of(scheduleSupplier).map(Supplier::get);
	}

	/**
	 * Schedules multiple suppliers to be invoked sequentially (as per the
	 * <code>initialDelay</code>, <code>delay</code> and
	 * <code>waitForPreviousTask</code> arguments). The result can be obtained
	 * only once as a {@link Stream} by invoking
	 * {@link AsyncSupplier#waitAndGetFromSuppliers(Class, Object...)} with the
	 * same <code>keys</code> argument used in this method call, from any
	 * thread, which will wait until the supplier code execution completes. <br>
	 * <br>
	 * The submission will fail if any exception occurs during the execution of
	 * supplier or due to thread interruption, or, if any supplier is already
	 * submitted with the same keys and the result is not yet obtained using
	 * {@link AsyncSupplier#waitAndGetFromSuppliers(Class, Object...)} at-least
	 * once.
	 *
	 * @param <T>
	 *            the generic type
	 * @param initialDelay
	 *            the initial delay for the first Supplier invocation
	 * @param delay
	 *            if<code>waitForPreviousTask</code> argument is
	 *            <code>true</code> this is the delay between the completion of
	 *            the predecessor supplier code execution and its succeeding
	 *            supplier code start. Otherwise, the delay will be periodic
	 *            from the start of the initial task (not related to the
	 *            completion of the suppliers' code execution).
	 * @param unit
	 *            the {@link TimeUnit} for which the <code>initialDelay</code>
	 *            and <code>delay</code> arguments are to be used.
	 * @param waitForPreviousTask
	 *            Set it to <code>true</code> argument is.... <code>true</code>
	 *            this is the delay between the completion of the predecessor
	 *            supplier code execution and its succeeding supplier code
	 *            start. Otherwise, the delay will be periodic from the start of
	 *            the initial task (not related to the completion of the
	 *            suppliers' code execution).
	 * @param suppliers
	 *            the suppliers to be scheduled sequentially
	 * @param keys
	 *            the keys which will be used to obtain the {@link Stream} of
	 *            results by invoking the method
	 *            {@link AsyncSupplier#waitAndGetFromSuppliers(Class, Object...)}.
	 * @return true, if scheduling the supplier is successful
	 */
	public <T> boolean scheduleSuppliersForSingleAccess(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, Supplier<T>[] suppliers, Object... keys) {
		Supplier<T>[] resultSuppliers = doScheduleSupplier(initialDelay, delay, unit, waitForPreviousTask, suppliers);
		boolean result = true;
		if (resultSuppliers.length == 1) {
			Supplier<T> resSupplier = resultSuppliers[0];
			result &= getAsyncContext().storeSupplier(ObjectsKey.of(keys), resSupplier, false);
		} else {
			for (int i = 0; i < resultSuppliers.length; i++) {
				Supplier<T> resSupplier = resultSuppliers[i];
				Object[] indexedKey = AsyncContext.getIndexedKey(i, keys);
				result &= getAsyncContext().storeSupplier(ObjectsKey.of(indexedKey), resSupplier, false);
			}
		}
		return result;
	}

	/**
	 * Do schedule supplier.
	 *
	 * @param <T>
	 *            the generic type
	 * @param initialDelay
	 *            the initial delay
	 * @param delay
	 *            the delay
	 * @param unit
	 *            the unit
	 * @param waitForPreviousTask
	 *            the wait for previous task
	 * @param suppliers
	 *            the suppliers
	 * @return the supplier[]
	 */
	private <T> Supplier<T>[] doScheduleSupplier(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, @SuppressWarnings("unchecked") Supplier<T>... suppliers) {
		@SuppressWarnings("unchecked")
		Supplier<T>[] resultSuppliers = new Supplier[suppliers.length];
		Scheduler.SchedulingFunction<Supplier<T>, T> schedulingSuppliers = new Scheduler.SchedulingFunction<Supplier<T>, T>() {
			private AtomicInteger index = new AtomicInteger(0);

			@Override
			public boolean canRun() {
				return index.get() < suppliers.length;
			}

			@Override
			public boolean canCancel() {
				return index.get() == suppliers.length;
			}

			@Override
			public T invokeNextFunction() {
				return suppliers[index.getAndIncrement()].get();
			}

			@Override
			public void consumeResult(T t) {
				synchronized (resultSuppliers) {
					resultSuppliers[index.get() - 1] = () -> t;
					resultSuppliers.notifyAll();
				}
			}

		};
		getScheduler().doScheduleFunction(initialDelay, delay, unit, waitForPreviousTask, schedulingSuppliers);

		@SuppressWarnings("unchecked")
		Supplier<T>[] blockingResultSupplier = new Supplier[suppliers.length];
		for (int i = 0; i < blockingResultSupplier.length; i++) {
			final int index = i;
			blockingResultSupplier[i] = new Supplier<T>() {
				@Override
				public synchronized T get() {
					synchronized (resultSuppliers) {
						while (resultSuppliers[index] == null) {
							try {
								resultSuppliers.wait();
							} catch (InterruptedException e) {
								logger.config(() -> e.getClass().getSimpleName() + ": " + e.getMessage());
								Thread.currentThread().interrupt();
							}
						}
					}

					return resultSuppliers[index].get();
				}
			};

		}

		return blockingResultSupplier;
	}

	/**
	 * Do schedule supplier until flag.
	 *
	 * @param <T>
	 *            the generic type
	 * @param initialDelay
	 *            the initial delay
	 * @param delay
	 *            the delay
	 * @param unit
	 *            the unit
	 * @param waitForPreviousTask
	 *            the wait for previous task
	 * @param suppliers
	 *            the suppliers
	 * @param flag
	 *            the flag
	 * @return the scheduled future
	 */
	private <T> ScheduledFuture<?> doScheduleSupplierUntilFlag(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, Supplier<T>[] suppliers, String flag) {
		AtomicBoolean canCancel = new AtomicBoolean(false);
		LinkedList<Supplier<T>> resultSuppliers = new LinkedList<>();
		Scheduler.SchedulingFunction<Supplier<T>, T> schedulingSuppliers = new Scheduler.SchedulingFunction<Supplier<T>, T>() {
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
			public T invokeNextFunction() {
				if (index.get() == suppliers.length) {
					// Cycle again
					index.set(0);
				}
				return suppliers[index.getAndIncrement()].get();
			}

			@Override
			public void consumeResult(T t) {
				synchronized (resultSuppliers) {
					Supplier<T> resSupplier = () -> t;
					resultSuppliers.add(resSupplier);
					Object[] indexedKey = AsyncContext.getIndexedKey(resultSuppliers.size() - 1, flag);
					getAsyncContext().storeSupplier(ObjectsKey.of(indexedKey), resSupplier, false);
				}
			}

		};

		return getScheduler().doScheduleFunction(initialDelay, delay, unit, waitForPreviousTask, schedulingSuppliers);

	}

	/**
	 * Schedules a supplier to be invoked multiple times (as per the
	 * <code>initialDelay</code>, <code>delay</code>,
	 * <code>waitForPreviousTask</code> and <code>times</code> arguments) and
	 * gets an array of result Suppliers handles. The result of each supplier
	 * execution can be obtained by calling the {@link Supplier#get()} from the
	 * returning suppliers which will wait until the scheduled Supplier code
	 * execution completes.
	 *
	 * @param <T>
	 *            the generic type
	 * @param initialDelay
	 *            the initial delay for the first Supplier invocation
	 * @param delay
	 *            if<code>waitForPreviousTask</code> argument is
	 *            <code>true</code> this is the delay between the completion of
	 *            the predecessor supplier code execution and its succeeding
	 *            supplier code start. Otherwise, the delay will be periodic
	 *            from the start of the initial task (not related to the
	 *            completion of the suppliers' code execution).
	 * @param unit
	 *            the {@link TimeUnit} for which the <code>initialDelay</code>
	 *            and <code>delay</code> arguments are to be used.
	 * @param waitForPreviousTask
	 *            Set it to <code>true</code> argument is.... <code>true</code>
	 *            this is the delay between the completion of the predecessor
	 *            supplier code execution and its succeeding supplier code
	 *            start. Otherwise, the delay will be periodic from the start of
	 *            the initial task (not related to the completion of the
	 *            suppliers' code execution).
	 * @param supplier
	 *            the supplier to be scheduled repeatedly for the given times
	 * @param times
	 *            the number of times the scheduling should be done for the
	 *            supplier
	 * @return the array of result supplier, whose result can be obtained using
	 *         {@link Supplier#get()}, which may wait until the completion of
	 *         Supplier code execution.
	 */
	public <T> Supplier<T>[] scheduleSupplier(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, Supplier<T> supplier, int times) {
		return scheduleSuppliers(initialDelay, delay, unit, waitForPreviousTask, AsyncContext.arrayOfTimes(supplier, times));
	}

	/**
	 * Schedules a single supplier to be invoked multiple times (as per the
	 * <code>initialDelay</code>, <code>delay</code>,
	 * <code>waitForPreviousTask</code> and <code>times</code> arguments), waits
	 * for the completion of execution of all scheduled Suppliers and gets a
	 * {@link Stream} of results.
	 *
	 * @param <T>
	 *            the generic type
	 * @param initialDelay
	 *            the initial delay for the first Supplier invocation
	 * @param delay
	 *            if<code>waitForPreviousTask</code> argument is
	 *            <code>true</code> this is the delay between the completion of
	 *            the predecessor supplier code execution and its succeeding
	 *            supplier code start. Otherwise, the delay will be periodic
	 *            from the start of the initial task (not related to the
	 *            completion of the suppliers' code execution).
	 * @param unit
	 *            the {@link TimeUnit} for which the <code>initialDelay</code>
	 *            and <code>delay</code> arguments are to be used.
	 * @param waitForPreviousTask
	 *            Set it to <code>true</code> argument is.... <code>true</code>
	 *            this is the delay between the completion of the predecessor
	 *            supplier code execution and its succeeding supplier code
	 *            start. Otherwise, the delay will be periodic from the start of
	 *            the initial task (not related to the completion of the
	 *            suppliers' code execution).
	 * @param supplier
	 *            the supplier to be scheduled repeatedly
	 * @param times
	 *            the number of times the scheduling should be done for the
	 *            supplier
	 * @return the {@link Stream} of results
	 */
	public <T> Stream<T> scheduleSupplierAndWait(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, Supplier<T> supplier, int times) {
		return scheduleSuppliersAndWait(initialDelay, delay, unit, waitForPreviousTask,
				AsyncContext.arrayOfTimes(supplier, times));
	}

	/**
	 * Schedules a supplier to be invoked multiple times (as per the
	 * <code>initialDelay</code>, <code>delay</code>,
	 * <code>waitForPreviousTask</code> and <code>times</code> arguments). The
	 * result can be obtained only once as a {@link Stream} by invoking
	 * {@link AsyncSupplier#waitAndGetFromSuppliers(Class, Object...)} with the
	 * same <code>keys</code> argument used in this method call, from any
	 * thread, which will wait until the supplier code execution completes. <br>
	 * <br>
	 * The submission will fail if any exception occurs during the execution of
	 * supplier or due to thread interruption, or, if any supplier is already
	 * submitted with the same keys and the result is not yet obtained using
	 * {@link AsyncSupplier#waitAndGetFromSuppliers(Class, Object...)} at-least
	 * once.
	 *
	 * @param <T>
	 *            the generic type
	 * @param initialDelay
	 *            the initial delay for the first Supplier invocation
	 * @param delay
	 *            if<code>waitForPreviousTask</code> argument is
	 *            <code>true</code> this is the delay between the completion of
	 *            the predecessor supplier code execution and its succeeding
	 *            supplier code start. Otherwise, the delay will be periodic
	 *            from the start of the initial task (not related to the
	 *            completion of the suppliers' code execution).
	 * @param unit
	 *            the {@link TimeUnit} for which the <code>initialDelay</code>
	 *            and <code>delay</code> arguments are to be used.
	 * @param waitForPreviousTask
	 *            Set it to <code>true</code> argument is.... <code>true</code>
	 *            this is the delay between the completion of the predecessor
	 *            supplier code execution and its succeeding supplier code
	 *            start. Otherwise, the delay will be periodic from the start of
	 *            the initial task (not related to the completion of the
	 *            suppliers' code execution).
	 * @param supplier
	 *            the supplier to be scheduled repeatedly
	 * @param times
	 *            the number of times the scheduling should be done for the
	 *            supplier
	 * @param keys
	 *            the keys which will be used to obtain the {@link Stream} of
	 *            results by invoking the method
	 *            {@link AsyncSupplier#waitAndGetFromSuppliers(Class, Object...)}.
	 * @return true, if scheduling the supplier is successful
	 */
	public <T> boolean scheduleSupplierForSingleAccess(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, Supplier<T> supplier, int times, Object... keys) {
		return scheduleSuppliersForSingleAccess(initialDelay, delay, unit, waitForPreviousTask,
				AsyncContext.arrayOfTimes(supplier, times), keys);
	}

	/**
	 * Schedules a supplier to be invoked one time (as per the
	 * <code>initialDelay</code> argument) and gets an array of result Suppliers
	 * handles. The result of each supplier execution can be obtained by calling
	 * the {@link Supplier#get()} from the returning suppliers which will wait
	 * until the scheduled Supplier code execution completes.
	 *
	 * @param <T>
	 *            the generic type
	 * @param initialDelay
	 *            the initial delay for the first Supplier invocation
	 * @param unit
	 *            the {@link TimeUnit} for which the <code>initialDelay</code>
	 *            argument is used.
	 * @param supplier
	 *            the supplier to be scheduled
	 * @return the result supplier, whose result can be obtained using
	 *         {@link Supplier#get()}, which may wait until the completion of
	 *         Supplier code execution.
	 */
	public <T> Supplier<T> scheduleSupplier(int initialDelay, TimeUnit unit, Supplier<T> supplier) {
		return scheduleSupplier(initialDelay, 1, unit, false, supplier, 1)[0];
	}

	/**
	 * Schedules a supplier to be invoked one time (as per the
	 * <code>initialDelay</code> argument) . This will wait until the scheduled
	 * Supplier code execution completes, and will return the {@link Optional}
	 * of result.
	 *
	 * @param <T>
	 *            the generic type
	 * @param initialDelay
	 *            the initial delay for the first Supplier invocation
	 * @param unit
	 *            the {@link TimeUnit} for which the <code>initialDelay</code>
	 *            argument is used.
	 * @param supplier
	 *            the supplier to be scheduled
	 * @return the {@link Optional} of the result. This {@link Optional} may be
	 *         empty based on the result or if the scheduling is not successful.
	 */
	public <T> Optional<T> scheduleSupplierAndWait(int initialDelay, TimeUnit unit, Supplier<T> supplier) {
		return scheduleSupplierAndWait(initialDelay, 1, unit, false, supplier, 1).findAny();
	}

	/**
	 * Schedules a supplier to be invoked one time (as per the
	 * <code>initialDelay</code> argument). The result can be obtained only once
	 * as a {@link Stream} by invoking
	 * {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)} with the
	 * same <code>keys</code> argument used in this method call, from any
	 * thread, which will wait until the supplier code execution completes. <br>
	 * <br>
	 * The submission will fail if any exception occurs during the execution of
	 * supplier or due to thread interruption, or, if any supplier is already
	 * submitted with the same keys and the result is not yet obtained using
	 * {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)} at-least
	 * once.
	 *
	 * @param <T>
	 *            the generic type
	 * @param initialDelay
	 *            the initial delay for the first Supplier invocation
	 * @param unit
	 *            the {@link TimeUnit} for which the <code>initialDelay</code>
	 *            argument is used.
	 * @param supplier
	 *            the supplier to be scheduled
	 * @param keys
	 *            the keys which will be used to obtain the {@link Stream} of
	 *            results by invoking the method
	 *            {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)}.
	 * @return true, if scheduling the supplier is successful
	 */
	public <T> boolean scheduleSupplierForSingleAccess(int initialDelay, TimeUnit unit, Supplier<T> supplier,
			Object... keys) {
		return scheduleSupplierForSingleAccess(initialDelay, 1, unit, false, supplier, 1, keys);
	}

	/* (non-Javadoc)
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public synchronized void close() {
		if(!closed) {
			scheduler.close();
			closed = true;
		}
	}

	/**
	 * Waits and gets the result from a supplier submitted asynchronously
	 * (using
	 * {@link AsyncSupplier#submitSupplierForSingleAccess(Supplier, Object...)}) or
	 * {@link AsyncSupplier#submitSupplierForMultipleAccess(Supplier, Object...)})
	 * or
	 * {@link AsyncSupplier#submitSupplierWithDropExistingForSingleAccess(Supplier, Object...)})
	 * or
	 * {@link AsyncSupplier#submitSupplierWithDropExistingForMultipleAccess(Supplier, Object...)})
	 * as a {@link Optional} based on the keys. If no supplier is submitted already with the
	 * keys provided, the result will be an empty {@link Optional}.
	 *
	 * @param <T>
	 *            the generic type
	 * @param clazz
	 *            the clazz
	 * @param keys
	 *            the keys
	 * @return the optional
	 * 
	 */
	public <T> Optional<T> waitAndGetFromSupplier(Class<T> clazz, Object... keys) {
		return getAsyncContext().waitAndGetFromSupplier(clazz, keys);
	}

	/**
	 * Waits and gets the result from multiple suppliers submitted asynchronously
	 * (using
	 * {@link AsyncSupplier#submitSuppliersForSingleAccess(Supplier[], Object...)}) or
	 * {@link AsyncSupplier#submitSuppliersForMultipleAccess(Supplier[], Object...)})
	 * or
	 * {@link AsyncSupplier#submitSuppliersWithDropExistingForSingleAccess(Supplier[], Object...)})
	 * or
	 * {@link AsyncSupplier#submitSuppliersWithDropExistingForMultipleAccess(Supplier[], Object...)})
	 * as a {@link Stream} based on the keys. If no supplier is submitted already with the
	 * keys provided, the result will be an empty {@link Stream}.
	 *
	 * @param <T>
	 *            the generic type
	 * @param clazz
	 *            the clazz
	 * @param keys
	 *            the keys
	 * @return the stream
	 */
	public <T> Stream<T> waitAndGetFromSuppliers(Class<T> clazz, Object... keys) {
		return getAsyncContext().waitAndGetFromSuppliers(clazz, keys);
	}

	/**
	 * Waits and gets the value submitted asynchronously (using
	 * {@link AsyncSupplier#submitValue(Object, Object...)}) or
	 * {@link AsyncSupplier#submitValueWithDropExisting(Object, Object...)}) as a
	 * {@link Optional} based on the keys. If no supplier is submitted already with
	 * the keys provided, the result will be an empty {@link Optional}.
	 *
	 * @param <T>
	 *            the generic type
	 * @param clazz
	 *            the clazz
	 * @param keys
	 *            the keys
	 * @return the optional
	 */
	public <T> Optional<T> waitAndGetValue(Class<T> clazz, Object... keys) {
		return getAsyncContext().waitAndGetFromSupplier(clazz, keys);
	}

	/**
	 * Gets the async context.
	 *
	 * @return the async context
	 */
	private AsyncContext getAsyncContext() {
		return asyncContext;
	}
	
	/**
	 * Notifies the scheduler of one or more Supplier(s) which are cyclically
	 * scheduled using either
	 * {@link SchedulingSupplier#scheduleSuppliersUntilFlag(int, int, TimeUnit, boolean, String, Supplier...)}
	 * or
	 * {@link SchedulingSupplier#scheduleSupplierUntilFlag(int, int, TimeUnit, boolean, String, Supplier)}
	 * with the flag passed, and obtains the Stream of results of the type
	 * passed. <br>
	 * If no Supplier is scheduled for the flag, returns an empty stream.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param clazz
	 *            the clazz
	 * @param flag
	 *            the flag
	 * @return the list
	 */
	public <T> Stream<T> notifyAndGetForFlag(Class<T> clazz, String... flag) {
		return getAsyncContext().notifyAndGetForFlag(clazz, flag);
	}
	
	/**
	 * Schedules a supplier to run exactly N times and collects all results.
	 *
	 * @param <T>
	 *            the generic type
	 * @param times
	 *            number of times to run
	 * @param initialDelay
	 *            initial delay
	 * @param delay
	 *            delay between executions
	 * @param unit
	 *            time unit
	 * @param supplier
	 *            the supplier
	 * @return Stream of results from N executions
	 */
	public <T> Stream<T> scheduleSupplierNTimes(int times, int initialDelay, int delay, TimeUnit unit, Supplier<T> supplier) {
		String tempFlag = "temp_flag_" + System.nanoTime();
		AtomicInteger count = new AtomicInteger(0);
		
		scheduleSupplierUntilFlag(initialDelay, delay, unit, true, tempFlag, new Supplier<T>() {
			@Override
			public T get() {
				T result = supplier.get();
				if (count.incrementAndGet() >= times) {
					getAsyncContext().notifyFlag(tempFlag);
				}
				return result;
			}
		});
		
		try {
			Thread.sleep(unit.toMillis(initialDelay + (delay * times) + 100));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
		return getAsyncContext().notifyAndGetForFlag((Class<T>) Object.class, tempFlag);
	}
	
	/**
	 * Gets the scheduler.
	 *
	 * @return the scheduler
	 */
	protected Scheduler getScheduler() {
		assertNotClosed();
		return scheduler;
	}
	
	/**
	 * Assert not closed.
	 */
	private void assertNotClosed() {
		if (closed) {
			throw new IllegalStateException("Already closed");
		}
	}

}

