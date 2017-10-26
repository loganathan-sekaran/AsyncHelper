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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 * The class SchedulingSupplierTest.
 * 
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
public final class AsyncSupplierTest {

	/** The watcher. */
	@Rule
	public TestRule watcher = new TestWatcherAndLogger();

	//
	// @BeforeClass
	// public static void setUpBeforeClass() throws Exception {
	// }
	//
	// @AfterClass
	// public static void tearDownAfterClass() throws Exception {
	// }

	// @Before
	// public void setUp() throws Exception {
	// }
	//
	// @After
	// public void tearDown() throws Exception {
	// }

	/**
	 * Test multiple async supplier submitted for multiple access.
	 */
	@Test
	public void testMultipleAsyncSupplierSubmittedForMultipleAccess() {
		AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query1");
		AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value2", 1000), "query2");
		AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value3", 700), "query3");
		AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value4", 1000), "query4");

		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query1").get(), "Value1");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query2").get(), "Value2");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query3").get(), "Value3");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query4").get(), "Value4");

		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query1").get(), "Value1");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query2").get(), "Value2");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query3").get(), "Value3");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query4").get(), "Value4");
	}

	/**
	 * Test multiple async supplier submitted for single access.
	 */
	@Test
	public void testMultipleAsyncSupplierSubmittedForSingleAccess() {
		AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query11");
		AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value2", 1000), "query22");
		AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value3", 700), "query33");
		AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value4", 1000), "query44");

		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query11").get(), "Value1");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query22").get(), "Value2");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query33").get(), "Value3");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query44").get(), "Value4");

		assertFalse(AsyncSupplier.waitAndGetFromSupplier(String.class, "query11").isPresent());
		assertFalse(AsyncSupplier.waitAndGetFromSupplier(String.class, "query22").isPresent());
		assertFalse(AsyncSupplier.waitAndGetFromSupplier(String.class, "query33").isPresent());
		assertFalse(AsyncSupplier.waitAndGetFromSupplier(String.class, "query44").isPresent());
	}

	/**
	 * Test multiple time async supplier submitted for multiple access.
	 */
	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForMultipleAccess() {
		assertTrue(
				AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query1111"));
		assertTrue(AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value2", 1000),
				"query2222"));
		assertTrue(AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value3", 700),
				"query3333"));
		assertTrue(AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value4", 1000),
				"query4444"));

		assertFalse(
				AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value1a"), "query1111"));
		assertFalse(AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value2a", 1000),
				"query2222"));
		assertFalse(AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value3a", 700),
				"query3333"));
		assertFalse(AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value4a", 1000),
				"query4444"));

		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query1111").get(), "Value1");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query2222").get(), "Value2");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query3333").get(), "Value3");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query4444").get(), "Value4");

		assertTrue(
				AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value11"), "query1111"));
		assertTrue(AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value22", 1000),
				"query2222"));
		assertTrue(AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value33", 700),
				"query3333"));
		assertTrue(AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value44", 1000),
				"query4444"));

		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query1111").get(), "Value11");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query2222").get(), "Value22");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query3333").get(), "Value33");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query4444").get(), "Value44");
	}

	/**
	 * Test multiple time async supplier submitted for multiple access then
	 * single access.
	 */
	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForMultipleAccessThenSingleAccess() {
		assertTrue(
				AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query11111"));
		assertTrue(AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value2", 1000),
				"query22222"));
		assertTrue(AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value3", 700),
				"query33333"));
		assertTrue(AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value4", 1000),
				"query44444"));

		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query11111").get(), "Value1");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query22222").get(), "Value2");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query33333").get(), "Value3");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query44444").get(), "Value4");

		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query11111").get(), "Value1");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query22222").get(), "Value2");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query33333").get(), "Value3");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query44444").get(), "Value4");

		assertTrue(
				AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value11"), "query11111"));
		assertTrue(AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value22", 1000),
				"query22222"));
		assertTrue(AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value33", 700),
				"query33333"));
		assertTrue(AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value44", 1000),
				"query44444"));

		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query11111").get(), "Value11");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query22222").get(), "Value22");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query33333").get(), "Value33");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query44444").get(), "Value44");

		assertFalse(AsyncSupplier.waitAndGetFromSupplier(String.class, "query11111").isPresent());
		assertFalse(AsyncSupplier.waitAndGetFromSupplier(String.class, "query22222").isPresent());
		assertFalse(AsyncSupplier.waitAndGetFromSupplier(String.class, "query33333").isPresent());
		assertFalse(AsyncSupplier.waitAndGetFromSupplier(String.class, "query44444").isPresent());
	}

	/**
	 * Test multiple time async supplier submitted for single access.
	 */
	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForSingleAccess() {
		assertTrue(AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query111"));
		assertTrue(AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value2", 1000),
				"query222"));
		assertTrue(
				AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value3", 700), "query333"));
		assertTrue(AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value4", 1000),
				"query444"));

		assertFalse(AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query111"));
		assertFalse(AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value2", 1000),
				"query222"));
		assertFalse(
				AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value3", 700), "query333"));
		assertFalse(AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value4", 1000),
				"query444"));

		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query111").get(), "Value1");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query222").get(), "Value2");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query333").get(), "Value3");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query444").get(), "Value4");

		assertTrue(AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value11"), "query111"));
		assertTrue(AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value22", 1000),
				"query222"));
		assertTrue(AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value33", 700),
				"query333"));
		assertTrue(AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value44", 1000),
				"query444"));

		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query111").get(), "Value11");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query222").get(), "Value22");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query333").get(), "Value33");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query444").get(), "Value44");

	}

	/**
	 * Test multiple time async supplier submitted for single access then
	 * multiple access.
	 */
	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForSingleAccessThenMultipleAccess() {
		assertTrue(
				AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query111111"));
		assertTrue(AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value2", 1000),
				"query222222"));
		assertTrue(AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value3", 700),
				"query333333"));
		assertTrue(AsyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value4", 1000),
				"query444444"));

		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query111111").get(), "Value1");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query222222").get(), "Value2");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query333333").get(), "Value3");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query444444").get(), "Value4");

		assertFalse(AsyncSupplier.waitAndGetFromSupplier(String.class, "query111111").isPresent());
		assertFalse(AsyncSupplier.waitAndGetFromSupplier(String.class, "query222222").isPresent());
		assertFalse(AsyncSupplier.waitAndGetFromSupplier(String.class, "query333333").isPresent());
		assertFalse(AsyncSupplier.waitAndGetFromSupplier(String.class, "query444444").isPresent());

		assertTrue(AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value11"),
				"query111111"));
		assertTrue(AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value22", 1000),
				"query222222"));
		assertTrue(AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value33", 700),
				"query333333"));
		assertTrue(AsyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value44", 1000),
				"query444444"));

		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query111111").get(), "Value11");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query222222").get(), "Value22");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query333333").get(), "Value33");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query444444").get(), "Value44");

		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query111111").get(), "Value11");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query222222").get(), "Value22");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query333333").get(), "Value33");
		assertEquals(AsyncSupplier.waitAndGetFromSupplier(String.class, "query444444").get(), "Value44");
	}

	/**
	 * Test suppliers.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testSuppliers() throws InterruptedException {
		Supplier<Integer>[] resultSuppliers = AsyncSupplier.submitSuppliers(TestUtil.delayedSupplier(() -> {
			return 10;
		}, 100), TestUtil.delayedSupplier(() -> {
			return 20;
		}, 100), TestUtil.delayedSupplier(() -> {
			return 30;
		}, 100), TestUtil.delayedSupplier(() -> {
			return 40;
		}, 100), TestUtil.delayedSupplier(() -> {
			return 50;
		}, 100));

		assertEquals(resultSuppliers.length, 5);
		AtomicInteger val = new AtomicInteger(0);
		Stream.of(resultSuppliers).map(Supplier::get).forEach(value -> assertEquals(val.addAndGet(10), (int) value));

	}

	/**
	 * Testsuppliers for single access.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testsuppliersForSingleAccess() throws InterruptedException {
		@SuppressWarnings("unchecked")
		boolean suppliers = AsyncSupplier
				.submitSuppliersForSingleAccess(new Supplier[] { TestUtil.delayedSupplier(() -> {
					return 10;
				}, 100), TestUtil.delayedSupplier(() -> {
					return 20;
				}, 100), TestUtil.delayedSupplier(() -> {
					return 30;
				}, 100), TestUtil.delayedSupplier(() -> {
					return 40;
				}, 100), TestUtil.delayedSupplier(() -> {
					return 50;
				}, 100) }, "Multiple", "Suppliers", "key");

		assertTrue(suppliers);

		List<Integer> retVals = AsyncSupplier.waitAndGetFromSuppliers(Integer.class, "Multiple", "Suppliers", "key")
				.collect(Collectors.toList());
		assertEquals(retVals.size(), 5);
		AtomicInteger val = new AtomicInteger(0);
		retVals.forEach(value -> assertEquals(val.addAndGet(10), (int) value));

	}
	
	/**
	 * Test submit.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testSubmit() throws InterruptedException {
		assertEquals(AsyncSupplier.submitAndGetSupplier(TestUtil.delayedSupplier(() -> "Value1")).get(), "Value1");
		assertEquals(AsyncSupplier.submitAndGetCallable(() -> TestUtil.delayedSupplier(() -> "Value2")).get().get(),
				"Value2");
		assertEquals(AsyncSupplier.submitSupplier(TestUtil.delayedSupplier(() -> "Value3")).get(), "Value3");
		assertEquals(AsyncSupplier.submitCallable(() -> TestUtil.delayedSupplier(() -> "Value4")).get().get(), "Value4");

		String[] val = new String[1];
		AsyncTask.submitTask(() -> {
			val[0] = "Value5";
		});
		Thread.sleep(1000);
		assertEquals(val[0], "Value5");
	}
}
