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
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 * The class SchedulingTaskTest.
 * 
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
public class SchedulingTaskTest {

	/** The watcher. */
	@Rule
	public TestRule watcher = new TestWatcherAndLogger();

	/**
	 * Test schedule task.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testScheduleTask() throws InterruptedException {
		int[] retVal = new int[3];
		AtomicInteger count = new AtomicInteger(0);
		SchedulingTask.scheduleTask(10, 100, TimeUnit.MILLISECONDS, true, () -> {
			int index = count.getAndIncrement();
			retVal[index] = (index + 1) * 10;
		}, 3);

		Thread.sleep(500);
		assertArrayEquals(retVal, new int[] { 10, 20, 30 });
	}

	/**
	 * Test schedule tasks.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testScheduleTasks() throws InterruptedException {
		int[] retVal = new int[5];
		SchedulingTask.scheduleTasks(10, 100, TimeUnit.MILLISECONDS, true, () -> {
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

		Thread.sleep(1000);
		assertArrayEquals(retVal, new int[] { 10, 20, 30, 40, 50 });
	}

	/**
	 * Test schedule task single time.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testScheduleTaskSingleTime() throws InterruptedException {
		int[] retVal = new int[] { 0, 20, 20 };
		AtomicInteger count = new AtomicInteger(0);
		SchedulingTask.scheduleTask(10, TimeUnit.MILLISECONDS, () -> {
			int index = count.getAndIncrement();
			retVal[index] = (index + 1) * 10;
		});

		Thread.sleep(500);
		assertArrayEquals(retVal, new int[] { 10, 20, 20 });
	}

	/**
	 * Test schedule tasks until flag.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testScheduleTasksUntilFlag() throws InterruptedException {
		int[] retVal = new int[5];
		SchedulingTask.scheduleTasksUntilFlag(10, 100, TimeUnit.MILLISECONDS, true, "ScheduledMultipleTasksTest",
				() -> {
					TestUtil.print("Task 0");
					TestUtil.printTime();
					retVal[0] += 1;
				}, () -> {
					TestUtil.print("Task 1");
					TestUtil.printTime();
					retVal[1] += 1;
				}, () -> {
					TestUtil.print("Task 2");
					TestUtil.printTime();
					retVal[2] += 1;
				}, () -> {
					TestUtil.print("Task 3");
					TestUtil.printTime();
					retVal[3] += 1;
				}, () -> {
					TestUtil.print("Task 4");
					TestUtil.printTime();
					retVal[4] += 1;
				});

		Thread.sleep(1200);
		Async.notifyFlag("ScheduledMultipleTasksTest");
		assertTrue(retVal[0] > 1);
		assertTrue(retVal[1] > 1);
		assertTrue(retVal[2] > 1);
		assertTrue(retVal[3] > 1);
		assertTrue(retVal[4] > 1);
	}

	/**
	 * Test schedule tasks wait.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testScheduleTasksWait() throws InterruptedException {
		int[] retVal = new int[5];
		SchedulingTask.scheduleTasksAndWait(0, 100, TimeUnit.MILLISECONDS, true, () -> {
			TestUtil.printTime();
			retVal[0] = 10;
		}, () -> {
			TestUtil.printTime();
			retVal[1] = 20;
		}, () -> {
			TestUtil.printTime();
			retVal[2] = 30;
		}, () -> {
			TestUtil.printTime();
			retVal[3] = 40;
		}, () -> {
			TestUtil.printTime();
			retVal[4] = 50;
		});

		TestUtil.printTime();
		assertArrayEquals(retVal, new int[] { 10, 20, 30, 40, 50 });
	}

	/**
	 * Test schedule task until flag.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testScheduleTaskUntilFlag() throws InterruptedException {
		int[] retVal = new int[5];
		SchedulingTask.scheduleTaskUntilFlag(10, 100, TimeUnit.MILLISECONDS, true, "ScheduledSingleTasksTest", () -> {
			TestUtil.print("Count " + retVal[0]);
			TestUtil.printTime();
			retVal[0] += 1;
		});

		Thread.sleep(1200);
		Async.notifyFlag("ScheduledSingleTasksTest");
		assertTrue(retVal[0] > 5);
	}

	/**
	 * Test schedule task wait.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testScheduleTaskWait() throws InterruptedException {
		int[] retVal = new int[3];
		AtomicInteger count = new AtomicInteger(0);
		SchedulingTask.scheduleTaskAndWait(0, 3, TimeUnit.SECONDS, true, () -> {
			TestUtil.printTime();
			int index = count.getAndIncrement();
			retVal[index] = (index + 1) * 10;
		}, 3);

		TestUtil.printTime();
		assertArrayEquals(retVal, new int[] { 10, 20, 30 });
	}

	/**
	 * Test schedule task wait single time.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testScheduleTaskWaitSingleTime() throws InterruptedException {
		int[] retVal = new int[] { 0, 20, 20 };
		AtomicInteger count = new AtomicInteger(0);
		SchedulingTask.scheduleTaskAndWait(0, TimeUnit.SECONDS, () -> {
			TestUtil.printTime();
			retVal[count.getAndIncrement()] = 10;
		});

		TestUtil.printTime();
		assertArrayEquals(retVal, new int[] { 10, 20, 20 });
	}

}
