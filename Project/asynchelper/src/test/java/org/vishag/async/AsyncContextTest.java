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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * The class AsyncTest.
 * 
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
@RunWith(Parameterized.class)
public class AsyncContextTest {

	/** The async task. */
	private AsyncTask asyncTask;
	/** The watcher. */
	@Rule
	public TestRule watcher = new TestWatcherAndLogger();
	
	/** The async context. */
	private AsyncContext asyncContext;
	
	/**
	 * Inputs.
	 *
	 * @return the collection
	 */
	@Parameters
	public static Collection<Object[]> inputs() {
		return Arrays.asList(new Object[][] {
				{AsyncContext.getDefault()},
				{AsyncContext.newInstance()	}
			});
	}
	
	 public AsyncContextTest(AsyncContext asyncContext) {
		 asyncTask = AsyncTask.getDefault();
		 this.asyncContext = asyncContext;
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
		System.out.println(asyncTask.getThreadPool());
		asyncTask.submitTask(TestUtil.delayedRunnable(() -> {
			retVal[0] = 10;
			asyncContext.notifyAllFlag("FLAG2");
		}, 200));

		asyncTask.submitTask(() -> {
			try {
				asyncContext.waitForFlag("FLAG2");
				retVal[1] = retVal[0] + 10;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Task2");

		asyncContext.waitForFlag("FLAG2");
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
			asyncContext.notifyFlag("FLAG1");
		}, 200));
		asyncContext.waitForFlag("FLAG1");
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
		Map<ObjectsKey, ObjectsKey> originalKeys = context.getOriginalKeys();
		originalKeys.put(ObjectsKey.of("111"), ObjectsKey.of("111"));
		context.close();
		context.close();
		assertTrue(originalKeys.isEmpty());
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
		fail();
	}
}
