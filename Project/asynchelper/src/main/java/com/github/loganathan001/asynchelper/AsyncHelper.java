
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

package com.github.loganathan001.asynchelper;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * The AsyncHelper.
 */
public enum AsyncHelper {
	;
	
	/**
	 * {@code Logger} for this class.
	 */
	static final Logger logger = Logger.getLogger(AsyncHelper.class.getName());
	
	/** The fork join pool. */
	static private ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
	
	/** The scheduled executor service. */
	static private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(ForkJoinPool.getCommonPoolParallelism());
	
	/** The future suppliers. */
	static private Map<ObjectsKey, Supplier<? extends Object>> futureSuppliers = new ConcurrentHashMap<>();
	
	/** The original keys. */
	static private Map<ObjectsKey, ObjectsKey> originalKeys = new ConcurrentHashMap<>();
	
	/** The multiple accessed keys. */
	static private Map<ObjectsKey, ObjectsKey> multipleAccessedKeys = new ConcurrentHashMap<>();
	
	/** The multiple accessed values. */
	static private Map<ObjectsKey, Object> multipleAccessedValues = new ConcurrentHashMap<>();

	/**
	 * Gets the fork join pool.
	 *
	 * @return the fork join pool
	 */
	static public ForkJoinPool getForkJoinPool() {
		return forkJoinPool;
	}

	/**
	 * Async get.
	 *
	 * @param <T> the generic type
	 * @param supplier the supplier
	 * @return the optional
	 */
	static public <T> Optional<T> asyncGet(Supplier<T> supplier) {
		ForkJoinTask<T> task = forkJoinPool.submit(() -> supplier.get());
		return safeGet(task);
	}

	/**
	 * Safe get.
	 *
	 * @param <T> the generic type
	 * @param task the task
	 * @return the optional
	 */
	static private <T> Optional<T> safeGet(ForkJoinTask<T> task) {
		try {
			return Optional.ofNullable(task.get());
		} catch (InterruptedException | ExecutionException e) {
			return Optional.empty();
		}
	}

	/**
	 * Safe supplier.
	 *
	 * @param <T> the generic type
	 * @param task the task
	 * @return the supplier
	 */
	static private <T> Supplier<T> safeSupplier(ForkJoinTask<T> task) {
		return () -> {
			try {
				return task.get();
			} catch (InterruptedException | ExecutionException e) {
			}
			return null;
		};
	}

	/**
	 * Submit supplier.
	 *
	 * @param <T> the generic type
	 * @param supplier the supplier
	 * @return the supplier
	 */
	static public <T> Supplier<T> submitSupplier(Supplier<T> supplier) {
		return safeSupplier(forkJoinPool.submit(() -> supplier.get()));
	}
	
	/**
	 * Submit multiple suppliers.
	 *
	 * @param <T> the generic type
	 * @param suppliers the suppliers
	 * @return the supplier[]
	 */
	@SuppressWarnings("unchecked")
	static public <T> Supplier<T>[] submitSuppliers(Supplier<T>... suppliers) {
		return Stream.of(suppliers)
				.map(supplier -> submitSupplier(supplier))
				.toArray(size -> new Supplier[size]);
	}

	/**
	 * Submit callable.
	 *
	 * @param <T> the generic type
	 * @param callable the callable
	 * @return the supplier
	 */
	static public <T> Supplier<T> submitCallable(Callable<T> callable) {
		return safeSupplier(forkJoinPool.submit(callable));
	}

	/**
	 * Submit and get.
	 *
	 * @param <T> the generic type
	 * @param callable the callable
	 * @return the optional
	 */
	static public synchronized <T> Optional<T> submitAndGet(Callable<T> callable) {
		ForkJoinTask<T> task = forkJoinPool.submit(callable);
		return safeGet(task);
	}

	/**
	 * Submit task.
	 *
	 * @param runnable the runnable
	 */
	static public void submitTask(Runnable runnable) {
		forkJoinPool.execute(runnable);
	}
	
	/**
	 * Submit tasks.
	 *
	 * @param runnables the runnables
	 */
	static public void submitTasks(Runnable... runnables) {
		Stream.of(runnables).forEach(forkJoinPool::execute);
	}
	
	/**
	 * Schedule tasks.
	 *
	 * @param initialDelay the initial delay
	 * @param delay the delay
	 * @param unit the unit
	 * @param waitForPreviousTask the wait for previous task
	 * @param runnables the runnables
	 */
	static public void scheduleTasks(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			Runnable... runnables) {
		doScheduleTasks(initialDelay, delay, unit, waitForPreviousTask, runnables);
	}
	
	/**
	 * Schedule tasks until flag.
	 *
	 * @param initialDelay the initial delay
	 * @param delay the delay
	 * @param unit the unit
	 * @param waitForPreviousTask the wait for previous task
	 * @param flag the flag
	 * @param runnables the runnables
	 */
	static public void scheduleTasksUntilFlag(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			String flag, Runnable... runnables) {
		doScheduleTasksUntilFlag(initialDelay, delay, unit, waitForPreviousTask, runnables, flag);
	}
	
	/**
	 * Schedule task until flag.
	 *
	 * @param initialDelay the initial delay
	 * @param delay the delay
	 * @param unit the unit
	 * @param waitForPreviousTask the wait for previous task
	 * @param flag the flag
	 * @param runnable the runnable
	 */
	static public void scheduleTaskUntilFlag(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			String flag, Runnable runnable) {
		scheduleTasksUntilFlag(initialDelay, delay, unit, waitForPreviousTask, flag, runnable);
	}
	
	/**
	 * Schedule tasks and wait.
	 *
	 * @param initialDelay the initial delay
	 * @param delay the delay
	 * @param unit the unit
	 * @param waitForPreviousTask the wait for previous task
	 * @param runnables the runnables
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
	 * @param initialDelay the initial delay
	 * @param delay the delay
	 * @param unit the unit
	 * @param waitForPreviousTask the wait for previous task
	 * @param runnables the runnables
	 * @return the scheduled future
	 */
	static private ScheduledFuture<?> doScheduleTasks(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			Runnable... runnables) {
		SchedulingFunction<Runnable, Void> schedulingRunnables = new SchedulingFunction<Runnable, Void>() {
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
			public  void consumeResult(Void v) {
			}
			
		};
		return doScheduleFunction(initialDelay, 
				delay, 
				unit, 
				waitForPreviousTask, 
				schedulingRunnables);
	}
	
	/**
	 * Do schedule tasks until flag.
	 *
	 * @param initialDelay the initial delay
	 * @param delay the delay
	 * @param unit the unit
	 * @param waitForPreviousTask the wait for previous task
	 * @param runnables the runnables
	 * @param flag the flag
	 * @return the scheduled future
	 */
	static private ScheduledFuture<?> doScheduleTasksUntilFlag(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			Runnable[] runnables, String flag) {
		AtomicBoolean canCancel = new AtomicBoolean(false);
		SchedulingFunction<Runnable, Void> schedulingRunnables = new SchedulingFunction<Runnable, Void>() {
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
				if(index.get() == runnables.length) {
					//Cycle again
					index.set(0);
				}
				runnables[index.getAndIncrement()].run();
				return null;
			}

			@Override
			public  void consumeResult(Void v) {
			}
			
		};
		
		AsyncHelper.submitTask(() -> {
			try {
				AsyncHelper.waitForFlag(flag);
			} catch (InterruptedException e) {
				logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
			canCancel.set(true);
		});
		
		return doScheduleFunction(initialDelay, 
				delay, 
				unit, 
				waitForPreviousTask, 
				schedulingRunnables);
	}
	
	/**
	 * Do schedule function.
	 *
	 * @param <T> the generic type
	 * @param <R> the generic type
	 * @param initialDelay the initial delay
	 * @param delay the delay
	 * @param unit the unit
	 * @param waitForPreviousFunction the wait for previous function
	 * @param schedulingFunction the scheduling function
	 * @return the scheduled future
	 */
	static private <T,R> ScheduledFuture<?> doScheduleFunction(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousFunction,
			SchedulingFunction<T,R> schedulingFunction) {
		final ScheduledFuture<?>[] scheduleFuture = new ScheduledFuture<?>[1];
		Runnable seq = new Runnable() {
			@Override
			public void run() {
				synchronized (schedulingFunction) {
					if (schedulingFunction.canRun()) {
						R res = schedulingFunction.invokeNextFunction();
						schedulingFunction.consumeResult(res);
						if (schedulingFunction.canCancel()) {
							if (scheduleFuture[0] != null) {
								scheduleFuture[0].cancel(true);
							}
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
	
	/**
	 * Schedule multiple suppliers.
	 *
	 * @param <T> the generic type
	 * @param initialDelay the initial delay
	 * @param delay the delay
	 * @param unit the unit
	 * @param waitForPreviousTask the wait for previous task
	 * @param suppliers the suppliers
	 * @return the supplier[]
	 */
	static public <T>  Supplier<T>[] scheduleSuppliers(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			@SuppressWarnings("unchecked") Supplier<T>... suppliers) {
		return doScheduleSupplier(initialDelay, delay, unit, waitForPreviousTask, false, suppliers);
	}
	
	/**
	 * Schedule multiple suppliers until flag.
	 *
	 * @param <T> the generic type
	 * @param initialDelay the initial delay
	 * @param delay the delay
	 * @param unit the unit
	 * @param waitForPreviousTask the wait for previous task
	 * @param flag the flag
	 * @param suppliers the suppliers
	 */
	@SafeVarargs
	static public <T> void scheduleSuppliersUntilFlag(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			String flag, Supplier<T>... suppliers) {
		doScheduleSupplierUntilFlag(initialDelay, delay, unit, waitForPreviousTask, false, suppliers, flag);
	}
	
	/**
	 * Schedule supplier until flag.
	 *
	 * @param <T> the generic type
	 * @param initialDelay the initial delay
	 * @param delay the delay
	 * @param unit the unit
	 * @param waitForPreviousTask the wait for previous task
	 * @param flag the flag
	 * @param supplier the supplier
	 */
	static public <T> void scheduleSupplierUntilFlag(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			String flag, Supplier<T> supplier) {
		scheduleSuppliersUntilFlag(initialDelay, delay, unit, waitForPreviousTask, flag, supplier);
	}
	
	/**
	 * Schedule multiple suppliers and wait.
	 *
	 * @param <T> the generic type
	 * @param initialDelay the initial delay
	 * @param delay the delay
	 * @param unit the unit
	 * @param waitForPreviousTask the wait for previous task
	 * @param suppliers the suppliers
	 * @return the stream
	 */
	static public <T>  Stream<T> scheduleSuppliersAndWait(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			@SuppressWarnings("unchecked") Supplier<T>... suppliers) {
		 Supplier<T>[] scheduleSupplier = doScheduleSupplier(initialDelay, delay, unit, waitForPreviousTask, true, suppliers);
		 return Stream.of(scheduleSupplier).map(Supplier::get);
	}
	
	/**
	 * Schedule multiple suppliers for single access.
	 *
	 * @param <T> the generic type
	 * @param initialDelay the initial delay
	 * @param delay the delay
	 * @param unit the unit
	 * @param waitForPreviousTask the wait for previous task
	 * @param suppliers the suppliers
	 * @param keys the keys
	 * @return true, if successful
	 */
	static public <T>  boolean scheduleSuppliersForSingleAccess(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			Supplier<T>[] suppliers, Object... keys) {
		Supplier<T>[] resultSuppliers = doScheduleSupplier(initialDelay, delay, unit, waitForPreviousTask, false, suppliers);
		boolean result = true;
		if(resultSuppliers.length == 1) {
			Supplier<T> resSupplier = resultSuppliers[0];
			result &= storeSupplier(ObjectsKey.of(keys), resSupplier, false);
		} else {
			for (int i = 0; i < resultSuppliers.length; i++) {
				Supplier<T> resSupplier = resultSuppliers[i];
				Object[] indexedKey = getIndexedKey(i, keys);
				result &= storeSupplier(ObjectsKey.of(indexedKey), resSupplier, false);
			}
		}
		return result;
	}
	
	/**
	 * Do schedule supplier.
	 *
	 * @param <T> the generic type
	 * @param initialDelay the initial delay
	 * @param delay the delay
	 * @param unit the unit
	 * @param waitForPreviousTask the wait for previous task
	 * @param waitForAllTasks the wait for all tasks
	 * @param suppliers the suppliers
	 * @return the supplier[]
	 */
	static private <T> Supplier<T>[] doScheduleSupplier(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			boolean waitForAllTasks, @SuppressWarnings("unchecked") Supplier<T>... suppliers) {
		@SuppressWarnings("unchecked")
		Supplier<T>[] resultSuppliers = new Supplier[suppliers.length]; 
		SchedulingFunction<Supplier<T>, T> schedulingSuppliers = new SchedulingFunction<Supplier<T>, T>() {
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
			public  void consumeResult(T t) {
				synchronized (resultSuppliers) {
					resultSuppliers[index.get() - 1] = () -> t;
					resultSuppliers.notifyAll();
				}
			}
			
		};
		doScheduleFunction(initialDelay, 
				delay, 
				unit, 
				waitForPreviousTask, 
				schedulingSuppliers);
		
		@SuppressWarnings("unchecked")
		Supplier<T>[] blockingResultSupplier = new Supplier[suppliers.length]; 
		for (int i = 0; i < blockingResultSupplier.length; i++) {
			final int index = i;
			blockingResultSupplier[i] = new Supplier<T>() {
				@Override
				public synchronized T get() {
					synchronized (resultSuppliers) {
						while(resultSuppliers[index] == null) {
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
	 * @param <T> the generic type
	 * @param initialDelay the initial delay
	 * @param delay the delay
	 * @param unit the unit
	 * @param waitForPreviousTask the wait for previous task
	 * @param waitForAllTasks the wait for all tasks
	 * @param suppliers the suppliers
	 * @param flag the flag
	 * @return the scheduled future
	 */
	static private <T> ScheduledFuture<?> doScheduleSupplierUntilFlag(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			boolean waitForAllTasks, Supplier<T>[] suppliers, String flag) {
		AtomicBoolean canCancel = new AtomicBoolean(false);
		LinkedList<Supplier<T>> resultSuppliers = new LinkedList<Supplier<T>>(); 
		SchedulingFunction<Supplier<T>, T> schedulingSuppliers = new SchedulingFunction<Supplier<T>, T>() {
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
				if(index.get() == suppliers.length) {
					//Cycle again
					index.set(0);
				}
				return suppliers[index.getAndIncrement()].get();
			}

			@Override
			public  void consumeResult(T t) {
				synchronized (resultSuppliers) {
					Supplier<T> resSupplier = () -> t;
					resultSuppliers.add(resSupplier);
					Object[] indexedKey = getIndexedKey(resultSuppliers.size() - 1, flag);
					storeSupplier(ObjectsKey.of(indexedKey), resSupplier, false);
				}
			}
			
		};
		
		return doScheduleFunction(initialDelay, 
				delay, 
				unit, 
				waitForPreviousTask, 
				schedulingSuppliers);
		
	}

	/**
	 * Submit supplier for multiple access.
	 *
	 * @param <T> the generic type
	 * @param supplier the supplier
	 * @param keys the keys
	 * @return true, if successful
	 */
	static public <T> boolean submitSupplierForMultipleAccess(Supplier<T> supplier, Object... keys) {
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
	static public <T> boolean submitSupplierForSingleAccess(Supplier<T> supplier, Object... keys) {
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
	static public <T> boolean submitSuppliersForSingleAccess(Supplier<T>[] suppliers, Object... keys) {
		boolean result = true;
		for (int i = 0; i < suppliers.length; i++) {
			Supplier<T> supplier = suppliers[i];
			Object[] indexedKey = getIndexedKey(i, keys);
			result &= doSubmitSupplier(supplier, false, indexedKey);
		}
		return result;
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
		if (!futureSuppliers.containsKey(key)) {
			Supplier<T> safeSupplier = safeSupplier(forkJoinPool.submit(() -> supplier.get()));
			return storeSupplier(key, safeSupplier, multipleAccess);
		}
		return false;
	}

	/**
	 * Store supplier.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @param resultSupplier the result supplier
	 * @param multipleAccess the multiple access
	 * @return true, if successful
	 */
	static private <T> boolean storeSupplier(ObjectsKey key, Supplier<T> resultSupplier, boolean multipleAccess) {
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
	 * Submit task.
	 *
	 * @param runnable the runnable
	 * @param keys the keys
	 * @return true, if successful
	 */
	static public boolean submitTask(Runnable runnable, Object... keys) {
		ObjectsKey key = ObjectsKey.of(keys);
		if (!futureSuppliers.containsKey(key)) {
			Supplier<Void> safeSupplier = safeSupplier(forkJoinPool.submit(() -> {
				runnable.run();
				return null;
			}));
			return storeSupplier(key, safeSupplier, false);
		}
		return false;
	}

	/**
	 * Wait and get.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param keys the keys
	 * @return the optional
	 */
	static public <T> Optional<T> waitAndGet(Class<T> clazz, Object... keys) {
		ObjectsKey objectsKey = ObjectsKey.of(keys);
		if (originalKeys.containsKey(objectsKey)) {
			synchronized (originalKeys.get(objectsKey)) {
				if (multipleAccessedValues.containsKey(objectsKey)) {
					return getCastedValue(clazz, () -> multipleAccessedValues.get(objectsKey));
				}

				if (futureSuppliers.containsKey(objectsKey)) {
					Optional<T> value = getCastedValue(clazz, () -> futureSuppliers.get(objectsKey).get());
					futureSuppliers.remove(objectsKey);

					if (multipleAccessedKeys.containsKey(objectsKey)) {
						multipleAccessedValues.put(objectsKey, value.orElse(null));
					} else {
						originalKeys.remove(objectsKey);
					}
					return value;
				}
			}
		}
		return Optional.empty();
	}
	
	/**
	 * Wait and get multiple.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param keys the keys
	 * @return the stream
	 */
	static public <T> Stream<T> waitAndGetMultiple(Class<T> clazz, Object... keys) {
		Stream.Builder<Optional<T>> builder = Stream.builder();
		for (int i = 0; originalKeys.containsKey(ObjectsKey.of(getIndexedKey(i, keys))); i++) {
			Object[] indexedKey = getIndexedKey(i, keys);
			builder.accept(waitAndGet(clazz, indexedKey));
		}
		return builder.build().filter(Optional::isPresent).map(Optional::get);
	}

	/**
	 * Gets the indexed key.
	 *
	 * @param i the i
	 * @param keys the keys
	 * @return the indexed key
	 */
	static private Object[] getIndexedKey(int i, Object... keys) {
		return Stream.concat(Stream.of(keys), Stream.of(i)).toArray();
	}

	/**
	 * Gets the casted value.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param supplier the supplier
	 * @return the casted value
	 */
	static public <T> Optional<T> getCastedValue(Class<T> clazz, Supplier<? extends Object> supplier) {
		Object object = supplier.get();
		if (clazz.isInstance(object)) {
			return Optional.of(clazz.cast(object));
		}
		return Optional.empty();
	}

	/**
	 * Wait for task.
	 *
	 * @param keys the keys
	 */
	static public void waitForTask(Object... keys) {
		ObjectsKey objectsKey = ObjectsKey.of(keys);
		if (originalKeys.containsKey(objectsKey)) {
			synchronized (originalKeys.get(objectsKey)) {
				if (multipleAccessedValues.containsKey(objectsKey)) {
					return;
				}
				if (futureSuppliers.containsKey(objectsKey)) {
					futureSuppliers.get(objectsKey).get();
					futureSuppliers.remove(objectsKey);

					if (multipleAccessedKeys.containsKey(objectsKey)) {
						multipleAccessedValues.put(objectsKey, objectsKey);
					} else {
						originalKeys.remove(objectsKey);
					}
				}
			}
		}
	}
	
	/**
	 * Wait for flag.
	 *
	 * @param flag the flag
	 * @throws InterruptedException the interrupted exception
	 */
	static public void waitForFlag(String... flag) throws InterruptedException {
		ObjectsKey key = ObjectsKey.of((Object[])flag);
		ObjectsKey originalKey = originalKeys.get(key);
		if(originalKey == null) {
			originalKey = key;
			originalKeys.put(key, originalKey);
		}
		synchronized (originalKey) {
			originalKey.wait();
		}
	}
	
	/**
	 * Notify flag.
	 *
	 * @param flag the flag
	 */
	static public void notifyFlag(String... flag) {
		ObjectsKey key = ObjectsKey.of((Object[])flag);
		ObjectsKey originalKey = originalKeys.get(key);
		if(originalKey != null) {
			originalKeys.remove(key);
			synchronized (originalKey) {
				originalKey.notify();
			}
		}
	}
	
	/**
	 * Notify and get for flag.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param flag the flag
	 * @return the list
	 */
	static public <T> Stream<T> notifyAndGetForFlag(Class<T> clazz, String... flag) {
		notifyFlag(flag);
		Stream.Builder<T> builder = Stream.builder();
		int count = 0;
		Object[] indexedKey = getIndexedKey(count, (Object[])flag);
		while(originalKeys.containsKey(ObjectsKey.of(indexedKey))) {
			waitAndGet(clazz, indexedKey).ifPresent(builder::accept);
			count++;
			 indexedKey = getIndexedKey(count,  (Object[])flag);
		}
		return builder.build();
	}
	
	/**
	 * Notify all flag.
	 *
	 * @param flag the flag
	 */
	static public void notifyAllFlag(String... flag) {
		ObjectsKey key = ObjectsKey.of((Object[])flag);
		ObjectsKey originalKey = originalKeys.get(key);
		if(originalKey != null) {
			originalKeys.remove(key);
			synchronized (originalKey) {
				originalKey.notifyAll();
			}
		}
	}

	/**
	 * Schedule supplier.
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
	static public <T> Supplier<T>[] scheduleSupplier(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			Supplier<T> supplier, int times) {
		return scheduleSuppliers(initialDelay, delay, unit, waitForPreviousTask,  arrayOfTimes(supplier, times));
	}

	/**
	 * Schedule supplier and wait.
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
		return scheduleSuppliersAndWait(initialDelay, delay, unit, waitForPreviousTask, arrayOfTimes(supplier, times));
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
	static private <T> T[] arrayOfTimes(T t, int times) {
		return Stream.generate(() -> t).limit(times).toArray(size -> (T[]) Array.newInstance(t.getClass(), size));
	}

	/**
	 * Schedule supplier for single access.
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
		return scheduleSuppliersForSingleAccess(initialDelay, delay, unit, waitForPreviousTask, arrayOfTimes(supplier, times), keys);
	}

	/**
	 * Schedule task.
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
	static public void scheduleTask(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask, Runnable runnable,
			int times) {
		scheduleTasks(initialDelay, delay, unit, waitForPreviousTask,  arrayOfTimes(runnable, times));
	}

	/**
	 * Schedule task and wait.
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
		scheduleTasksAndWait(initialDelay, delay, unit, waitForPreviousTask, arrayOfTimes(runnable, times));
	}

	/**
	 * Schedule supplier.
	 *
	 * @param <T> the generic type
	 * @param initialDelay the initial delay
	 * @param unit the unit
	 * @param supplier the supplier
	 * @return the supplier[]
	 */
	static public <T> Supplier<T> scheduleSupplier(int initialDelay, TimeUnit unit, Supplier<T> supplier) {
		return scheduleSupplier(initialDelay, 1, unit, false, supplier, 1)[0];
	}

	/**
	 * Schedule supplier and wait.
	 *
	 * @param <T> the generic type
	 * @param initialDelay the initial delay
	 * @param unit the unit
	 * @param supplier the supplier
	 * @return the stream
	 */
	static public <T> Optional<T> scheduleSupplierAndWait(int initialDelay, TimeUnit unit, Supplier<T> supplier) {
		return scheduleSupplierAndWait(initialDelay, 1, unit, false, supplier, 1).findAny();
	}

	/**
	 * Schedule supplier for single access.
	 *
	 * @param <T> the generic type
	 * @param initialDelay the initial delay
	 * @param unit the unit
	 * @param supplier the supplier
	 * @param keys the keys
	 * @return true, if successful
	 */
	static public <T> boolean scheduleSupplierForSingleAccess(int initialDelay, TimeUnit unit, Supplier<T> supplier,
			Object... keys) {
		return scheduleSupplierForSingleAccess(initialDelay, 1, unit, false, supplier, 1, keys);
	}

	/**
	 * Schedule task.
	 *
	 * @param initialDelay the initial delay
	 * @param unit the unit
	 * @param runnable the runnable
	 */
	static public void scheduleTask(int initialDelay, TimeUnit unit, Runnable runnable) {
		scheduleTask(initialDelay, 1, unit, false, runnable, 1);
	}

	/**
	 * Schedule task and wait.
	 *
	 * @param initialDelay the initial delay
	 * @param unit the unit
	 * @param runnable the runnable
	 */
	static public void scheduleTaskAndWait(int initialDelay, TimeUnit unit, Runnable runnable) {
		scheduleTaskAndWait(initialDelay, 1, unit, false, runnable, 1);
	}

	/**
	 * The Interface SchedulingFunction.
	 *
	 * @param <T> the generic type
	 * @param <R> the generic type
	 */
	interface SchedulingFunction<T,R> {
		
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
		 * @param r the r
		 */
		void consumeResult(R r);
	}
	
}
