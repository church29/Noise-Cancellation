package com.example.equalizer.Mic;

import junit.framework.TestCase;

public class MicTest extends TestCase {
	private Mic testMic;

	@Override
	protected void setUp() throws Exception {
		testMic = new Mic();
		super.setUp();
		
		
	}

	@Override
	protected void tearDown() throws Exception {
		testMic = new Mic();
		super.tearDown();
		

	}

	public void testStart() {
		testMic.start();
		boolean test = testMic.isRecording;
		testMic.stop();
		assertTrue("Not Recording", test);
		
	}

	public void testStop() {
		testMic.start();
		testMic.stop();
		assertFalse("Did not stop", testMic.isRecording);
	}
	public void testRestart() {
		testMic.start();
		testMic.stop();
		testMic.start();
		boolean test = testMic.isRecording;
		testMic.stop();
		assertTrue("Did not restart", test);
		
	}

	public void testGetRecordData() {
		testMic.start();
		byte[] test = new byte[testMic.bufferSize];
		byte[] data = testMic.recordData;
		testMic.stop();
		assertFalse("Is not recording data", (data == test));
		
	}

}
