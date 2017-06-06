package org.ls.asyncfetcher;

import java.util.function.Supplier;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AsyncFetcherTest {

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
		assertEquals(AsyncFetcher.INSTANCE.asyncGet(delayedSupplier(() -> "Value1")).get(), "Value1");
		assertEquals(AsyncFetcher.INSTANCE.submitAndGet(() -> delayedSupplier(() -> "Value2")).get().get(), "Value2");
		assertEquals(AsyncFetcher.INSTANCE.submitSupplier(delayedSupplier(() -> "Value3")).get(), "Value3");
		assertEquals(AsyncFetcher.INSTANCE.submitCallable(() -> delayedSupplier(() -> "Value4")).get().get(), "Value4");

		String[] val = new String[1];
		AsyncFetcher.INSTANCE.submitTask(() -> {
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
		AsyncFetcher.INSTANCE.submitTask(delayedRunnable(() -> {
			val[0] = "Value1";
		}), "task1");
		AsyncFetcher.INSTANCE.submitTask(delayedRunnable(() -> {
			val[1] = "Value2";
		}, 1000), "task2");
		AsyncFetcher.INSTANCE.submitTask(delayedRunnable(() -> {
			val[2] = "Value3";
		}, 700), "task3");
		AsyncFetcher.INSTANCE.submitTask(delayedRunnable(() -> {
			val[3] = "Value4";
		}, 1000), "task4");

		AsyncFetcher.INSTANCE.waitForTask("task1");
		AsyncFetcher.INSTANCE.waitForTask("task2");
		AsyncFetcher.INSTANCE.waitForTask("task3");
		AsyncFetcher.INSTANCE.waitForTask("task4");

		assertEquals(val[0], "Value1");
		assertEquals(val[1], "Value2");
		assertEquals(val[2], "Value3");
		assertEquals(val[3], "Value4");

	}
	
	@Test
	public void testMultipleAsyncTasksMultipleTimes() {
		String[] val = new String[4];
		AsyncFetcher.INSTANCE.submitTask(delayedRunnable(() -> {
			val[0] = "Value1";
		}), "task1");
		AsyncFetcher.INSTANCE.submitTask(delayedRunnable(() -> {
			val[1] = "Value2";
		}, 1000), "task2");
		AsyncFetcher.INSTANCE.submitTask(delayedRunnable(() -> {
			val[2] = "Value3";
		}, 700), "task3");
		AsyncFetcher.INSTANCE.submitTask(delayedRunnable(() -> {
			val[3] = "Value4";
		}, 1000), "task4");

		AsyncFetcher.INSTANCE.waitForTask("task1");
		AsyncFetcher.INSTANCE.waitForTask("task2");
		AsyncFetcher.INSTANCE.waitForTask("task3");
		AsyncFetcher.INSTANCE.waitForTask("task4");

		assertEquals(val[0], "Value1");
		assertEquals(val[1], "Value2");
		assertEquals(val[2], "Value3");
		assertEquals(val[3], "Value4");
		
		AsyncFetcher.INSTANCE.waitForTask("task1");
		AsyncFetcher.INSTANCE.waitForTask("task2");
		AsyncFetcher.INSTANCE.waitForTask("task3");
		AsyncFetcher.INSTANCE.waitForTask("task4");

		assertEquals(val[0], "Value1");
		assertEquals(val[1], "Value2");
		assertEquals(val[2], "Value3");
		assertEquals(val[3], "Value4");
		
		AsyncFetcher.INSTANCE.submitTask(delayedRunnable(() -> {
			val[0] = "Value11";
		}), "task1");
		AsyncFetcher.INSTANCE.submitTask(delayedRunnable(() -> {
			val[1] = "Value22";
		}, 1000), "task2");
		AsyncFetcher.INSTANCE.submitTask(delayedRunnable(() -> {
			val[2] = "Value33";
		}, 700), "task3");
		AsyncFetcher.INSTANCE.submitTask(delayedRunnable(() -> {
			val[3] = "Value44";
		}, 1000), "task4");
		
		AsyncFetcher.INSTANCE.waitForTask("task1");
		AsyncFetcher.INSTANCE.waitForTask("task2");
		AsyncFetcher.INSTANCE.waitForTask("task3");
		AsyncFetcher.INSTANCE.waitForTask("task4");
		
		assertEquals(val[0], "Value11");
		assertEquals(val[1], "Value22");
		assertEquals(val[2], "Value33");
		assertEquals(val[3], "Value44");

	}
	
	@Test
	public void testMultipleAsyncSupplierSubmittedForMultipleAccess() {
		AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value1"), "query1");
		AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value2", 1000), "query2");
		AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value3", 700), "query3");
		AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value4", 1000), "query4");

		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query1").get(), "Value1");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query2").get(), "Value2");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query3").get(), "Value3");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query4").get(), "Value4");

		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query1").get(), "Value1");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query2").get(), "Value2");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query3").get(), "Value3");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query4").get(), "Value4");
	}

	@Test
	public void testMultipleAsyncSupplierSubmittedForSingleAccess() {
		AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value1"), "query11");
		AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value2", 1000), "query22");
		AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value3", 700), "query33");
		AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value4", 1000), "query44");

		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query11").get(), "Value1");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query22").get(), "Value2");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query33").get(), "Value3");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query44").get(), "Value4");

		assertTrue(!AsyncFetcher.INSTANCE.waitAndGet(String.class, "query11").isPresent());
		assertTrue(!AsyncFetcher.INSTANCE.waitAndGet(String.class, "query22").isPresent());
		assertTrue(!AsyncFetcher.INSTANCE.waitAndGet(String.class, "query33").isPresent());
		assertTrue(!AsyncFetcher.INSTANCE.waitAndGet(String.class, "query44").isPresent());
	}

	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForSingleAccess() {
		assertTrue(AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value1"), "query111"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value2", 1000), "query222"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value3", 700), "query333"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value4", 1000), "query444"));

		assertFalse(AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value1"), "query111"));
		assertFalse(
				AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value2", 1000), "query222"));
		assertFalse(
				AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value3", 700), "query333"));
		assertFalse(
				AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value4", 1000), "query444"));

		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query111").get(), "Value1");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query222").get(), "Value2");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query333").get(), "Value3");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query444").get(), "Value4");

		assertTrue(AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value11"), "query111"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value22", 1000), "query222"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value33", 700), "query333"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value44", 1000), "query444"));
		
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query111").get(), "Value11");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query222").get(), "Value22");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query333").get(), "Value33");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query444").get(), "Value44");

	}

	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForMultipleAccess() {
		assertTrue(AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value1"), "query1111"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value2", 1000), "query2222"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value3", 700), "query3333"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value4", 1000), "query4444"));

		assertFalse(AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value1a"), "query1111"));
		assertFalse(
				AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value2a", 1000), "query2222"));
		assertFalse(
				AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value3a", 700), "query3333"));
		assertFalse(
				AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value4a", 1000), "query4444"));

		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query1111").get(), "Value1");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query2222").get(), "Value2");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query3333").get(), "Value3");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query4444").get(), "Value4");

		assertTrue(AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value11"), "query1111"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value22", 1000), "query2222"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value33", 700), "query3333"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value44", 1000), "query4444"));
		
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query1111").get(), "Value11");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query2222").get(), "Value22");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query3333").get(), "Value33");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query4444").get(), "Value44");
	}
	
	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForMultipleAccessThenSingleAccess() {
		assertTrue(AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value1"), "query11111"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value2", 1000), "query22222"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value3", 700), "query33333"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value4", 1000), "query44444"));

		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query11111").get(), "Value1");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query22222").get(), "Value2");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query33333").get(), "Value3");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query44444").get(), "Value4");
		
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query11111").get(), "Value1");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query22222").get(), "Value2");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query33333").get(), "Value3");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query44444").get(), "Value4");

		assertTrue(AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value11"), "query11111"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value22", 1000), "query22222"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value33", 700), "query33333"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value44", 1000), "query44444"));
		
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query11111").get(), "Value11");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query22222").get(), "Value22");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query33333").get(), "Value33");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query44444").get(), "Value44");
		
		assertTrue(!AsyncFetcher.INSTANCE.waitAndGet(String.class, "query11111").isPresent());
		assertTrue(!AsyncFetcher.INSTANCE.waitAndGet(String.class, "query22222").isPresent());
		assertTrue(!AsyncFetcher.INSTANCE.waitAndGet(String.class, "query33333").isPresent());
		assertTrue(!AsyncFetcher.INSTANCE.waitAndGet(String.class, "query44444").isPresent());
	}
	
	@Test
	public void testMultipleTimeAsyncSupplierSubmittedForSingleAccessThenMultipleAccess() {
		assertTrue(AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value1"), "query111111"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value2", 1000), "query222222"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value3", 700), "query333333"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForSingleAccess(delayedSupplier(() -> "Value4", 1000), "query444444"));
		
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query111111").get(), "Value1");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query222222").get(), "Value2");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query333333").get(), "Value3");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query444444").get(), "Value4");
		
		assertTrue(!AsyncFetcher.INSTANCE.waitAndGet(String.class, "query111111").isPresent());
		assertTrue(!AsyncFetcher.INSTANCE.waitAndGet(String.class, "query222222").isPresent());
		assertTrue(!AsyncFetcher.INSTANCE.waitAndGet(String.class, "query333333").isPresent());
		assertTrue(!AsyncFetcher.INSTANCE.waitAndGet(String.class, "query444444").isPresent());
		
		assertTrue(AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value11"), "query111111"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value22", 1000), "query222222"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value33", 700), "query333333"));
		assertTrue(
				AsyncFetcher.INSTANCE.submitSupplierForMultipleAccess(delayedSupplier(() -> "Value44", 1000), "query444444"));

		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query111111").get(), "Value11");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query222222").get(), "Value22");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query333333").get(), "Value33");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query444444").get(), "Value44");
		
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query111111").get(), "Value11");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query222222").get(), "Value22");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query333333").get(), "Value33");
		assertEquals(AsyncFetcher.INSTANCE.waitAndGet(String.class, "query444444").get(), "Value44");
	}

	private void print(String msg) {
		System.out.println(msg);
	}

}
