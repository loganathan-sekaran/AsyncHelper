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

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 * The class AsyncTest.
 * 
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
public class AsyncContextTest {

	/** The async task. */
	private static AsyncTask asyncTask;
	/** The watcher. */
	@Rule
	public TestRule watcher = new TestWatcherAndLogger();
	
	/** The async. */
	private static AsyncContext async;
	
	 /**
 	 * Sets the up before class.
 	 *
 	 * @throws Exception the exception
 	 */
 	@BeforeClass
	 public static void setUpBeforeClass() throws Exception {
		 asyncTask = AsyncTask.getDefault();
		 async = AsyncContext.getDefault();
	 }

	/**
	 * Test wait and notify all for flag.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testWaitAndNotifyAllForFlag() throws InterruptedException {
		int[] retVal = new int[2];
		asyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			retVal[0] = 10;
			async.notifyAllFlag("FLAG2");
		}, 2000));

		asyncTask.submitTask(() -> {
			try {
				async.waitForFlag("FLAG2");
				retVal[1] = retVal[0] + 10;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Task2");

		async.waitForFlag("FLAG2");
		assertEquals(retVal[0], 10);

		asyncTask.waitForTask("Task2");
		assertEquals(retVal[1], 20);
	}

	/**
	 * Test wait and notify for flag.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testWaitAndNotifyForFlag() throws InterruptedException {
		int[] retVal = new int[1];
		asyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			retVal[0] = 10;
			async.notifyFlag("FLAG1");
		}, 2000));
		async.waitForFlag("FLAG1");
		assertEquals(retVal[0], 10);
	}
	
	/**
	 * Test close.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testClose() throws Exception {
		AsyncContext context = AsyncContext.newInstance();
		context.getOriginalKeys();
		context.close();
		context.close();
		assert(true);
	}
	
	/**
	 * Test close with exception.
	 *
	 * @throws Exception the exception
	 */
	@Test (expected=Exception.class)
	public void testCloseWithException() throws Exception {
		AsyncContext context = AsyncContext.newInstance();
		context.getOriginalKeys();
		context.close();
		context.getOriginalKeys();
	}
}
