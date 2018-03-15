package test.java.bgu.spl.a2;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import bgu.spl.a2.VersionMonitor;

public class VersionMonitorTest {
	
	private VersionMonitor tester;
	
	public VersionMonitorTest(){}

	@Before
	public void setUp() throws Exception {
		tester = new VersionMonitor();
	}

	@Test
	public void testGetVersion() {
		assertEquals(0,tester.getVersion());
	}

	@Test
	public void testInc() {
		tester.inc();
		assertEquals(1,tester.getVersion());
	}

	@Test
	public void testAwait() {
            Thread toRun=new Thread(() -> {
                while(true) {
                    tester.inc();
                }
            });
            toRun.start();
            int init = tester.getVersion();
            try {
                tester.await(init);
            } catch (java.lang.InterruptedException e ) {
                //who care
            }
            assertNotSame(init,tester.getVersion());
            toRun.interrupt();

	}

}
