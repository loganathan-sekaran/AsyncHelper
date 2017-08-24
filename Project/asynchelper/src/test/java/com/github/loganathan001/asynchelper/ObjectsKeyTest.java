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

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.loganathan001.asynchelper.ObjectsKey;

public class ObjectsKeyTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test (expected = AssertionError.class)
	public void testEmptyArray() {
		ObjectsKey.of(new Object[0]);
	}
	
	@Test (expected = AssertionError.class)
	public void testNullKey() {
		ObjectsKey.of(new Object[]{null});
	}
	
	@Test
	public void testEqualKey() {
		assertEquals(ObjectsKey.of("111"), ObjectsKey.of("111"));
		assertEquals(ObjectsKey.of("111", Integer.class), ObjectsKey.of("111", Integer.class));
		assertEquals(ObjectsKey.of("111", String.class), ObjectsKey.of(String.class, "111"));
	}
	
	@Test
	public void testUnEqualKey() {
		assertNotEquals(ObjectsKey.of("111"), ObjectsKey.of("222"));
		assertNotEquals(ObjectsKey.of("111", Integer.class), ObjectsKey.of("111"));
		assertNotEquals(ObjectsKey.of("111"), ObjectsKey.of("111", String.class));
	}

}
