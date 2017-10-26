package org.vishag.async;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * The Class TestUtil.
 */
public class TestUtil {

	/** The Constant logger. */
	private static final Logger logger = Logger.getLogger(TestUtil.class.getName());

	/**
	 * Delayed supplier.
	 *
	 * @param <T>
	 *            the generic type
	 * @param supplier
	 *            the supplier
	 * @param msecs
	 *            the msecs
	 * @return the supplier
	 */
	protected static <T> Supplier<T> delayedSupplier(Supplier<T> supplier, long msecs) {
		return () -> {
			try {
				Thread.sleep(msecs);
			} catch (InterruptedException e) {
			}
			return supplier.get();
		};
	}

	/**
	 * Delayed runnable.
	 *
	 * @param <T>
	 *            the generic type
	 * @param runnable
	 *            the runnable
	 * @return the runnable
	 */
	protected static <T> Runnable delayedRunnable(Runnable runnable) {
		return delayedRunnable(runnable, 500);
	}

	/**
	 * Delayed runnable.
	 *
	 * @param <T>
	 *            the generic type
	 * @param runnable
	 *            the runnable
	 * @param msecs
	 *            the msecs
	 * @return the runnable
	 */
	protected static <T> Runnable delayedRunnable(Runnable runnable, long msecs) {
		return () -> {
			try {
				Thread.sleep(msecs);
				runnable.run();
			} catch (Exception e) {
				logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
		};
	}

	/**
	 * Delayed supplier.
	 *
	 * @param <T>
	 *            the generic type
	 * @param supplier
	 *            the supplier
	 * @return the supplier
	 */
	protected static <T> Supplier<T> delayedSupplier(Supplier<T> supplier) {
		return delayedSupplier(supplier, 500);
	}

	/**
	 * Prints the time.
	 */
	protected static void printTime() {
		System.out.println(new SimpleDateFormat("hh:mm:ss.SSS").format(new Date()));
	}

	/**
	 * Prints the.
	 *
	 * @param msg
	 *            the msg
	 */
	protected static void print(String msg) {
		System.out.println(msg);
	}

}
