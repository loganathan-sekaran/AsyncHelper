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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The AsyncSupplier Helper class with methods for submitting {@link Runnable}s to
 * invoke them as asynchronous tasks and optionally wait for them in same of
 * another thread.
 * 
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
public final class AsyncTask {

	/**
	 * {@code Logger} for this class.
	 */
	private static final Logger logger = Logger.getLogger(AsyncTask.class.getName());

	/**
	 * Prevent instantiation outside the class
	 */
	private AsyncTask() {
	}

	/**
	 * Submits a task (Runnable) to be invoke asynchronously.
	 *
	 * @param runnable
	 *            the runnable
	 */
	static public void submitTask(Runnable runnable) {
		Async.getThreadPool().execute(runnable);
	}

	/**
	 * Submits multiple tasks (Runnable) to be invoke asynchronously.
	 *
	 * @param runnables
	 *            the runnables
	 */
	static public void submitTasks(Runnable... runnables) {
		Stream.of(runnables).forEach(Async.getThreadPool()::execute);
	}

	/**
	 * Submits multiple tasks (Runnable) to be invoke asynchronously and wait
	 * until the tasks are finished. But this can be cancelled in the middle by
	 * providing a Supplier that returns false at any time.
	 *
	 * @param cancelConditionSupplier
	 *            the cancel condition supplier
	 * @param cancelCanInterruptRunning
	 *            the cancel can interrupt running
	 * @param runnables
	 *            the runnables
	 */
	static public void submitTasksAndWaitCancellable(Supplier<Boolean> cancelConditionSupplier,
			boolean cancelCanInterruptRunning, Runnable... runnables) {
		ExecutorService threadPool = Async.newThreadPool();
		List<Future<?>> futures = Stream.of(runnables).map(threadPool::submit).collect(Collectors.toList());
		AtomicBoolean allTasksCompleted = new AtomicBoolean(false);
		AsyncTask.submitTask(() -> {
			for (Future<?> future : futures) {
				try {
					future.get();
				} catch (Exception e) {
					logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			}
			allTasksCompleted.set(true);
		}, futures, "getting");

		AsyncTask.submitTask(() -> {
			while (!allTasksCompleted.get()) {
				if (!cancelConditionSupplier.get()) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
					}
				} else {
					futures.parallelStream().forEach(future -> {
						future.cancel(cancelCanInterruptRunning);
					});
					break;
				}
			}
		}, futures, "cancelling");

		AsyncTask.waitForTask(futures, "getting");
		AsyncTask.waitForTask(futures, "cancelling");
		threadPool.shutdownNow();
		try {
			threadPool.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	/**
	 * Submits multiple tasks (Runnable) to be invoke asynchronously and wait
	 * until the tasks are finished.
	 *
	 * @param runnables
	 *            the runnables
	 */
	static public void submitTasksAndWait(Runnable... runnables) {
		List<Callable<Object>> tasks = Stream.of(runnables).map(runnable -> (Callable<Object>) () -> {
			runnable.run();
			return (Object) null;
		}).collect(Collectors.toList());

		try {
			Async.getThreadPool().invokeAll(tasks).parallelStream().forEach(future -> {
				try {
					future.get();
				} catch (InterruptedException | ExecutionException e) {
					logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			});
		} catch (InterruptedException e) {
			logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	/**
	 * Submits multiple tasks (Runnable) to be invoke asynchronously which is
	 * marked by keys. The keys can be then used with
	 * {@link AsyncTask#waitForMultipleTasks(Object...)} which can be invoked in
	 * any thread so that that thread will wait until the tasks are finished.
	 *
	 * @param keys
	 *            the keys
	 * @param runnables
	 *            the runnables
	 */
	static public void submitTasks(Object[] keys, Runnable... runnables) {
		for (int i = 0; i < runnables.length; i++) {
			Runnable runnable = runnables[i];
			Object[] indexedKey = Async.getIndexedKey(i, keys);
			submitTask(runnable, indexedKey);
		}
	}

	/**
	 * Submits a task (Runnable) to be invoke asynchronously which is marked by
	 * keys. The keys can be then used with
	 * {@link AsyncTask#waitForMultipleTasks(Object...)} which can be invoked in
	 * any thread so that that thread will wait until the task is finished.
	 *
	 * @param runnable
	 *            the runnable
	 * @param keys
	 *            the keys
	 * @return true, if successful
	 */
	static public boolean submitTask(Runnable runnable, Object... keys) {
		ObjectsKey key = ObjectsKey.of(keys);
		if (!Async.futureSuppliers.containsKey(key)) {
			Supplier<Void> safeSupplier = Async.safeSupplier(Async.getThreadPool().submit(() -> {
				runnable.run();
				return null;
			}));
			return Async.storeSupplier(key, safeSupplier, false);
		}
		return false;
	}

	/**
	 * Wait for multiple tasks submitted by
	 * {@link AsyncTask#submitTasks(Object[], Runnable...)}. The keys should be
	 * the same keys provided during the tasks submission. This can be invoked
	 * in any thread so that that thread will wait until the tasks are finished.
	 *
	 * @param keys
	 *            the keys
	 */
	static public void waitForMultipleTasks(Object... keys) {
		for (int i = 0; Async.originalKeys.containsKey(ObjectsKey.of(Async.getIndexedKey(i, keys))); i++) {
			Object[] indexedKey = Async.getIndexedKey(i, keys);
			waitForTask(indexedKey);
		}
	}

	/**
	 * Wait for a task submitted by
	 * {@link AsyncTask#submitTask(Runnable, Object...)}. The keys should be the
	 * same keys provided during the task submission. This can be invoked in any
	 * thread so that that thread will wait until the task is finished.
	 *
	 * @param keys
	 *            the keys
	 */
	static public void waitForTask(Object... keys) {
		ObjectsKey objectsKey = ObjectsKey.of(keys);
		if (Async.originalKeys.containsKey(objectsKey)) {
			synchronized (Async.originalKeys.get(objectsKey)) {
				if (Async.multipleAccessedValues.containsKey(objectsKey)) {
					return;
				}
				if (Async.futureSuppliers.containsKey(objectsKey)) {
					Async.futureSuppliers.get(objectsKey).get();
					Async.futureSuppliers.remove(objectsKey);

					if (Async.multipleAccessedKeys.containsKey(objectsKey)) {
						Async.multipleAccessedValues.put(objectsKey, objectsKey);
					} else {
						Async.originalKeys.remove(objectsKey);
					}
				}
			}
		}
	}

}
