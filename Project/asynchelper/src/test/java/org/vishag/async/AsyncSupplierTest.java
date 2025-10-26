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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * The class SchedulingSupplierTest.
 * 
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */

@RunWith(Parameterized.class)
public final class AsyncSupplierTest {

	/** The watcher. */
	@Rule
	public TestRule watcher = new TestWatcherAndLogger();
	
	/** The async supplier. */
	private AsyncSupplier asyncSupplier;
	
	/**
	 * Inputs.
	 *
	 * @return the collection
	 */
	@Parameters
	public static Collection<Object[]> inputs() {
		return Arrays.asList(new Object[][] {
				{AsyncSupplier.getDefault()},
				{AsyncSupplier.of(Executors.newFixedThreadPool(10))	},
				{AsyncSupplier.of(Executors.newFixedThreadPool(10), AsyncContext.newInstance())	}
			});
	}
	
	 /**
 	 * Instantiates a new async supplier test.
 	 *
 	 * @param asyncSupplier the async supplier
 	 * @throws Exception the exception
 	 */
 	public AsyncSupplierTest(AsyncSupplier asyncSupplier) throws Exception {
		 this.asyncSupplier = asyncSupplier;
	 }
	 
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
		asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query1");
		asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value2", 100), "query2");
		asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value3", 70), "query3");
		asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value4", 100), "query4");

		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query1").get(), "Value1");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query2").get(), "Value2");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query3").get(), "Value3");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query4").get(), "Value4");

		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query1").get(), "Value1");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query2").get(), "Value2");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query3").get(), "Value3");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query4").get(), "Value4");
	}

	/**
	 * Test multiple async supplier submitted for single access.
	 */
	@Test
	public void testMultipleAsyncSupplierSubmittedForSingleAccess() {
		asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query11");
		asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value2", 100), "query22");
		asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value3", 70), "query33");
		asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value4", 100), "query44");

		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query11").get(), "Value1");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query22").get(), "Value2");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query33").get(), "Value3");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query44").get(), "Value4");

		assertFalse(asyncSupplier.waitAndGetFromSupplier(String.class, "query11").isPresent());
		assertFalse(asyncSupplier.waitAndGetFromSupplier(String.class, "query22").isPresent());
		assertFalse(asyncSupplier.waitAndGetFromSupplier(String.class, "query33").isPresent());
		assertFalse(asyncSupplier.waitAndGetFromSupplier(String.class, "query44").isPresent());
	}

	/**
	 * Test multiple time async supplier submitted for multiple access.
	 */
	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForMultipleAccess() {
		assertTrue(
				asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query1111"));
		assertTrue(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value2", 100),
				"query2222"));
		assertTrue(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value3", 70),
				"query3333"));
		assertTrue(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value4", 100),
				"query4444"));

		assertFalse(
				asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value1a"), "query1111"));
		assertFalse(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value2a", 100),
				"query2222"));
		assertFalse(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value3a", 70),
				"query3333"));
		assertFalse(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value4a", 100),
				"query4444"));

		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query1111").get(), "Value1");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query2222").get(), "Value2");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query3333").get(), "Value3");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query4444").get(), "Value4");

		assertTrue(
				asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value11"), "query1111"));
		assertTrue(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value22", 100),
				"query2222"));
		assertTrue(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value33", 70),
				"query3333"));
		assertTrue(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value44", 100),
				"query4444"));

		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query1111").get(), "Value11");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query2222").get(), "Value22");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query3333").get(), "Value33");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query4444").get(), "Value44");
	}
	
	/**
	 * Test multiple time async supplier submitted for multiple access then
	 * single access.
	 */
	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForMultipleAccessThenSingleAccess() {
		assertTrue(
				asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query11111"));
		assertTrue(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value2", 100),
				"query22222"));
		assertTrue(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value3", 70),
				"query33333"));
		assertTrue(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value4", 100),
				"query44444"));

		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query11111").get(), "Value1");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query22222").get(), "Value2");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query33333").get(), "Value3");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query44444").get(), "Value4");

		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query11111").get(), "Value1");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query22222").get(), "Value2");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query33333").get(), "Value3");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query44444").get(), "Value4");

		assertTrue(
				asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value11"), "query11111"));
		assertTrue(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value22", 100),
				"query22222"));
		assertTrue(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value33", 70),
				"query33333"));
		assertTrue(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value44", 100),
				"query44444"));

		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query11111").get(), "Value11");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query22222").get(), "Value22");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query33333").get(), "Value33");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query44444").get(), "Value44");

		assertFalse(asyncSupplier.waitAndGetFromSupplier(String.class, "query11111").isPresent());
		assertFalse(asyncSupplier.waitAndGetFromSupplier(String.class, "query22222").isPresent());
		assertFalse(asyncSupplier.waitAndGetFromSupplier(String.class, "query33333").isPresent());
		assertFalse(asyncSupplier.waitAndGetFromSupplier(String.class, "query44444").isPresent());
	}

	/**
	 * Test multiple time async supplier submitted for single access.
	 */
	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForSingleAccess() {
		assertTrue(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query111"));
		assertTrue(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value2", 100),
				"query222"));
		assertTrue(
				asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value3", 70), "query333"));
		assertTrue(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value4", 100),
				"query444"));

		assertFalse(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query111"));
		assertFalse(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value2", 100),
				"query222"));
		assertFalse(
				asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value3", 70), "query333"));
		assertFalse(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value4", 100),
				"query444"));

		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query111").get(), "Value1");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query222").get(), "Value2");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query333").get(), "Value3");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query444").get(), "Value4");

		assertTrue(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value11"), "query111"));
		assertTrue(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value22", 100),
				"query222"));
		assertTrue(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value33", 70),
				"query333"));
		assertTrue(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value44", 100),
				"query444"));

		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query111").get(), "Value11");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query222").get(), "Value22");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query333").get(), "Value33");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query444").get(), "Value44");

	}

	/**
	 * Test multiple time async supplier submitted for single access then
	 * multiple access.
	 */
	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForSingleAccessThenMultipleAccess() {
		assertTrue(
				asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query111111"));
		assertTrue(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value2", 100),
				"query222222"));
		assertTrue(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value3", 70),
				"query333333"));
		assertTrue(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value4", 100),
				"query444444"));

		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query111111").get(), "Value1");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query222222").get(), "Value2");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query333333").get(), "Value3");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query444444").get(), "Value4");

		assertFalse(asyncSupplier.waitAndGetFromSupplier(String.class, "query111111").isPresent());
		assertFalse(asyncSupplier.waitAndGetFromSupplier(String.class, "query222222").isPresent());
		assertFalse(asyncSupplier.waitAndGetFromSupplier(String.class, "query333333").isPresent());
		assertFalse(asyncSupplier.waitAndGetFromSupplier(String.class, "query444444").isPresent());

		assertTrue(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value11"),
				"query111111"));
		assertTrue(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value22", 100),
				"query222222"));
		assertTrue(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value33", 70),
				"query333333"));
		assertTrue(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value44", 100),
				"query444444"));

		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query111111").get(), "Value11");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query222222").get(), "Value22");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query333333").get(), "Value33");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query444444").get(), "Value44");

		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query111111").get(), "Value11");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query222222").get(), "Value22");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query333333").get(), "Value33");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query444444").get(), "Value44");
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
		Supplier<Integer>[] resultSuppliers = asyncSupplier.submitSuppliers(TestUtil.delayedSupplier(() -> {
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
		boolean suppliers = asyncSupplier
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

		List<Integer> retVals = asyncSupplier.waitAndGetFromSuppliers(Integer.class, "Multiple", "Suppliers", "key")
				.collect(Collectors.toList());
		assertEquals(retVals.size(), 5);
		AtomicInteger val = new AtomicInteger(0);
		retVals.forEach(value -> assertEquals(val.addAndGet(10), (int) value));

	}
	
	/**
	 * Testsuppliers for single access 2.
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	@Test
	public void testsuppliersForSingleAccess2() throws InterruptedException {
		@SuppressWarnings("unchecked")
		boolean suppliers = asyncSupplier
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

		List<Integer> retVals = asyncSupplier.waitAndGetFromSuppliers(Integer.class, "Multiple", "Suppliers", "key")
				.collect(Collectors.toList());
		assertEquals(retVals.size(), 5);
		AtomicInteger val = new AtomicInteger(0);
		retVals.forEach(value -> assertEquals(val.addAndGet(10), (int) value));
		
		List<Integer> retVals1 = asyncSupplier.waitAndGetFromSuppliers(Integer.class, "Multiple", "Suppliers", "key")
				.collect(Collectors.toList());
		assertEquals(retVals1.size(), 0);
	}
	
	
	/**
	 * Testsuppliers for multiple access.
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	@Test
	public void testsuppliersForMultipleAccess() throws InterruptedException {
		@SuppressWarnings("unchecked")
		boolean suppliers = asyncSupplier
				.submitSuppliersForMultipleAccess(new Supplier[] { TestUtil.delayedSupplier(() -> {
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

		List<Integer> retVals = asyncSupplier.waitAndGetFromSuppliers(Integer.class, "Multiple", "Suppliers", "key")
				.collect(Collectors.toList());
		assertEquals(retVals.size(), 5);
		AtomicInteger val = new AtomicInteger(0);
		retVals.forEach(value -> assertEquals(val.addAndGet(10), (int) value));
		
		List<Integer> retVals1 = asyncSupplier.waitAndGetFromSuppliers(Integer.class, "Multiple", "Suppliers", "key")
				.collect(Collectors.toList());
		assertEquals(retVals1.size(), 5);
		AtomicInteger val1 = new AtomicInteger(0);
		retVals1.forEach(value -> assertEquals(val1.addAndGet(10), (int) value));

	}
	
	/**
	 * Test submit.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testSubmit() throws InterruptedException {
		assertEquals(asyncSupplier.submitAndGetSupplier(TestUtil.delayedSupplier(() -> "Value1")).get(), "Value1");
		assertEquals(asyncSupplier.submitAndGetCallable(() -> TestUtil.delayedSupplier(() -> "Value2")).get().get(),
				"Value2");
		assertEquals(asyncSupplier.submitSupplier(TestUtil.delayedSupplier(() -> "Value3")).get(), "Value3");
		assertEquals(asyncSupplier.submitCallable(() -> TestUtil.delayedSupplier(() -> "Value4")).get().get(), "Value4");

		String[] val = new String[1];
		AsyncTask.getDefault().submitTask(() -> {
			val[0] = "Value5";
		});
		Thread.sleep(100);
		assertEquals(val[0], "Value5");
	}

	/**
	 * Test drop suppliers submitted for multiple access.
	 */
	@Test
	public void testDropSuppliersSubmittedForMultipleAccess() {
		@SuppressWarnings("unchecked")
		boolean suppliers = asyncSupplier
				.submitSuppliersForMultipleAccess(new Supplier[] { TestUtil.delayedSupplier(() -> {
					return 10;
				}, 100), TestUtil.delayedSupplier(() -> {
					return 20;
				}, 100), TestUtil.delayedSupplier(() -> {
					return 30;
				}, 100), TestUtil.delayedSupplier(() -> {
					return 40;
				}, 100), TestUtil.delayedSupplier(() -> {
					return 50;
				}, 100) }, "Multiple", "Suppliers", "key1");

		assertTrue(suppliers);
		
		asyncSupplier.dropSubmittedSuppliers("Multiple", "Suppliers", "key1");

		List<Integer> retVals = asyncSupplier.waitAndGetFromSuppliers(Integer.class, "Multiple", "Suppliers", "key1")
				.collect(Collectors.toList());
		assertEquals(retVals.size(), 0);
	
	}

	/**
	 * Test drop suppliers submitted for multiple access with one fetch.
	 */
	public void testDropSuppliersSubmittedForMultipleAccessWithOneFetch() {
		@SuppressWarnings("unchecked")
		boolean suppliers = asyncSupplier
				.submitSuppliersForMultipleAccess(new Supplier[] { TestUtil.delayedSupplier(() -> {
					return 10;
				}, 100), TestUtil.delayedSupplier(() -> {
					return 20;
				}, 100), TestUtil.delayedSupplier(() -> {
					return 30;
				}, 100), TestUtil.delayedSupplier(() -> {
					return 40;
				}, 100), TestUtil.delayedSupplier(() -> {
					return 50;
				}, 100) }, "Multiple", "Suppliers", "key2");

		assertTrue(suppliers);
		
		asyncSupplier.dropSubmittedSuppliers("Multiple", "Suppliers", "key2");

		List<Integer> retVals = asyncSupplier.waitAndGetFromSuppliers(Integer.class, "Multiple", "Suppliers", "key2")
				.collect(Collectors.toList());
		assertEquals(retVals.size(), 5);
		AtomicInteger val = new AtomicInteger(0);
		retVals.forEach(value -> assertEquals(val.addAndGet(10), (int) value));
		
		List<Integer> retVals1 = asyncSupplier.waitAndGetFromSuppliers(Integer.class, "Multiple", "Suppliers", "key2")
				.collect(Collectors.toList());
		assertEquals(retVals1.size(), 0);
	
	}

	/**
	 * Test drop suppliers submitted for single access.
	 */
	@Test
	public void testDropSuppliersSubmittedForSingleAccess() {
		@SuppressWarnings("unchecked")
		boolean suppliers = asyncSupplier
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
				}, 100) }, "Multiple", "Suppliers", "key3");

		assertTrue(suppliers);
		
		asyncSupplier.dropSubmittedSuppliers("Multiple", "Suppliers", "key3");

		List<Integer> retVals = asyncSupplier.waitAndGetFromSuppliers(Integer.class, "Multiple", "Suppliers", "key3")
				.collect(Collectors.toList());
		assertEquals(retVals.size(), 0);
	
	}

	/**
	 * Test drop suppliers submitted for single access with one fetch.
	 */
	public void testDropSuppliersSubmittedForSingleAccessWithOneFetch() {
		@SuppressWarnings("unchecked")
		boolean suppliers = asyncSupplier
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
				}, 100) }, "Multiple", "Suppliers", "key4");

		assertTrue(suppliers);
		
		asyncSupplier.dropSubmittedSuppliers("Multiple", "Suppliers", "key4");

		List<Integer> retVals = asyncSupplier.waitAndGetFromSuppliers(Integer.class, "Multiple", "Suppliers", "key4")
				.collect(Collectors.toList());
		assertEquals(retVals.size(), 5);
		AtomicInteger val = new AtomicInteger(0);
		retVals.forEach(value -> assertEquals(val.addAndGet(10), (int) value));
		
		List<Integer> retVals1 = asyncSupplier.waitAndGetFromSuppliers(Integer.class, "Multiple", "Suppliers", "key4")
				.collect(Collectors.toList());
		assertEquals(retVals1.size(), 0);
	
	}

	/**
	 * Test drop non existing suppliers.
	 */
	@Test
	public void testDropNonExistingSuppliers() {
		asyncSupplier.dropSubmittedSuppliers("Multiple", "Suppliers", "key5");

		List<Integer> retVals = asyncSupplier.waitAndGetFromSuppliers(Integer.class, "Multiple", "Suppliers", "key5")
				.collect(Collectors.toList());
		assertEquals(retVals.size(), 0);
	}

	/**
	 * Test drop supplier submitted for multiple access.
	 */
	@Test
	public void testDropSupplierSubmittedForMultipleAccess() {
		assertTrue(
				asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query1111"));
		assertTrue(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value2", 100),
				"query2222"));
		assertTrue(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value3", 70),
				"query3333"));
		assertTrue(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value4", 100),
				"query4444"));
	
		
		asyncSupplier.dropSubmittedSupplier("query1111");
		asyncSupplier.dropSubmittedSupplier("query2222");
		asyncSupplier.dropSubmittedSupplier("query3333");
		asyncSupplier.dropSubmittedSupplier("query4444");
	
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query1111").isPresent(), false);
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query2222").isPresent(), false);
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query3333").isPresent(), false);
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query4444").isPresent(), false);
	
	}

	/**
	 * Test drop supplier submitted for multiple access with one fetch.
	 */
	public void testDropSupplierSubmittedForMultipleAccessWithOneFetch() {
		assertTrue(
				asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query1111"));
		assertTrue(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value2", 100),
				"query2222"));
		assertTrue(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value3", 70),
				"query3333"));
		assertTrue(asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value4", 100),
				"query4444"));
		
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query1111").get(), "Value1");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query2222").get(), "Value2");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query3333").get(), "Value3");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query4444").get(), "Value4");
	
		
		asyncSupplier.dropSubmittedSupplier("query1111");
		asyncSupplier.dropSubmittedSupplier("query2222");
		asyncSupplier.dropSubmittedSupplier("query3333");
		asyncSupplier.dropSubmittedSupplier("query4444");
	
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query1111").isPresent(), false);
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query2222").isPresent(), false);
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query3333").isPresent(), false);
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query4444").isPresent(), false);
	
	}

	/**
	 * Test drop supplier submitted for single access.
	 */
	@Test
	public void testDropSupplierSubmittedForSingleAccess() {
		assertTrue(
				asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query1111"));
		assertTrue(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value2", 100),
				"query2222"));
		assertTrue(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value3", 70),
				"query3333"));
		assertTrue(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value4", 100),
				"query4444"));
		
		asyncSupplier.dropSubmittedSupplier("query1111");
		asyncSupplier.dropSubmittedSupplier("query2222");
		asyncSupplier.dropSubmittedSupplier("query3333");
		asyncSupplier.dropSubmittedSupplier("query4444");
	
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query1111").isPresent(), false);
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query2222").isPresent(), false);
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query3333").isPresent(), false);
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query4444").isPresent(), false);
	
	}

	/**
	 * Test drop supplier submitted for single access with one fetch.
	 */
	public void testDropSupplierSubmittedForSingleAccessWithOneFetch() {
		assertTrue(
				asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query1111"));
		assertTrue(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value2", 100),
				"query2222"));
		assertTrue(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value3", 70),
				"query3333"));
		assertTrue(asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value4", 100),
				"query4444"));
		
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query1111").get(), "Value1");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query2222").get(), "Value2");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query3333").get(), "Value3");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query4444").get(), "Value4");
	
		
		asyncSupplier.dropSubmittedSupplier("query1111");
		asyncSupplier.dropSubmittedSupplier("query2222");
		asyncSupplier.dropSubmittedSupplier("query3333");
		asyncSupplier.dropSubmittedSupplier("query4444");
	
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query1111").isPresent(), false);
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query2222").isPresent(), false);
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query3333").isPresent(), false);
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query4444").isPresent(), false);
	
	}

	/**
	 * Test drop non existing supplier.
	 */
	@Test
	public void testDropNonExistingSupplier() {
		asyncSupplier.dropSubmittedSupplier("query1111aa");
		asyncSupplier.dropSubmittedSupplier("query2222aa");
		asyncSupplier.dropSubmittedSupplier("query3333aa");
		asyncSupplier.dropSubmittedSupplier("query4444aa");
	
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query1111aa").isPresent(), false);
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query2222aa").isPresent(), false);
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query3333aa").isPresent(), false);
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query4444aa").isPresent(), false);
	}

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
	 * Test multiple async supplier submitted with drop existing for multiple access 0.
	 */
	@Test
	public void testMultipleAsyncSupplierSubmittedWithDropExistingForMultipleAccess0() {
		asyncSupplier.submitSupplierWithDropExistingForMultipleAccess(TestUtil.delayedSupplier(() -> "Value11"), "query1aa");
		asyncSupplier.submitSupplierWithDropExistingForMultipleAccess(TestUtil.delayedSupplier(() -> "Value22", 100), "query2aa");
		asyncSupplier.submitSupplierWithDropExistingForMultipleAccess(TestUtil.delayedSupplier(() -> "Value33", 70), "query3aa");
		asyncSupplier.submitSupplierWithDropExistingForMultipleAccess(TestUtil.delayedSupplier(() -> "Value44", 100), "query4aa");
	
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query1aa").get(), "Value11");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query2aa").get(), "Value22");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query3aa").get(), "Value33");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query4aa").get(), "Value44");
	
	}
	
	/**
	 * Test multiple async supplier submitted with drop existing for multiple access.
	 */
	@Test
	public void testMultipleAsyncSupplierSubmittedWithDropExistingForMultipleAccess() {
		asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query1");
		asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value2", 100), "query2");
		asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value3", 70), "query3");
		asyncSupplier.submitSupplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value4", 100), "query4");
		
		asyncSupplier.submitSupplierWithDropExistingForMultipleAccess(TestUtil.delayedSupplier(() -> "Value11"), "query1");
		asyncSupplier.submitSupplierWithDropExistingForMultipleAccess(TestUtil.delayedSupplier(() -> "Value22", 100), "query2");
		asyncSupplier.submitSupplierWithDropExistingForMultipleAccess(TestUtil.delayedSupplier(() -> "Value33", 70), "query3");
		asyncSupplier.submitSupplierWithDropExistingForMultipleAccess(TestUtil.delayedSupplier(() -> "Value44", 100), "query4");
	
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query1").get(), "Value11");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query2").get(), "Value22");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query3").get(), "Value33");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query4").get(), "Value44");
	
	}
	
	/**
	 * Test multiple async supplier submitted with drop existing for single access 0.
	 */
	@Test
	public void testMultipleAsyncSupplierSubmittedWithDropExistingForSingleAccess0() {
		asyncSupplier.submitSupplierWithDropExistingForSingleAccess(TestUtil.delayedSupplier(() -> "Value1a"), "query11b");
		asyncSupplier.submitSupplierWithDropExistingForSingleAccess(TestUtil.delayedSupplier(() -> "Value2a", 100), "query22b");
		asyncSupplier.submitSupplierWithDropExistingForSingleAccess(TestUtil.delayedSupplier(() -> "Value3a", 70), "query33b");
		asyncSupplier.submitSupplierWithDropExistingForSingleAccess(TestUtil.delayedSupplier(() -> "Value4a", 100), "query44b");
	
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query11b").get(), "Value1a");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query22b").get(), "Value2a");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query33b").get(), "Value3a");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query44b").get(), "Value4a");
	
		assertFalse(asyncSupplier.waitAndGetFromSupplier(String.class, "query11").isPresent());
		assertFalse(asyncSupplier.waitAndGetFromSupplier(String.class, "query22").isPresent());
		assertFalse(asyncSupplier.waitAndGetFromSupplier(String.class, "query33").isPresent());
		assertFalse(asyncSupplier.waitAndGetFromSupplier(String.class, "query44").isPresent());
	}

	/**
	 * Test multiple async supplier submitted with drop existing for single access.
	 */
	@Test
	public void testMultipleAsyncSupplierSubmittedWithDropExistingForSingleAccess() {
		asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query11");
		asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value2", 100), "query22");
		asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value3", 70), "query33");
		asyncSupplier.submitSupplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value4", 100), "query44");
		
		asyncSupplier.submitSupplierWithDropExistingForSingleAccess(TestUtil.delayedSupplier(() -> "Value1a"), "query11");
		asyncSupplier.submitSupplierWithDropExistingForSingleAccess(TestUtil.delayedSupplier(() -> "Value2a", 100), "query22");
		asyncSupplier.submitSupplierWithDropExistingForSingleAccess(TestUtil.delayedSupplier(() -> "Value3a", 70), "query33");
		asyncSupplier.submitSupplierWithDropExistingForSingleAccess(TestUtil.delayedSupplier(() -> "Value4a", 100), "query44");
	
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query11").get(), "Value1a");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query22").get(), "Value2a");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query33").get(), "Value3a");
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "query44").get(), "Value4a");
	
		assertFalse(asyncSupplier.waitAndGetFromSupplier(String.class, "query11").isPresent());
		assertFalse(asyncSupplier.waitAndGetFromSupplier(String.class, "query22").isPresent());
		assertFalse(asyncSupplier.waitAndGetFromSupplier(String.class, "query33").isPresent());
		assertFalse(asyncSupplier.waitAndGetFromSupplier(String.class, "query44").isPresent());
	}

	/**
	 * Testsuppliers for single access.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testsuppliersWithDropExistingForSingleAccess() throws InterruptedException {
		boolean suppliers = asyncSupplier
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
		
		suppliers = asyncSupplier
				.submitSuppliersWithDropExistingForSingleAccess(new Supplier[] { TestUtil.delayedSupplier(() -> {
					return 100;
				}, 100), TestUtil.delayedSupplier(() -> {
					return 200;
				}, 100), TestUtil.delayedSupplier(() -> {
					return 300;
				}, 100), TestUtil.delayedSupplier(() -> {
					return 400;
				}, 100), TestUtil.delayedSupplier(() -> {
					return 500;
				}, 100) }, "Multiple", "Suppliers", "key");
	
		assertTrue(suppliers);
	
		List<Integer> retVals = asyncSupplier.waitAndGetFromSuppliers(Integer.class, "Multiple", "Suppliers", "key")
				.collect(Collectors.toList());
		assertEquals(retVals.size(), 5);
		AtomicInteger val = new AtomicInteger(0);
		retVals.forEach(value -> assertEquals(val.addAndGet(100), (int) value));
	
	}

	/**
	 * Testsuppliers with drop existing for single access 2.
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	@Test
	public void testsuppliersWithDropExistingForSingleAccess2() throws InterruptedException {
		@SuppressWarnings("unchecked")
		boolean suppliers = asyncSupplier
				.submitSuppliersWithDropExistingForSingleAccess(new Supplier[] { TestUtil.delayedSupplier(() -> {
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
	
		List<Integer> retVals = asyncSupplier.waitAndGetFromSuppliers(Integer.class, "Multiple", "Suppliers", "key")
				.collect(Collectors.toList());
		assertEquals(retVals.size(), 5);
		AtomicInteger val = new AtomicInteger(0);
		retVals.forEach(value -> assertEquals(val.addAndGet(10), (int) value));
		
		List<Integer> retVals1 = asyncSupplier.waitAndGetFromSuppliers(Integer.class, "Multiple", "Suppliers", "key")
				.collect(Collectors.toList());
		assertEquals(retVals1.size(), 0);
	}

	/**
	 * Testsuppliers with drop existing for multiple access.
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testsuppliersWithDropExistingForMultipleAccess() throws InterruptedException {
		boolean suppliers = asyncSupplier
				.submitSuppliersForMultipleAccess(new Supplier[] { TestUtil.delayedSupplier(() -> {
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
		
		suppliers = asyncSupplier
				.submitSuppliersWithDropExistingForMultipleAccess(new Supplier[] { TestUtil.delayedSupplier(() -> {
					return 100;
				}, 100), TestUtil.delayedSupplier(() -> {
					return 200;
				}, 100), TestUtil.delayedSupplier(() -> {
					return 300;
				}, 100), TestUtil.delayedSupplier(() -> {
					return 400;
				}, 100), TestUtil.delayedSupplier(() -> {
					return 500;
				}, 100) }, "Multiple", "Suppliers", "key");
	
		assertTrue(suppliers);
	
		List<Integer> retVals = asyncSupplier.waitAndGetFromSuppliers(Integer.class, "Multiple", "Suppliers", "key")
				.collect(Collectors.toList());
		assertEquals(retVals.size(), 5);
		AtomicInteger val = new AtomicInteger(0);
		retVals.forEach(value -> assertEquals(val.addAndGet(100), (int) value));
		
		List<Integer> retVals1 = asyncSupplier.waitAndGetFromSuppliers(Integer.class, "Multiple", "Suppliers", "key")
				.collect(Collectors.toList());
		assertEquals(retVals1.size(), 5);
		AtomicInteger val1 = new AtomicInteger(0);
		retVals1.forEach(value -> assertEquals(val1.addAndGet(100), (int) value));
	
	}

	
	/**
	 * Test value submitted.
	 */
	@Test
	public void testValueSubmitted() {
		asyncSupplier.submitValue("StringValue", "String1");
		asyncSupplier.submitValue(100, "Integer1");
		asyncSupplier.submitValue(true, "Boolean1");
		Object obj = new Object();
		asyncSupplier.submitValue(obj, "Object1");
	
		assertEquals(asyncSupplier.waitAndGetValue(String.class, "String1").get(), "StringValue");
		assertEquals(asyncSupplier.waitAndGetValue(Integer.class, "Integer1").get(), Integer.valueOf(100));
		assertEquals(asyncSupplier.waitAndGetValue(Boolean.class, "Boolean1").get(), Boolean.TRUE);
		assertEquals(asyncSupplier.waitAndGetValue(Object.class, "Object1").get(), obj);
	
		assertEquals(asyncSupplier.waitAndGetValue(String.class, "String1").get(), "StringValue");
		assertEquals(asyncSupplier.waitAndGetValue(Integer.class, "Integer1").get(), Integer.valueOf(100));
		assertEquals(asyncSupplier.waitAndGetValue(Boolean.class, "Boolean1").get(), Boolean.TRUE);
		assertEquals(asyncSupplier.waitAndGetValue(Object.class, "Object1").get(), obj);
	}

	/**
	 * Test drop value.
	 */
	@Test
	public void testDropValue() {
		asyncSupplier.submitValue("StringValue", "String3");
		asyncSupplier.submitValue(100, "Integer3");
		asyncSupplier.submitValue(true, "Boolean3");
		Object obj = new Object();
		asyncSupplier.submitValue(obj, "Object3");
	
		
		asyncSupplier.dropValue("String3");
		asyncSupplier.dropValue("Integer3");
		asyncSupplier.dropValue("Boolean3");
		asyncSupplier.dropValue("Object3");
	
		assertEquals(asyncSupplier.waitAndGetFromSupplier(String.class, "String3").isPresent(), false);
		assertEquals(asyncSupplier.waitAndGetFromSupplier(Integer.class, "Integer3").isPresent(), false);
		assertEquals(asyncSupplier.waitAndGetFromSupplier(Boolean.class, "Boolean3").isPresent(), false);
		assertEquals(asyncSupplier.waitAndGetFromSupplier(Object.class, "Object3").isPresent(), false);
	
	}

	/**
	 * Test value submitted with drop existing.
	 */
	@Test
	public void testValueSubmittedWithDropExisting() {
		asyncSupplier.submitValue("StringValue", "String2");
		asyncSupplier.submitValue(100, "Integer2");
		asyncSupplier.submitValue(true, "Boolean2");
		Object obj = new Object();
		asyncSupplier.submitValue(obj, "Object2");
		
		asyncSupplier.submitValueWithDropExisting("StringValue2", "String2");
		asyncSupplier.submitValueWithDropExisting(200, "Integer2");
		asyncSupplier.submitValueWithDropExisting(false, "Boolean2");
		Object obj1 = new Object();
		asyncSupplier.submitValueWithDropExisting(obj1, "Object2");
	
		assertEquals(asyncSupplier.waitAndGetValue(String.class, "String2").get(), "StringValue2");
		assertEquals(asyncSupplier.waitAndGetValue(Integer.class, "Integer2").get(), Integer.valueOf(200));
		assertEquals(asyncSupplier.waitAndGetValue(Boolean.class, "Boolean2").get(), Boolean.FALSE);
		assertEquals(asyncSupplier.waitAndGetValue(Object.class, "Object2").get(), obj1);	
	}
	
	/**
	 * Test close.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testClose() throws Exception {
		AsyncSupplier asyncSupplier = AsyncSupplier.of(Executors.newFixedThreadPool(2));
		Supplier<String> resultSupplier = asyncSupplier.submitSupplier(() -> "Test");
		asyncSupplier.close();
		assertEquals(resultSupplier.get(), "Test");
		asyncSupplier.close();
	}
	
	/**
	 * Test close with exception.
	 *
	 * @throws Exception the exception
	 */
	@Test (expected=Exception.class)
	public void testCloseWithException() throws Exception {
		AsyncSupplier asyncSupplier = AsyncSupplier.of(Executors.newFixedThreadPool(2));
		Supplier<String> resultSupplier = asyncSupplier.submitSupplier(() -> "Test1");
		asyncSupplier.close();
		assertEquals(resultSupplier.get(), "Test1");
		
		asyncSupplier.submitSupplier(() -> "Test2");
		fail();
	}
	
	/**
	 * Test submit and get with timeout.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testSubmitAndGetWithTimeout() throws Exception {
		Optional<String> result = asyncSupplier.submitAndGetWithTimeout(() -> {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return "Success";
		}, 500, TimeUnit.MILLISECONDS);
		assertTrue(result.isPresent());
		assertEquals("Success", result.get());
	}
	
	/**
	 * Test submit and get with timeout exception.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testSubmitAndGetWithTimeoutException() throws Exception {
		Optional<String> result = asyncSupplier.submitAndGetWithTimeout(() -> {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return "Should timeout";
		}, 100, TimeUnit.MILLISECONDS);
		assertFalse(result.isPresent());
	}
	
	/**
	 * Test submit and process all.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testSubmitAndProcessAll() throws Exception {
		List<Integer> inputs = Arrays.asList(1, 2, 3, 4, 5);
		List<Integer> results = asyncSupplier.submitAndProcessAll(inputs, i -> i * 2);
		assertEquals(Arrays.asList(2, 4, 6, 8, 10), results);
	}
	
	/**
	 * Test submit and process all with limit.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testSubmitAndProcessAllWithLimit() throws Exception {
		List<Integer> inputs = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
		List<Integer> results = asyncSupplier.submitAndProcessAllWithLimit(inputs, i -> i * 2, 3);
		assertEquals(Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16), results);
	}
	
	/**
	 * Test submit supplier with retry.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testSubmitSupplierWithRetry() throws Exception {
		AtomicInteger attempts = new AtomicInteger(0);
		Supplier<String> result = asyncSupplier.submitSupplierWithRetry(() -> {
			if (attempts.incrementAndGet() < 3) {
				throw new RuntimeException("Simulated failure");
			}
			return "Success after retry";
		}, 5, 50);
		assertEquals("Success after retry", result.get());
		assertTrue(attempts.get() >= 3);
	}
	
	/**
	 * Test submit supplier with retry all attempts fail.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testSubmitSupplierWithRetryAllAttemptsFail() throws Exception {
		Supplier<String> result = asyncSupplier.submitSupplierWithRetry(() -> {
			throw new RuntimeException("Always fails");
		}, 3, 50);
		// Wait for retries to complete
		Thread.sleep(250);
		// Safe supplier returns null when all retries fail
		assertNull(result.get());
	}
	
	/**
	 * Test submit supplier with fallback.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testSubmitSupplierWithFallback() throws Exception {
		Supplier<String> result = asyncSupplier.submitSupplierWithFallback(() -> {
			throw new RuntimeException("Primary fails");
		}, "Fallback value");
		assertEquals("Fallback value", result.get());
	}
	
	/**
	 * Test submit supplier with fallback success.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testSubmitSupplierWithFallbackSuccess() throws Exception {
		Supplier<String> result = asyncSupplier.submitSupplierWithFallback(() -> "Primary success", "Fallback value");
		assertEquals("Primary success", result.get());
	}
	
	/**
	 * Test submit supplier with error handler.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testSubmitSupplierWithErrorHandler() throws Exception {
		AtomicInteger errorCount = new AtomicInteger(0);
		Supplier<String> result = asyncSupplier.submitSupplierWithErrorHandler(() -> {
			throw new RuntimeException("Error occurred");
		}, e -> {
			errorCount.incrementAndGet();
			return "Handled: " + e.getMessage();
		});
		assertEquals("Handled: Error occurred", result.get());
		assertEquals(1, errorCount.get());
	}
	
	/**
	 * Test submit chained.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testSubmitChained() throws Exception {
		Supplier<String> result = asyncSupplier.submitChained(() -> "Hello", s -> s + " World");
		assertEquals("Hello World", result.get());
	}
	
	/**
	 * Test submit and combine.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testSubmitAndCombine() throws Exception {
		Supplier<String> result = asyncSupplier.submitAndCombine(
			results -> {
				String first = (String) results.get(0);
				String second = (String) results.get(1);
				return first + " " + second;
			},
			() -> "Hello", 
			() -> "World"
		);
		assertEquals("Hello World", result.get());
	}
	
	/**
	 * Test submit race.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testSubmitRace() throws Exception {
		Supplier<String>[] suppliers = new Supplier[] {
			(Supplier<String>) () -> {
				try { Thread.sleep(200); } catch (InterruptedException e) { 
					Thread.currentThread().interrupt();
				}
				return "Slow";
			},
			(Supplier<String>) () -> {
				try { Thread.sleep(50); } catch (InterruptedException e) { 
					Thread.currentThread().interrupt();
				}
				return "Fast";
			},
			(Supplier<String>) () -> {
				try { Thread.sleep(150); } catch (InterruptedException e) { 
					Thread.currentThread().interrupt();
				}
				return "Medium";
			}
		};
		Supplier<String> fastest = asyncSupplier.submitRace(suppliers);
		assertEquals("Fast", fastest.get());
	}
	
	/**
	 * Test submit and get fastest.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testSubmitAndGetFastest() throws Exception {
		Optional<String> fastest = asyncSupplier.submitAndGetFastest(
			(Supplier<String>) () -> {
				try { Thread.sleep(200); } catch (InterruptedException e) { 
					Thread.currentThread().interrupt();
				}
				return "Slow";
			},
			(Supplier<String>) () -> {
				try { Thread.sleep(50); } catch (InterruptedException e) { 
					Thread.currentThread().interrupt();
				}
				return "Fast";
			}
		);
		assertTrue(fastest.isPresent());
		assertEquals("Fast", fastest.get());
	}
	
	/**
	 * Test submit as completable future.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testSubmitAsCompletableFuture() throws Exception {
		CompletableFuture<String> future = asyncSupplier.submitAsCompletableFuture(() -> "Test");
		assertEquals("Test", future.get());
	}
	
	/**
	 * Test is pending.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testIsPending() throws Exception {
		asyncSupplier.submitSupplierForSingleAccess(() -> {
			try { Thread.sleep(500); } catch (InterruptedException e) { 
				Thread.currentThread().interrupt();
			}
			return "Value";
		}, "testKey");
		
		// Check immediately after submission - should be pending
		Thread.sleep(10); // Tiny delay to ensure task is registered
		boolean pending = asyncSupplier.isPending("testKey");
		assertTrue("Task should be pending immediately after submission", pending);
		
		// Wait for task to complete and retrieve value to trigger cleanup
		Thread.sleep(600);
		Optional<String> value = asyncSupplier.waitAndGetFromSupplier(String.class, "testKey");
		assertTrue("Value should be present", value.isPresent());
		assertEquals("Value", value.get());
		
		// Now it should not be pending since it was retrieved
		boolean notPending = asyncSupplier.isPending("testKey");
		assertFalse("Task should not be pending after retrieval", notPending);
	}
	
	/**
	 * Test cancel supplier.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testCancelSupplier() throws Exception {
		asyncSupplier.submitSupplierForSingleAccess(() -> {
			try { Thread.sleep(1000); } catch (InterruptedException e) { 
				Thread.currentThread().interrupt();
				throw new RuntimeException("Cancelled");
			}
			return "Should not complete";
		}, "cancelKey");
		Thread.sleep(100);
		assertTrue(asyncSupplier.cancelSupplier("cancelKey"));
		assertFalse(asyncSupplier.isPending("cancelKey"));
	}
}
