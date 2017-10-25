package org.vishag.async;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class TestUtil {
	private static final Logger logger = Logger.getLogger(TestUtil.class.getName());

	static <T> Supplier<T> delayedSupplier(Supplier<T> supplier, long msecs) {
		return () -> {
			try {
				Thread.sleep(msecs);
			} catch (InterruptedException e) {
			}
			return supplier.get();
		};
	}

	static <T> Runnable delayedRunnable(Runnable runnable) {
		return delayedRunnable(runnable, 500);
	}

	static <T> Runnable delayedRunnable(Runnable runnable, long msecs) {
		return () -> {
			try {
				Thread.sleep(msecs);
				runnable.run();
			} catch (Exception e) {
				logger.config(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
		};
	}

	static <T> Supplier<T> delayedSupplier(Supplier<T> supplier) {
		return delayedSupplier(supplier, 500);
	}

	static void printTime() {
		System.out.println(new SimpleDateFormat("hh:mm:ss.SSS").format(new Date()));
	}

	static void print(String msg) {
		System.out.println(msg);
	}

}
