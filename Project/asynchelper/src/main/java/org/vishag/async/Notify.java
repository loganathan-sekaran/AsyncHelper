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

import java.util.stream.Stream;

/**
 * The class Notify .
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
public final class Notify {
	
	private Notify() {
	}

	/**
	 * Notify all flag.
	 *
	 * @param flag the flag
	 */
	static public void notifyAllFlag(String... flag) {
		Notify.notify(true, flag);
	}

	/**
	 * Notify flag.
	 *
	 * @param flag the flag
	 */
	static public void notifyFlag(String... flag) {
		notify(false, flag);
	}

	private static void notify(boolean all, String... flag) {
		ObjectsKey key = ObjectsKey.of((Object[])flag);
		ObjectsKey originalKey = Async.originalKeys.get(key);
		if(originalKey != null) {
			Async.originalKeys.remove(key);
			synchronized (originalKey) {
				if(all) {
					originalKey.notifyAll();
				} else {
					originalKey.notify();
				}
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
		Object[] indexedKey = Async.getIndexedKey(count, (Object[])flag);
		while(Async.originalKeys.containsKey(ObjectsKey.of(indexedKey))) {
			Wait.waitAndGet(clazz, indexedKey).ifPresent(builder::accept);
			count++;
			 indexedKey = Async.getIndexedKey(count,  (Object[])flag);
		}
		return builder.build();
	}
}
