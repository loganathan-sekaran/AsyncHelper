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
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The class Submit.
 * @author Loganathan.S <https://github.com/loganathan001>
 */
public final class Submit {
	
	/**
	 * {@code Logger} for this class.
	 */
	private static final Logger logger = Logger.getLogger(Submit.class.getName());
	

	private Submit() {
	}

	/**
	 * Submit supplier.
	 *
	 * @param <T> the generic type
	 * @param supplier the supplier
	 * @return the supplier
	 */
	static public <T> Supplier<T> supplier(Supplier<T> supplier) {
		return Async.safeSupplier(Async.forkJoinPool.submit(() -> supplier.get()));
	}

	/**
	 * Submit multiple suppliers.
	 *
	 * @param <T> the generic type
	 * @param suppliers the suppliers
	 * @return the supplier[]
	 */
	@SuppressWarnings("unchecked")
	static public <T> Supplier<T>[] suppliers(Supplier<T>... suppliers) {
		return Stream.of(suppliers)
				.map(supplier -> supplier(supplier))
				.toArray(size -> new Supplier[size]);
	}

	/**
	 * Submit callable.
	 *
	 * @param <T> the generic type
	 * @param callable the callable
	 * @return the supplier
	 */
	static public <T> Supplier<T> callable(Callable<T> callable) {
		return Async.safeSupplier(Async.forkJoinPool.submit(callable));
	}

	/**
	 * Submit and get.
	 *
	 * @param <T> the generic type
	 * @param callable the callable
	 * @return the optional
	 */
	static public synchronized <T> Optional<T> andGet(Callable<T> callable) {
		ForkJoinTask<T> task = Async.forkJoinPool.submit(callable);
		return Async.safeGet(task);
	}

	/**
	 * Submit task.
	 *
	 * @param runnable the runnable
	 */
	static public void task(Runnable runnable) {
		Async.forkJoinPool.execute(runnable);
	}

	/**
	 * Submit tasks.
	 *
	 * @param runnables the runnables
	 */
	static public void tasks(Runnable... runnables) {
		Stream.of(runnables).forEach(Async.forkJoinPool::execute);
	}

	/**
	 * Submit tasks and wait.
	 *
	 * @param cancelCondition the cancel condition
	 * @param cancelCanInterruptRunning the cancel can interrupt running
	 * @param runnables the runnables
	 */
	static public void tasksAndWait(Supplier<Boolean> cancelCondition, boolean cancelCanInterruptRunning, Runnable... runnables) {
		ForkJoinPool forkJoinPool = new ForkJoinPool(ForkJoinPool.getCommonPoolParallelism());
		List<Future<?>> futures = Stream.of(runnables).map(forkJoinPool::submit).collect(Collectors.toList());
		AtomicBoolean allTasksCompleted = new AtomicBoolean(false);
		Submit.task(() -> {
			for (Future<?> future : futures) {
				try {
					future.get();
				} catch (Exception e) {
					logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			}
			allTasksCompleted.set(true);
		}, futures, "getting");
		
		Submit.task(() -> {
			while(!allTasksCompleted.get()) {
				if(!cancelCondition.get()) {
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
		
		Wait.waitForTask(futures, "getting");
		Wait.waitForTask(futures, "cancelling");
		forkJoinPool.shutdownNow();
		try {
			forkJoinPool.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	/**
	 * Submit tasks and wait.
	 *
	 * @param runnables the runnables
	 */
	static public void tasksAndWait(Runnable... runnables) {
		List<Callable<Object>> tasks = Stream.of(runnables).map(runnable -> (Callable<Object>) () ->  {
			runnable.run();
			return (Object)null;
		}).collect(Collectors.toList());
		
		Async.forkJoinPool.invokeAll(tasks).parallelStream().forEach(future -> {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
		});
	}

	/**
	 * Submit tasks.
	 *
	 * @param keys the keys
	 * @param runnables the runnables
	 */
	static public void tasks(Object[] keys, Runnable... runnables) {
		for (int i = 0; i < runnables.length; i++) {
			Runnable runnable = runnables[i];
			Object[] indexedKey = Async.getIndexedKey(i, keys);
			task(runnable, indexedKey);
		}
	}

	/**
	 * Submit supplier for multiple access.
	 *
	 * @param <T> the generic type
	 * @param supplier the supplier
	 * @param keys the keys
	 * @return true, if successful
	 */
	static public <T> boolean supplierForMultipleAccess(Supplier<T> supplier, Object... keys) {
		return doSubmitSupplier(supplier, true, keys);
	}

	/**
	 * Submit supplier for single access.
	 *
	 * @param <T> the generic type
	 * @param supplier the supplier
	 * @param keys the keys
	 * @return true, if successful
	 */
	static public <T> boolean supplierForSingleAccess(Supplier<T> supplier, Object... keys) {
		return doSubmitSupplier(supplier, false, keys);
	}

	/**
	 * Submit multiple suppliers for single access.
	 *
	 * @param <T> the generic type
	 * @param suppliers the suppliers
	 * @param keys the keys
	 * @return true, if successful
	 */
	static public <T> boolean suppliersForSingleAccess(Supplier<T>[] suppliers, Object... keys) {
		boolean result = true;
		for (int i = 0; i < suppliers.length; i++) {
			Supplier<T> supplier = suppliers[i];
			Object[] indexedKey = Async.getIndexedKey(i, keys);
			result &= doSubmitSupplier(supplier, false, indexedKey);
		}
		return result;
	}

	/**
	 * Submit task.
	 *
	 * @param runnable the runnable
	 * @param keys the keys
	 * @return true, if successful
	 */
	static public boolean task(Runnable runnable, Object... keys) {
		ObjectsKey key = ObjectsKey.of(keys);
		if (!Async.futureSuppliers.containsKey(key)) {
			Supplier<Void> safeSupplier = Async.safeSupplier(Async.forkJoinPool.submit(() -> {
				runnable.run();
				return null;
			}));
			return Async.storeSupplier(key, safeSupplier, false);
		}
		return false;
	}

	/**
	 * Do submit supplier.
	 *
	 * @param <T> the generic type
	 * @param supplier the supplier
	 * @param multipleAccess the multiple access
	 * @param keys the keys
	 * @return true, if successful
	 */
	static private <T> boolean doSubmitSupplier(Supplier<T> supplier, boolean multipleAccess, Object... keys) {
		ObjectsKey key = ObjectsKey.of(keys);
		if (!Async.futureSuppliers.containsKey(key)) {
			Supplier<T> safeSupplier = Async.safeSupplier(Async.forkJoinPool.submit(() -> supplier.get()));
			return Async.storeSupplier(key, safeSupplier, multipleAccess);
		}
		return false;
	}
	
}
