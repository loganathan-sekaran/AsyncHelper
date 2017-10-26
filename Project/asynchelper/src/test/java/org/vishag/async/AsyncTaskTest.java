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

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 * The class AsyncTaskTest.
 * 
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
public class AsyncTaskTest {

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
	 * Test multiple async tasks.
	 */
	@Test
	public void testMultipleAsyncTasks() {
		String[] val = new String[4];
		AsyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[0] = "Value1";
		}), "task1");
		AsyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[1] = "Value2";
		}, 1000), "task2");
		AsyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[2] = "Value3";
		}, 700), "task3");
		AsyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[3] = "Value4";
		}, 1000), "task4");

		AsyncTask.waitForTask("task1");
		AsyncTask.waitForTask("task2");
		AsyncTask.waitForTask("task3");
		AsyncTask.waitForTask("task4");

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
		AsyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[0] = "Value1";
		}), "task1");
		AsyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[1] = "Value2";
		}, 1000), "task2");
		AsyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[2] = "Value3";
		}, 700), "task3");
		AsyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[3] = "Value4";
		}, 1000), "task4");

		AsyncTask.waitForTask("task1");
		AsyncTask.waitForTask("task2");
		AsyncTask.waitForTask("task3");
		AsyncTask.waitForTask("task4");

		assertEquals(val[0], "Value1");
		assertEquals(val[1], "Value2");
		assertEquals(val[2], "Value3");
		assertEquals(val[3], "Value4");

		AsyncTask.waitForTask("task1");
		AsyncTask.waitForTask("task2");
		AsyncTask.waitForTask("task3");
		AsyncTask.waitForTask("task4");

		assertEquals(val[0], "Value1");
		assertEquals(val[1], "Value2");
		assertEquals(val[2], "Value3");
		assertEquals(val[3], "Value4");

		AsyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[0] = "Value11";
		}), "task1");
		AsyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[1] = "Value22";
		}, 1000), "task2");
		AsyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[2] = "Value33";
		}, 700), "task3");
		AsyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			val[3] = "Value44";
		}, 1000), "task4");

		AsyncTask.waitForTask("task1");
		AsyncTask.waitForTask("task2");
		AsyncTask.waitForTask("task3");
		AsyncTask.waitForTask("task4");

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
		AsyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			retVal[0] = 10;
		}, 100));

		Thread.sleep(500);
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
		AsyncTask.submitTasks(TestUtil.delayedRunnable(() -> {
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
		AsyncTask.submitTasksAndWait(() -> {
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
		AsyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			cancel.set(true);
		}, 5000));

		AsyncTask.submitTasksAndWaitCancellable(() -> cancel.get(), true, TestUtil.delayedRunnable(() -> {
			retVal[0] = 10;
			TestUtil.printTime();
		}, 2000), TestUtil.delayedRunnable(() -> {
			retVal[1] = 20;
			TestUtil.printTime();
		}, 4000), TestUtil.delayedRunnable(() -> {
			retVal[2] = 30;
			TestUtil.printTime();
		}, 6000));

		assertFalse(Arrays.toString(new int[] { 10, 20, 30 }).equals(Arrays.toString(retVal)));
	}

	/**
	 * Testtasks with keys.
	 */
	@Test
	public void testtasksWithKeys() {
		int[] retVal = new int[5];
		Object[] keys = { "Submitted", "Multiple", "Tasks" };
		AsyncTask.submitTasks(keys, () -> {
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

		AsyncTask.waitForMultipleTasks(keys);

		assertArrayEquals(retVal, new int[] { 10, 20, 30, 40, 50 });
	}

}
