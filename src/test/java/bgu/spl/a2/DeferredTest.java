package test.java.bgu.spl.a2;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import bgu.spl.a2.Deferred;

public class DeferredTest {

	private Deferred<Integer> tester;
	private int temp = 0;
	
	public DeferredTest(){}
	@Before
	public void setUp() throws Exception {
		tester = new Deferred<Integer>();
	}

	@Test
	public void testGet() {
		tester.resolve(10);
		if (tester.get() != 10)
			fail("wrong result");
	}

	@Test
	public void testIsResolved() {
		tester.resolve(null);
		assertEquals(true,tester.isResolved());
	}

	@Test
	public void testResolve() {
		tester.resolve(10);
		assertEquals(new Integer(10),tester.get());
	}

	@Test
	public void testWhenResolved() {
		Runnable callback = new Runnable() {
			public void run() {
				temp++;
			}
		};
		tester.whenResolved(callback);
		tester.resolve(10);
		assertEquals(1,temp);
	}

}
