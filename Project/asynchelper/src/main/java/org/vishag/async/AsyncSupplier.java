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

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * The AsyncSupplier Helper class with methods for submitting Suppliers for
 * asynchronous invocation and obtaining their results asynchronously.
 * 
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
public final class AsyncSupplier {

	/**
	 * Prevent instantiation outside the class
	 */
	private AsyncSupplier() {
	}

	/**
	 * Submits a supplier to be invoke asynchronously and gets a result Supplier
	 * handle. The result of the supplier can be obtained by calling the
	 * {@link Supplier#get()} from the returning supplier which may wait until
	 * the submitted Supplier code execution completes.
	 * 
	 *
	 * @param <T>
	 *            the generic type
	 * @param supplier
	 *            the supplier
	 * @return the supplier
	 */
	static public <T> Supplier<T> submitSupplier(Supplier<T> supplier) {
		return Async.safeSupplier(Async.forkJoinPool.submit(() -> supplier.get()));
	}

	/**
	 * Submits multiple suppliers to be invoke asynchronously and gets an array
	 * of result Suppliers handles. The result of each suppliers can be obtained
	 * by calling the {@link Supplier#get()} from the returning suppliers which
	 * will wait until the submitted Supplier code execution completes.
	 *
	 * @param <T>
	 *            the generic type
	 * @param suppliers
	 *            the suppliers
	 * @return the supplier[]
	 */
	@SuppressWarnings("unchecked")
	static public <T> Supplier<T>[] submitSuppliers(Supplier<T>... suppliers) {
		return Stream.of(suppliers).map(supplier -> submitSupplier(supplier)).toArray(size -> new Supplier[size]);
	}

	/**
	 * Submits a supplier to be invoke asynchronously for multiple access with
	 * keys. The result can be obtained multiple times by invoking
	 * {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)} from any
	 * thread which will wait until the supplier code execution completes.
	 * <br>
	 * The submission will fail if any exception occurs during the execution of
	 * supplier or due to thread interruption, or, if any supplier is already
	 * submitted with the same keys and the result is not yet obtained using
	 * {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)} at-least
	 * once.
	 *
	 * @param <T>
	 *            the generic type
	 * @param supplier
	 *            the supplier
	 * @param keys
	 *            the keys
	 * @return true, if successful
	 */
	static public <T> boolean submitSupplierForMultipleAccess(Supplier<T> supplier, Object... keys) {
		return doSubmitSupplier(supplier, true, keys);
	}
	
	/**
	 * Submits a value for the keys. The value can be obtained multiple times by
	 * invoking {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)} from
	 * any thread which will wait until the supplier code execution completes. <br>
	 * The submission will fail if any exception occurs during the execution of
	 * supplier or due to thread interruption, or, if any supplier is already
	 * submitted with the same keys and the result is not yet obtained using
	 * {@link AsyncSupplier#waitAndGetValue(Class, Object...)} at-least once.
	 *
	 * @param <T>
	 *            the generic type
	 * @param supplier
	 *            the supplier
	 * @param keys
	 *            the keys
	 * @return true, if successful
	 */
	static public <T> boolean submitValue(T value, Object... keys) {
		Supplier<T> supplier = () -> value;
		return submitSupplierForMultipleAccess(supplier, keys);
	}

	/**
	 * Submits a supplier to be invoke asynchronously for single access and get
	 * the status of the submission. The result can be obtained only once by
	 * invoking {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)}
	 * from any thread which will wait until the supplier code execution
	 * completes. <br>
	 * <br>
	 * The submission will fail if any exception occurs during the execution of
	 * supplier or due to thread interruption, or, if any supplier is already
	 * submitted with the same keys and the result is not yet obtained using
	 * {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)} at-least
	 * once.
	 *
	 * @param <T>
	 *            the generic type
	 * @param supplier
	 *            the supplier
	 * @param keys
	 *            the keys
	 * @return true, if successful
	 */
	static public <T> boolean submitSupplierForSingleAccess(Supplier<T> supplier, Object... keys) {
		return doSubmitSupplier(supplier, false, keys);
	}
	
	/**
	 * This first drops the already submitted supplier with the same key (if any)
	 * and then submits a supplier to be invoke asynchronously for multiple access
	 * with keys. The result can be obtained multiple times by invoking
	 * {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)} from any
	 * thread which will wait until the supplier code execution completes. <br>
	 * The submission will fail if any exception occurs during the execution of
	 * supplier or due to thread interruption, or, if any supplier is already
	 * submitted with the same keys and the result is not yet obtained using
	 * {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)} at-least once.
	 *
	 * @param <T>
	 *            the generic type
	 * @param supplier
	 *            the supplier
	 * @param keys
	 *            the keys
	 * @return true, if successful
	 */
	static public <T> boolean submitSupplierWithDropExistingForMultipleAccess(Supplier<T> supplier, Object... keys) {
		dropSubmittedSupplier(keys);
		return submitSupplierForMultipleAccess(supplier, keys);
	}
	
	/**
	 * This first drops the already submitted value with the same key (if any)
	 * and then submits the value for the keys. The value can be obtained multiple times by
	 * invoking {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)} from
	 * any thread which will wait until the supplier code execution completes. <br>
	 * The submission will fail if any exception occurs during the execution of
	 * supplier or due to thread interruption, or, if any supplier is already
	 * submitted with the same keys and the result is not yet obtained using
	 * {@link AsyncSupplier#waitAndGetValue(Class, Object...)} at-least once.
	 *
	 * @param <T>
	 *            the generic type
	 * @param supplier
	 *            the supplier
	 * @param keys
	 *            the keys
	 * @return true, if successful
	 */
	static public <T> boolean submitValueWithDropExisting(T value, Object... keys) {
		Supplier<T> supplier = () -> value;
		return submitSupplierWithDropExistingForMultipleAccess(supplier, keys);
	}

	/**
	 * This first drops the already submitted supplier with the same key (if any)
	 * and then submits a supplier to be invoke asynchronously for single access and
	 * get the status of the submission. The result can be obtained only once by
	 * invoking {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)} from
	 * any thread which will wait until the supplier code execution completes. <br>
	 * <br>
	 * The submission will fail if any exception occurs during the execution of
	 * supplier or due to thread interruption, or, if any supplier is already
	 * submitted with the same keys and the result is not yet obtained using
	 * {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)} at-least once.
	 *
	 * @param <T>
	 *            the generic type
	 * @param supplier
	 *            the supplier
	 * @param keys
	 *            the keys
	 * @return true, if successful
	 */
	static public <T> boolean submitSupplierWithDropExistingForSingleAccess(Supplier<T> supplier, Object... keys) {
		dropSubmittedSupplier(keys);
		return submitSupplierForSingleAccess(supplier, keys);
	}

	/**
	 * Submits multiple suppliers to be invoke asynchronously for single access
	 * and get the status of the submission. The result can be obtained only
	 * once by invoking
	 * {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)} from any
	 * thread which will wait until the supplier code execution completes.
	 *
	 * <br>
	 * The submission will fail if any exception occurs during the execution of
	 * supplier or due to thread interruption, or, if any supplier is already
	 * submitted with the same keys and the result is not yet obtained using
	 * {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)} at-least
	 * once.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param suppliers
	 *            the suppliers
	 * @param keys
	 *            the keys
	 * @return true, if successful
	 */
	static public <T> boolean submitSuppliersForSingleAccess(Supplier<T>[] suppliers, Object... keys) {
		boolean result = true;
		for (int i = 0; i < suppliers.length; i++) {
			Supplier<T> supplier = suppliers[i];
			Object[] indexedKey = Async.getIndexedKey(i, keys);
			result &= doSubmitSupplier(supplier, false, indexedKey);
		}
		return result;
	}
	
	/**
	 * Submits multiple suppliers to be invoke asynchronously for multiple access
	 * and get the status of the submission. The result can be obtained only
	 * once by invoking
	 * {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)} from any
	 * thread which will wait until the supplier code execution completes.
	 *
	 * <br>
	 * The submission will fail if any exception occurs during the execution of
	 * supplier or due to thread interruption, or, if any supplier is already
	 * submitted with the same keys and the result is not yet obtained using
	 * {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)} at-least
	 * once.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param suppliers
	 *            the suppliers
	 * @param keys
	 *            the keys
	 * @return true, if successful
	 */
	static public <T> boolean submitSuppliersForMultipleAccess(Supplier<T>[] suppliers, Object... keys) {
		boolean result = true;
		for (int i = 0; i < suppliers.length; i++) {
			Supplier<T> supplier = suppliers[i];
			Object[] indexedKey = Async.getIndexedKey(i, keys);
			result &= doSubmitSupplier(supplier, true, indexedKey);
		}
		return result;
	}
	
	/**
	 * This first drops the already submitted suppliers with the same key (if any)
	 * and then submits multiple suppliers to be invoke asynchronously for single
	 * access and get the status of the submission. The result can be obtained only
	 * once by invoking
	 * {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)} from any
	 * thread which will wait until the supplier code execution completes.
	 *
	 * <br>
	 * The submission will fail if any exception occurs during the execution of
	 * supplier or due to thread interruption, or, if any supplier is already
	 * submitted with the same keys and the result is not yet obtained using
	 * {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)} at-least once.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param suppliers
	 *            the suppliers
	 * @param keys
	 *            the keys
	 * @return true, if successful
	 */
	static public <T> boolean submitSuppliersWithDropExistingForSingleAccess(Supplier<T>[] suppliers, Object... keys) {
		dropSubmittedSuppliers(keys);
		return submitSuppliersForSingleAccess(suppliers, keys);
	}
	
	/**
	 * This first drops the already submitted suppliers with the same key (if any)
	 * and then submits multiple suppliers to be invoke asynchronously for multiple
	 * access and get the status of the submission. The result can be obtained only
	 * once by invoking
	 * {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)} from any
	 * thread which will wait until the supplier code execution completes.
	 *
	 * <br>
	 * The submission will fail if any exception occurs during the execution of
	 * supplier or due to thread interruption, or, if any supplier is already
	 * submitted with the same keys and the result is not yet obtained using
	 * {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)} at-least once.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param suppliers
	 *            the suppliers
	 * @param keys
	 *            the keys
	 * @return true, if successful
	 */
	static public <T> boolean submitSuppliersWithDropExistingForMultipleAccess(Supplier<T>[] suppliers, Object... keys) {
		dropSubmittedSuppliers(keys);
		return submitSuppliersForMultipleAccess(suppliers, keys);
	}

	/**
	 * Do submit supplier.
	 *
	 * @param <T>
	 *            the generic type
	 * @param supplier
	 *            the supplier
	 * @param multipleAccess
	 *            the multiple access
	 * @param keys
	 *            the keys
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

	/**
	 * Waits and gets the result from a supplier submitted asynchronously
	 * (using
	 * {@link AsyncSupplier#submitSupplierForSingleAccess(Supplier, Object...)}) or
	 * {@link AsyncSupplier#submitSupplierForMultipleAccess(Supplier[], Object...)})
	 * or
	 * {@link AsyncSupplier#submitSupplierWithDropExistingForSingleAccess(Supplier[], Object...)})
	 * or
	 * {@link AsyncSupplier#submitSupplierWithDropExistingForMultipleAccess(Supplier[], Object...)})
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
	static public <T> Optional<T> waitAndGetFromSupplier(Class<T> clazz, Object... keys) {
		ObjectsKey objectsKey = ObjectsKey.of(keys);
		if (Async.originalKeys.containsKey(objectsKey)) {
			synchronized (Async.originalKeys.get(objectsKey)) {
				if (Async.multipleAccessedValues.containsKey(objectsKey)) {
					return Async.getCastedValue(clazz, () -> Async.multipleAccessedValues.get(objectsKey));
				}

				if (Async.futureSuppliers.containsKey(objectsKey)) {
					Optional<T> value = Async.getCastedValue(clazz, () -> Async.futureSuppliers.get(objectsKey).get());
					Async.futureSuppliers.remove(objectsKey);

					if (Async.multipleAccessedKeys.containsKey(objectsKey)) {
						Async.multipleAccessedValues.put(objectsKey, value.orElse(null));
					} else {
						Async.originalKeys.remove(objectsKey);
					}
					return value;
				}
			}
		}
		return Optional.empty();
	}
	
	/**
	 * Waits and gets the value submitted asynchronously (using
	 * {@link AsyncSupplier#submitValue(Object, Object...)}) or
	 * {@link AsyncSupplier#submitValueWithDropExisting(Supplier, Object...)}) as a
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
	static public <T> Optional<T> waitAndGetValue(Class<T> clazz, Object... keys) {
		return waitAndGetFromSupplier(clazz, keys);
	}
	
	/**
	 * Drops a value submitted for the keys by with one of the methods
	 * {@link AsyncSupplier#submitValue(Object, Object...)},
	 * or
	 * {@link AsyncSupplier#submitValueWithDropExisting(Supplier, Object...)}
	 * <br>
	 * Once drop the submitted supplier will no longer be accessible by
	 * {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)}.
	 * 
	 * @param keys
	 *            the keys
	 */
	static public <T> void dropValue(Object... keys) {
		dropSubmittedSupplier(keys);
	}
	
	/**
	 * Drops a supplier submitted for the keys by with one of the methods
	 * {@link AsyncSupplier#submitSupplierForMultipleAccess(Supplier, Object...)},
	 * or {@link AsyncSupplier#submitSupplierForSingleAccess(Supplier, Object...)},
	 * or
	 * {@link AsyncSupplier#submitSupplierWithDropExistingForMultipleAccess(Supplier, Object...)},
	 * or
	 * {@link AsyncSupplier#submitSupplierWithDropExistingForSingleAccess(Supplier, Object...)}
	 * <br>
	 * Once drop the submitted supplier will no longer be accessible by
	 * {@link AsyncSupplier#waitAndGetFromSupplier(Class, Object...)}.
	 * 
	 * @param keys
	 *            the keys
	 */
	static public <T> void dropSubmittedSupplier(Object... keys) {
		ObjectsKey objectsKey = ObjectsKey.of(keys);
		if (Async.originalKeys.containsKey(objectsKey)) {
			synchronized (Async.originalKeys.get(objectsKey)) {
				if (Async.multipleAccessedValues.containsKey(objectsKey)) {
					Async.multipleAccessedValues.remove(objectsKey);
				}

				if (Async.futureSuppliers.containsKey(objectsKey)) {
					Async.futureSuppliers.remove(objectsKey);

					if (Async.multipleAccessedKeys.containsKey(objectsKey)) {
						Async.multipleAccessedKeys.remove(objectsKey);
					}
					Async.originalKeys.remove(objectsKey);
				}
			}
		}
	}

	/**
	 * Waits and gets the result from multiple suppliers submitted asynchronously
	 * (using
	 * {@link AsyncSupplier#submitSuppliersForSingleAccess(Supplier, Object...)}) or
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
	static public <T> Stream<T> waitAndGetFromSuppliers(Class<T> clazz, Object... keys) {
		Stream.Builder<Optional<T>> builder = Stream.builder();
		for (int i = 0; Async.originalKeys.containsKey(ObjectsKey.of(Async.getIndexedKey(i, keys))); i++) {
			Object[] indexedKey = Async.getIndexedKey(i, keys);
			builder.accept(waitAndGetFromSupplier(clazz, indexedKey));
		}
		return builder.build().filter(Optional::isPresent).map(Optional::get);
	}
	
	/**
	 * Drops the suppliers submitted for the keys by with one of the methods
	 * {@link AsyncSupplier#submitSuppliersForMultipleAccess(Supplier, Object...)},
	 * or {@link AsyncSupplier#submitSuppliersForSingleAccess(Supplier, Object...)},
	 * or
	 * {@link AsyncSupplier#submitSuppliersWithDropExistingForMultipleAccess(Supplier, Object...)},
	 * or
	 * {@link AsyncSupplier#submitSuppliersWithDropExistingForSingleAccess(Supplier, Object...)}
	 * <br>
	 * Once drop the submitted suppliers will no longer be accessible by
	 * {@link AsyncSupplier#waitAndGetFromSuppliers(Class, Object...)}.
	 * 
	 * @param keys
	 *            the keys
	 */
	static public <T> void dropSubmittedSuppliers(Object... keys) {
		for (int i = 0; Async.originalKeys.containsKey(ObjectsKey.of(Async.getIndexedKey(i, keys))); i++) {
			Object[] indexedKey = Async.getIndexedKey(i, keys);
			dropSubmittedSupplier(indexedKey);
		}
	}

	/**
	 * Submits a supplier asynchronously and gets the value as Optional. If any
	 * exception occurs during the execution of supplier or due to thread
	 * interruption it will return empty result.
	 *
	 * @param <T>
	 *            the generic type
	 * @param supplier
	 *            the supplier
	 * @return the optional
	 */
	static public <T> Optional<T> submitAndGetSupplier(Supplier<T> supplier) {
		ForkJoinTask<T> task = Async.forkJoinPool.submit(() -> supplier.get());
		return Async.safeGet(task);
	}

	/**
	 * Submit callable.
	 *
	 * @param <T>
	 *            the generic type
	 * @param callable
	 *            the callable
	 * @return the supplier
	 */
	static public <T> Supplier<T> submitCallable(Callable<T> callable) {
		return Async.safeSupplier(Async.forkJoinPool.submit(callable));
	}

	/**
	 * Submits and callable and waits until it finishes and then returns the
	 * Optional of result.
	 *
	 * @param <T>
	 *            the generic type
	 * @param callable
	 *            the callable
	 * @return the optional
	 */
	static public synchronized <T> Optional<T> submitAndGetCallable(Callable<T> callable) {
		ForkJoinTask<T> task = Async.forkJoinPool.submit(callable);
		return Async.safeGet(task);
	}

}
