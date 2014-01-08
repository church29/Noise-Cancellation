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
     *  Returns true if the function successfully opened
     *  a device. If the device has already been opened,
     *  then this function returns false.
     */
    public boolean open()
    {
        if( null != output_device )
        {
            Log.i( "OutputDevice--open()", "Device is already open" );
            return( false );
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
        
        return( true );
        
    }   /* open() */
    
    /**
     * Starts audio playback.
     * 
     * @return
     *  Returns true if the device was able to
     *  start playing audio data and false if
     *  it wasn't.
     */
    public boolean start()
    {
        if( null == output_device )
        {
            Log.i( "OutputDevice--start()", "Device not created" );
            return( false );
        }

        output_device.play();
        return( true );
        
    }   /* start() */
    
    /**
     * Writes a buffer to the output stream.
     * 
     * @param buf
     *  A buffer containing audio data
     *  
     * @return
     *  Returns true if the function was able to write the
     *  buffer to the output device and false if it wasn't.
     */
    public boolean write( byte [] buf )
    {
        Log.i( "OutputDevice--write()", "Writing data..." );
        int bytes_written = output_device.write( buf, 0, buf.length );
        if( bytes_written != buf.length )
        {
            Log.i( "OutputDevice--write()", "Buffer wasn't entirely written to output device" );
            if( AudioTrack.ERROR_BAD_VALUE == bytes_written )
            {
                Log.i( "OutputDevice--write()", "values don't have valid indices" );
                return( false );
            }
            else if( AudioTrack.ERROR_INVALID_OPERATION == bytes_written )
            {
                Log.i( "OutputDevice--write()", "Device wasn't initialized properly" );
                return( false );
            }
        }
        
        return( true );
        
    }   /* write() */
      
    /**
     * Stops the output device from outputting stuff.
     * It should be noted that this function isn't
     * necessarily needed. It would suffice to just
     * call close().
     * 
     * @return
     *  Returns true if the function stopped the output and
     *  false if it wasn't.
     */
    public boolean stop()
    {
        if( null == output_device )
        {
            Log.i( "OutputDevice--stop()", "Device was never created" );
            return( false );
        }
        
        output_device.stop();
        return( true );
        
    }   /* stop() */
      
    /**
     * Closes the audio device.
     * 
     * @return
     *  Returns true if the device was closed,
     *  and false if it wasn't.
     */
    public boolean close()
    {
        if( null == output_device )
        {
            Log.i( "OutputDevice--close()", "Device was never created" );
            return( false );
        }
        
        output_device.stop();
        output_device.release();
        output_device = null;
        return( true );
        
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
