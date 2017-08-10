package org.ls.asynchelper;

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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.ls.javautils.stream.StreamUtil;

public enum AsyncHelper {
	INSTANCE;

	/**
	 * {@code Logger} for this class.
	 */
	static final Logger logger = Logger.getLogger(AsyncHelper.class.getName());
	
	private ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
	private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(ForkJoinPool.getCommonPoolParallelism());
	private Map<ObjectsKey, Supplier<? extends Object>> futureSuppliers = new ConcurrentHashMap<>();
	private Map<ObjectsKey, ObjectsKey> originalKeys = new ConcurrentHashMap<>();
	private Map<ObjectsKey, ObjectsKey> multipleAccessedKeys = new ConcurrentHashMap<>();
	private Map<ObjectsKey, Object> multipleAccessedValues = new ConcurrentHashMap<>();

	public ForkJoinPool getForkJoinPool() {
		return forkJoinPool;
	}

	public void setForkJoinPool(ForkJoinPool forkJoinPool) {
		assert (forkJoinPool != null);
		this.forkJoinPool = forkJoinPool;
	}

	public <T> Optional<T> asyncGet(Supplier<T> supplier) {
		ForkJoinTask<T> task = forkJoinPool.submit(() -> supplier.get());
		return safeGet(task);
	}

	private <T> Optional<T> safeGet(ForkJoinTask<T> task) {
		try {
			return Optional.ofNullable(task.get());
		} catch (InterruptedException | ExecutionException e) {
			return Optional.empty();
		}
	}

	private <T> Supplier<T> safeSupplier(ForkJoinTask<T> task) {
		return () -> {
			try {
				return task.get();
			} catch (InterruptedException | ExecutionException e) {
			}
			return null;
		};
	}

	public <T> Supplier<T> submitSupplier(Supplier<T> supplier) {
		return safeSupplier(forkJoinPool.submit(() -> supplier.get()));
	}
	
	@SuppressWarnings("unchecked")
	public <T> Supplier<T>[] submitMultipleSuppliers(Supplier<T>... suppliers) {
		return Stream.of(suppliers)
				.map(supplier -> submitSupplier(supplier))
				.toArray(size -> new Supplier[size]);
	}

	public <T> Supplier<T> submitCallable(Callable<T> callable) {
		return safeSupplier(forkJoinPool.submit(callable));
	}

	public synchronized <T> Optional<T> submitAndGet(Callable<T> callable) {
		ForkJoinTask<T> task = forkJoinPool.submit(callable);
		return safeGet(task);
	}

	public void submitTask(Runnable runnable) {
		forkJoinPool.execute(runnable);
	}
	
	public void submitTasks(Runnable... runnables) {
		Stream.of(runnables).forEach(forkJoinPool::execute);
	}
	
	public void scheduleTasks(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			Runnable... runnables) {
		doScheduleTasks(initialDelay, delay, unit, waitForPreviousTask, runnables);
	}
	
	public void scheduleTasksAndWait(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			Runnable... runnables) {
		try {
			doScheduleTasks(initialDelay, delay, unit, waitForPreviousTask, runnables).get();
		} catch (InterruptedException | ExecutionException | CancellationException e) {
			logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
	
	private ScheduledFuture<?> doScheduleTasks(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			Runnable... runnables) {
		final ScheduledFuture<?>[] scheduleFuture = new ScheduledFuture<?>[1];
		Runnable seq = new Runnable() {
			private AtomicInteger sno = new AtomicInteger(0);
			@Override
			public void run() {
				if(sno.get() < runnables.length) {
					runnables[sno.getAndIncrement()].run();
					
					if(sno.get() == runnables.length) {
						if(scheduleFuture[0] != null) {
							scheduleFuture[0].cancel(true);
						}
					}
				}
			}
		};
		
		if (waitForPreviousTask) {
			scheduleFuture[0] = scheduledExecutorService.scheduleWithFixedDelay(seq, initialDelay, delay, unit);
		} else {
			scheduleFuture[0] = scheduledExecutorService.scheduleAtFixedRate(seq, initialDelay, delay, unit);
		}
		
		return scheduleFuture[0];
	}
	
	public <T>  Supplier<T>[] scheduleMultipleSuppliers(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			@SuppressWarnings("unchecked") Supplier<T>... suppliers) {
		return doScheduleSupplier(initialDelay, delay, unit, waitForPreviousTask, false, suppliers);
	}
	
	public <T>  Stream<T> scheduleMultipleSuppliersAndWait(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			@SuppressWarnings("unchecked") Supplier<T>... suppliers) {
		 Supplier<T>[] scheduleSupplier = doScheduleSupplier(initialDelay, delay, unit, waitForPreviousTask, true, suppliers);
		 return Stream.of(scheduleSupplier).map(Supplier::get);
	}
	
	public <T>  boolean scheduleMultipleSuppliersForSingleAccess(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			Supplier<T>[] suppliers, Object... keys) {
		Supplier<T>[] resultSuppliers = doScheduleSupplier(initialDelay, delay, unit, waitForPreviousTask, false, suppliers);
		boolean result = true;
		for (int i = 0; i < resultSuppliers.length; i++) {
			Supplier<T> supplier = resultSuppliers[i];
			Object[] indexedKey = getIndexedKey(i, keys);
			result &= storeSupplier(ObjectsKey.of(indexedKey), supplier, false);
		}
		return result;
	}
	
	private <T> Supplier<T>[] doScheduleSupplier(int initialDelay, int delay, TimeUnit unit, boolean waitForPreviousTask,
			boolean waitForAllTasks, @SuppressWarnings("unchecked") Supplier<T>... suppliers) {
		final ScheduledFuture<?>[] scheduleFuture = new ScheduledFuture<?>[1];
		@SuppressWarnings("unchecked")
		Supplier<T>[] resultSuppliers = new Supplier[suppliers.length]; 
		Runnable seq = new Runnable() {
			private AtomicInteger sno = new AtomicInteger(0);
			@Override
			public void run() {
				if(sno.get() < suppliers.length) {
					final int current = sno.getAndIncrement();
					synchronized (resultSuppliers) {
						T res = suppliers[current].get();
						resultSuppliers[current] = () -> res;
						resultSuppliers.notifyAll();
					}
					
					if(sno.get() == suppliers.length) {
						if(scheduleFuture[0] != null) {
							scheduleFuture[0].cancel(true);
						}
					}
				}
			}
		};
		
		if (waitForPreviousTask) {
			scheduleFuture[0] = scheduledExecutorService.scheduleWithFixedDelay(seq, initialDelay, delay, unit);
		} else {
			scheduleFuture[0] = scheduledExecutorService.scheduleAtFixedRate(seq, initialDelay, delay, unit);
		}
		
		if(waitForAllTasks) {
			try {
			scheduleFuture[0].get();
			} catch (InterruptedException | ExecutionException | CancellationException e) {
				logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}
		
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

	public <T> boolean submitSupplierForMultipleAccess(Supplier<T> supplier, Object... keys) {
		return doSubmitSupplier(supplier, true, keys);
	}
	
	public <T> boolean submitSupplierForSingleAccess(Supplier<T> supplier, Object... keys) {
		return doSubmitSupplier(supplier, false, keys);
	}
	
	public <T> boolean submitMultipleSuppliersForSingleAccess(Supplier<T>[] suppliers, Object... keys) {
		boolean result = true;
		for (int i = 0; i < suppliers.length; i++) {
			Supplier<T> supplier = suppliers[i];
			Object[] indexedKey = getIndexedKey(i, keys);
			result &= doSubmitSupplier(supplier, false, indexedKey);
		}
		return result;
	}

	private <T> boolean doSubmitSupplier(Supplier<T> supplier, boolean multipleAccess, Object... keys) {
		ObjectsKey key = ObjectsKey.of(keys);
		if (!futureSuppliers.containsKey(key)) {
			Supplier<T> safeSupplier = safeSupplier(forkJoinPool.submit(() -> supplier.get()));
			return storeSupplier(key, safeSupplier, multipleAccess);
		}
		return false;
	}

	private <T> boolean storeSupplier(ObjectsKey key, Supplier<T> resultSupplier, boolean multipleAccess) {
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

	public boolean submitTask(Runnable runnable, Object... keys) {
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

	public <T> Optional<T> waitAndGet(Class<T> clazz, Object... keys) {
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
	
	public <T> Stream<T> waitAndGetMultiple(Class<T> clazz, Object... keys) {
		Stream.Builder<Optional<T>> builder = Stream.builder();
		for (int i = 0; originalKeys.containsKey(ObjectsKey.of(getIndexedKey(i, keys))); i++) {
			Object[] indexedKey = getIndexedKey(i, keys);
			builder.accept(waitAndGet(clazz, indexedKey));
		}
		return StreamUtil.flatten(builder.build());
	}

	private Object[] getIndexedKey(int i, Object... keys) {
		return Stream.concat(Stream.of(keys), Stream.of(i)).toArray();
	}

	public <T> Optional<T> getCastedValue(Class<T> clazz, Supplier<? extends Object> supplier) {
		Object object = supplier.get();
		if (clazz.isInstance(object)) {
			return Optional.of(clazz.cast(object));
		}
		return Optional.empty();
	}

	public void waitForTask(Object... keys) {
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
	
	public void waitForFlag(String... flag) throws InterruptedException {
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
	
	public void notifyFlag(String... flag) {
		ObjectsKey key = ObjectsKey.of((Object[])flag);
		ObjectsKey originalKey = originalKeys.get(key);
		if(originalKey != null) {
			originalKeys.remove(key);
			synchronized (originalKey) {
				originalKey.notify();
			}
		}
	}
	
	public void notifyAllFlag(String... flag) {
		ObjectsKey key = ObjectsKey.of((Object[])flag);
		ObjectsKey originalKey = originalKeys.get(key);
		if(originalKey != null) {
			originalKeys.remove(key);
			synchronized (originalKey) {
				originalKey.notifyAll();
			}
		}
	}
	
}
