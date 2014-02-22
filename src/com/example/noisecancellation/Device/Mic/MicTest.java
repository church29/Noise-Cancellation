package com.example.noisecancellation.Device.Mic;

import junit.framework.TestCase;

public class MicTest extends TestCase {
	private Mic testMic;

	@Override
    protected void setUp() throws Exception
	{
        testMic = new Mic();
        super.setUp();

    }   /* setUp() */

    @Override
    protected void tearDown() throws Exception
    {
        testMic = new Mic();
        super.tearDown();

    }   /* tearDown() */

    
    /*-------------------------------------------
     * TESTING Mic::open()
     * 
     * TEST CASES:
     *   1. Calling open() when the device
     *      is closed
     *   2. Calling open() when the device
     *      is already open
     *-----------------------------------------*/
    
    /**
     * Tests whether the program is able to 
     * open a microphone object when the device
     * is closed prior to calling open().
     * <br /><br />
     * Expected return value: NO_ERROR
     */
    public void testOpenMicClosed()
    {
        byte test;
        
        test = testMic.open();
        testMic.close();
        
        assertTrue( "Mic was successfully opened", Mic.NO_ERROR == test );
        
    }   /* testOpenMicClosed() */
    
    /**
     * Tests whether the program is able to 
     * open a microphone object when the 
     * microphone is opened prior to calling
     * open().
     * <br /><br />
     * Expected return value: NO_ERROR
     */
    public void testOpenMicOpen()
    {
        byte test;
        
        testMic.open();
        test = testMic.open();
        testMic.close();
        
        assertTrue( "Mic is still open", Mic.NO_ERROR == test );
        
    }   /* testOpenMicOpen() */
    
    /*-------------------------------------------
     * TESTING Mic::close()
     * 
     * TEST CASES:
     *   1. Calling close() when the device
     *      is opened
     *   2. Calling close() when the device
     *      is already closed
     *-----------------------------------------*/
    
    /**
     * Tests whether the program is able to 
     * close a microphone object when the device
     * is opened prior to calling close().
     * <br /><br />
     * Expected return value: NO_ERROR
     */
    public void testCloseMicOpen()
    {
        byte test;
        
        testMic.open();
        test = testMic.close();
        
        assertTrue( "Microphone closed successfully", Mic.NO_ERROR == test );
        
    }   /* testCloseMicOpen() */
    
    /**
     * Tests whether the program is able to
     * close a microphone object when the device
     * is already closed prior to the call to close().
     * <br /><br />
     * Expected return value: ERROR_NO_DEVICE
     */
    public void testCloseMicClosed()
    {
        byte test;
        
        testMic.open();
        testMic.close();
        test = testMic.close();
        
        assertTrue( "Microphone couldn't be closed", Mic.ERROR_NO_DEVICE == test );
        
    }   /* testCloseMicClosed() */
    
    /*-------------------------------------------
     * TESTING Mic::start()
     * 
     * TEST CASES:
     *   1. Calling start() when the device
     *      is opened
     *   2. Calling start() when the device
     *      has been closed
     *-----------------------------------------*/
    
    /**
     * Tests whether the program is able to 
     * start a microphone object when the device
     * is opened prior to calling start().
     * <br /><br />
     * Expected return value: NO_ERROR
     */
	public void testStartMicOpen()
	{
	    boolean test_recording;
	    byte    test_start;
	    
	    testMic.open();
        test_start     = testMic.start();
        test_recording = testMic.isRecording();
        testMic.stop();
        testMic.close();
        
        assertTrue( "Recorder started successfully", Mic.NO_ERROR == test_start );
        assertTrue( "Recording", test_recording );

    }   /* testStartMicOpen() */
    
	/**
	 * Tests whether the program is able to
	 * start a microphone object when the device
	 * is closed prior to calling start().
	 * <br /><br />
	 * Expected return value: ERROR_NO_DEVICE
	 */
	public void testStartMicClosed()
    {
        boolean test_recording;
        byte    test_start;
        
        testMic.open();
        testMic.close();
        test_start     = testMic.start();
        test_recording = testMic.isRecording();
        
        assertTrue( "Recorder didn't start successfully", Mic.ERROR_NO_DEVICE == test_start );
        assertFalse( "Not recording", test_recording );

    }   /* testStartMicClosed() */

	/*-------------------------------------------
     * TESTING Mic::stop()
     * 
     * TEST CASES:
     *   1. Calling stop() when the device
     *      is open
     *   2. Calling stop() when the device
     *      is already closed
     *-----------------------------------------*/
    
    /**
     * Tests whether the program is able to 
     * stop a microphone object when the device
     * is opened prior to calling stop().
     * <br /><br />
     * Expected return value: NO_ERROR
     */
	public void testStopMicOpen()
    {
        boolean test_recording;
        byte    test_stop;
        
        testMic.open();
        testMic.start();
        test_stop      = testMic.stop();
        test_recording = testMic.isRecording();
        testMic.close();
        
        assertTrue( "Recorder stopped successfully", Mic.NO_ERROR == test_stop );
        assertFalse( "Not recording", test_recording );

    }   /* testStopMicOpen() */
    
	/**
	 * Tests whether the program is able to
	 * stop a microphone object when the device
	 * is already closed prior to calling stop()
	 * <br /><br />
	 * Expected return value: ERROR_NO_DEVICE
	 */
    public void testStopMicClosed()
    {
        boolean test_recording;
        byte    test_stop;
        
        testMic.open();
        testMic.close();
        test_stop      = testMic.stop();
        test_recording = testMic.isRecording();
        
        assertTrue( "Recorder couldn't stop", Mic.ERROR_NO_DEVICE == test_stop );
        assertFalse( "Not recording", test_recording );

    }   /* testStopMicClosed() */

    /*-------------------------------------------
     * TESTING Mic's restart capabilities
     *-----------------------------------------*/
    
    /**
     * Tests whether the program is able to 
     * restart the recording process.
     * <br /><br />
     * Expected return value: NO_ERROR
     */
    public void testRestart()
    {
        byte test;
        
        testMic.open();
        testMic.start();
        testMic.stop();
        test = testMic.start();
        testMic.stop();
        testMic.close();
        
        assertTrue( "Did restart", Mic.NO_ERROR == test );

    }   /* testRestart() */

    /*-------------------------------------------
     * TESTING Mic::getRecordDataMicOpen()
     *                 (A.K.A. "____")
     * TEST CASES:
     *   1. Calling ____ when the device
     *      is open
     *   2. Calling ____ when the device
     *      is already closed
     *-----------------------------------------*/
    
    /**
     * Tests whether the program is able to 
     * get buffer data for a device opened prior
     * to calling _____
     * <br /><br />
     * Expected return value: not ERROR_NO_DEVICE
     */
    public void testGetRecordDataMicOpen()
    {
        int     test;
        byte [] data     = new byte[ testMic.getSuggestedBufferSize() ];
        byte [] not_good = new byte[ testMic.getSuggestedBufferSize() ];
        
        testMic.open();
        testMic.start();
        test = testMic.getRecordData( data );
        testMic.stop();
        testMic.close();
        
        assertFalse( "Recorded data is valid", ( data == not_good ) );
        assertFalse( "Data was actually read", Mic.ERROR_NO_DEVICE == test );

    }   /* testGetRecordDataMicOpen() */
    
    /**
     * Tests whether the program is able to
     * get buffer data for a device closed prior
     * to calling ____
     * <br /><br />
     * Expected return value: ERROR_NO_DEVICE
     */
    public void testGetRecordDataMicClosed()
    {
        int     test;
        byte [] data = new byte[ testMic.getSuggestedBufferSize() ];
        
        testMic.open();
        testMic.close();
        test = testMic.getRecordData( data );
        
        assertTrue( "There was an error reading from microphone", Mic.ERROR_NO_DEVICE == test );
        
    }   /* testGetRecordDataMicClosed() */

};  /* MicTest */

