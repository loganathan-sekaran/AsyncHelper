package org.ls.asynchelper;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AsyncHelperTest {

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
			} catch (InterruptedException e) {
			}
			runnable.run();
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
		boolean[] retVal = new boolean[1];
		AsyncHelper.submitTask(delayedRunnable(() -> {
			retVal[0] = true;
			AsyncHelper.notifyFlag("FLAG1");
		}, 2000));
		AsyncHelper.waitForFlag("FLAG1");
		assertTrue(retVal[0]);
	}
	
	@Test
	public void testSubmitTask() throws InterruptedException {
		boolean[] retVal = new boolean[1];
		AsyncHelper.submitTask(delayedRunnable(() -> {
			retVal[0] = true;
		}, 100));
		
		Thread.sleep(500);
		assertTrue(retVal[0]);
	}
	
	@Test
	public void testSubmitTasks() throws InterruptedException {
		boolean[] retVal = new boolean[5];
		AsyncHelper.submitTasks(delayedRunnable(() -> {
			retVal[0] = true;
		}, 100),
		delayedRunnable(() -> {
			retVal[1] = true;
		}, 100),
		delayedRunnable(() -> {
			retVal[2] = true;
		}, 100),
		delayedRunnable(() -> {
			retVal[3] = true;
		}, 100),
		delayedRunnable(() -> {
			retVal[4] = true;
		}, 100));
		
		Thread.sleep(1000);
		assertArrayEquals(retVal, new boolean[]{true, true, true, true, true});
	}
	
	@Test
	public void testScheduleTasks()  throws InterruptedException {
		boolean[] retVal = new boolean[5];
		AsyncHelper.scheduleTasks(10, 100, TimeUnit.MILLISECONDS, true,
		() -> {
			retVal[0] = true;
		},
		() -> {
			retVal[1] = true;
		},
		() -> {
			retVal[2] = true;
		},
		() -> {
			retVal[3] = true;
		},
		() -> {
			retVal[4] = true;
		});
		
		Thread.sleep(1000);
		assertArrayEquals(retVal, new boolean[]{true, true, true, true, true});
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testScheduleSuppliers()  throws InterruptedException {
		Supplier<Boolean>[] scheduleSuppliers = AsyncHelper.scheduleMultipleSuppliers(10, 100, TimeUnit.MILLISECONDS, true,
		() -> {
			return true;
		},
		() -> {
			return true;
		},
		() -> {
			return true;
		},
		() -> {
			return true;
		},
		() -> {
			return true;
		});
		
		Stream.of(scheduleSuppliers).map(Supplier::get).forEach(Assert::assertTrue);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testScheduleSuppliersAndWait()  throws InterruptedException {
		Stream<Boolean> retVals = AsyncHelper.scheduleMultipleSuppliersAndWait(0, 3, TimeUnit.SECONDS, true,
		() -> {
			return true;
		},
		() -> {
			return true;
		},
		() -> {
			return true;
		},
		() -> {
			return true;
		},
		() -> {
			return true;
		});
		retVals.forEach(Assert::assertTrue);
	}
	
	private void printTime() {
		System.out.println(new SimpleDateFormat("hh:mm:ss.SSS").format(new Date()));
	}
	
	
	@Test
	public void testScheduleTasksWait()  throws InterruptedException {
		boolean[] retVal = new boolean[5];
		AsyncHelper.scheduleTasksAndWait(0, 3, TimeUnit.SECONDS, true,
		() -> {
			printTime();
			retVal[0] = true;
		},
		() -> {
			printTime();
			retVal[1] = true;
		},
		() -> {
			printTime();
			retVal[2] = true;
		},
		() -> {
			printTime();
			retVal[3] = true;
		},
		() -> {
			printTime();
			retVal[4] = true;
		});
		
		printTime();
		assertArrayEquals(retVal, new boolean[]{true, true, true, true, true});
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testMultipleSuppliers() throws InterruptedException {
		Supplier<Boolean>[] submitMultipleSuppliers = AsyncHelper.submitMultipleSuppliers(delayedSupplier(() -> {
			return true;
		}, 100),
		delayedSupplier(() -> {
			return true;
		}, 100),
		delayedSupplier(() -> {
			return true;
		}, 100),
		delayedSupplier(() -> {
			return true;
		}, 100),
		delayedSupplier(() -> {
			return true;
		}, 100)
		);
		
		Stream.of(submitMultipleSuppliers).map(Supplier::get).forEach(val -> assertTrue(val));
		
	}
	
	@Test
	public void testScheduleMultipleSuppliersForSingleAccess() throws InterruptedException {
		@SuppressWarnings("unchecked")
		boolean submitMultipleSuppliers = AsyncHelper.scheduleMultipleSuppliersForSingleAccess(10, 100, TimeUnit.MILLISECONDS, true,
				new Supplier[] {
					() -> {
						return true;
					},
					() -> {
						return true;
					},
					() -> {
						return true;
					},
					() -> {
						return true;
					},
					() -> {
						return true;
					}
				}, 
				"Scheduled", "Multiple","Suppliers","key"
		);
		
		assertTrue(submitMultipleSuppliers);
		
		List<Boolean> waitAndGetMultiple = AsyncHelper.waitAndGetMultiple(Boolean.class, "Scheduled", "Multiple","Suppliers","key").collect(Collectors.toList());
		assertEquals(waitAndGetMultiple.size(), 5);
		waitAndGetMultiple.forEach(Assert::assertTrue);
		
	}
	
	@Test
	public void testSubmitMultipleSuppliersForSingleAccess() throws InterruptedException {
		@SuppressWarnings("unchecked")
		boolean submitMultipleSuppliers = AsyncHelper.submitMultipleSuppliersForSingleAccess(
				new Supplier[] {
					delayedSupplier(() -> {
						return true;
					}, 100),
					delayedSupplier(() -> {
						return true;
					}, 100),
					delayedSupplier(() -> {
						return true;
					}, 100),
					delayedSupplier(() -> {
						return true;
					}, 100),
					delayedSupplier(() -> {
						return true;
					}, 100)
					}, 
				"Multiple","Suppliers","key"
		);
		
		assertTrue(submitMultipleSuppliers);
		
		List<Boolean> waitAndGetMultiple = AsyncHelper.waitAndGetMultiple(Boolean.class, "Multiple","Suppliers","key").collect(Collectors.toList());
		assertEquals(waitAndGetMultiple.size(), 5);
		waitAndGetMultiple.forEach(Assert::assertTrue);
		
	}
	
	@Test
	public void testWaitAndNotifyAllForFlag() throws InterruptedException {
		boolean[] retVal = new boolean[2];
		AsyncHelper.submitTask(delayedRunnable(() -> {
			retVal[0] = true;
			AsyncHelper.notifyAllFlag("FLAG2");
		}, 2000));
		
		
		AsyncHelper.submitTask(() -> {
			try {
				AsyncHelper.waitForFlag("FLAG2");
				retVal[1] = retVal[0];
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Task2");
		
		AsyncHelper.waitForFlag("FLAG2");
		assertTrue(retVal[0]);
		
		AsyncHelper.waitForTask("Task2");
		assertTrue(retVal[1]);
	}
	
	
	private void print(String msg) {
		System.out.println(msg);
	}

	@Test
	public void testScheduleTask()  throws InterruptedException {
		boolean[] retVal = new boolean[3];
		AtomicInteger count = new AtomicInteger(0);
		AsyncHelper.scheduleTask(10, 100, TimeUnit.MILLISECONDS, true,
		() -> {
			retVal[count.getAndIncrement()] = true;
		}, 3);
		
		Thread.sleep(500);
		assertArrayEquals(retVal, new boolean[]{true, true, true});
	}

	@Test
	public void testScheduleSupplier()  throws InterruptedException {
		Supplier<Boolean>[] scheduleSuppliers = AsyncHelper.scheduleSupplier(10, 100, TimeUnit.MILLISECONDS, true,
		() -> {
			return true;
		}, 3);
		
		Stream.of(scheduleSuppliers).map(Supplier::get).forEach(Assert::assertTrue);
	}

	@Test
	public void testScheduleSupplierAndWait()  throws InterruptedException {
		Stream<Boolean> retVals = AsyncHelper.scheduleSupplierAndWait(0, 3, TimeUnit.SECONDS, true,
		() -> {
			return true;
		}, 3);
		retVals.forEach(Assert::assertTrue);
	}

	@Test
	public void testScheduleTaskWait()  throws InterruptedException {
		boolean[] retVal = new boolean[3];
		AtomicInteger count = new AtomicInteger(0);
		AsyncHelper.scheduleTaskAndWait(0, 3, TimeUnit.SECONDS, true,
		() -> {
			printTime();
			retVal[count.getAndIncrement()] = true;
		}, 3);
		
		printTime();
		assertArrayEquals(retVal, new boolean[]{true, true, true});
	}

	@Test
	public void testScheduleMultipleSupplierForSingleAccess() throws InterruptedException {
		boolean submitMultipleSuppliers = AsyncHelper.scheduleSupplierForSingleAccess(10, 100, TimeUnit.MILLISECONDS, true,
					() -> {
						return true;
					}, 3, 
				"Scheduled", "Multiple","Suppliers","key"
		);
		
		assertTrue(submitMultipleSuppliers);
		
		List<Boolean> waitAndGetMultiple = AsyncHelper.waitAndGetMultiple(Boolean.class, "Scheduled", "Multiple","Suppliers","key").collect(Collectors.toList());
		assertEquals(waitAndGetMultiple.size(), 3);
		waitAndGetMultiple.forEach(Assert::assertTrue);
		
	}

	@Test
	public void testScheduleTaskSingleTime()  throws InterruptedException {
		boolean[] retVal = new boolean[3];
		AtomicInteger count = new AtomicInteger(0);
		AsyncHelper.scheduleTask(10, TimeUnit.MILLISECONDS,
		() -> {
			retVal[count.getAndIncrement()] = true;
		});
		
		Thread.sleep(500);
		assertArrayEquals(retVal, new boolean[]{true, false, false});
	}

	@Test
	public void testScheduleSupplierSingleTime()  throws InterruptedException {
		Supplier<Boolean> scheduleSupplier = AsyncHelper.scheduleSupplier(10, TimeUnit.MILLISECONDS,
		() -> {
			return true;
		});
		
		assertTrue(scheduleSupplier.get());
	}

	@Test
	public void testScheduleSupplierAndWaitSingleTime()  throws InterruptedException {
		Optional<Boolean> retVal = AsyncHelper.scheduleSupplierAndWait(0, TimeUnit.SECONDS,
		() -> {
			return true;
		});
		assertTrue(retVal.get());
	}

	@Test
	public void testScheduleTaskWaitSingleTime()  throws InterruptedException {
		boolean[] retVal = new boolean[3];
		AtomicInteger count = new AtomicInteger(0);
		AsyncHelper.scheduleTaskAndWait(0, TimeUnit.SECONDS,
		() -> {
			printTime();
			retVal[count.getAndIncrement()] = true;
		});
		
		printTime();
		assertArrayEquals(retVal, new boolean[]{true, false, false});
	}

	@Test
	public void testScheduleMultipleSupplierForSingleAccessSingleTime() throws InterruptedException {
		boolean submitMultipleSuppliers = AsyncHelper.scheduleSupplierForSingleAccess(10, TimeUnit.MILLISECONDS,
					() -> {
						return true;
					}, 
				"Scheduled", "Multiple","Suppliers","key"
		);
		
		assertTrue(submitMultipleSuppliers);
		
		List<Boolean> waitAndGetMultiple = AsyncHelper.waitAndGetMultiple(Boolean.class, "Scheduled", "Multiple","Suppliers","key").collect(Collectors.toList());
		assertEquals(waitAndGetMultiple.size(), 1);
		waitAndGetMultiple.forEach(Assert::assertTrue);
		
	}

}
