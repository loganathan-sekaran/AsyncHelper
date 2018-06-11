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
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * The class AsyncTaskTest.
 * 
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
@RunWith(Parameterized.class)
public class AsyncTaskTest {

	/** The watcher. */
	@Rule
	public TestRule watcher = new TestWatcherAndLogger();
	
	/** The async task. */
	private AsyncTask asyncTask;
	
	/**
	 * Inputs.
	 *
	 * @return the collection
	 */
	@Parameters
	public static Collection<Object[]> inputs() {
		return Arrays.asList(new Object[][] {
				{AsyncTask.getDefault()},
				{AsyncTask.of(Executors.newFixedThreadPool(10))	},
				{AsyncTask.of(Executors.newFixedThreadPool(10), AsyncContext.newInstance())	},
			});
	}
	
	 /**
 	 * Instantiates a new async task test.
 	 *
 	 * @param asyncTask the async task
 	 * @throws Exception the exception
 	 */
 	public AsyncTaskTest(AsyncTask asyncTask) throws Exception {
		 this.asyncTask = asyncTask;
	 }
	 
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
	 * Test multiple async tasks.
	 */
	@Test
	public void testMultipleAsyncTasks() {
		String[] val = new String[4];
		asyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[0] = "Value1";
		}), "task1");
		asyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[1] = "Value2";
		}, 100), "task2");
		asyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[2] = "Value3";
		}, 70), "task3");
		asyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[3] = "Value4";
		}, 100), "task4");

		asyncTask.waitForTask("task1");
		asyncTask.waitForTask("task2");
		asyncTask.waitForTask("task3");
		asyncTask.waitForTask("task4");

		assertEquals(val[0], "Value1");
		assertEquals(val[1], "Value2");
		assertEquals(val[2], "Value3");
		assertEquals(val[3], "Value4");

	}

	/**
	 * Test multiple async tasks multiple times.
	 */
	@Test
	public void testMultipleAsyncTasksMultipleTimes() {
		String[] val = new String[4];
		asyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[0] = "Value1";
		}), "task1");
		asyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[1] = "Value2";
		}, 100), "task2");
		asyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[2] = "Value3";
		}, 70), "task3");
		asyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[3] = "Value4";
		}, 100), "task4");

		asyncTask.waitForTask("task1");
		asyncTask.waitForTask("task2");
		asyncTask.waitForTask("task3");
		asyncTask.waitForTask("task4");

		assertEquals(val[0], "Value1");
		assertEquals(val[1], "Value2");
		assertEquals(val[2], "Value3");
		assertEquals(val[3], "Value4");

		asyncTask.waitForTask("task1");
		asyncTask.waitForTask("task2");
		asyncTask.waitForTask("task3");
		asyncTask.waitForTask("task4");

		assertEquals(val[0], "Value1");
		assertEquals(val[1], "Value2");
		assertEquals(val[2], "Value3");
		assertEquals(val[3], "Value4");

		asyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[0] = "Value11";
		}), "task1");
		asyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[1] = "Value22";
		}, 100), "task2");
		asyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[2] = "Value33";
		}, 70), "task3");
		asyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[3] = "Value44";
		}, 100), "task4");

		asyncTask.waitForTask("task1");
		asyncTask.waitForTask("task2");
		asyncTask.waitForTask("task3");
		asyncTask.waitForTask("task4");

		assertEquals(val[0], "Value11");
		assertEquals(val[1], "Value22");
		assertEquals(val[2], "Value33");
		assertEquals(val[3], "Value44");

	}

	/**
	 * Testtask.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testtask() throws InterruptedException {
		int[] retVal = new int[1];
		asyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			retVal[0] = 10;
		}, 10));

		Thread.sleep(50);
		assertEquals(retVal[0], 10);
	}

	/**
	 * Testtasks.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testtasks() throws InterruptedException {
		int[] retVal = new int[5];
		asyncTask.submitTasks(TestUtil.delayedRunnable(() -> {
			retVal[0] = 10;
		}, 100), TestUtil.delayedRunnable(() -> {
			retVal[1] = 20;
		}, 100), TestUtil.delayedRunnable(() -> {
			retVal[2] = 30;
		}, 100), TestUtil.delayedRunnable(() -> {
			retVal[3] = 40;
		}, 100), TestUtil.delayedRunnable(() -> {
			retVal[4] = 50;
		}, 100));

		Thread.sleep(1000);
		assertArrayEquals(retVal, new int[] { 10, 20, 30, 40, 50 });
	}

	/**
	 * Testtasks and wait.
	 */
	@Test
	public void testtasksAndWait() {
		int[] retVal = new int[5];
		asyncTask.submitTasksAndWait(() -> {
			retVal[0] = 10;
		}, () -> {
			retVal[1] = 20;
		}, () -> {
			retVal[2] = 30;
		}, () -> {
			retVal[3] = 40;
		}, () -> {
			retVal[4] = 50;
		});

		assertArrayEquals(retVal, new int[] { 10, 20, 30, 40, 50 });
	}

	/**
	 * Testtasks and wait with cancel with interrupt.
	 */
	@Test
	public void testtasksAndWaitWithCancelWithInterrupt() {
		int[] retVal = new int[] { 0, 0, 0 };
		AtomicBoolean cancel = new AtomicBoolean(false);
		asyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			cancel.set(true);
		}, 500));

		asyncTask.submitTasksAndWaitCancellable(() -> cancel.get(), true, TestUtil.delayedRunnable(() -> {
			retVal[0] = 10;
			TestUtil.printTime();
		}, 200), TestUtil.delayedRunnable(() -> {
			retVal[1] = 20;
			TestUtil.printTime();
		}, 400), TestUtil.delayedRunnable(() -> {
			retVal[2] = 30;
			TestUtil.printTime();
		}, 600));

		assertFalse(Arrays.toString(new int[] { 10, 20, 30 }).equals(Arrays.toString(retVal)));
	}

	/**
	 * Testtasks with keys.
	 */
	@Test
	public void testtasksWithKeys() {
		int[] retVal = new int[5];
		Object[] keys = { "Submitted", "Multiple", "Tasks" };
		asyncTask.submitTasks(keys, () -> {
			retVal[0] = 10;
		}, () -> {
			retVal[1] = 20;
		}, () -> {
			retVal[2] = 30;
		}, () -> {
			retVal[3] = 40;
		}, () -> {
			retVal[4] = 50;
		});

		asyncTask.waitForMultipleTasks(keys);

		assertArrayEquals(retVal, new int[] { 10, 20, 30, 40, 50 });
	}
	
	/**
	 * Testsubmit task in new thread.
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	@Test
	public void testsubmitTaskInNewThread() throws InterruptedException {
		int[] retVal = new int[1];
		AsyncTask.submitTaskInNewThread(TestUtil.delayedRunnable(() -> {
			retVal[0] = 10;
		}, 10));

		Thread.sleep(50);
		assertEquals(retVal[0], 10);
	}
	
	/**
	 * Test close.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testClose() throws Exception {
		AsyncTask asyncTask = AsyncTask.of(Executors.newFixedThreadPool(2));
		asyncTask.submitTask(() -> System.out.println("Test1"));
		asyncTask.submitTask(() -> System.out.println("Test2"));

		asyncTask.close();
		asyncTask.close();
		assertTrue(true);
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

}
