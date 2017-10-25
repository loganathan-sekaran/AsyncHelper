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

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * The class TestWatcherAndLogger.
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
final class TestWatcherAndLogger extends TestWatcher {
		protected void starting(Description description) {
		      System.out.println("====Starting test: " + description.getMethodName());
		   }

		protected void succeeded(Description description) {
			   System.out.println("[Succeeded] test: " + description.getMethodName());
		   }

		protected void failed(Throwable e, Description description) {
			   System.err.println("[Failed] test: " + description.getMethodName());
			   e.printStackTrace();
		   }

		protected void finished(Description description) {
			   System.out.println("----Finished test: " + description.getMethodName());
		   }
	}