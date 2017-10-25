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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 * The class ScheduleTest.
 * @author Loganathan.S &lt;https://github.com/loganathan001&gt;
 */
public class ScheduleTest {
	
	@Rule
	public TestRule watcher = new TestWatcherAndLogger();
	
	@Test
	public void testScheduleMultipleSupplierForSingleAccess() throws InterruptedException {
		AtomicInteger count = new AtomicInteger(0);
		boolean suppliers = Schedule.scheduleSupplierForSingleAccess(10, 100, TimeUnit.MILLISECONDS, true,
					() -> {
						int index = count.getAndIncrement();
						return (index + 1) * 10;
					}, 3, 
				"Scheduled", "Multiple","Suppliers","key"
		);
		
		assertTrue(suppliers);
		
		List<Integer> retVals = Wait.waitAndGetFromMultipleSuppliers(Integer.class, "Scheduled", "Multiple","Suppliers","key").collect(Collectors.toList());
		assertEquals(retVals.size(), 3);
		AtomicInteger val = new AtomicInteger(0);
		retVals.forEach(value -> assertEquals(val.addAndGet(10), (int)value));
		
	}

	@Test
	public void testScheduleSingleSupplierUntilFlag()  throws InterruptedException {
		int[] retVal = new int[1];
		Schedule.scheduleSupplierUntilFlag(10, 100, TimeUnit.MILLISECONDS, true,
				"TestSingleSuppliersUntilFlag",
		() -> {
			return retVal[0]++;
		});
		
		Thread.sleep(1000);
		
		List<Integer> result = Notify.notifyAndGetForFlag(Integer.class, "TestSingleSuppliersUntilFlag").collect(Collectors.toList());
		assertTrue(result.size() > 0);
		for (int i = 0; i < result.size(); i++) {
			assertTrue(i == result.get(i));
		}
		TestUtil.print("" + result);
		
	}

	@Test
	public void testScheduleSupplier()  throws InterruptedException {
		AtomicInteger count = new AtomicInteger(0);
		Supplier<Integer>[] scheduleSuppliers = Schedule.scheduleSupplier(10, 100, TimeUnit.MILLISECONDS, true,
		() -> {
			TestUtil.printTime();
			int index = count.getAndIncrement();
			return (index + 1) * 10;  
		}, 3);
		
		assertEquals(scheduleSuppliers.length, 3);
		AtomicInteger val = new AtomicInteger(0);
		Stream.of(scheduleSuppliers).map(Supplier::get).forEach(value -> assertEquals(val.addAndGet(10), (int)value));
	}

	@Test
	public void testScheduleSupplierAndWait()  throws InterruptedException {
		AtomicInteger count = new AtomicInteger(0);
		List<Integer> retVals = Schedule.scheduleSupplierAndWait(0, 100, TimeUnit.MILLISECONDS, true,
		() -> {
			TestUtil.printTime();
			int index = count.getAndIncrement();
			return (index + 1) * 10;  
		}, 3).collect(Collectors.toList());
		assertEquals(retVals.size(), 3);
		AtomicInteger val = new AtomicInteger(0);
		retVals.forEach(value -> assertEquals(val.addAndGet(10), (int)value));
	}

	@Test
	public void testScheduleSupplierAndWaitSingleTime()  throws InterruptedException {
		Optional<Integer> retVal = Schedule.scheduleSupplierAndWait(0, TimeUnit.SECONDS,
		() -> {
			return 10;
		});
		assertEquals((int)retVal.get(), 10);
	}

	@Test
	public void testScheduleSupplierForSingleAccessSingleTime() throws InterruptedException {
		boolean suppliers = Schedule.scheduleSupplierForSingleAccess(10, TimeUnit.MILLISECONDS,
					() -> {
						return 10;
					}, 
				"Scheduled", "Single","Supplier","key"
		);
		
		assertTrue(suppliers);
		
		Optional<Integer> result = Wait.waitAndGet(Integer.class, "Scheduled", "Single","Supplier","key");
		assertTrue(result.isPresent());
		assertEquals((int)result.get(), 10);
		
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testScheduleSuppliers()  throws InterruptedException {
		Supplier<Integer>[] scheduleSuppliers = Schedule.scheduleSuppliers(10, 100, TimeUnit.MILLISECONDS, true,
		() -> {
			return 10;
		},
		() -> {
			return 20;
		},
		() -> {
			return 30;
		},
		() -> {
			return 40;
		},
		() -> {
			return 50;
		});
		
		assertEquals(scheduleSuppliers.length, 5);
		AtomicInteger val = new AtomicInteger(0);
		Stream.of(scheduleSuppliers).map(Supplier::get).forEach(value -> assertEquals(val.addAndGet(10), (int)value));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testScheduleSuppliersAndWait()  throws InterruptedException {
		List<Integer> retVals = Schedule.scheduleSuppliersAndWait(0, 3, TimeUnit.SECONDS, true,
				() -> {
					return 10;
				},
				() -> {
					return 20;
				},
				() -> {
					return 30;
				},
				() -> {
					return 40;
				},
				() -> {
					return 50;
				}).collect(Collectors.toList());
		
		assertEquals(retVals.size(), 5);
		AtomicInteger val = new AtomicInteger(0);
		retVals.forEach(value -> assertEquals(val.addAndGet(10), (int)value));
	}

	@Test
	public void testScheduleSuppliersForSingleAccess() throws InterruptedException {
		@SuppressWarnings("unchecked")
		boolean suppliers = Schedule.scheduleSuppliersForSingleAccess(10, 100, TimeUnit.MILLISECONDS, true,
				new Supplier[] {
					() -> {
						return 10;
					},
					() -> {
						return 20;
					},
					() -> {
						return 30;
					},
					() -> {
						return 40;
					},
					() -> {
						return 50;
					}
				}, 
				"Scheduled", "Multiple","Suppliers","key"
		);
		
		assertTrue(suppliers);
		
		List<Integer> retVals = Wait.waitAndGetFromMultipleSuppliers(Integer.class, "Scheduled", "Multiple","Suppliers","key").collect(Collectors.toList());
		assertEquals(retVals.size(), 5);
		AtomicInteger val = new AtomicInteger(0);
		retVals.forEach(value -> assertEquals(val.addAndGet(10), (int)value));
		
	}

	@Test
	public void testScheduleSupplierSingleTime()  throws InterruptedException {
		Supplier<Integer> scheduleSupplier = Schedule.scheduleSupplier(10, TimeUnit.MILLISECONDS,
		() -> {
			return 10;
		});
		
		assertEquals((int)scheduleSupplier.get(), 10);
	}

	@Test
	public void testScheduleSuppliersUntilFlag()  throws InterruptedException {
		Schedule.scheduleSuppliersUntilFlag(10, 100, TimeUnit.MILLISECONDS, true,
				"TestSuppliersUntilFlag",
		() -> {
			return 0;
		},
		() -> {
			return 1;
		},
		() -> {
			return 2;
		},
		() -> {
			return 3;
		},
		() -> {
			return 4;
		});
		
		Thread.sleep(1000);
		
		List<Integer> result = Notify.notifyAndGetForFlag(Integer.class, "TestSuppliersUntilFlag").collect(Collectors.toList());
		assertTrue(result.size() > 0);
		int val = 0;
		for (int i = 0; i < result.size(); i++) {
			assertTrue(val == result.get(i));
			if(val < 5 - 1) {
				val++;
			} else {
				val = 0;
			}
		}
		TestUtil.print("" + result);
		
	}

	@Test
	public void testScheduleTask()  throws InterruptedException {
		int[] retVal = new int[3];
		AtomicInteger count = new AtomicInteger(0);
		Schedule.scheduleTask(10, 100, TimeUnit.MILLISECONDS, true,
		() -> {
			int index = count.getAndIncrement();
			retVal[index] = (index + 1) * 10  ;
		}, 3);
		
		Thread.sleep(500);
		assertArrayEquals(retVal, new int[]{10, 20, 30});
	}

	@Test
	public void testScheduleTasks()  throws InterruptedException {
		int[] retVal = new int[5];
		Schedule.scheduleTasks(10, 100, TimeUnit.MILLISECONDS, true,
		() -> {
			retVal[0] = 10;
		},
		() -> {
			retVal[1] = 20;
		},
		() -> {
			retVal[2] = 30;
		},
		() -> {
			retVal[3] = 40;
		},
		() -> {
			retVal[4] = 50;
		});
		
		Thread.sleep(1000);
		assertArrayEquals(retVal, new int[]{10, 20, 30, 40, 50});
	}

	@Test
	public void testScheduleTaskSingleTime()  throws InterruptedException {
		int[] retVal = new int[]{0,20,20};
		AtomicInteger count = new AtomicInteger(0);
		Schedule.scheduleTask(10, TimeUnit.MILLISECONDS,
		() -> {
			int index = count.getAndIncrement();
			retVal[index] = (index + 1) * 10;		});
		
		Thread.sleep(500);
		assertArrayEquals(retVal, new int[]{10, 20, 20});
	}

	@Test
	public void testScheduleTasksUntilFlag()  throws InterruptedException {
		int[] retVal = new int[5];
		Schedule.scheduleTasksUntilFlag(10, 100, TimeUnit.MILLISECONDS, true, "ScheduledMultipleTasksTest",
		() -> {
			TestUtil.print("Task 0");
			TestUtil.printTime();
			retVal[0]+= 1;
		},
		() -> {
			TestUtil.print("Task 1");
			TestUtil.printTime();
			retVal[1]+= 1;
		},
		() -> {
			TestUtil.print("Task 2");
			TestUtil.printTime();
			retVal[2]+= 1;
		},
		() -> {
			TestUtil.print("Task 3");
			TestUtil.printTime();
			retVal[3]+= 1;
		},
		() -> {
			TestUtil.print("Task 4");
			TestUtil.printTime();
			retVal[4]+= 1;
		});
		
		Thread.sleep(1200);
		Notify.notifyFlag("ScheduledMultipleTasksTest");
		assertTrue(retVal[0] > 1);
		assertTrue(retVal[1] > 1);
		assertTrue(retVal[2] > 1);
		assertTrue(retVal[3] > 1);
		assertTrue(retVal[4] > 1);
	}

	@Test
	public void testScheduleTasksWait()  throws InterruptedException {
		int[] retVal = new int[5];
		Schedule.scheduleTasksAndWait(0, 100, TimeUnit.MILLISECONDS, true,
		() -> {
			TestUtil.printTime();
			retVal[0] = 10;
		},
		() -> {
			TestUtil.printTime();
			retVal[1] = 20;
		},
		() -> {
			TestUtil.printTime();
			retVal[2] = 30;
		},
		() -> {
			TestUtil.printTime();
			retVal[3] = 40;
		},
		() -> {
			TestUtil.printTime();
			retVal[4] = 50;
		});
		
		TestUtil.printTime();
		assertArrayEquals(retVal, new int[]{10, 20, 30, 40, 50});
	}

	@Test
	public void testScheduleTaskUntilFlag()  throws InterruptedException {
		int[] retVal = new int[5];
		Schedule.scheduleTaskUntilFlag(10, 100, TimeUnit.MILLISECONDS, true, "ScheduledSingleTasksTest",
		() -> {
			TestUtil.print("Count " + retVal[0]);
			TestUtil.printTime();
			retVal[0]+= 1;
		});
		
		Thread.sleep(1200);
		Notify.notifyFlag("ScheduledSingleTasksTest");
		assertTrue(retVal[0] > 5);
	}

	@Test
	public void testScheduleTaskWait()  throws InterruptedException {
		int[] retVal = new int[3];
		AtomicInteger count = new AtomicInteger(0);
		Schedule.scheduleTaskAndWait(0, 3, TimeUnit.SECONDS, true,
		() -> {
			TestUtil.printTime();
			int index = count.getAndIncrement();
			retVal[index] = (index + 1) * 10;
		}, 3);
		
		TestUtil.printTime();
		assertArrayEquals(retVal, new int[]{10, 20, 30});
	}

	@Test
	public void testScheduleTaskWaitSingleTime()  throws InterruptedException {
		int[] retVal = new int[]{0,20,20};
		AtomicInteger count = new AtomicInteger(0);
		Schedule.scheduleTaskAndWait(0, TimeUnit.SECONDS,
		() -> {
			TestUtil.printTime();
			retVal[count.getAndIncrement()] = 10;
		});
		
		TestUtil.printTime();
		assertArrayEquals(retVal, new int[]{10, 20, 20});
	}

}
