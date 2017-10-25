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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 * The class SubmitTest.
 * @author Loganathan.S <https://github.com/loganathan001>
 */
public class SubmitTest {
	
	@Rule
	public TestRule watcher = new TestWatcherAndLogger();

	//
	//	@BeforeClass
	//	public static void setUpBeforeClass() throws Exception {
	//	}
	//
	//	@AfterClass
	//	public static void tearDownAfterClass() throws Exception {
	//	}
	
	//	@Before
	//	public void setUp() throws Exception {
	//	}
	//
	//	@After
	//	public void tearDown() throws Exception {
	//	}

	@Test
	public void testMultipleAsyncSupplierSubmittedForMultipleAccess() {
		Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query1");
		Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value2", 1000), "query2");
		Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value3", 700), "query3");
		Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value4", 1000), "query4");
	
		assertEquals(Wait.waitAndGet(String.class, "query1").get(), "Value1");
		assertEquals(Wait.waitAndGet(String.class, "query2").get(), "Value2");
		assertEquals(Wait.waitAndGet(String.class, "query3").get(), "Value3");
		assertEquals(Wait.waitAndGet(String.class, "query4").get(), "Value4");
	
		assertEquals(Wait.waitAndGet(String.class, "query1").get(), "Value1");
		assertEquals(Wait.waitAndGet(String.class, "query2").get(), "Value2");
		assertEquals(Wait.waitAndGet(String.class, "query3").get(), "Value3");
		assertEquals(Wait.waitAndGet(String.class, "query4").get(), "Value4");
	}

	@Test
	public void testMultipleAsyncSupplierSubmittedForSingleAccess() {
		Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query11");
		Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value2", 1000), "query22");
		Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value3", 700), "query33");
		Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value4", 1000), "query44");
	
		assertEquals(Wait.waitAndGet(String.class, "query11").get(), "Value1");
		assertEquals(Wait.waitAndGet(String.class, "query22").get(), "Value2");
		assertEquals(Wait.waitAndGet(String.class, "query33").get(), "Value3");
		assertEquals(Wait.waitAndGet(String.class, "query44").get(), "Value4");
	
		assertFalse(Wait.waitAndGet(String.class, "query11").isPresent());
		assertFalse(Wait.waitAndGet(String.class, "query22").isPresent());
		assertFalse(Wait.waitAndGet(String.class, "query33").isPresent());
		assertFalse(Wait.waitAndGet(String.class, "query44").isPresent());
	}

	@Test
	public void testMultipleAsyncTasks() {
		String[] val = new String[4];
		Submit.task(TestUtil.delayedRunnable(() -> {
			val[0] = "Value1";
		}), "task1");
		Submit.task(TestUtil.delayedRunnable(() -> {
			val[1] = "Value2";
		}, 1000), "task2");
		Submit.task(TestUtil.delayedRunnable(() -> {
			val[2] = "Value3";
		}, 700), "task3");
		Submit.task(TestUtil.delayedRunnable(() -> {
			val[3] = "Value4";
		}, 1000), "task4");
	
		Wait.waitForTask("task1");
		Wait.waitForTask("task2");
		Wait.waitForTask("task3");
		Wait.waitForTask("task4");
	
		assertEquals(val[0], "Value1");
		assertEquals(val[1], "Value2");
		assertEquals(val[2], "Value3");
		assertEquals(val[3], "Value4");
	
	}

	@Test
	public void testMultipleAsyncTasksMultipleTimes() {
		String[] val = new String[4];
		Submit.task(TestUtil.delayedRunnable(() -> {
			val[0] = "Value1";
		}), "task1");
		Submit.task(TestUtil.delayedRunnable(() -> {
			val[1] = "Value2";
		}, 1000), "task2");
		Submit.task(TestUtil.delayedRunnable(() -> {
			val[2] = "Value3";
		}, 700), "task3");
		Submit.task(TestUtil.delayedRunnable(() -> {
			val[3] = "Value4";
		}, 1000), "task4");
	
		Wait.waitForTask("task1");
		Wait.waitForTask("task2");
		Wait.waitForTask("task3");
		Wait.waitForTask("task4");
	
		assertEquals(val[0], "Value1");
		assertEquals(val[1], "Value2");
		assertEquals(val[2], "Value3");
		assertEquals(val[3], "Value4");
		
		Wait.waitForTask("task1");
		Wait.waitForTask("task2");
		Wait.waitForTask("task3");
		Wait.waitForTask("task4");
	
		assertEquals(val[0], "Value1");
		assertEquals(val[1], "Value2");
		assertEquals(val[2], "Value3");
		assertEquals(val[3], "Value4");
		
		Submit.task(TestUtil.delayedRunnable(() -> {
			val[0] = "Value11";
		}), "task1");
		Submit.task(TestUtil.delayedRunnable(() -> {
			val[1] = "Value22";
		}, 1000), "task2");
		Submit.task(TestUtil.delayedRunnable(() -> {
			val[2] = "Value33";
		}, 700), "task3");
		Submit.task(TestUtil.delayedRunnable(() -> {
			val[3] = "Value44";
		}, 1000), "task4");
		
		Wait.waitForTask("task1");
		Wait.waitForTask("task2");
		Wait.waitForTask("task3");
		Wait.waitForTask("task4");
		
		assertEquals(val[0], "Value11");
		assertEquals(val[1], "Value22");
		assertEquals(val[2], "Value33");
		assertEquals(val[3], "Value44");
	
	}

	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForMultipleAccess() {
		assertTrue(Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query1111"));
		assertTrue(
				Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value2", 1000), "query2222"));
		assertTrue(
				Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value3", 700), "query3333"));
		assertTrue(
				Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value4", 1000), "query4444"));
	
		assertFalse(Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value1a"), "query1111"));
		assertFalse(
				Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value2a", 1000), "query2222"));
		assertFalse(
				Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value3a", 700), "query3333"));
		assertFalse(
				Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value4a", 1000), "query4444"));
	
		assertEquals(Wait.waitAndGet(String.class, "query1111").get(), "Value1");
		assertEquals(Wait.waitAndGet(String.class, "query2222").get(), "Value2");
		assertEquals(Wait.waitAndGet(String.class, "query3333").get(), "Value3");
		assertEquals(Wait.waitAndGet(String.class, "query4444").get(), "Value4");
	
		assertTrue(Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value11"), "query1111"));
		assertTrue(
				Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value22", 1000), "query2222"));
		assertTrue(
				Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value33", 700), "query3333"));
		assertTrue(
				Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value44", 1000), "query4444"));
		
		assertEquals(Wait.waitAndGet(String.class, "query1111").get(), "Value11");
		assertEquals(Wait.waitAndGet(String.class, "query2222").get(), "Value22");
		assertEquals(Wait.waitAndGet(String.class, "query3333").get(), "Value33");
		assertEquals(Wait.waitAndGet(String.class, "query4444").get(), "Value44");
	}

	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForMultipleAccessThenSingleAccess() {
		assertTrue(Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query11111"));
		assertTrue(
				Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value2", 1000), "query22222"));
		assertTrue(
				Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value3", 700), "query33333"));
		assertTrue(
				Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value4", 1000), "query44444"));
	
		assertEquals(Wait.waitAndGet(String.class, "query11111").get(), "Value1");
		assertEquals(Wait.waitAndGet(String.class, "query22222").get(), "Value2");
		assertEquals(Wait.waitAndGet(String.class, "query33333").get(), "Value3");
		assertEquals(Wait.waitAndGet(String.class, "query44444").get(), "Value4");
		
		assertEquals(Wait.waitAndGet(String.class, "query11111").get(), "Value1");
		assertEquals(Wait.waitAndGet(String.class, "query22222").get(), "Value2");
		assertEquals(Wait.waitAndGet(String.class, "query33333").get(), "Value3");
		assertEquals(Wait.waitAndGet(String.class, "query44444").get(), "Value4");
	
		assertTrue(Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value11"), "query11111"));
		assertTrue(
				Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value22", 1000), "query22222"));
		assertTrue(
				Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value33", 700), "query33333"));
		assertTrue(
				Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value44", 1000), "query44444"));
		
		assertEquals(Wait.waitAndGet(String.class, "query11111").get(), "Value11");
		assertEquals(Wait.waitAndGet(String.class, "query22222").get(), "Value22");
		assertEquals(Wait.waitAndGet(String.class, "query33333").get(), "Value33");
		assertEquals(Wait.waitAndGet(String.class, "query44444").get(), "Value44");
		
		assertFalse(Wait.waitAndGet(String.class, "query11111").isPresent());
		assertFalse(Wait.waitAndGet(String.class, "query22222").isPresent());
		assertFalse(Wait.waitAndGet(String.class, "query33333").isPresent());
		assertFalse(Wait.waitAndGet(String.class, "query44444").isPresent());
	}

	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForSingleAccess() {
		assertTrue(Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query111"));
		assertTrue(
				Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value2", 1000), "query222"));
		assertTrue(
				Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value3", 700), "query333"));
		assertTrue(
				Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value4", 1000), "query444"));
	
		assertFalse(Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query111"));
		assertFalse(
				Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value2", 1000), "query222"));
		assertFalse(
				Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value3", 700), "query333"));
		assertFalse(
				Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value4", 1000), "query444"));
	
		assertEquals(Wait.waitAndGet(String.class, "query111").get(), "Value1");
		assertEquals(Wait.waitAndGet(String.class, "query222").get(), "Value2");
		assertEquals(Wait.waitAndGet(String.class, "query333").get(), "Value3");
		assertEquals(Wait.waitAndGet(String.class, "query444").get(), "Value4");
	
		assertTrue(Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value11"), "query111"));
		assertTrue(
				Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value22", 1000), "query222"));
		assertTrue(
				Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value33", 700), "query333"));
		assertTrue(
				Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value44", 1000), "query444"));
		
		assertEquals(Wait.waitAndGet(String.class, "query111").get(), "Value11");
		assertEquals(Wait.waitAndGet(String.class, "query222").get(), "Value22");
		assertEquals(Wait.waitAndGet(String.class, "query333").get(), "Value33");
		assertEquals(Wait.waitAndGet(String.class, "query444").get(), "Value44");
	
	}

	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForSingleAccessThenMultipleAccess() {
		assertTrue(Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value1"), "query111111"));
		assertTrue(
				Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value2", 1000), "query222222"));
		assertTrue(
				Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value3", 700), "query333333"));
		assertTrue(
				Submit.supplierForSingleAccess(TestUtil.delayedSupplier(() -> "Value4", 1000), "query444444"));
		
		assertEquals(Wait.waitAndGet(String.class, "query111111").get(), "Value1");
		assertEquals(Wait.waitAndGet(String.class, "query222222").get(), "Value2");
		assertEquals(Wait.waitAndGet(String.class, "query333333").get(), "Value3");
		assertEquals(Wait.waitAndGet(String.class, "query444444").get(), "Value4");
		
		assertFalse(Wait.waitAndGet(String.class, "query111111").isPresent());
		assertFalse(Wait.waitAndGet(String.class, "query222222").isPresent());
		assertFalse(Wait.waitAndGet(String.class, "query333333").isPresent());
		assertFalse(Wait.waitAndGet(String.class, "query444444").isPresent());
		
		assertTrue(Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value11"), "query111111"));
		assertTrue(
				Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value22", 1000), "query222222"));
		assertTrue(
				Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value33", 700), "query333333"));
		assertTrue(
				Submit.supplierForMultipleAccess(TestUtil.delayedSupplier(() -> "Value44", 1000), "query444444"));
	
		assertEquals(Wait.waitAndGet(String.class, "query111111").get(), "Value11");
		assertEquals(Wait.waitAndGet(String.class, "query222222").get(), "Value22");
		assertEquals(Wait.waitAndGet(String.class, "query333333").get(), "Value33");
		assertEquals(Wait.waitAndGet(String.class, "query444444").get(), "Value44");
		
		assertEquals(Wait.waitAndGet(String.class, "query111111").get(), "Value11");
		assertEquals(Wait.waitAndGet(String.class, "query222222").get(), "Value22");
		assertEquals(Wait.waitAndGet(String.class, "query333333").get(), "Value33");
		assertEquals(Wait.waitAndGet(String.class, "query444444").get(), "Value44");
	}

	@Test
	public void testSubmit() throws InterruptedException {
		assertEquals(Submit.andGet(() -> TestUtil.delayedSupplier(() -> "Value2")).get().get(), "Value2");
		assertEquals(Submit.supplier(TestUtil.delayedSupplier(() -> "Value3")).get(), "Value3");
		assertEquals(Submit.callable(() -> TestUtil.delayedSupplier(() -> "Value4")).get().get(), "Value4");
	
		String[] val = new String[1];
		Submit.task(() -> {
			val[0] = "Value5";
		});
		Thread.sleep(1000);
		assertEquals(val[0], "Value5");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSuppliers() throws InterruptedException {
		Supplier<Integer>[] resultSuppliers = Submit.suppliers(TestUtil.delayedSupplier(() -> {
			return 10;
		}, 100),
		TestUtil.delayedSupplier(() -> {
			return 20;
		}, 100),
		TestUtil.delayedSupplier(() -> {
			return 30;
		}, 100),
		TestUtil.delayedSupplier(() -> {
			return 40;
		}, 100),
		TestUtil.delayedSupplier(() -> {
			return 50;
		}, 100)
		);
		
		assertEquals(resultSuppliers.length, 5);
		AtomicInteger val = new AtomicInteger(0);
		Stream.of(resultSuppliers).map(Supplier::get).forEach(value -> assertEquals(val.addAndGet(10), (int)value));
		
	}

	@Test
	public void testsuppliersForSingleAccess() throws InterruptedException {
		@SuppressWarnings("unchecked")
		boolean suppliers = Submit.suppliersForSingleAccess(
				new Supplier[] {
					TestUtil.delayedSupplier(() -> {
						return 10;
					}, 100),
					TestUtil.delayedSupplier(() -> {
						return 20;
					}, 100),
					TestUtil.delayedSupplier(() -> {
						return 30;
					}, 100),
					TestUtil.delayedSupplier(() -> {
						return 40;
					}, 100),
					TestUtil.delayedSupplier(() -> {
						return 50;
					}, 100)
					}, 
				"Multiple","Suppliers","key"
		);
		
		assertTrue(suppliers);
		
		List<Integer> retVals = Wait.waitAndGetFromMultipleSuppliers(Integer.class, "Multiple","Suppliers","key").collect(Collectors.toList());
		assertEquals(retVals.size(), 5);
		AtomicInteger val = new AtomicInteger(0);
		retVals.forEach(value -> assertEquals(val.addAndGet(10), (int)value));
		
	}

	@Test
	public void testtask() throws InterruptedException {
		int[] retVal = new int[1];
		Submit.task(TestUtil.delayedRunnable(() -> {
			retVal[0] = 10;
		}, 100));
		
		Thread.sleep(500);
		assertEquals(retVal[0], 10);
	}

	@Test
	public void testtasks() throws InterruptedException {
		int[] retVal = new int[5];
		Submit.tasks(TestUtil.delayedRunnable(() -> {
			retVal[0] = 10;
		}, 100),
		TestUtil.delayedRunnable(() -> {
			retVal[1] = 20;
		}, 100),
		TestUtil.delayedRunnable(() -> {
			retVal[2] = 30;
		}, 100),
		TestUtil.delayedRunnable(() -> {
			retVal[3] = 40;
		}, 100),
		TestUtil.delayedRunnable(() -> {
			retVal[4] = 50;
		}, 100));
		
		Thread.sleep(1000);
		assertArrayEquals(retVal, new int[]{10, 20, 30, 40, 50});
	}

	@Test
	public void testtasksAndWait() {
		int[] retVal = new int[5];
		Submit.tasksAndWait(
		() -> {
			retVal[0] = 10;
		},
		() -> {
			retVal[1] = 20;
		},
		() -> {
			retVal[2] = 30;
		},
		() -> {
			retVal[3] = 40;
		},
		() -> {
			retVal[4] = 50;
		});
		
		assertArrayEquals(retVal, new int[]{10, 20, 30, 40, 50});
	}

	@Test
	public void testtasksAndWaitWithCancelWithInterrupt() {
		int[] retVal = new int[]{0, 0, 0};
		AtomicBoolean cancel = new AtomicBoolean(false);
		Submit.task(TestUtil.delayedRunnable(() -> {
			cancel.set(true);
		}, 5000));
		
		Submit.tasksAndWait(
		() -> cancel.get(), true,
		TestUtil.delayedRunnable(() -> {
			retVal[0] = 10;
			TestUtil.printTime();
		}, 2000),
		TestUtil.delayedRunnable(() -> {
			retVal[1] = 20;
			TestUtil.printTime();
		}, 4000),
		TestUtil.delayedRunnable(() -> {
			retVal[2] = 30;
			TestUtil.printTime();
		}, 6000)
		);
		
		assertFalse(Arrays.toString(new int[]{10, 20, 30}).equals(Arrays.toString(retVal)));
	}

	@Test
	public void testtasksWithKeys() {
		int[] retVal = new int[5];
		Object[] keys = {"Submitted", "Multiple", "Tasks"};
		Submit.tasks(
		keys,
		() -> {
			retVal[0] = 10;
		},
		() -> {
			retVal[1] = 20;
		},
		() -> {
			retVal[2] = 30;
		},
		() -> {
			retVal[3] = 40;
		},
		() -> {
			retVal[4] = 50;
		});
		
		Wait.waitForMultipleTasks(keys);
		
		assertArrayEquals(retVal, new int[]{10, 20, 30, 40, 50});
	}

}
