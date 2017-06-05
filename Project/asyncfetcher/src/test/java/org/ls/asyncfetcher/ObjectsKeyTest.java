package org.ls.asyncfetcher;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ObjectsKeyTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test (expected = AssertionError.class)
	public void testEmptyArray() {
		ObjectsKey.of(new Object[0]);
	}
	
	@Test (expected = AssertionError.class)
	public void testNullKey() {
		ObjectsKey.of(new Object[]{null});
	}
	
	@Test
	public void testEqualKey() {
		assertEquals(ObjectsKey.of("111"), ObjectsKey.of("111"));
		assertEquals(ObjectsKey.of("111", Integer.class), ObjectsKey.of("111", Integer.class));
		assertEquals(ObjectsKey.of("111", String.class), ObjectsKey.of(String.class, "111"));
	}
	
	@Test
	public void testUnEqualKey() {
		assertNotEquals(ObjectsKey.of("111"), ObjectsKey.of("222"));
		assertNotEquals(ObjectsKey.of("111", Integer.class), ObjectsKey.of("111"));
		assertNotEquals(ObjectsKey.of("111"), ObjectsKey.of("111", String.class));
	}

}
