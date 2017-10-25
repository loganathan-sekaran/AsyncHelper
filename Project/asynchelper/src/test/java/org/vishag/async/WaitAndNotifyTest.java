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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 * The class WaitAndNotifyTest.
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
public class WaitAndNotifyTest {
	
	@Rule
	public TestRule watcher = new TestWatcherAndLogger();
	
	@Test
	public void testWaitAndNotifyAllForFlag() throws InterruptedException {
		int[] retVal = new int[2];
		Submit.task(TestUtil.delayedRunnable(() -> {
			retVal[0] = 10;
			Notify.notifyAllFlag("FLAG2");
		}, 2000));
		
		
		Submit.task(() -> {
			try {
				Wait.waitForFlag("FLAG2");
				retVal[1] = retVal[0] + 10;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Task2");
		
		Wait.waitForFlag("FLAG2");
		assertEquals(retVal[0], 10);
		
		Wait.waitForTask("Task2");
		assertEquals(retVal[1], 20);
	}
	
	@Test
	public void testWaitAndNotifyForFlag() throws InterruptedException {
		int[] retVal = new int[1];
		Submit.task(TestUtil.delayedRunnable(() -> {
			retVal[0] = 10;
			Notify.notifyFlag("FLAG1");
		}, 2000));
		Wait.waitForFlag("FLAG1");
		assertEquals(retVal[0], 10);
	}
}
