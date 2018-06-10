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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * The Executor class that comprises of an executor service and methods used to
 * submit tasks and suppliers.
 * 
 *  @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
class Executor implements AutoCloseable {

	/**
	 * The Constant DEFAULT_POOL. <br>
	 * Note: By default {@link ForkJoinPool#commonPool()} is used, but if the
	 * {@link ForkJoinPool#getCommonPoolParallelism()} is {@code 1} using a
	 * {@link ForkJoinPool} of parallelism {@code 5}, because using
	 * {@code ForkJoinPool} with parallelism 1 will not make this multi-threading
	 * utility class unusable, and many of the test cases will fail.
	 */
	private static final ForkJoinPool DEFAULT_POOL = ForkJoinPool.getCommonPoolParallelism() == 1
			? ForkJoinPool.commonPool()
			: new ForkJoinPool(5);

	/** The default instance of Executor. */
	private static Executor DEFAULT_INSTANCE = new Executor(DEFAULT_POOL);

	/** The thread pool. */
	private ExecutorService threadPool;

	/** The closed flag. */
	private volatile boolean closed;

	/**
	 * Instantiates a new Executor.
	 *
	 * @param threadPool
	 *            the thread pool
	 */
	private Executor(ExecutorService threadPool) {
		this.threadPool = threadPool;
	}

	/**
	 * Gets an Executor instance made of the given executor service.
	 *
	 * @param executorService
	 *            the executor service
	 * @return the executor
	 */
	static Executor ofExecutorService(ExecutorService executorService) {
		return new Executor(executorService);
	}

	/**
	 * Gets the default instance of Executor.
	 *
	 * @return the default
	 */
	static Executor getDefault() {
		return DEFAULT_INSTANCE;
	}

	/**
	 * Gets the fork join pool.
	 *
	 * @return the fork join pool
	 */
	protected ExecutorService getThreadPool() {
		assertNotClosed();
		return threadPool;
	}

	/**
	 * Assert not closed.
	 */
	private void assertNotClosed() {
		if (closed) {
			throw new RuntimeException(new IllegalStateException("Already closed"));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public synchronized void close() {
		if (!closed) {
			threadPool.shutdown();
			closed = true;
		}
	}

}
