package org.vishag.asynchelper;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

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