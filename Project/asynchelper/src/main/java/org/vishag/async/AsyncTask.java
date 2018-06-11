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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The AsyncSupplier Helper class with methods for submitting {@link Runnable}s
 * to invoke them as asynchronous tasks and optionally wait for them in same of
 * another thread. <br>
 * <br>
 * Note: The default thread pool used in the default instance
 * ({@link AsyncTask#getDefault()}) is ForkJoinPool. A I/O intensive or blocking
 * task should prevent using the default instance, instead pass its own thread
 * pool executor (using {@link AsyncTask#of(ExecutorService)}) or
 * {@link AsyncTask#of(ExecutorService, AsyncContext)} or use
 * {@link AsyncTask#submitTaskInNewThread(Runnable)} if applicable.
 * 
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
public final class AsyncTask implements AutoCloseable{

	/**
	 * {@code Logger} for this class.
	 */
	private static final Logger logger = Logger.getLogger(AsyncTask.class.getName());
	
	/** The executor. */
	private Executor executor;
	
	/** The closed. */
	private volatile boolean closed;

	/** The async context. */
	private AsyncContext asyncContext;
	
	/** The default instance. */
	private static AsyncTask DEFAULT_INSTANCE = new AsyncTask(Executor.getDefault(), AsyncContext.getDefault()); 

	/**
	 * Prevent instantiation outside the class.
	 *
	 * @param executor the executor
	 * @param asyncContext the async context
	 */
	private AsyncTask(Executor executor, AsyncContext asyncContext) {
		this.executor = executor;
		this.asyncContext = asyncContext;
	}
	
	/**
	 * Gets the default instance of AsyncTask.
	 *
	 * @return the default
	 */
	public static AsyncTask getDefault() {
		return DEFAULT_INSTANCE;
	}
	
	/**
	 * Gets a new AsyncTask instance made of the given executor service.
	 *
	 * @param executorService the executor service
	 * @return the async task
	 */
	public static AsyncTask of(ExecutorService executorService) {
		return of(executorService, AsyncContext.getDefault());
	}
	
	/**
	 * Gets a new AsyncTask instance made of the given executor service and async context.
	 *
	 * @param executorService the executor service
	 * @param asyncContext the async context
	 * @return the async task
	 */
	public static AsyncTask of(ExecutorService executorService, AsyncContext asyncContext) {
		return new AsyncTask(Executor.ofExecutorService(executorService), asyncContext);
	}

	/**
	 * Submits a task (Runnable) to be invoke asynchronously.
	 *
	 * @param runnable
	 *            the runnable
	 */
	public void submitTask(Runnable runnable) {
		getThreadPool().execute(runnable);
	}
	
	
	/**
	 * Submit task in new thread.
	 *
	 * @param runnable the runnable
	 */
	public static void submitTaskInNewThread(Runnable runnable) {
		new Thread(runnable).start();
	}

	/**
	 * Gets the thread pool.
	 *
	 * @return the thread pool
	 */
	protected ExecutorService getThreadPool() {
		assertNotClosed();
		return executor.getThreadPool();
	}
	
	/**
	 * Submits multiple tasks (Runnable) to be invoke asynchronously.
	 *
	 * @param runnables
	 *            the runnables
	 */
	public void submitTasks(Runnable... runnables) {
		Stream.of(runnables).forEach(getThreadPool()::execute);
	}

	/**
	 * Submits multiple tasks (Runnable) to be invoke asynchronously and wait
	 * until the tasks are finished. But this can be cancelled in the middle by
	 * providing a Supplier that returns false at any time.
	 *
	 * @param cancelConditionSupplier
	 *            the cancel condition supplier
	 * @param cancelCanInterruptRunning
	 *            the cancel can interrupt running
	 * @param runnables
	 *            the runnables
	 */
	public void submitTasksAndWaitCancellable(Supplier<Boolean> cancelConditionSupplier,
			boolean cancelCanInterruptRunning, Runnable... runnables) {
		ExecutorService threadPool = getThreadPool();
		List<Future<?>> futures = Stream.of(runnables).map(threadPool::submit).collect(Collectors.toList());
		AtomicBoolean allTasksCompleted = new AtomicBoolean(false);
		this.submitTask(() -> {
			for (Future<?> future : futures) {
				try {
					future.get();
				} catch (Exception e) {
					logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			}
			allTasksCompleted.set(true);
		}, futures, "getting");

		this.submitTask(() -> {
			while (!allTasksCompleted.get()) {
				if (!cancelConditionSupplier.get()) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
					}
				} else {
					futures.parallelStream().forEach(future -> {
						future.cancel(cancelCanInterruptRunning);
					});
					break;
				}
			}
		}, futures, "cancelling");

		this.waitForTask(futures, "getting");
		this.waitForTask(futures, "cancelling");
	}

	/**
	 * Submits multiple tasks (Runnable) to be invoke asynchronously and wait
	 * until the tasks are finished.
	 *
	 * @param runnables
	 *            the runnables
	 */
	public void submitTasksAndWait(Runnable... runnables) {
		List<Callable<Object>> tasks = Stream.of(runnables).map(runnable -> (Callable<Object>) () -> {
			runnable.run();
			return (Object) null;
		}).collect(Collectors.toList());

		try {
			getThreadPool().invokeAll(tasks).parallelStream().forEach(future -> {
				try {
					future.get();
				} catch (InterruptedException | ExecutionException e) {
					logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			});
		} catch (InterruptedException e) {
			logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	/**
	 * Submits multiple tasks (Runnable) to be invoke asynchronously which is
	 * marked by keys. The keys can be then used with
	 * {@link AsyncTask#waitForMultipleTasks(Object...)} which can be invoked in
	 * any thread so that that thread will wait until the tasks are finished.
	 *
	 * @param keys
	 *            the keys
	 * @param runnables
	 *            the runnables
	 */
	public void submitTasks(Object[] keys, Runnable... runnables) {
		for (int i = 0; i < runnables.length; i++) {
			Runnable runnable = runnables[i];
			Object[] indexedKey = AsyncContext.getIndexedKey(i, keys);
			submitTask(runnable, indexedKey);
		}
	}

	/**
	 * Submits a task (Runnable) to be invoke asynchronously which is marked by
	 * keys. The keys can be then used with
	 * {@link AsyncTask#waitForMultipleTasks(Object...)} which can be invoked in
	 * any thread so that that thread will wait until the task is finished.
	 *
	 * @param runnable
	 *            the runnable
	 * @param keys
	 *            the keys
	 * @return true, if successful
	 */
	public boolean submitTask(Runnable runnable, Object... keys) {
		ObjectsKey key = ObjectsKey.of(keys);
		AsyncContext context = getAsyncContect();
		if (!context.getFutureSuppliers().containsKey(key)) {
			Supplier<Void> safeSupplier = AsyncContext.safeSupplier(getThreadPool().submit(() -> {
				runnable.run();
				return null;
			}));
			return context.storeSupplier(key, safeSupplier, false);
		}
		return false;
	}

	/**
	 * Wait for multiple tasks submitted by
	 * {@link AsyncTask#submitTasks(Object[], Runnable...)}. The keys should be
	 * the same keys provided during the tasks submission. This can be invoked
	 * in any thread so that that thread will wait until the tasks are finished.
	 *
	 * @param keys
	 *            the keys
	 */
	public void waitForMultipleTasks(Object... keys) {
		getAsyncContect().waitForMultipleTasks(keys);
	}

	/**
	 * Gets the async contect.
	 *
	 * @return the async contect
	 */
	private AsyncContext getAsyncContect() {
		return asyncContext;
	}

	/**
	 * Wait for a task submitted by
	 * {@link AsyncTask#submitTask(Runnable, Object...)}. The keys should be the
	 * same keys provided during the task submission. This can be invoked in any
	 * thread so that that thread will wait until the task is finished.
	 *
	 * @param keys
	 *            the keys
	 */
	public void waitForTask(Object... keys) {
		getAsyncContect().waitForTask(keys);
	}

	/* (non-Javadoc)
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public synchronized void close() {
		if(!closed) {
			executor.close();
			closed = true;
		}
	}
	
	/**
	 * Assert not closed.
	 */
	private void assertNotClosed() {
		if (closed) {
			throw new IllegalStateException("Already closed");
		}
	}

}
