package org.vishag.async;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class TestUtil {
	private static final Logger logger = Logger.getLogger(TestUtil.class.getName());

	protected static <T> Supplier<T> delayedSupplier(Supplier<T> supplier, long msecs) {
		return () -> {
			try {
				Thread.sleep(msecs);
			} catch (InterruptedException e) {
			}
			return supplier.get();
		};
	}

	protected static <T> Runnable delayedRunnable(Runnable runnable) {
		return delayedRunnable(runnable, 500);
	}

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

	protected static <T> Supplier<T> delayedSupplier(Supplier<T> supplier) {
		return delayedSupplier(supplier, 500);
	}

	protected static void printTime() {
		System.out.println(new SimpleDateFormat("hh:mm:ss.SSS").format(new Date()));
	}

	protected static void print(String msg) {
		System.out.println(msg);
	}

}
