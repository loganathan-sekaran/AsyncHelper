
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
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * The Async class.
 * @author Loganathan.S <https://github.com/loganathan001>
 */
public final class Async {
	
	/** The fork join pool. */
	static ForkJoinPool forkJoinPool;
	
	static {
		forkJoinPool = ForkJoinPool.commonPool();
		if(ForkJoinPool.getCommonPoolParallelism() == 1) {
			forkJoinPool = new ForkJoinPool(5);
		} else {
			forkJoinPool = ForkJoinPool.commonPool();
		}
	}
	
	/** The future suppliers. */
	static Map<ObjectsKey, Supplier<? extends Object>> futureSuppliers = new ConcurrentHashMap<>();
	
	/** The original keys. */
	static Map<ObjectsKey, ObjectsKey> originalKeys = new ConcurrentHashMap<>();
	
	/** The multiple accessed keys. */
	static Map<ObjectsKey, ObjectsKey> multipleAccessedKeys = new ConcurrentHashMap<>();
	
	/** The multiple accessed values. */
	static Map<ObjectsKey, Object> multipleAccessedValues = new ConcurrentHashMap<>();
	
	private Async() {
	}
	

	/**
	 * Gets the fork join pool.
	 *
	 * @return the fork join pool
	 */
	static ForkJoinPool getForkJoinPool() {
		return forkJoinPool;
	}

	/**
	 * Async get. Gets the supplier in another thread and returns the Optional of value from supplier.
	 *
	 * @param <T> the generic type
	 * @param supplier the supplier
	 * @return the optional
	 */
	static public <T> Optional<T> get(Supplier<T> supplier) {
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
	static <T> Optional<T> safeGet(ForkJoinTask<T> task) {
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
	static <T> Supplier<T> safeSupplier(ForkJoinTask<T> task) {
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
	 * @param <T> the generic type
	 * @param key the key
	 * @param resultSupplier the result supplier
	 * @param multipleAccess the multiple access
	 * @return true, if successful
	 */
	static <T> boolean storeSupplier(ObjectsKey key, Supplier<T> resultSupplier, boolean multipleAccess) {
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
	 * @param i the i
	 * @param keys the keys
	 * @return the indexed key
	 */
	static Object[] getIndexedKey(int i, Object... keys) {
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
	static <T> Optional<T> getCastedValue(Class<T> clazz, Supplier<? extends Object> supplier) {
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
	static <T> T[] arrayOfTimes(T t, int times) {
		return Stream.generate(() -> t).limit(times).toArray(size -> (T[]) Array.newInstance(t.getClass(), size));
	}
	
}
