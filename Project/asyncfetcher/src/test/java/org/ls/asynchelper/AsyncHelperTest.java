package org.ls.asynchelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.Supplier;

import org.junit.After;
import org.junit.AfterClass;
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
		assertEquals(AsyncHelper.INSTANCE.asyncGet(delayedSupplier(() -> "Value1")).get(), "Value1");
		assertEquals(AsyncHelper.INSTANCE.submitAndGet(() -> delayedSupplier(() -> "Value2")).get().get(), "Value2");
		assertEquals(AsyncHelper.INSTANCE.submitSupplier(delayedSupplier(() -> "Value3")).get(), "Value3");
		assertEquals(AsyncHelper.INSTANCE.submitCallable(() -> delayedSupplier(() -> "Value4")).get().get(), "Value4");

		String[] val = new String[1];
		AsyncHelper.INSTANCE.submitTask(() -> {
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
		AsyncHelper.INSTANCE.submitTask(delayedRunnable(() -> {
			val[0] = "Value1";
		}), "task1");
		AsyncHelper.INSTANCE.submitTask(delayedRunnable(() -> {
			val[1] = "Value2";
		}, 1000), "task2");
		AsyncHelper.INSTANCE.submitTask(delayedRunnable(() -> {
			val[2] = "Value3";
		}, 700), "task3");
		AsyncHelper.INSTANCE.submitTask(delayedRunnable(() -> {
			val[3] = "Value4";
		}, 1000), "task4");

		AsyncHelper.INSTANCE.waitForTask("task1");
		AsyncHelper.INSTANCE.waitForTask("task2");
		AsyncHelper.INSTANCE.waitForTask("task3");
		AsyncHelper.INSTANCE.waitForTask("task4");

		assertEquals(val[0], "Value1");
		assertEquals(val[1], "Value2");
		assertEquals(val[2], "Value3");
		assertEquals(val[3], "Value4");

	}
	
	@Test
	public void testMultipleAsyncTasksMultipleTimes() {
		String[] val = new String[4];
		AsyncHelper.INSTANCE.submitTask(delayedRunnable(() -> {
			val[0] = "Value1";
		}), "task1");
		AsyncHelper.INSTANCE.submitTask(delayedRunnable(() -> {
			val[1] = "Value2";
		}, 1000), "task2");
		AsyncHelper.INSTANCE.submitTask(delayedRunnable(() -> {
			val[2] = "Value3";
		}, 700), "task3");
		AsyncHelper.INSTANCE.submitTask(delayedRunnable(() -> {
			val[3] = "Value4";
		}, 1000), "task4");

		AsyncHelper.INSTANCE.waitForTask("task1");
		AsyncHelper.INSTANCE.waitForTask("task2");
		AsyncHelper.INSTANCE.waitForTask("task3");
		AsyncHelper.INSTANCE.waitForTask("task4");

		assertEquals(val[0], "Value1");
		assertEquals(val[1], "Value2");
		assertEquals(val[2], "Value3");
		assertEquals(val[3], "Value4");
		
		AsyncHelper.INSTANCE.waitForTask("task1");
		AsyncHelper.INSTANCE.waitForTask("task2");
		AsyncHelper.INSTANCE.waitForTask("task3");
		AsyncHelper.INSTANCE.waitForTask("task4");

		assertEquals(val[0], "Value1");
		assertEquals(val[1], "Value2");
		assertEquals(val[2], "Value3");
		assertEquals(val[3], "Value4");
		
		AsyncHelper.INSTANCE.submitTask(delayedRunnable(() -> {
			val[0] = "Value11";
		}), "task1");
		AsyncHelper.INSTANCE.submitTask(delayedRunnable(() -> {
			val[1] = "Value22";
		}, 1000), "task2");
		AsyncHelper.INSTANCE.submitTask(delayedRunnable(() -> {
			val[2] = "Value33";
		}, 700), "task3");
		AsyncHelper.INSTANCE.submitTask(delayedRunnable(() -> {
			val[3] = "Value44";
		}, 1000), "task4");
		
		AsyncHelper.INSTANCE.waitForTask("task1");
		AsyncHelper.INSTANCE.waitForTask("task2");
		AsyncHelper.INSTANCE.waitForTask("task3");
		AsyncHelper.INSTANCE.waitForTask("task4");
		
		assertEquals(val[0], "Value11");
		assertEquals(val[1], "Value22");
		assertEquals(val[2], "Value33");
		assertEquals(val[3], "Value44");

	}
	
	@Test
	public void testMultipleAsyncSupplierSubmittedForMultipleAccess() {
		AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value1"), "query1");
		AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value2", 1000), "query2");
		AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value3", 700), "query3");
		AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value4", 1000), "query4");

		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query1").get(), "Value1");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query2").get(), "Value2");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query3").get(), "Value3");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query4").get(), "Value4");

		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query1").get(), "Value1");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query2").get(), "Value2");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query3").get(), "Value3");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query4").get(), "Value4");
	}

	@Test
	public void testMultipleAsyncSupplierSubmittedForSingleAccess() {
		AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value1"), "query11");
		AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value2", 1000), "query22");
		AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value3", 700), "query33");
		AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value4", 1000), "query44");

		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query11").get(), "Value1");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query22").get(), "Value2");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query33").get(), "Value3");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query44").get(), "Value4");

		assertTrue(!AsyncHelper.INSTANCE.waitAndGet(String.class, "query11").isPresent());
		assertTrue(!AsyncHelper.INSTANCE.waitAndGet(String.class, "query22").isPresent());
		assertTrue(!AsyncHelper.INSTANCE.waitAndGet(String.class, "query33").isPresent());
		assertTrue(!AsyncHelper.INSTANCE.waitAndGet(String.class, "query44").isPresent());
	}

	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForSingleAccess() {
		assertTrue(AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value1"), "query111"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value2", 1000), "query222"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value3", 700), "query333"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value4", 1000), "query444"));

		assertFalse(AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value1"), "query111"));
		assertFalse(
				AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value2", 1000), "query222"));
		assertFalse(
				AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value3", 700), "query333"));
		assertFalse(
				AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value4", 1000), "query444"));

		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query111").get(), "Value1");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query222").get(), "Value2");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query333").get(), "Value3");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query444").get(), "Value4");

		assertTrue(AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value11"), "query111"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value22", 1000), "query222"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value33", 700), "query333"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value44", 1000), "query444"));
		
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query111").get(), "Value11");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query222").get(), "Value22");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query333").get(), "Value33");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query444").get(), "Value44");

	}

	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForMultipleAccess() {
		assertTrue(AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value1"), "query1111"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value2", 1000), "query2222"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value3", 700), "query3333"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value4", 1000), "query4444"));

		assertFalse(AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value1a"), "query1111"));
		assertFalse(
				AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value2a", 1000), "query2222"));
		assertFalse(
				AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value3a", 700), "query3333"));
		assertFalse(
				AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value4a", 1000), "query4444"));

		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query1111").get(), "Value1");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query2222").get(), "Value2");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query3333").get(), "Value3");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query4444").get(), "Value4");

		assertTrue(AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value11"), "query1111"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value22", 1000), "query2222"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value33", 700), "query3333"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value44", 1000), "query4444"));
		
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query1111").get(), "Value11");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query2222").get(), "Value22");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query3333").get(), "Value33");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query4444").get(), "Value44");
	}
	
	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForMultipleAccessThenSingleAccess() {
		assertTrue(AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value1"), "query11111"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value2", 1000), "query22222"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value3", 700), "query33333"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value4", 1000), "query44444"));

		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query11111").get(), "Value1");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query22222").get(), "Value2");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query33333").get(), "Value3");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query44444").get(), "Value4");
		
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query11111").get(), "Value1");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query22222").get(), "Value2");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query33333").get(), "Value3");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query44444").get(), "Value4");

		assertTrue(AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value11"), "query11111"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value22", 1000), "query22222"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value33", 700), "query33333"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value44", 1000), "query44444"));
		
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query11111").get(), "Value11");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query22222").get(), "Value22");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query33333").get(), "Value33");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query44444").get(), "Value44");
		
		assertTrue(!AsyncHelper.INSTANCE.waitAndGet(String.class, "query11111").isPresent());
		assertTrue(!AsyncHelper.INSTANCE.waitAndGet(String.class, "query22222").isPresent());
		assertTrue(!AsyncHelper.INSTANCE.waitAndGet(String.class, "query33333").isPresent());
		assertTrue(!AsyncHelper.INSTANCE.waitAndGet(String.class, "query44444").isPresent());
	}
	
	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForSingleAccessThenMultipleAccess() {
		assertTrue(AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value1"), "query111111"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value2", 1000), "query222222"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value3", 700), "query333333"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value4", 1000), "query444444"));
		
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query111111").get(), "Value1");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query222222").get(), "Value2");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query333333").get(), "Value3");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query444444").get(), "Value4");
		
		assertTrue(!AsyncHelper.INSTANCE.waitAndGet(String.class, "query111111").isPresent());
		assertTrue(!AsyncHelper.INSTANCE.waitAndGet(String.class, "query222222").isPresent());
		assertTrue(!AsyncHelper.INSTANCE.waitAndGet(String.class, "query333333").isPresent());
		assertTrue(!AsyncHelper.INSTANCE.waitAndGet(String.class, "query444444").isPresent());
		
		assertTrue(AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value11"), "query111111"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value22", 1000), "query222222"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value33", 700), "query333333"));
		assertTrue(
				AsyncHelper.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value44", 1000), "query444444"));

		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query111111").get(), "Value11");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query222222").get(), "Value22");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query333333").get(), "Value33");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query444444").get(), "Value44");
		
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query111111").get(), "Value11");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query222222").get(), "Value22");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query333333").get(), "Value33");
		assertEquals(AsyncHelper.INSTANCE.waitAndGet(String.class, "query444444").get(), "Value44");
	}
	
	@Test
	public void testWaitAndNotifyForFlag() throws InterruptedException {
		boolean[] retVal = new boolean[1];
		AsyncHelper.INSTANCE.submitTask(delayedRunnable(() -> {
			retVal[0] = true;
			AsyncHelper.INSTANCE.notifyFlag("FLAG1");
		}, 2000));
		AsyncHelper.INSTANCE.waitForFlag("FLAG1");
		assertTrue(retVal[0]);
	}
	
	@Test
	public void testWaitAndNotifyAllForFlag() throws InterruptedException {
		boolean[] retVal = new boolean[2];
		AsyncHelper.INSTANCE.submitTask(delayedRunnable(() -> {
			retVal[0] = true;
			AsyncHelper.INSTANCE.notifyAllFlag("FLAG2");
		}, 2000));
		
		
		AsyncHelper.INSTANCE.submitTask(() -> {
			try {
				AsyncHelper.INSTANCE.waitForFlag("FLAG2");
				retVal[1] = retVal[0];
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Task2");
		
		AsyncHelper.INSTANCE.waitForFlag("FLAG2");
		assertTrue(retVal[0]);
		
		AsyncHelper.INSTANCE.waitForTask("Task2");
		assertTrue(retVal[1]);
	}
	
	private void print(String msg) {
		System.out.println(msg);
	}

}
