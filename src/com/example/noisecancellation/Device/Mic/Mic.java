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
     *---------------------------------------*/
    public static final byte ERROR_NO_DEVICE = -1;
    
    /*-----------------------------------------
     * Class Attributes
     *---------------------------------------*/
    private Configuration configuration;
    private AudioRecord   recorder;
    private boolean       is_recording;
    private int	          bytes_read;
    
    /**
     * Default constructor for an FFT_Wrapper object.
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
     *  Returns true if the microphone was 
     *  successfully opened, and false if 
     *  the microphone wasn't.  
     */
    public boolean open()
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
            return( false );
        }
        
        return( true );

    }   /* open() */
    
    
    /**
     * This function starts recording audio data.
     *
     * @return
     *  This function returns true if the microphone
     *  was able to start recording data and false if
     *  it isn't. If this returns false, then the
     *  microphone wasn't initialized properly,
     *  and it would be suggested that the
     *  programmer close the device and attempt
     *  to reopen it.
     */
    public boolean start()
    {
        if( ( null == recorder ) || ( AudioRecord.STATE_UNINITIALIZED == recorder.getState() ) )
        {
            return( false );
        }
        
        if( AudioRecord.RECORDSTATE_RECORDING != recorder.getRecordingState() )
        {
            recorder.startRecording();
            Log.i( "info", "Recorder..............start...." );
            is_recording = true;
            Log.i( "info", "start............." );
            
            return( true );
        }
        
        return( false );

    }   /* start() */


    /**
     * This function stops the device from
     * recording audio data.
     * 
     * @return
     *  Returns true if the function was able to stop
     *  the microphone from recording and false if it
     *  wasn't. A likely cause for failure is a poorly
     *  initialized microphone object.
     */
    public boolean stop()
    {
        if( null != recorder )
        {
            if( AudioRecord.STATE_UNINITIALIZED != recorder.getState() )
            {
                recorder.stop();
                is_recording = false;
                Log.i("info", "stop.............");
                return( true );
            }
        }
        
        Log.i("Mic--stop()", "No recorder object, or recorder object not initialized");
        return( false );

    }   /* stop() */
    
    
    /**
     * This function closes the microphone
     * 
     * @return
     *  Returns true if the function was able to close
     *  the microphone and false if it
     *  wasn't. A likely cause for failure is a poorly
     *  initialized microphone object.
     */
    public boolean close()
    {
        if( null != recorder ) 
        {
            if( AudioRecord.STATE_UNINITIALIZED != recorder.getState() )
            {
                recorder.stop();
                recorder.release();
                recorder = null;
                return( true );
            }
        }
        
        Log.i("Mic--close()", "No recorder object, or recorder object not initialized");
        return( false );

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
            return( ERROR_NO_DEVICE );
        }
        
        Log.i( "data", "reading recording data.............." );
        bytes_read = recorder.read( buf, 0, buf.length );
        Log.i( "data", "done recording data.............." );
        
        return( bytes_read );

    }   /* getRecordData() */
    
};  /* Mic */