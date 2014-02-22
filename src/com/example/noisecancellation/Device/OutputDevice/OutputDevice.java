package com.example.noisecancellation.Device.OutputDevice;

import android.util.Log;
import android.media.AudioManager;
import android.media.AudioTrack;
import com.example.noisecancellation.Device.Configuration;

public class OutputDevice
{
    /*-------------------------------------------
     * Literal constants
     *-----------------------------------------*/
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    
    /*-------------------------------------------
     * Error codes
     * 
     * NO_ERROR              - There were no errors
     * ERROR_NO_DEVICE       - Device hasn't been created
     * ERROR_ALREADY_OPEN    - Device is already open
     * ERROR_NOT_INITIALIZED - Device hasn't
     *                         been initialized for
     *                         recording audio
     * ERROR_BUFFER_ISSUES   - Miscellaneous error for write()
     *-----------------------------------------*/
    public static final byte NO_ERROR                =  0;
    public static final byte ERROR_NO_DEVICE         = -1;
    public static final byte ERROR_ALREADY_OPEN      = -2;
    public static final byte ERROR_NOT_INITIALIZED   = -3;
    public static final byte ERROR_BUFFER_ISSUES     = -4;
    
    /*-------------------------------------------
     * Class variables
     *-----------------------------------------*/
    private int           buffer_size;
    private AudioTrack    output_device;
    private Configuration configuration;
       
    /**
     * Default constructor for an output device
     */
    public OutputDevice()
    {
        buffer_size   = 0;
        output_device = null;
        configuration = new Configuration( Configuration.OUTPUT_DEVICE_CONFIGURATION );
        
    }   /* OutputDevice() */
      
    /**
     * Opens an output device.
     * 
     * @return
     * Returns one of the following error codes:
     *  <br/><br/>
     *  <ul>
     *    <li>NO_ERROR is returned if there 
     *        were no errors</li>
     *    <li>ERROR_ALREADY_OPEN is returned 
     *        if the device is already open</li>
     *  </ul>
     */
    public byte open()
    {
        if( null != output_device )
        {
            Log.i( "OutputDevice--open()", "Device is already open" );
            return( ERROR_ALREADY_OPEN );
        }
        
        buffer_size = getSuggestedBufferSize();
        
        /*---------------------------------------
         * Right now, we're opening the output
         * device in streaming mode. It may be
         * better for us if we were to open the
         * audio device in a static mode. If we
         * were to do this, latency would be
         * reduced, and the calls to write() 
         * would instantaneously return (i.e.
         * blocking write() in streaming mode).
         * 
         * I couldn't get static mode to work
         * with the current settings, but it
         * could possibly be something that 
         * could be done in the future.
         *-------------------------------------*/
        output_device = new AudioTrack( AudioManager.STREAM_MUSIC, 
                                        configuration.getSamplingRate(), 
                                        configuration.getChannelConfig(),
                                        configuration.getAudioFormat(),
                                        buffer_size, 
                                        AudioTrack.MODE_STREAM );
        Log.i( "OutputDevice--open()", "Device has been created" );
        
        return( NO_ERROR );
        
    }   /* open() */
    
    /**
     * Starts audio playback.
     * 
     * @return
     * Returns one of the following error codes:
     *  <br/><br/>
     *  <ul>
     *    <li>NO_ERROR is returned if there 
     *        were no errors</li>
     *    <li>ERROR_NO_DEVICE is returned if
     *        the device hasn't been created yet</li>
     *  </ul>
     */
    public byte start()
    {
        if( null == output_device )
        {
            Log.i( "OutputDevice--start()", "Device not created" );
            return( ERROR_NO_DEVICE );
        }

        output_device.play();
        return( NO_ERROR );
        
    }   /* start() */
    
    /**
     * Writes a buffer to the output stream.
     * 
     * @param buf
     *  A buffer containing audio data
     *  
     * @return
     *  Returns one of the following error codes:
     *  <br/><br/>
     *  <ul>
     *    <li>NO_ERROR is returned if there 
     *        were no errors</li>
     *    <li>ERROR_NO_DEVICE is returned if
     *        the device hasn't been created</li>
     *    <li>ERROR_NOT_INITIALIZED is returned 
     *        if the device wsn't initialized
     *        properly</li>
     *    <li>ERROR_BUFFER_ISSUES is returned
     *        if there were any other issues
     *        that occurred while writing to
     *        the device</li>
     *  </ul>
     */
    public byte write( byte [] buf )
    {
        if( null == output_device )
        {
            return( ERROR_NO_DEVICE );
        }
        
        Log.i( "OutputDevice--write()", "Writing data..." );
        int bytes_written = output_device.write( buf, 0, buf.length );
        if( bytes_written != buf.length )
        {
            Log.i( "OutputDevice--write()", "Buffer wasn't entirely written to output device" );
            if( AudioTrack.ERROR_BAD_VALUE == bytes_written )
            {
                Log.i( "OutputDevice--write()", "values don't have valid indices" );
                return( ERROR_BUFFER_ISSUES );
            }
            else if( AudioTrack.ERROR_INVALID_OPERATION == bytes_written )
            {
                Log.i( "OutputDevice--write()", "Device wasn't initialized properly" );
                return( ERROR_NOT_INITIALIZED );
            }
        }
        
        return( NO_ERROR );
        
    }   /* write() */
      
    /**
     * Stops the output device from outputting stuff.
     * It should be noted that this function isn't
     * necessarily needed. It would suffice to just
     * call close().
     * 
     * @return
     *  Returns one of the following error codes:
     *  <br/><br/>
     *  <ul>
     *    <li>NO_ERROR is returned if there 
     *        were no errors</li>
     *    <li>ERROR_NO_DEVICE is returned if
     *        the device hasn't been created yet</li>
     *  </ul>
     */
    public byte stop()
    {
        if( null == output_device )
        {
            Log.i( "OutputDevice--stop()", "Device was never created" );
            return( ERROR_NO_DEVICE );
        }
        
        output_device.stop();
        return( NO_ERROR );
        
    }   /* stop() */
      
    /**
     * Closes the audio device.
     * 
     * @return
     *  Returns one of the following error codes:
     *  <br/><br/>
     *  <ul>
     *    <li>NO_ERROR is returned if there 
     *        were no errors</li>
     *    <li>ERROR_NO_DEVICE if the device
     *         hasn't been created yet</li>
     *  </ul>
     */
    public byte close()
    {
        if( null == output_device )
        {
            Log.i( "OutputDevice--close()", "Device was never created" );
            return( ERROR_NO_DEVICE );
        }
        
        output_device.stop();
        output_device.release();
        output_device = null;
        return( NO_ERROR );
        
    }   /* close() */
      
    /**
     * Returns the suggested buffer size.
     *
     * @return
     *  Returns the suggested buffer size.
     *  The returned size is at least 1024.
     */
    public int getSuggestedBufferSize()
    {
        int min_size = AudioTrack.getMinBufferSize( configuration.getSamplingRate(),
                                                    configuration.getChannelConfig(),
                                                    configuration.getAudioFormat()    );
        if( DEFAULT_BUFFER_SIZE > min_size )
        {
            return( DEFAULT_BUFFER_SIZE );
        }

        return( min_size );

    }   /* getSuggestedBufferSize() */
    
};
