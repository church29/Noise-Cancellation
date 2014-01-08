package com.example.noisecancellation.Device.OutputDevice;

import junit.framework.TestCase;

public class OutputDeviceTest extends TestCase
{
    private OutputDevice test_device;
    
    @Override
    protected void setUp() throws Exception
    {
        test_device = new OutputDevice();
        super.setUp();
        
    }   /* setUp() */
    
    @Override
    protected void tearDown() throws Exception
    {
        test_device = new OutputDevice();
        super.tearDown();
        
    }   /* tearDown() */
    
    /*-------------------------------------------
     * TESTING OutputDevice::open()
     * 
     * TEST CASES:
     *   1. Calling open() when the device
     *      has not been created 
     *   2. Calling open() when the device
     *      has already been created
     *-----------------------------------------*/
    
    /**
     * Tests OutputDevice.open() when the
     * device is closed prior to the call.
     * <br /><br />
     * Expected return value: True
     */
    public void testOpenOnClosedDevice()
    {
        boolean test;
        
        test = test_device.open();
        test_device.close();
        
        assertTrue( "Device was opened", test );
        
    }   /* testOpenOnClosedDevice() */
    
    /**
     * Tests OutputDevice.open() when the
     * device is opened prior to the call.
     * <br /><br />
     * Expected return value: False
     */
    public void testOpenOnOpenDevice()
    {
        boolean test;
        
        test_device.open();
        test = test_device.open();
        test_device.close();
        
        assertFalse( "Device wasn't created a second time", test );
    
    }   /* testOpenOnOpenDevice() */
    
    /*-------------------------------------------
     * TESTING OutputDevice::close()
     * 
     * TEST CASES:
     *   1. Calling close() when the device
     *      has been created and is currently
     *      playing audio data
     *   2. Calling close() when the device
     *      has already been closed (i.e. hasn't
     *      been created)
     *-----------------------------------------*/
    
    /**
     * Tests OutputDevice.close() when the
     * device is opened prior to the call.
     * <br /><br />
     * Expected return value: True
     */
    public void testCloseOnOpenDevice()
    {
        boolean test;
        
        test_device.open();
        test = test_device.close();
        
        assertTrue( "Device was successfully closed", test );
    
    }   /* testCloseOnOpenDevice() */
    
    /**
     * Tests OutputDevice.close() when the
     * device is closed prior to the call.
     * <br /><br />
     * Expected return value: False
     */
    public void testCloseOnClosedDevice()
    {
        boolean test;
        
        test_device.open();
        test_device.close();
        test = test_device.close();
        
        assertFalse( "Device wasn't closed a second time", test );
    
    }   /* testCloseOnClosedDevice() */
    
    /*-------------------------------------------
     * TESTING OutputDevice::start()
     * 
     * TEST CASES:
     *   1. Calling start() when the device
     *      has been created 
     *   2. Calling start() when the device
     *      has already been closed (i.e. hasn't
     *      been created)
     *   3. Calling start() when playback of
     *      audio data has already started
     *-----------------------------------------*/
    
    /**
     * Tests OutputDevice.start() when the
     * device is opened prior to the call.
     * <br /><br />
     * Expected return value: True
     */
    public void testStartOnOpenDevice()
    {
        boolean test;
        
        test_device.open();
        test = test_device.start();
        test_device.close();
        
        assertTrue( "Device was started", test );
    
    }   /* testStartOnOpenDevice() */
    
    /**
     * Tests OutputDevice.start() when the
     * device is closed prior to the call.
     * <br /><br />
     * Expected return value: False
     */
    public void testStartOnClosedDevice()
    {
        boolean test;
        
        test_device.open();
        test_device.close();
        test = test_device.start();
        
        assertFalse( "Device was not stopped", test );
    
    }   /* testStartOnClosedDevice() */
    
    /**
     * Tests OutputDevice.start() when the
     * device is open and has already been
     * started prior to the call.
     * <br /><br />
     * Expected return value: True
     */
    public void testStartOnOpenDevice2x()
    {
        boolean test;
        
        test_device.open();
        test_device.start();
        test = test_device.start();
        test_device.close();
        
        assertTrue( "Device remained in its start state", test );
    
    }   /* testStartOnOpenDevice2x() */
    
    /*-------------------------------------------
     * TESTING OutputDevice::stop()
     * 
     * TEST CASES:
     *   1. Calling stop() when the device
     *      has been created and is currently
     *      playing audio data
     *   2. Calling stop() when the device
     *      has already been closed (i.e. hasn't
     *      been created)
     *   3. Calling stop() when playback of
     *      audio data has already been stopped
     *-----------------------------------------*/
    
    /**
     * Tests OutputDevice.stop() when the
     * device is opened prior to the call.
     * <br /><br />
     * Expected return value: True
     */
    public void testStopOnOpenDevice()
    {
        boolean test;
        
        test_device.open();
        test = test_device.stop();
        test_device.close();
        
        assertTrue( "Device was stopped", test );
    
    }   /* testStopOnOpenDevice() */
    
    /**
     * Tests OutputDevice.stop() when the
     * device is closed prior to the call.
     * <br /><br />
     * Expected return value: False
     */
    public void testStopOnClosedDevice()
    {
        boolean test;
        
        test_device.open();
        test_device.close();
        test = test_device.stop();
        
        assertFalse( "Device was not stopped", test );
    
    }   /* testStopOnClosedDevice() */
    
    /**
     * Tests OutputDevice.stop() when the
     * device is open and has already been
     * stopped prior to the call.
     * <br /><br />
     * Expected return value: True
     */
    public void testStopOnOpenDevice2x()
    {
        boolean test;
        
        test_device.open();
        test_device.stop();
        test = test_device.stop();
        test_device.close();
        
        assertTrue( "Device remained in its stopped state", test );
    
    }   /* testStopOnOpenDevice2x() */
    
    /*-------------------------------------------
     * TESTING restart capabilities
     *-----------------------------------------*/
    
    /**
     * Testing the class' ability to restart
     * after the playback has been stopped.
     * <br /><br />
     * Expected return result: True
     */
    public void testRestart()
    {
        boolean test;
        
        test_device.open();
        test_device.start();
        test_device.stop();
        test = test_device.start();
        test_device.stop();
        test_device.close();
        
        assertTrue( "Device was able to restart", test );
        
    }   /* testRestart() */
    
};
