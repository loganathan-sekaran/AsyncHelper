
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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * The AsyncContext class. This contains the contextual states for the
 * asynchronous tasks/supplier execution and utility methods to wait for a flag
 * in one thread and notify the flag in another thread.
 * 
 * <br>
 * <br>
 * Note: In most of the cases default instance obtained with
 * ({@link AsyncContext#getDefault()}) is sufficient. But it is possible to
 * create new instance using {@link AsyncContext#newInstance()}) and can be
 * passed to
 * {@link AsyncTask#of(java.util.concurrent.ExecutorService, AsyncContext)} or
 * {@link AsyncSupplier#of(java.util.concurrent.ExecutorService, AsyncContext)}
 * or
 * {@link SchedulingSupplier#of(java.util.concurrent.ScheduledExecutorService, AsyncContext)}
 * or
 * {@link SchedulingTask#of(java.util.concurrent.ScheduledExecutorService, AsyncContext)}.
 * 
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
public class AsyncContext implements AutoCloseable{

	/** The future suppliers. */
	private final Map<ObjectsKey, Supplier<? extends Object>> futureSuppliers = new ConcurrentHashMap<>();

	/** The original keys. */
	private final Map<ObjectsKey, ObjectsKey> originalKeys = new ConcurrentHashMap<>();

	/** The multiple accessed keys. */
	private final Map<ObjectsKey, ObjectsKey> multipleAccessedKeys = new ConcurrentHashMap<>();

	/** The multiple accessed values. */
	private final Map<ObjectsKey, Object> multipleAccessedValues = new ConcurrentHashMap<>();

	
	/** The closed flag. */
	private volatile boolean closed;

	/**
	 * Instantiates a new async context.
	 */
	private AsyncContext() {
	}
	
	/** The Constant DEFAULT_INSTANCE. */
	private static final AsyncContext DEFAULT_INSTANCE = new AsyncContext();
	
	/**
	 * Gets the default AsyncContext instance.
	 *
	 * @return the default
	 */
	public static AsyncContext getDefault() {
		return DEFAULT_INSTANCE;
	}
	
	/**
	 * Get new instance of AsyncContext.
	 *
	 * @return the async context
	 */
	public static AsyncContext newInstance() {
		return new AsyncContext();
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
	protected <T> Optional<T> safeGet(Future<T> task) {
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
	protected static <T> Supplier<T> safeSupplier(Future<T> task) {
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
	protected <T> boolean storeSupplier(ObjectsKey key, Supplier<T> resultSupplier, boolean multipleAccess) {
		if (!getFutureSuppliers().containsKey(key)) {
			getFutureSuppliers().put(key, resultSupplier);
			getOriginalKeys().put(key, key);
			if (multipleAccess) {
				getMultipleAccessedKeys().put(key, key);
			} else {
				getMultipleAccessedKeys().remove(key);
			}

			if (getMultipleAccessedValues().containsKey(key)) {
				getMultipleAccessedValues().remove(key);
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
	protected <T> Optional<T> getCastedValue(Class<T> clazz, Supplier<? extends Object> supplier) {
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
	 * {@link AsyncContext#notifyFlag(String...)} or
	 * {@link AsyncContext#notifyAllFlag(String...)} in another thread.
	 *
	 * @param flag
	 *            the flag
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public void waitForFlag(String... flag) throws InterruptedException {
		ObjectsKey key = ObjectsKey.of((Object[]) flag);
		ObjectsKey originalKey = getOriginalKeys().get(key);
		if (originalKey == null) {
			originalKey = key;
			getOriginalKeys().put(key, originalKey);
		}
		synchronized (originalKey) {
			originalKey.wait();
		}
	}

	/**
	 * Notify all threads which are waiting for a flag with the invocation of
	 * {@link AsyncContext#waitForFlag(String...)}
	 *
	 * @param flag
	 *            the flag
	 */
	public void notifyAllFlag(String... flag) {
		this.notify(true, flag);
	}

	/**
	 * Notify a thread that is waiting for a flag with the invocation of
	 * {@link AsyncContext#waitForFlag(String...)}
	 *
	 * @param flag
	 *            the flag
	 */
	public void notifyFlag(String... flag) {
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
	private void notify(boolean all, String... flag) {
		ObjectsKey key = ObjectsKey.of((Object[]) flag);
		ObjectsKey originalKey = getOriginalKeys().get(key);
		if (originalKey != null) {
			getOriginalKeys().remove(key);
			synchronized (originalKey) {
				if (all) {
					originalKey.notifyAll();
				} else {
					originalKey.notify();
				}
			}
			
			originalKey.close();
		}
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
		this.notifyFlag(flag);
		Stream.Builder<T> builder = Stream.builder();
		int count = 0;
		Object[] indexedKey = getIndexedKey(count, (Object[]) flag);
		while (getOriginalKeys().containsKey(ObjectsKey.of(indexedKey))) {
			this.waitAndGetFromSupplier(clazz, indexedKey).ifPresent(builder::accept);
			count++;
			indexedKey = getIndexedKey(count, (Object[]) flag);
		}
		return builder.build();
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
	 */
	public <T> Optional<T> waitAndGetFromSupplier(Class<T> clazz, Object... keys) {
		ObjectsKey objectsKey = ObjectsKey.of(keys);
		if (getOriginalKeys().containsKey(objectsKey)) {
			synchronized (getOriginalKeys().get(objectsKey)) {
				if (getMultipleAccessedValues().containsKey(objectsKey)) {
					return getCastedValue(clazz, () -> getMultipleAccessedValues().get(objectsKey));
				}
	
				if (getFutureSuppliers().containsKey(objectsKey)) {
					Optional<T> value = this.getCastedValue(clazz, () -> getFutureSuppliers().get(objectsKey).get());
					getFutureSuppliers().remove(objectsKey);
	
					if (getMultipleAccessedKeys().containsKey(objectsKey)) {
						getMultipleAccessedValues().put(objectsKey, value.orElse(null));
					} else {
						ObjectsKey originalKey = getOriginalKeys().remove(objectsKey);
						if(originalKey != null) {
							originalKey.close();
						}
					}
					return value;
				}
			}
		}
		return Optional.empty();
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
		Stream.Builder<Optional<T>> builder = Stream.builder();
		for (int i = 0; getOriginalKeys().containsKey(ObjectsKey.of(AsyncContext.getIndexedKey(i, keys))); i++) {
			Object[] indexedKey = AsyncContext.getIndexedKey(i, keys);
			builder.accept(waitAndGetFromSupplier(clazz, indexedKey));
		}
		return builder.build().filter(Optional::isPresent).map(Optional::get);
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
		return waitAndGetFromSupplier(clazz, keys);
	}

	/* (non-Javadoc)
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public synchronized void close() {
		if(!closed) {
			futureSuppliers.clear();
			originalKeys.clear();
			multipleAccessedKeys.clear();
			multipleAccessedValues.clear();
			closed = true;
		}
	}

	/**
	 * Gets the future suppliers.
	 *
	 * @return the future suppliers
	 */
	public Map<ObjectsKey, Supplier<? extends Object>> getFutureSuppliers() {
		assertNotClosed();
		return futureSuppliers;
	}

	/**
	 * Gets the original keys.
	 *
	 * @return the original keys
	 */
	public Map<ObjectsKey, ObjectsKey> getOriginalKeys() {
		assertNotClosed();
		return originalKeys;
	}

	/**
	 * Gets the multiple accessed keys.
	 *
	 * @return the multiple accessed keys
	 */
	public Map<ObjectsKey, ObjectsKey> getMultipleAccessedKeys() {
		assertNotClosed();
		return multipleAccessedKeys;
	}

	/**
	 * Gets the multiple accessed values.
	 *
	 * @return the multiple accessed values
	 */
	public Map<ObjectsKey, Object> getMultipleAccessedValues() {
		assertNotClosed();
		return multipleAccessedValues;
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
	public void waitForMultipleTasks(Object... keys) {
		for (int i = 0; getOriginalKeys().containsKey(ObjectsKey.of(AsyncContext.getIndexedKey(i, keys))); i++) {
			Object[] indexedKey = AsyncContext.getIndexedKey(i, keys);
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
	public void waitForTask(Object... keys) {
		ObjectsKey objectsKey = ObjectsKey.of(keys);
		if (getOriginalKeys().containsKey(objectsKey)) {
			synchronized (getOriginalKeys().get(objectsKey)) {
				if (getMultipleAccessedValues().containsKey(objectsKey)) {
					return;
				}
				if (getFutureSuppliers().containsKey(objectsKey)) {
					getFutureSuppliers().get(objectsKey).get();
					getFutureSuppliers().remove(objectsKey);
	
					if (getMultipleAccessedKeys().containsKey(objectsKey)) {
						getMultipleAccessedValues().put(objectsKey, objectsKey);
					} else {
						getOriginalKeys().remove(objectsKey);
					}
				}
			}
		}
	}

	/**
	 * Assert not closed.
	 */
	private void assertNotClosed() {
		if (closed) {
			throw new RuntimeException(new IllegalStateException("Already closed"));
		}
	}
}
