package com.example.noisecancellation.Device.Mic;

import android.media.AudioRecord;
import android.util.Log;

import com.example.noisecancellation.Device.Configuration;

public class Mic
{
    /*-----------------------------------------
     * Literal Constants
     *---------------------------------------*/
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    
    /*-----------------------------------------
     * Error codes
     * 
     * NO_ERROR        - There were no errors
     * ERROR_NO_DEVICE - Device hasn't been 
     *                   created
     * ERROR_ALREADY_RECORDING 
     *                 - Device is
     *                   already recording
     * ERROR_NOT_INITIALIZED 
     *                 - Device hasn't
     *                   been initialized for
     *                   recording audio
     *---------------------------------------*/
    public static final byte NO_ERROR                =  0;
    public static final byte ERROR_NO_DEVICE         = -1;
    public static final byte ERROR_ALREADY_RECORDING = -2;
    public static final byte ERROR_NOT_INITIALIZED   = -3;
    
    /*-----------------------------------------
     * Class Attributes
     *---------------------------------------*/
    private Configuration configuration;
    private AudioRecord   recorder;
    private boolean       is_recording;
    private int	          bytes_read;
    
    /**
     * Default constructor for a microphone object.
     */
    public Mic()
    {
        bytes_read    = 0;
        is_recording  = false;
        recorder      = null;
        configuration = new Configuration( Configuration.INPUT_DEVICE_CONFIGURATION );

    }   /* Mic() */


    /**
     * Returns the suggested buffer size.
     *
     * @return
     *  Returns the suggested buffer size.
     *  The returned size is at least 1024.
     */
    public int getSuggestedBufferSize()
    {
        int min_size = AudioRecord.getMinBufferSize( configuration.getSamplingRate(),
                                                     configuration.getChannelConfig(),
                                                     configuration.getAudioFormat()    );
        if( min_size < DEFAULT_BUFFER_SIZE )
        {
            return( DEFAULT_BUFFER_SIZE );
        }

        return( min_size );

    }   /* getSuggestedBufferSize() */


    /**
     * Returns the state of the recorder.
     *
     * @return
     *  Returns true if the device is currently recording,
     *  and false otherwise.
     */
    public boolean isRecording()
    {
        return( is_recording );

    }   /* isRecording() */


    /**
     * Returns the number of bytes read during the last call
     * to getRecordData().
     *
     * @return
     *  Returns an integer containing the number of bytes stored
     *  in the recorded_data buffer during the last call to
     *  getRecordData().
     */
    public int getBytesLastRead()
    {
        return( bytes_read );

    }   /* getBytesLastRead() */

    
    /**
     * This function opens a recording device.
     * 
     * @return
     *  Returns one of the following error codes:
     *  <br/><br/>
     *  <ul>
     *    <li>NO_ERROR is returned if there 
     *        were no errors</li>
     *    <li>ERROR_NOT_INITIALIZED is returned 
     *        if the device couldn't be initialized</li>
     *  </ul>  
     */
    public byte open()
    {
        int buffer_size = getSuggestedBufferSize();

        try
        {
            if( null == recorder )
            {
                recorder = new AudioRecord( configuration.getAudioSource(),
                                            configuration.getSamplingRate(),
                                            configuration.getChannelConfig(),
                                            configuration.getAudioFormat(),
                                            buffer_size );
                Log.i( "info", "Recorder..............created...." );
            }

            if( AudioRecord.STATE_UNINITIALIZED == recorder.getState() )
            {
                Log.i( "info", "Recorder..............initialize...." );
                recorder = new AudioRecord( configuration.getAudioSource(),
                                            configuration.getSamplingRate(),
                                            configuration.getChannelConfig(),
                                            configuration.getAudioFormat(),
                                            buffer_size );
            }
        }
        catch( IllegalStateException ie )
        {
            Log.i("error", "Could not create audio record object");
            return( ERROR_NOT_INITIALIZED );
        }
        
        return( NO_ERROR );

    }   /* open() */
    
    
    /**
     * This function starts recording audio data.
     *
     * @return
     * Returns one of the following error codes:
     *  <br/><br/>
     *  <ul>
     *    <li>NO_ERROR is returned if there 
     *        were no errors</li>
     *    <li>ERROR_NO_DEVICE is returned if
     *        the device has not been created yet</li>
     *    <li>ERROR_ALREADY_RECORDING is returned
     *        if the device is already recording
     *        audio (i.e. it has already been started)</li>
     *  </ul>
     *  <br/>
     *  If this function fails, it would be 
     *  suggested that the programmer close 
     *  the device and attempt to reopen it.
     */
    public byte start()
    {
        if( ( null == recorder ) || ( AudioRecord.STATE_UNINITIALIZED == recorder.getState() ) )
        {
            return( ERROR_NO_DEVICE );
        }
        
        if( AudioRecord.RECORDSTATE_RECORDING != recorder.getRecordingState() )
        {
            recorder.startRecording();
            Log.i( "info", "Recorder..............start...." );
            is_recording = true;
            Log.i( "info", "start............." );
            
            return( NO_ERROR );
        }
        
        return( ERROR_ALREADY_RECORDING );

    }   /* start() */


    /**
     * This function stops the device from
     * recording audio data.
     * 
     * @return
     * Returns one of the following error codes:
     *  <br/><br/>
     *  <ul>
     *    <li>NO_ERROR is returned if there 
     *        were no errors</li>
     *    <li>ERROR_NO_DEVICE is returned if
     *        the device has not been created yet</li>
     *    <li>ERROR_NOT_INITIALIZED is returned
     *        if the device hasn't been initialized</li>
     *  </ul>
     *  <br/>
     *  A likely cause for failure is a poorly
     *  initialized microphone object.
     */
    public byte stop()
    {
        if( null != recorder )
        {
            if( AudioRecord.STATE_UNINITIALIZED != recorder.getState() )
            {
                recorder.stop();
                is_recording = false;
                Log.i("info", "stop.............");
                return( NO_ERROR );
            }
            
            Log.i( "Mic--stop()", "Recorder object not initialized." );
            return( ERROR_NOT_INITIALIZED );
        }
        
        Log.i("Mic--stop()", "No recorder object, or recorder object not initialized");
        return( ERROR_NO_DEVICE );

    }   /* stop() */
    
    
    /**
     * This function closes the microphone
     * 
     * @return
     * Returns one of the following error codes:
     *  <br/><br/>
     *  <ul>
     *    <li>NO_ERROR is returned if there 
     *        were no errors</li>
     *    <li>ERROR_NO_DEVICE is returned if
     *        the device has not been created yet</li>
     *    <li>ERROR_NOT_INITIALIZED is returned
     *        if the device hasn't been initialized</li>
     *  </ul>
     *  <br />
     *  A likely cause for failure is a poorly
     *  initialized microphone object.
     */
    public byte close()
    {
        if( null != recorder ) 
        {
            if( AudioRecord.STATE_UNINITIALIZED != recorder.getState() )
            {
                recorder.stop();
                recorder.release();
                recorder = null;
                return( NO_ERROR );
            }
            
            Log.i( "Mic--close()", "Recorder object not initialized." );
            return( ERROR_NOT_INITIALIZED );
        }
        
        Log.i("Mic--close()", "No recorder object");
        return( ERROR_NO_DEVICE );

    }   /* close() */
     
    
    /**
     * This function grabs audio data from the
     * recording device.
     *
     * @param buf
     * 	      Buffer containing recorded audio data.
     *        When this function returns, the buffer
     *        will be filled with the audio data that
     *        was read.
     *
     * @return
     *  Returns the number of bytes read This will always
     *  be less than or equal to the size of the supplied
     *  buffer. If there was an error, ERROR_NO_DEVICE is
     *  returned.
     */
    public int getRecordData( byte [] buf )
    {
        if( ( null == recorder ) || ( AudioRecord.STATE_UNINITIALIZED == recorder.getState() ) )
        {
            return( (int)ERROR_NO_DEVICE );
        }
        
        Log.i( "data", "reading recording data.............." );
        bytes_read = recorder.read( buf, 0, buf.length );
        Log.i( "data", "done recording data.............." );
        
        return( bytes_read );

    }   /* getRecordData() */
    
};  /* Mic */