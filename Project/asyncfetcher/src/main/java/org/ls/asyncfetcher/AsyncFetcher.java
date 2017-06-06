package org.ls.asyncfetcher;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Supplier;

public enum AsyncFetcher {
	INSTANCE;

	private ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

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

	public <T> boolean submitSupplierForMultipleAccess(Supplier<T> supplier, Object... keys) {
		return doSubmitSupplier(supplier, true, keys);
	}
	
	public <T> boolean submitSupplierForSingleAccess(Supplier<T> supplier, Object... keys) {
		return doSubmitSupplier(supplier, false, keys);
	}

	private <T> boolean doSubmitSupplier(Supplier<T> supplier, boolean multipleAccess, Object... keys) {
		ObjectsKey key = ObjectsKey.of(keys);
		if (!futureSuppliers.containsKey(key)) {
			Supplier<T> safeSupplier = safeSupplier(forkJoinPool.submit(() -> supplier.get()));
			storeSupplier(key, safeSupplier, multipleAccess);
			return true;
		}
		return false;
	}

	private <T> void storeSupplier(ObjectsKey key, Supplier<T> safeSupplier, boolean multipleAccess) {
		futureSuppliers.put(key, safeSupplier);
		originalKeys.put(key, key);
		if (multipleAccess) {
			multipleAccessedKeys.put(key, key);
		} else {
			multipleAccessedKeys.remove(key);
		}
		
		if(multipleAccessedValues.containsKey(key)) {
			multipleAccessedValues.remove(key);
		}
	}

	public boolean submitTask(Runnable runnable, Object... keys) {
		ObjectsKey key = ObjectsKey.of(keys);
		if (!futureSuppliers.containsKey(key)) {
			Supplier<Void> safeSupplier = safeSupplier(forkJoinPool.submit(() -> {
				runnable.run();
				return null;
			}));
			storeSupplier(key, safeSupplier, false);
			return true;
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
}
