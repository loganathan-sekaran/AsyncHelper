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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * The class SchedulingTask.
 * 
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
public final class SchedulingSupplier {

	/**
	 * {@code Logger} for this class.
	 */
	private static final Logger logger = Logger.getLogger(SchedulingSupplier.class.getName());

	/**
	 * SchedulingTask multiple suppliers.
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
	static public <T> Supplier<T>[] scheduleSuppliers(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, @SuppressWarnings("unchecked") Supplier<T>... suppliers) {
		return doScheduleSupplier(initialDelay, delay, unit, waitForPreviousTask, suppliers);
	}

	/**
	 * SchedulingTask multiple suppliers until flag.
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
	 * @param flag
	 *            the flag
	 * @param suppliers
	 *            the suppliers
	 */
	@SafeVarargs
	static public <T> void scheduleSuppliersUntilFlag(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, String flag, Supplier<T>... suppliers) {
		doScheduleSupplierUntilFlag(initialDelay, delay, unit, waitForPreviousTask, suppliers, flag);
	}

	/**
	 * SchedulingTask supplier until flag.
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
	 * @param flag
	 *            the flag
	 * @param supplier
	 *            the supplier
	 */
	static public <T> void scheduleSupplierUntilFlag(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, String flag, Supplier<T> supplier) {
		scheduleSuppliersUntilFlag(initialDelay, delay, unit, waitForPreviousTask, flag, supplier);
	}

	/**
	 * SchedulingTask multiple suppliers and wait. This will submit the task, waits until it finishes and
	 * then returns the Optional of result.
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
	 * @return the stream
	 */
	static public <T> Stream<T> scheduleSuppliersAndWait(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, @SuppressWarnings("unchecked") Supplier<T>... suppliers) {
		Supplier<T>[] scheduleSupplier = doScheduleSupplier(initialDelay, delay, unit, waitForPreviousTask, suppliers);
		return Stream.of(scheduleSupplier).map(Supplier::get);
	}

	/**
	 * SchedulingTask multiple suppliers for single access.
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
	 * @param keys
	 *            the keys
	 * @return true, if successful
	 */
	static public <T> boolean scheduleSuppliersForSingleAccess(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, Supplier<T>[] suppliers, Object... keys) {
		Supplier<T>[] resultSuppliers = doScheduleSupplier(initialDelay, delay, unit, waitForPreviousTask, suppliers);
		boolean result = true;
		if (resultSuppliers.length == 1) {
			Supplier<T> resSupplier = resultSuppliers[0];
			result &= Async.storeSupplier(ObjectsKey.of(keys), resSupplier, false);
		} else {
			for (int i = 0; i < resultSuppliers.length; i++) {
				Supplier<T> resSupplier = resultSuppliers[i];
				Object[] indexedKey = Async.getIndexedKey(i, keys);
				result &= Async.storeSupplier(ObjectsKey.of(indexedKey), resSupplier, false);
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
	static private <T> Supplier<T>[] doScheduleSupplier(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, @SuppressWarnings("unchecked") Supplier<T>... suppliers) {
		@SuppressWarnings("unchecked")
		Supplier<T>[] resultSuppliers = new Supplier[suppliers.length];
		Async.SchedulingFunction<Supplier<T>, T> schedulingSuppliers = new Async.SchedulingFunction<Supplier<T>, T>() {
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
		Async.doScheduleFunction(initialDelay, delay, unit, waitForPreviousTask, schedulingSuppliers);

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
								logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
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
	static <T> ScheduledFuture<?> doScheduleSupplierUntilFlag(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, Supplier<T>[] suppliers, String flag) {
		AtomicBoolean canCancel = new AtomicBoolean(false);
		LinkedList<Supplier<T>> resultSuppliers = new LinkedList<Supplier<T>>();
		Async.SchedulingFunction<Supplier<T>, T> schedulingSuppliers = new Async.SchedulingFunction<Supplier<T>, T>() {
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
					Object[] indexedKey = Async.getIndexedKey(resultSuppliers.size() - 1, flag);
					Async.storeSupplier(ObjectsKey.of(indexedKey), resSupplier, false);
				}
			}

		};

		return Async.doScheduleFunction(initialDelay, delay, unit, waitForPreviousTask, schedulingSuppliers);

	}

	/**
	 * SchedulingTask supplier.
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
	 * @param supplier
	 *            the supplier
	 * @param times
	 *            the times
	 * @return the supplier[]
	 */
	static public <T> Supplier<T>[] scheduleSupplier(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, Supplier<T> supplier, int times) {
		return scheduleSuppliers(initialDelay, delay, unit, waitForPreviousTask, Async.arrayOfTimes(supplier, times));
	}

	/**
	 * SchedulingTask supplier and wait.
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
	 * @param supplier
	 *            the supplier
	 * @param times
	 *            the times
	 * @return the stream
	 */
	static public <T> Stream<T> scheduleSupplierAndWait(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, Supplier<T> supplier, int times) {
		return scheduleSuppliersAndWait(initialDelay, delay, unit, waitForPreviousTask,
				Async.arrayOfTimes(supplier, times));
	}

	/**
	 * SchedulingTask supplier for single access.
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
	 * @param supplier
	 *            the supplier
	 * @param times
	 *            the times
	 * @param keys
	 *            the keys
	 * @return true, if successful
	 */
	static public <T> boolean scheduleSupplierForSingleAccess(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousTask, Supplier<T> supplier, int times, Object... keys) {
		return scheduleSuppliersForSingleAccess(initialDelay, delay, unit, waitForPreviousTask,
				Async.arrayOfTimes(supplier, times), keys);
	}

	/**
	 * SchedulingTask supplier.
	 *
	 * @param <T>
	 *            the generic type
	 * @param initialDelay
	 *            the initial delay
	 * @param unit
	 *            the unit
	 * @param supplier
	 *            the supplier
	 * @return the supplier[]
	 */
	static public <T> Supplier<T> scheduleSupplier(int initialDelay, TimeUnit unit, Supplier<T> supplier) {
		return scheduleSupplier(initialDelay, 1, unit, false, supplier, 1)[0];
	}

	/**
	 * SchedulingTask supplier and wait.
	 *
	 * @param <T>
	 *            the generic type
	 * @param initialDelay
	 *            the initial delay
	 * @param unit
	 *            the unit
	 * @param supplier
	 *            the supplier
	 * @return the stream
	 */
	static public <T> Optional<T> scheduleSupplierAndWait(int initialDelay, TimeUnit unit, Supplier<T> supplier) {
		return scheduleSupplierAndWait(initialDelay, 1, unit, false, supplier, 1).findAny();
	}

	/**
	 * SchedulingTask supplier for single access.
	 *
	 * @param <T>
	 *            the generic type
	 * @param initialDelay
	 *            the initial delay
	 * @param unit
	 *            the unit
	 * @param supplier
	 *            the supplier
	 * @param keys
	 *            the keys
	 * @return true, if successful
	 */
	static public <T> boolean scheduleSupplierForSingleAccess(int initialDelay, TimeUnit unit, Supplier<T> supplier,
			Object... keys) {
		return scheduleSupplierForSingleAccess(initialDelay, 1, unit, false, supplier, 1, keys);
	}

}
