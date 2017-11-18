
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

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * The Async Helper method. This contains utility methods to wait for a flag in
 * one thread and notify the flag in another thread.
 * 
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
public final class Async {

	/**
	 * The Interface SchedulingFunction.
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

	/** The fork join pool. */
	protected static ForkJoinPool forkJoinPool;

	static {
		forkJoinPool = ForkJoinPool.commonPool();
		if (ForkJoinPool.getCommonPoolParallelism() == 1) {
			forkJoinPool = new ForkJoinPool(5);
		} else {
			forkJoinPool = ForkJoinPool.commonPool();
		}
	}

	/** The future suppliers. */
	protected static Map<ObjectsKey, Supplier<? extends Object>> futureSuppliers = new ConcurrentHashMap<>();

	/** The original keys. */
	protected static Map<ObjectsKey, ObjectsKey> originalKeys = new ConcurrentHashMap<>();

	/** The multiple accessed keys. */
	protected static Map<ObjectsKey, ObjectsKey> multipleAccessedKeys = new ConcurrentHashMap<>();

	/** The multiple accessed values. */
	protected static Map<ObjectsKey, Object> multipleAccessedValues = new ConcurrentHashMap<>();

	/** The scheduled executor service. */
	static private ScheduledExecutorService scheduledExecutorService = Executors
			.newScheduledThreadPool(ForkJoinPool.getCommonPoolParallelism());

	/**
	 * Instantiates a new async.
	 */
	private Async() {
	}

	/**
	 * Gets the fork join pool.
	 *
	 * @return the fork join pool
	 */
	protected static ForkJoinPool getForkJoinPool() {
		return forkJoinPool;
	}

	/**
	 * Safe get.
	 *
	 * @param <T>
	 *            the generic type
	 * @param task
	 *            the task
	 * @return the optional
	 */
	protected static <T> Optional<T> safeGet(ForkJoinTask<T> task) {
		try {
			return Optional.ofNullable(task.get());
		} catch (InterruptedException | ExecutionException e) {
			return Optional.empty();
		}
	}

	/**
	 * Safe supplier.
	 *
	 * @param <T>
	 *            the generic type
	 * @param task
	 *            the task
	 * @return the supplier
	 */
	protected static <T> Supplier<T> safeSupplier(ForkJoinTask<T> task) {
		return () -> {
			try {
				return task.get();
			} catch (InterruptedException | ExecutionException e) {
			}
			return null;
		};
	}

	/**
	 * Store supplier.
	 *
	 * @param <T>
	 *            the generic type
	 * @param key
	 *            the key
	 * @param resultSupplier
	 *            the result supplier
	 * @param multipleAccess
	 *            the multiple access
	 * @return true, if successful
	 */
	protected static <T> boolean storeSupplier(ObjectsKey key, Supplier<T> resultSupplier, boolean multipleAccess) {
		if (!futureSuppliers.containsKey(key)) {
			futureSuppliers.put(key, resultSupplier);
			originalKeys.put(key, key);
			if (multipleAccess) {
				multipleAccessedKeys.put(key, key);
			} else {
				multipleAccessedKeys.remove(key);
			}

			if (multipleAccessedValues.containsKey(key)) {
				multipleAccessedValues.remove(key);
			}
			return true;
		}
		return false;
	}

	/**
	 * Gets the indexed key.
	 *
	 * @param i
	 *            the i
	 * @param keys
	 *            the keys
	 * @return the indexed key
	 */
	protected static Object[] getIndexedKey(int i, Object... keys) {
		return Stream.concat(Stream.of(keys), Stream.of(i)).toArray();
	}

	/**
	 * Gets the casted value.
	 *
	 * @param <T>
	 *            the generic type
	 * @param clazz
	 *            the clazz
	 * @param supplier
	 *            the supplier
	 * @return the casted value
	 */
	protected static <T> Optional<T> getCastedValue(Class<T> clazz, Supplier<? extends Object> supplier) {
		Object object = supplier.get();
		if (clazz.isInstance(object)) {
			return Optional.of(clazz.cast(object));
		}
		return Optional.empty();
	}

	/**
	 * Array of times.
	 *
	 * @param <T>
	 *            the generic type
	 * @param t
	 *            the t
	 * @param times
	 *            the times
	 * @return the t[]
	 */
	@SuppressWarnings("unchecked")
	protected static <T> T[] arrayOfTimes(T t, int times) {
		return Stream.generate(() -> t).limit(times).toArray(size -> (T[]) Array.newInstance(t.getClass(), size));
	}

	/**
	 * Waits for flag, until the flag is notified by either
	 * {@link Async#notifyFlag(String...)} or
	 * {@link Async#notifyAllFlag(String...)} in another thread.
	 *
	 * @param flag
	 *            the flag
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	static public void waitForFlag(String... flag) throws InterruptedException {
		ObjectsKey key = ObjectsKey.of((Object[]) flag);
		ObjectsKey originalKey = originalKeys.get(key);
		if (originalKey == null) {
			originalKey = key;
			originalKeys.put(key, originalKey);
		}
		synchronized (originalKey) {
			originalKey.wait();
		}
	}

	/**
	 * Notify all threads which are waiting for a flag with the invocation of
	 * {@link Async#waitForFlag(String...)}
	 *
	 * @param flag
	 *            the flag
	 */
	static public void notifyAllFlag(String... flag) {
		Async.notify(true, flag);
	}

	/**
	 * Notify a thread that is waiting for a flag with the invocation of
	 * {@link Async#waitForFlag(String...)}
	 *
	 * @param flag
	 *            the flag
	 */
	static public void notifyFlag(String... flag) {
		notify(false, flag);
	}

	/**
	 * Notify.
	 *
	 * @param all
	 *            the all
	 * @param flag
	 *            the flag
	 */
	private static void notify(boolean all, String... flag) {
		ObjectsKey key = ObjectsKey.of((Object[]) flag);
		ObjectsKey originalKey = originalKeys.get(key);
		if (originalKey != null) {
			originalKeys.remove(key);
			synchronized (originalKey) {
				if (all) {
					originalKey.notifyAll();
				} else {
					originalKey.notify();
				}
			}
		}
	}

	/**
	 * Notifies the scheduler of one or more Supplier(s) which are cyclically
	 * scheduled using either
	 * {@link SchedulingSupplier#scheduleSupplierUntilFlag(int, int, TimeUnit, boolean, String, Supplier...)}
	 * or
	 * {@link SchedulingSupplier#scheduleSuppliersAndWait(int, int, TimeUnit, boolean, Supplier...)}
	 * with the flag passed, and obtains the Stream of results of the type passed. <br>
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
	static public <T> Stream<T> notifyAndGetForFlag(Class<T> clazz, String... flag) {
		notifyFlag(flag);
		Stream.Builder<T> builder = Stream.builder();
		int count = 0;
		Object[] indexedKey = getIndexedKey(count, (Object[]) flag);
		while (originalKeys.containsKey(ObjectsKey.of(indexedKey))) {
			AsyncSupplier.waitAndGetFromSupplier(clazz, indexedKey).ifPresent(builder::accept);
			count++;
			indexedKey = getIndexedKey(count, (Object[]) flag);
		}
		return builder.build();
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
	static protected <T, R> ScheduledFuture<?> doScheduleFunction(int initialDelay, int delay, TimeUnit unit,
			boolean waitForPreviousFunction, SchedulingFunction<T, R> schedulingFunction) {
		final ScheduledFuture<?>[] scheduleFuture = new ScheduledFuture<?>[1];
		Runnable seq = new Runnable() {
			@Override
			public void run() {
				synchronized (schedulingFunction) {
					if (schedulingFunction.canRun()) {
						R res = schedulingFunction.invokeNextFunction();
						schedulingFunction.consumeResult(res);
						if (schedulingFunction.canCancel() && scheduleFuture[0] != null) {
							scheduleFuture[0].cancel(true);
						}
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

}
