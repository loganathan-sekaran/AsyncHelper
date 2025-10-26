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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The class ObjectsKey - This is internally used as a key made of multiple
 * objects (the order does not matter). The equality and hash-code generation of
 * this key will be based on the equality and hash-codes of the objects.
 * 
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
class ObjectsKey implements AutoCloseable{

	/** The keys. */
	private final List<Object> keys;
	
	/** The closed. */
	private volatile boolean closed;

	/**
	 * Instantiates a new objects key.
	 *
	 * @param keys
	 *            the keys
	 */
	private ObjectsKey(Object[] keys) {
		this.keys = Collections.synchronizedList(new ArrayList<>(Arrays.asList(keys)));
	}

	/**
	 * Of.
	 *
	 * @param keys
	 *            the keys
	 * @return the objects key
	 */
	public static ObjectsKey of(Object... keys) {
		assert (keys != null);
		assert (keys.length > 0);
		for (Object key : keys) {
			assert (key != null);
		}
		return new ObjectsKey(keys);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		assertNotClosed();
		final int prime = 31;
		int result = 1;
		for (Object obj : keys) {
			result = prime * result + ((obj == null) ? 0 : obj.hashCode());
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		assertNotClosed();
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ObjectsKey other = (ObjectsKey) obj;
		return this.keys.containsAll(other.keys) && other.keys.containsAll(this.keys);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ObjectsKey [keys=" + keys + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public synchronized void close() {
		if(!closed) {
			keys.clear();
			closed = true;
		}
	}
	
	/**
	 * Assert not closed.
	 */
	private void assertNotClosed() {
		if (closed) {
			throw new IllegalStateException("Already closed");
		}
	}

}