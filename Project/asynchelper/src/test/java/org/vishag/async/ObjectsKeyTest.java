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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 * The class ObjectsKeyTest.
 * 
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
public class ObjectsKeyTest {

	/** The watcher. */
	@Rule
	public TestRule watcher = new TestWatcherAndLogger();

	/**
	 * Test empty array.
	 */
	@Test(expected = AssertionError.class)
	public void testEmptyArray() {
		ObjectsKey.of(new Object[0]);
	}

	/**
	 * Test null key.
	 */
	@Test(expected = AssertionError.class)
	public void testNullKey() {
		ObjectsKey.of(new Object[] { null });
	}

	/**
	 * Test equal key.
	 */
	@Test
	public void testEqualKey() {
		ObjectsKey objectsKey = ObjectsKey.of("111");
		assertEquals(objectsKey, objectsKey);
		assertEquals(ObjectsKey.of("111"), ObjectsKey.of("111"));
		assertEquals(ObjectsKey.of("111", Integer.class), ObjectsKey.of("111", Integer.class));
		assertEquals(ObjectsKey.of("111", String.class), ObjectsKey.of(String.class, "111"));
	}

	/**
	 * Test un equal key.
	 */
	@Test
	public void testUnEqualKey() {
		assertNotEquals(ObjectsKey.of("111"), null);
		assertNotEquals(ObjectsKey.of("111"), 111);
		assertNotEquals(ObjectsKey.of("111"), ObjectsKey.of("222"));
		assertNotEquals(ObjectsKey.of("111", Integer.class), ObjectsKey.of("111"));
		assertNotEquals(ObjectsKey.of("111"), ObjectsKey.of("111", String.class));
	}
	
	/**
	 * Test close.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testClose() throws Exception {
		ObjectsKey key = ObjectsKey.of("123");
		assertTrue(key.equals(ObjectsKey.of("123")));
		key.close();
		key.close();
	}
	
	/**
	 * Test close with exception.
	 *
	 * @throws Exception the exception
	 */
	@Test (expected=Exception.class)
	public void testCloseWithException() throws Exception {
		ObjectsKey key = ObjectsKey.of("1234");
		assertTrue(key.equals(ObjectsKey.of("1234")));
		key.close();
		key.equals(ObjectsKey.of("1234"));
		fail();
	}

}
