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

package org.vishag.asynchelper;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class AsyncHelperTest {

	@Rule
	public TestRule watcher = new TestWatcherAndLogger();
	
	static final Logger logger = Logger.getLogger(AsyncHelperTest.class.getName());

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

	@Test
	public void test() throws InterruptedException {
		assertEquals(AsyncHelper.asyncGet(delayedSupplier(() -> "Value1")).get(), "Value1");
		assertEquals(AsyncHelper.submitAndGet(() -> delayedSupplier(() -> "Value2")).get().get(), "Value2");
		assertEquals(AsyncHelper.submitSupplier(delayedSupplier(() -> "Value3")).get(), "Value3");
		assertEquals(AsyncHelper.submitCallable(() -> delayedSupplier(() -> "Value4")).get().get(), "Value4");

		String[] val = new String[1];
		AsyncHelper.submitTask(() -> {
			val[0] = "Value5";
		});
		Thread.sleep(1000);
		assertEquals(val[0], "Value5");
	}

	private <T> Supplier<T> delayedSupplier(Supplier<T> supplier, long msecs) {
		return () -> {
			try {
				Thread.sleep(msecs);
			} catch (InterruptedException e) {
			}
			return supplier.get();
		};
	}

	private <T> Runnable delayedRunnable(Runnable runnable) {
		return delayedRunnable(runnable, 500);
	}

	private <T> Runnable delayedRunnable(Runnable runnable, long msecs) {
		return () -> {
			try {
				Thread.sleep(msecs);
				runnable.run();
			} catch (Exception e) {
				logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
		};
	}

	private <T> Supplier<T> delayedSupplier(Supplier<T> supplier) {
		return delayedSupplier(supplier, 500);
	}

	@Test
	public void testMultipleAsyncTasks() {
		String[] val = new String[4];
		AsyncHelper.submitTask(delayedRunnable(() -> {
			val[0] = "Value1";
		}), "task1");
		AsyncHelper.submitTask(delayedRunnable(() -> {
			val[1] = "Value2";
		}, 1000), "task2");
		AsyncHelper.submitTask(delayedRunnable(() -> {
			val[2] = "Value3";
		}, 700), "task3");
		AsyncHelper.submitTask(delayedRunnable(() -> {
			val[3] = "Value4";
		}, 1000), "task4");

		AsyncHelper.waitForTask("task1");
		AsyncHelper.waitForTask("task2");
		AsyncHelper.waitForTask("task3");
		AsyncHelper.waitForTask("task4");

		assertEquals(val[0], "Value1");
		assertEquals(val[1], "Value2");
		assertEquals(val[2], "Value3");
		assertEquals(val[3], "Value4");

	}
	
	@Test
	public void testMultipleAsyncTasksMultipleTimes() {
		String[] val = new String[4];
		AsyncHelper.submitTask(delayedRunnable(() -> {
			val[0] = "Value1";
		}), "task1");
		AsyncHelper.submitTask(delayedRunnable(() -> {
			val[1] = "Value2";
		}, 1000), "task2");
		AsyncHelper.submitTask(delayedRunnable(() -> {
			val[2] = "Value3";
		}, 700), "task3");
		AsyncHelper.submitTask(delayedRunnable(() -> {
			val[3] = "Value4";
		}, 1000), "task4");

		AsyncHelper.waitForTask("task1");
		AsyncHelper.waitForTask("task2");
		AsyncHelper.waitForTask("task3");
		AsyncHelper.waitForTask("task4");

		assertEquals(val[0], "Value1");
		assertEquals(val[1], "Value2");
		assertEquals(val[2], "Value3");
		assertEquals(val[3], "Value4");
		
		AsyncHelper.waitForTask("task1");
		AsyncHelper.waitForTask("task2");
		AsyncHelper.waitForTask("task3");
		AsyncHelper.waitForTask("task4");

		assertEquals(val[0], "Value1");
		assertEquals(val[1], "Value2");
		assertEquals(val[2], "Value3");
		assertEquals(val[3], "Value4");
		
		AsyncHelper.submitTask(delayedRunnable(() -> {
			val[0] = "Value11";
		}), "task1");
		AsyncHelper.submitTask(delayedRunnable(() -> {
			val[1] = "Value22";
		}, 1000), "task2");
		AsyncHelper.submitTask(delayedRunnable(() -> {
			val[2] = "Value33";
		}, 700), "task3");
		AsyncHelper.submitTask(delayedRunnable(() -> {
			val[3] = "Value44";
		}, 1000), "task4");
		
		AsyncHelper.waitForTask("task1");
		AsyncHelper.waitForTask("task2");
		AsyncHelper.waitForTask("task3");
		AsyncHelper.waitForTask("task4");
		
		assertEquals(val[0], "Value11");
		assertEquals(val[1], "Value22");
		assertEquals(val[2], "Value33");
		assertEquals(val[3], "Value44");

	}
	
	@Test
	public void testMultipleAsyncSupplierSubmittedForMultipleAccess() {
		AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value1"), "query1");
		AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value2", 1000), "query2");
		AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value3", 700), "query3");
		AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value4", 1000), "query4");

		assertEquals(AsyncHelper.waitAndGet(String.class, "query1").get(), "Value1");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query2").get(), "Value2");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query3").get(), "Value3");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query4").get(), "Value4");

		assertEquals(AsyncHelper.waitAndGet(String.class, "query1").get(), "Value1");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query2").get(), "Value2");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query3").get(), "Value3");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query4").get(), "Value4");
	}

	@Test
	public void testMultipleAsyncSupplierSubmittedForSingleAccess() {
		AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value1"), "query11");
		AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value2", 1000), "query22");
		AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value3", 700), "query33");
		AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value4", 1000), "query44");

		assertEquals(AsyncHelper.waitAndGet(String.class, "query11").get(), "Value1");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query22").get(), "Value2");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query33").get(), "Value3");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query44").get(), "Value4");

		assertTrue(!AsyncHelper.waitAndGet(String.class, "query11").isPresent());
		assertTrue(!AsyncHelper.waitAndGet(String.class, "query22").isPresent());
		assertTrue(!AsyncHelper.waitAndGet(String.class, "query33").isPresent());
		assertTrue(!AsyncHelper.waitAndGet(String.class, "query44").isPresent());
	}

	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForSingleAccess() {
		assertTrue(AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value1"), "query111"));
		assertTrue(
				AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value2", 1000), "query222"));
		assertTrue(
				AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value3", 700), "query333"));
		assertTrue(
				AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value4", 1000), "query444"));

		assertFalse(AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value1"), "query111"));
		assertFalse(
				AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value2", 1000), "query222"));
		assertFalse(
				AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value3", 700), "query333"));
		assertFalse(
				AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value4", 1000), "query444"));

		assertEquals(AsyncHelper.waitAndGet(String.class, "query111").get(), "Value1");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query222").get(), "Value2");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query333").get(), "Value3");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query444").get(), "Value4");

		assertTrue(AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value11"), "query111"));
		assertTrue(
				AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value22", 1000), "query222"));
		assertTrue(
				AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value33", 700), "query333"));
		assertTrue(
				AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value44", 1000), "query444"));
		
		assertEquals(AsyncHelper.waitAndGet(String.class, "query111").get(), "Value11");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query222").get(), "Value22");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query333").get(), "Value33");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query444").get(), "Value44");

	}

	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForMultipleAccess() {
		assertTrue(AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value1"), "query1111"));
		assertTrue(
				AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value2", 1000), "query2222"));
		assertTrue(
				AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value3", 700), "query3333"));
		assertTrue(
				AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value4", 1000), "query4444"));

		assertFalse(AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value1a"), "query1111"));
		assertFalse(
				AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value2a", 1000), "query2222"));
		assertFalse(
				AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value3a", 700), "query3333"));
		assertFalse(
				AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value4a", 1000), "query4444"));

		assertEquals(AsyncHelper.waitAndGet(String.class, "query1111").get(), "Value1");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query2222").get(), "Value2");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query3333").get(), "Value3");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query4444").get(), "Value4");

		assertTrue(AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value11"), "query1111"));
		assertTrue(
				AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value22", 1000), "query2222"));
		assertTrue(
				AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value33", 700), "query3333"));
		assertTrue(
				AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value44", 1000), "query4444"));
		
		assertEquals(AsyncHelper.waitAndGet(String.class, "query1111").get(), "Value11");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query2222").get(), "Value22");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query3333").get(), "Value33");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query4444").get(), "Value44");
	}
	
	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForMultipleAccessThenSingleAccess() {
		assertTrue(AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value1"), "query11111"));
		assertTrue(
				AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value2", 1000), "query22222"));
		assertTrue(
				AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value3", 700), "query33333"));
		assertTrue(
				AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value4", 1000), "query44444"));

		assertEquals(AsyncHelper.waitAndGet(String.class, "query11111").get(), "Value1");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query22222").get(), "Value2");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query33333").get(), "Value3");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query44444").get(), "Value4");
		
		assertEquals(AsyncHelper.waitAndGet(String.class, "query11111").get(), "Value1");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query22222").get(), "Value2");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query33333").get(), "Value3");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query44444").get(), "Value4");

		assertTrue(AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value11"), "query11111"));
		assertTrue(
				AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value22", 1000), "query22222"));
		assertTrue(
				AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value33", 700), "query33333"));
		assertTrue(
				AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value44", 1000), "query44444"));
		
		assertEquals(AsyncHelper.waitAndGet(String.class, "query11111").get(), "Value11");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query22222").get(), "Value22");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query33333").get(), "Value33");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query44444").get(), "Value44");
		
		assertTrue(!AsyncHelper.waitAndGet(String.class, "query11111").isPresent());
		assertTrue(!AsyncHelper.waitAndGet(String.class, "query22222").isPresent());
		assertTrue(!AsyncHelper.waitAndGet(String.class, "query33333").isPresent());
		assertTrue(!AsyncHelper.waitAndGet(String.class, "query44444").isPresent());
	}
	
	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForSingleAccessThenMultipleAccess() {
		assertTrue(AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value1"), "query111111"));
		assertTrue(
				AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value2", 1000), "query222222"));
		assertTrue(
				AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value3", 700), "query333333"));
		assertTrue(
				AsyncHelper.submitSupplierForSingleAccess(delayedSupplier(() -> "Value4", 1000), "query444444"));
		
		assertEquals(AsyncHelper.waitAndGet(String.class, "query111111").get(), "Value1");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query222222").get(), "Value2");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query333333").get(), "Value3");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query444444").get(), "Value4");
		
		assertTrue(!AsyncHelper.waitAndGet(String.class, "query111111").isPresent());
		assertTrue(!AsyncHelper.waitAndGet(String.class, "query222222").isPresent());
		assertTrue(!AsyncHelper.waitAndGet(String.class, "query333333").isPresent());
		assertTrue(!AsyncHelper.waitAndGet(String.class, "query444444").isPresent());
		
		assertTrue(AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value11"), "query111111"));
		assertTrue(
				AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value22", 1000), "query222222"));
		assertTrue(
				AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value33", 700), "query333333"));
		assertTrue(
				AsyncHelper.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value44", 1000), "query444444"));

		assertEquals(AsyncHelper.waitAndGet(String.class, "query111111").get(), "Value11");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query222222").get(), "Value22");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query333333").get(), "Value33");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query444444").get(), "Value44");
		
		assertEquals(AsyncHelper.waitAndGet(String.class, "query111111").get(), "Value11");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query222222").get(), "Value22");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query333333").get(), "Value33");
		assertEquals(AsyncHelper.waitAndGet(String.class, "query444444").get(), "Value44");
	}
	
	@Test
	public void testWaitAndNotifyForFlag() throws InterruptedException {
		int[] retVal = new int[1];
		AsyncHelper.submitTask(delayedRunnable(() -> {
			retVal[0] = 10;
			AsyncHelper.notifyFlag("FLAG1");
		}, 2000));
		AsyncHelper.waitForFlag("FLAG1");
		assertEquals(retVal[0], 10);
	}
	
	@Test
	public void testSubmitTask() throws InterruptedException {
		int[] retVal = new int[1];
		AsyncHelper.submitTask(delayedRunnable(() -> {
			retVal[0] = 10;
		}, 100));
		
		Thread.sleep(500);
		assertEquals(retVal[0], 10);
	}
	
	@Test
	public void testSubmitTasks() throws InterruptedException {
		int[] retVal = new int[5];
		AsyncHelper.submitTasks(delayedRunnable(() -> {
			retVal[0] = 10;
		}, 100),
		delayedRunnable(() -> {
			retVal[1] = 20;
		}, 100),
		delayedRunnable(() -> {
			retVal[2] = 30;
		}, 100),
		delayedRunnable(() -> {
			retVal[3] = 40;
		}, 100),
		delayedRunnable(() -> {
			retVal[4] = 50;
		}, 100));
		
		Thread.sleep(1000);
		assertArrayEquals(retVal, new int[]{10, 20, 30, 40, 50});
	}
	
	@Test
	public void testScheduleTasks()  throws InterruptedException {
		int[] retVal = new int[5];
		AsyncHelper.scheduleTasks(10, 100, TimeUnit.MILLISECONDS, true,
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
		
		Thread.sleep(1000);
		assertArrayEquals(retVal, new int[]{10, 20, 30, 40, 50});
	}
	
	@Test
	public void testScheduleTasksUntilFlag()  throws InterruptedException {
		int[] retVal = new int[5];
		AsyncHelper.scheduleTasksUntilFlag(10, 100, TimeUnit.MILLISECONDS, true, "ScheduledMultipleTasksTest",
		() -> {
			print("Task 0");
			printTime();
			retVal[0]+= 1;
		},
		() -> {
			print("Task 1");
			printTime();
			retVal[1]+= 1;
		},
		() -> {
			print("Task 2");
			printTime();
			retVal[2]+= 1;
		},
		() -> {
			print("Task 3");
			printTime();
			retVal[3]+= 1;
		},
		() -> {
			print("Task 4");
			printTime();
			retVal[4]+= 1;
		});
		
		Thread.sleep(1200);
		AsyncHelper.notifyFlag("ScheduledMultipleTasksTest");
		assertTrue(retVal[0] > 1);
		assertTrue(retVal[1] > 1);
		assertTrue(retVal[2] > 1);
		assertTrue(retVal[3] > 1);
		assertTrue(retVal[4] > 1);
	}
	
	@Test
	public void testScheduleTaskUntilFlag()  throws InterruptedException {
		int[] retVal = new int[5];
		AsyncHelper.scheduleTaskUntilFlag(10, 100, TimeUnit.MILLISECONDS, true, "ScheduledSingleTasksTest",
		() -> {
			print("Count " + retVal[0]);
			printTime();
			retVal[0]+= 1;
		});
		
		Thread.sleep(1200);
		AsyncHelper.notifyFlag("ScheduledSingleTasksTest");
		assertTrue(retVal[0] > 5);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testScheduleSuppliers()  throws InterruptedException {
		Supplier<Integer>[] scheduleSuppliers = AsyncHelper.scheduleSuppliers(10, 100, TimeUnit.MILLISECONDS, true,
		() -> {
			return 10;
		},
		() -> {
			return 20;
		},
		() -> {
			return 30;
		},
		() -> {
			return 40;
		},
		() -> {
			return 50;
		});
		
		assertEquals(scheduleSuppliers.length, 5);
		AtomicInteger val = new AtomicInteger(0);
		Stream.of(scheduleSuppliers).map(Supplier::get).forEach(value -> Assert.assertEquals(val.addAndGet(10), (int)value));
	}
	
	@Test
	public void testScheduleSuppliersUntilFlag()  throws InterruptedException {
		AsyncHelper.scheduleSuppliersUntilFlag(10, 100, TimeUnit.MILLISECONDS, true,
				"TestSuppliersUntilFlag",
		() -> {
			return 0;
		},
		() -> {
			return 1;
		},
		() -> {
			return 2;
		},
		() -> {
			return 3;
		},
		() -> {
			return 4;
		});
		
		Thread.sleep(1000);
		
		List<Integer> result = AsyncHelper.notifyAndGetForFlag(Integer.class, "TestSuppliersUntilFlag").collect(Collectors.toList());
		assertTrue(result.size() > 0);
		int val = 0;
		for (int i = 0; i < result.size(); i++) {
			assertTrue(val == result.get(i));
			if(val < 5 - 1) {
				val++;
			} else {
				val = 0;
			}
		}
		print("" + result);
		
	}
	
	@Test
	public void testScheduleSingleSupplierUntilFlag()  throws InterruptedException {
		int[] retVal = new int[1];
		AsyncHelper.scheduleSupplierUntilFlag(10, 100, TimeUnit.MILLISECONDS, true,
				"TestSingleSuppliersUntilFlag",
		() -> {
			return retVal[0]++;
		});
		
		Thread.sleep(1000);
		
		List<Integer> result = AsyncHelper.notifyAndGetForFlag(Integer.class, "TestSingleSuppliersUntilFlag").collect(Collectors.toList());
		assertTrue(result.size() > 0);
		for (int i = 0; i < result.size(); i++) {
			assertTrue(i == result.get(i));
		}
		print("" + result);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testScheduleSuppliersAndWait()  throws InterruptedException {
		List<Integer> retVals = AsyncHelper.scheduleSuppliersAndWait(0, 3, TimeUnit.SECONDS, true,
				() -> {
					return 10;
				},
				() -> {
					return 20;
				},
				() -> {
					return 30;
				},
				() -> {
					return 40;
				},
				() -> {
					return 50;
				}).collect(Collectors.toList());
		
		assertEquals(retVals.size(), 5);
		AtomicInteger val = new AtomicInteger(0);
		retVals.forEach(value -> Assert.assertEquals(val.addAndGet(10), (int)value));
	}
	
	private void printTime() {
		System.out.println(new SimpleDateFormat("hh:mm:ss.SSS").format(new Date()));
	}
	
	
	@Test
	public void testScheduleTasksWait()  throws InterruptedException {
		int[] retVal = new int[5];
		AsyncHelper.scheduleTasksAndWait(0, 100, TimeUnit.MILLISECONDS, true,
		() -> {
			printTime();
			retVal[0] = 10;
		},
		() -> {
			printTime();
			retVal[1] = 20;
		},
		() -> {
			printTime();
			retVal[2] = 30;
		},
		() -> {
			printTime();
			retVal[3] = 40;
		},
		() -> {
			printTime();
			retVal[4] = 50;
		});
		
		printTime();
		assertArrayEquals(retVal, new int[]{10, 20, 30, 40, 50});
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSuppliers() throws InterruptedException {
		Supplier<Integer>[] resultSuppliers = AsyncHelper.submitSuppliers(delayedSupplier(() -> {
			return 10;
		}, 100),
		delayedSupplier(() -> {
			return 20;
		}, 100),
		delayedSupplier(() -> {
			return 30;
		}, 100),
		delayedSupplier(() -> {
			return 40;
		}, 100),
		delayedSupplier(() -> {
			return 50;
		}, 100)
		);
		
		assertEquals(resultSuppliers.length, 5);
		AtomicInteger val = new AtomicInteger(0);
		Stream.of(resultSuppliers).map(Supplier::get).forEach(value -> Assert.assertEquals(val.addAndGet(10), (int)value));
		
	}
	
	@Test
	public void testScheduleSuppliersForSingleAccess() throws InterruptedException {
		@SuppressWarnings("unchecked")
		boolean submitSuppliers = AsyncHelper.scheduleSuppliersForSingleAccess(10, 100, TimeUnit.MILLISECONDS, true,
				new Supplier[] {
					() -> {
						return 10;
					},
					() -> {
						return 20;
					},
					() -> {
						return 30;
					},
					() -> {
						return 40;
					},
					() -> {
						return 50;
					}
				}, 
				"Scheduled", "Multiple","Suppliers","key"
		);
		
		assertTrue(submitSuppliers);
		
		List<Integer> retVals = AsyncHelper.waitAndGetFromMultipleSuppliers(Integer.class, "Scheduled", "Multiple","Suppliers","key").collect(Collectors.toList());
		assertEquals(retVals.size(), 5);
		AtomicInteger val = new AtomicInteger(0);
		retVals.forEach(value -> Assert.assertEquals(val.addAndGet(10), (int)value));
		
	}
	
	@Test
	public void testSubmitSuppliersForSingleAccess() throws InterruptedException {
		@SuppressWarnings("unchecked")
		boolean submitSuppliers = AsyncHelper.submitSuppliersForSingleAccess(
				new Supplier[] {
					delayedSupplier(() -> {
						return 10;
					}, 100),
					delayedSupplier(() -> {
						return 20;
					}, 100),
					delayedSupplier(() -> {
						return 30;
					}, 100),
					delayedSupplier(() -> {
						return 40;
					}, 100),
					delayedSupplier(() -> {
						return 50;
					}, 100)
					}, 
				"Multiple","Suppliers","key"
		);
		
		assertTrue(submitSuppliers);
		
		List<Integer> retVals = AsyncHelper.waitAndGetFromMultipleSuppliers(Integer.class, "Multiple","Suppliers","key").collect(Collectors.toList());
		assertEquals(retVals.size(), 5);
		AtomicInteger val = new AtomicInteger(0);
		retVals.forEach(value -> Assert.assertEquals(val.addAndGet(10), (int)value));
		
	}
	
	@Test
	public void testWaitAndNotifyAllForFlag() throws InterruptedException {
		int[] retVal = new int[2];
		
		AsyncHelper.submitTask(() -> {
			System.out.println("Inside notification task.");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			retVal[0] = 10;
			AsyncHelper.notifyAllFlag("FLAG2");
			System.out.println("Notifyed FLAG2");
		});
		
		
		AsyncHelper.submitTask(() -> {
			try {
				System.out.println("Wating for FLAG2 in Task2");
				AsyncHelper.waitForFlag("FLAG2");
				System.out.println("Completed waiting for FLAG2 in Task2");
				retVal[1] = retVal[0] + 10;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Task2");
		
		System.out.println("Wating for FLAG2 in main");
		AsyncHelper.waitForFlag("FLAG2");
		System.out.println("Completed waiting for FLAG2 in main");
		assertEquals(retVal[0], 10);
		
		System.out.println("Wating for Task2 in main");
		AsyncHelper.waitForTask("Task2");
		assertEquals(retVal[1], 20);
		System.out.println("Completed waiting for Task2 in main");
	}
	
	
	private void print(String msg) {
		System.out.println(msg);
	}

	@Test
	public void testScheduleTask()  throws InterruptedException {
		int[] retVal = new int[3];
		AtomicInteger count = new AtomicInteger(0);
		AsyncHelper.scheduleTask(10, 100, TimeUnit.MILLISECONDS, true,
		() -> {
			int index = count.getAndIncrement();
			retVal[index] = (index + 1) * 10  ;
		}, 3);
		
		Thread.sleep(500);
		assertArrayEquals(retVal, new int[]{10, 20, 30});
	}

	@Test
	public void testScheduleSupplier()  throws InterruptedException {
		AtomicInteger count = new AtomicInteger(0);
		Supplier<Integer>[] scheduleSuppliers = AsyncHelper.scheduleSupplier(10, 100, TimeUnit.MILLISECONDS, true,
		() -> {
			printTime();
			int index = count.getAndIncrement();
			return (index + 1) * 10;  
		}, 3);
		
		assertEquals(scheduleSuppliers.length, 3);
		AtomicInteger val = new AtomicInteger(0);
		Stream.of(scheduleSuppliers).map(Supplier::get).forEach(value -> Assert.assertEquals(val.addAndGet(10), (int)value));
	}

	@Test
	public void testScheduleSupplierAndWait()  throws InterruptedException {
		AtomicInteger count = new AtomicInteger(0);
		List<Integer> retVals = AsyncHelper.scheduleSupplierAndWait(0, 100, TimeUnit.MILLISECONDS, true,
		() -> {
			printTime();
			int index = count.getAndIncrement();
			return (index + 1) * 10;  
		}, 3).collect(Collectors.toList());
		assertEquals(retVals.size(), 3);
		AtomicInteger val = new AtomicInteger(0);
		retVals.forEach(value -> Assert.assertEquals(val.addAndGet(10), (int)value));
	}

	@Test
	public void testScheduleTaskWait()  throws InterruptedException {
		int[] retVal = new int[3];
		AtomicInteger count = new AtomicInteger(0);
		AsyncHelper.scheduleTaskAndWait(0, 3, TimeUnit.SECONDS, true,
		() -> {
			printTime();
			int index = count.getAndIncrement();
			retVal[index] = (index + 1) * 10;
		}, 3);
		
		printTime();
		assertArrayEquals(retVal, new int[]{10, 20, 30});
	}

	@Test
	public void testScheduleMultipleSupplierForSingleAccess() throws InterruptedException {
		AtomicInteger count = new AtomicInteger(0);
		boolean submitSuppliers = AsyncHelper.scheduleSupplierForSingleAccess(10, 100, TimeUnit.MILLISECONDS, true,
					() -> {
						int index = count.getAndIncrement();
						return (index + 1) * 10;
					}, 3, 
				"Scheduled", "Multiple","Suppliers","key"
		);
		
		assertTrue(submitSuppliers);
		
		List<Integer> retVals = AsyncHelper.waitAndGetFromMultipleSuppliers(Integer.class, "Scheduled", "Multiple","Suppliers","key").collect(Collectors.toList());
		assertEquals(retVals.size(), 3);
		AtomicInteger val = new AtomicInteger(0);
		retVals.forEach(value -> Assert.assertEquals(val.addAndGet(10), (int)value));
		
	}

	@Test
	public void testScheduleTaskSingleTime()  throws InterruptedException {
		int[] retVal = new int[]{0,20,20};
		AtomicInteger count = new AtomicInteger(0);
		AsyncHelper.scheduleTask(10, TimeUnit.MILLISECONDS,
		() -> {
			int index = count.getAndIncrement();
			retVal[index] = (index + 1) * 10;		});
		
		Thread.sleep(500);
		assertArrayEquals(retVal, new int[]{10, 20, 20});
	}

	@Test
	public void testScheduleSupplierSingleTime()  throws InterruptedException {
		Supplier<Integer> scheduleSupplier = AsyncHelper.scheduleSupplier(10, TimeUnit.MILLISECONDS,
		() -> {
			return 10;
		});
		
		assertEquals((int)scheduleSupplier.get(), 10);
	}

	@Test
	public void testScheduleSupplierAndWaitSingleTime()  throws InterruptedException {
		Optional<Integer> retVal = AsyncHelper.scheduleSupplierAndWait(0, TimeUnit.SECONDS,
		() -> {
			return 10;
		});
		assertEquals((int)retVal.get(), 10);
	}

	@Test
	public void testScheduleTaskWaitSingleTime()  throws InterruptedException {
		int[] retVal = new int[]{0,20,20};
		AtomicInteger count = new AtomicInteger(0);
		AsyncHelper.scheduleTaskAndWait(0, TimeUnit.SECONDS,
		() -> {
			printTime();
			retVal[count.getAndIncrement()] = 10;
		});
		
		printTime();
		assertArrayEquals(retVal, new int[]{10, 20, 20});
	}

	@Test
	public void testScheduleSupplierForSingleAccessSingleTime() throws InterruptedException {
		boolean submitSuppliers = AsyncHelper.scheduleSupplierForSingleAccess(10, TimeUnit.MILLISECONDS,
					() -> {
						return 10;
					}, 
				"Scheduled", "Single","Supplier","key"
		);
		
		assertTrue(submitSuppliers);
		
		Optional<Integer> result = AsyncHelper.waitAndGet(Integer.class, "Scheduled", "Single","Supplier","key");
		assertTrue(result.isPresent());
		assertEquals((int)result.get(), 10);
		
	}
	
	@Test
	public void testSubmitTasksAndWait() {
		int[] retVal = new int[5];
		AsyncHelper.submitTasksAndWait(
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
	public void testSubmitTasksAndWaitWithCancelWithInterrupt() {
		int[] retVal = new int[]{0, 0, 0, 0, 0};
		AtomicBoolean cancel = new AtomicBoolean(false);
		AsyncHelper.submitTask(delayedRunnable(() -> {
			cancel.set(true);
		}, 3500));
		
		AsyncHelper.submitTasksAndWait(
		() -> cancel.get(), true,
		delayedRunnable(() -> {
			retVal[0] = 10;
			printTime();
		}, 1000),
		delayedRunnable(() -> {
			retVal[1] = 20;
			printTime();
		}, 2000),
		delayedRunnable(() -> {
			retVal[2] = 30;
			printTime();
		}, 3000),
		delayedRunnable(() -> {
			retVal[3] = 40;
			printTime();
		}, 4000),
		delayedRunnable(() -> {
			retVal[4] = 50;
			printTime();
		}, 5000));
		
		assertArrayEquals(retVal, new int[]{10, 20, 30, 0, 0});
	}
	
	
	@Test
	public void testSubmitTasksWithKeys() {
		int[] retVal = new int[5];
		Object[] keys = {"Submitted", "Multiple", "Tasks"};
		AsyncHelper.submitTasks(
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
		
		AsyncHelper.waitForMultipleTasks(keys);
		
		assertArrayEquals(retVal, new int[]{10, 20, 30, 40, 50});
	}

}
