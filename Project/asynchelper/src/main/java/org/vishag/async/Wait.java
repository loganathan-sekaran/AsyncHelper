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
import java.util.stream.Stream;

/**
 * The class Wait.
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
public final class Wait {

	private Wait(){
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
	 * Wait and get multiple.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param keys the keys
	 * @return the stream
	 */
	static public <T> Stream<T> waitAndGetFromMultipleSuppliers(Class<T> clazz, Object... keys) {
		Stream.Builder<Optional<T>> builder = Stream.builder();
		for (int i = 0; Async.originalKeys.containsKey(ObjectsKey.of(Async.getIndexedKey(i, keys))); i++) {
			Object[] indexedKey = Async.getIndexedKey(i, keys);
			builder.accept(waitAndGet(clazz, indexedKey));
		}
		return builder.build().filter(Optional::isPresent).map(Optional::get);
	}

	/**
	 * Wait for multiple tasks.
	 *
	 * @param keys the keys
	 */
	static public void waitForMultipleTasks(Object... keys) {
		for (int i = 0; Async.originalKeys.containsKey(ObjectsKey.of(Async.getIndexedKey(i, keys))); i++) {
			Object[] indexedKey = Async.getIndexedKey(i, keys);
			waitForTask(indexedKey);
		}
	}

	/**
	 * Wait for task.
	 *
	 * @param keys the keys
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

	/**
	 * Wait for flag.
	 *
	 * @param flag the flag
	 * @throws InterruptedException the interrupted exception
	 */
	static public void waitForFlag(String... flag) throws InterruptedException {
		ObjectsKey key = ObjectsKey.of((Object[])flag);
		ObjectsKey originalKey = Async.originalKeys.get(key);
		if(originalKey == null) {
			originalKey = key;
			Async.originalKeys.put(key, originalKey);
		}
		synchronized (originalKey) {
			originalKey.wait();
		}
	}
}
