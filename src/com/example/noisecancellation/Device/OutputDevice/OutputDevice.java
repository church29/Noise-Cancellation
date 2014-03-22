package com.example.noisecancellation.Device.OutputDevice;

import android.util.Log;
import android.media.AudioManager;
import android.media.AudioTrack;
import com.example.noisecancellation.Device.Configuration;

public class OutputDevice
{    
    /*-------------------------------------------
     * Class variables
     *-----------------------------------------*/
    private OutDevLoader  output_device;
    private Thread        reload_thread;
    private boolean       closed;
       
    /**
     * Default constructor for an output device
     */
    public OutputDevice()
    {
        closed        = true;
        reload_thread = null;
        output_device = new OutDevLoader();
        
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
        if( !closed )
        {
            Log.i( "OutputDevice--open()", "Device is already open" );
            return( false );
        }
        
        /*---------------------------------------
         * Open the devices
         *-------------------------------------*/
        closed = false;
        if( !output_device.openDevs() )
        {
            return( false );
        }
        
        Log.i( "OutputDevice--open()", "Device was created" );
        
        /*---------------------------------------
         * Create and start the reloading thread
         *-------------------------------------*/
        reload_thread = new Thread( output_device );
        reload_thread.start();
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
        if( closed )
        {
            Log.i( "OutputDevice--start()", "Device not created" );
            return( false );
        }

        //output_device.play();
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
        return( output_device.write( buf ) );
        
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
        if( closed )
        {
            Log.i( "OutputDevice--stop()", "Device was never created" );
            return( false );
        }
        
        output_device.stopDevs();
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
        if( closed )
        {
            Log.i( "OutputDevice--close()", "Device was never created" );
            return( false );
        }
        
        /*-----------------------------
         * Close the devices and
         * join the thread with this 
         * one
         *---------------------------*/
        output_device.stopReloading();
        output_device.closeDevs();
        try
        {
            reload_thread.join();
        }
        catch( Exception e )
        {
            return( false );
        }
        
        closed = true;
        return( true );
        
    }   /* close() */
    
};  /* OutputDevice() */

/**
 * This class is extremely inefficient. Since
 * Android doesn't let you rewrite to an
 * output device with a static playback mode,
 * we needed a workaround in order minimize
 * our latency. The class has been created as a
 * workaround for the one-time write. Essentially,
 * this is a thread that closes and reopens every
 * device once it is written to so that a different
 * thread can write data to the output device again.
 * Surprisingly, this somehow works, although it
 * probably isn't recommended. We should probably
 * try to find other ways of solving our latency
 * problem, because this class is SUPER ugly.
 */
class OutDevLoader implements Runnable
{
    /*-------------------------------------------
     * Literal constants
     * 
     * DEFAULT_BUFFER_SIZE - default buffer size
     *                       of the output devices
     * MAX_DEVICES    - maximum number of devices
     *                  that this will open
     * FREE_DEVICES   - maximum number of free
     *                  devices before the loader
     *                  thread should start reloading
     *                  the devices
     * MIN_DEV_OFFSET - the maximum number of
     *                  devices between the loader
     *                  thread's current device
     *                  (that it frees), and cur_dev
     *-----------------------------------------*/
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final int MAX_DEVICES         = 16;
    private static final int FREE_DEVICES        = 13 * MAX_DEVICES / 16;
    private static final int MIN_DEV_OFFSET      = 3  * MAX_DEVICES / 4; 
    
    /*-------------------------------------------
     * Class variables
     * 
     * out_devs - an array of output devices
     * cur_dev  - index of the device we're
     *            writing to
     * configuration - configuration of all
     *            the devices
     * buffer_size - size fo the internal
     *            audio device buffer
     * closing_time - a flag telling our
     *            loading thread whether it's
     *            time to close
     * release_devs - a flag telling the
     *            loader thread whether it
     *            should start relaoding
     *            the devices
     *-----------------------------------------*/
    private AudioTrack [] out_devs;
    private int           cur_dev;
    private Configuration configuration;
    private int           buffer_size;
    private boolean       closing_time;
    private boolean       release_devs;
    
    /**
     * Default constructor
     */
    public OutDevLoader()
    {
        /*-----------------------------
         * Make sure our flags our set
         *---------------------------*/
        release_devs = false;
        closing_time = true;
        
        /*-----------------------------
         * Set our current device
         *---------------------------*/
        cur_dev = 0;
        
        /*-----------------------------
         * Create a device config
         * four our output devices
         * and set the buffer size
         *---------------------------*/
        configuration = new Configuration( Configuration.OUTPUT_DEVICE_CONFIGURATION );
        buffer_size   = getSuggestedBufferSize();
        
        /*-----------------------------
         * Reserve memory for our
         * device array and initialize
         * them all to null
         *---------------------------*/
        out_devs = new AudioTrack[ MAX_DEVICES ];
        for( int i = 0; i < MAX_DEVICES; ++i )
        {
            out_devs[ i ] = null;
        }
        
    }   /* OutDevLoader() */
    
    /**
     * Opens the output devices
     * 
     * @return
     *  Returns a true if the devices could
     *  be opened and flase if the couldn't.
     */
    public boolean openDevs()
    {
        for( int i = 0; i < MAX_DEVICES; ++i )
        {
            if( !createDevice( i ) )
            {
                return( false );
            }
        }
        
        return( true );
        
    }   /* openDevs() */
    
    /**
     * Closes all of the devices
     * 
     * @return
     *  Returns true if the devices were
     *  closed successfully, and false if
     *  they weren't
     */
    public boolean closeDevs()
    {
        /*-----------------------------
         * Wait for the loader thread
         * to finish
         *---------------------------*/
        while( release_devs );
        
        /*-----------------------------
         * Close the devices
         *---------------------------*/
        for( int i = 0; i < MAX_DEVICES; ++i )
        {
            if( !releaseDevice( i ) )
            {
                return( false );
            }
        }
        
        return( true );
        
    }   /* closeDevs() */
    
    /**
     * Stops all devices from playing
     * 
     * @return
     *  Returns true if all of the devices
     *  could be closed, and false if they
     *  coudln't.
     */
    public boolean stopDevs()
    {
        /*-----------------------------
         * Wait until the loader thread
         * is finished
         *---------------------------*/
        while( release_devs );
        
        /*-----------------------------
         * Attempt to stop all devices
         *---------------------------*/
        for( int i = 0; i < MAX_DEVICES; ++i )
        {
            switch( out_devs[ i ].getPlayState() )
            {
                case AudioTrack.PLAYSTATE_STOPPED:
                    break;
                
                case AudioTrack.PLAYSTATE_PAUSED:
                case AudioTrack.PLAYSTATE_PLAYING:
                    out_devs[ i ].stop();
                    out_devs[ i ].flush();
                    break;
                    
                default:
                    Log.i( "DevOutLoader--stopDevs()", "Invalid play state" );
                    break;
            }
        }
        
        return( true );
        
    }   /* stopDevs() */
    
    /**
     * Sets the flag that stops the
     * thread from reloading data
     */
    public void stopReloading() 
    {
        closing_time = true;
        
    }   /* stopReloading() */
    
    /**
     * Writes a buffer to the current
     * output device
     * @param buf
     *  A byte buffer to write to the device
     *  
     * @return
     *  Returns true if the buffer was successfully
     *  written, and false if it wasn't
     */
    public boolean write( byte [] buf )
    {
        /*-----------------------------
         * Local variables
         *---------------------------*/
        int bytes_written = 0;
        
        /*-----------------------------
         * Attempt to write to the
         * current device
         *---------------------------*/
        Log.i( "DevOutLoader--write()", "Writing data..." );
        switch( out_devs[ cur_dev ].getPlayState() ) 
        {
            case AudioTrack.PLAYSTATE_PAUSED:
            case AudioTrack.PLAYSTATE_PLAYING:
                out_devs[ cur_dev ].stop();
                out_devs[ cur_dev ].flush();
                bytes_written = out_devs[ cur_dev ].write( buf, 0, buf.length );
                out_devs[ cur_dev ].play();
                break;
                
            case AudioTrack.PLAYSTATE_STOPPED:
                out_devs[ cur_dev ].flush();
                bytes_written = out_devs[ cur_dev ].write( buf, 0, buf.length );
                out_devs[ cur_dev ].play();
                break;
                
            default:
                break;
        }

        /*-----------------------------
         * Check for errors
         *---------------------------*/
        if( bytes_written != buf.length )
        {
            Log.i( "DevOutLoader--write()", "Buffer wasn't entirely written to output device" );
            if( AudioTrack.ERROR_BAD_VALUE == bytes_written )
            {
                Log.i( "DevOutLoader--write()", "values don't have valid indices" );
                return( false );
            }
            else if( AudioTrack.ERROR_INVALID_OPERATION == bytes_written )
            {
                Log.i( "DevOutLoader--write()", "Device wasn't initialized properly" );
                return( false );
            }
        }
        
        /*-----------------------------
         * Update our current device
         * and check if we should start
         * reloading the devices
         *---------------------------*/
        cur_dev = ++cur_dev < MAX_DEVICES ? cur_dev : 0;
        if( cur_dev >= FREE_DEVICES )
        {
            release_devs = true;
        }
       
        return( true );

    }   /* write() */
    
    /**
     * The reloading thread function
     */
    public void run()
    {
        /*-------------------------------------
         * Set this flag
         *-----------------------------------*/
        closing_time = false;
        
        /*-------------------------------------
         * Loop until the flag is set
         *-----------------------------------*/
        while( !closing_time )
        {
            /*---------------------------------
             * Only reload devices if this 
             * flag is set
             *-------------------------------*/
            if( release_devs )
            {
                /*-----------------------------
                 * Loop through all devices, 
                 * being sure to close and 
                 * reopen all of them. If we
                 * are within three devices
                 * of the current write device,
                 * then wait. This is so that
                 * playback isn't cut off
                 *---------------------------*/
                Log.i( "OutDevLoader--run()", "Reloading audio devices" );
                for( int i = 0; i < MAX_DEVICES; ++i )
                {
                    while( i == ( cur_dev - MIN_DEV_OFFSET ) % MAX_DEVICES )
                    {
                        if( closing_time )
                        {
                            break;
                        }
                        
                    }
                    releaseDevice( i );
                    createDevice( i );
                }
                
                /*-----------------------------
                 * Tell the garbage collector
                 * to run
                 *---------------------------*/
                System.gc();
                
                /*-----------------------------
                 * Reset the flag
                 *---------------------------*/
                release_devs = false;
            }
        }
        
    }   /* run() */
    
    /**
     * Opens a single device
     * 
     * @param dev_idx
     *  The index of the device to
     *  open
     *  
     * @return
     *  Returns true if the device was
     *  successfully opened, and false
     *  otherwise.
     */
    private boolean createDevice( int dev_idx )
    {
        /*-----------------------------
         * Check if the index is valid
         *---------------------------*/
        if( ( dev_idx < 0 ) || ( dev_idx >= MAX_DEVICES ) )
        {
            return( false );
        }
        
        /*-----------------------------
         * Check if the device is open
         *---------------------------*/
        if( out_devs[ dev_idx ] != null )
        {
            return( false );
        }
        
        /*-----------------------------
         * Create the device
         *---------------------------*/
        out_devs[ dev_idx ] = new AudioTrack( AudioManager.STREAM_MUSIC, 
                                              configuration.getSamplingRate(), 
                                              configuration.getChannelConfig(),
                                              configuration.getAudioFormat(),
                                              buffer_size, 
                                              AudioTrack.MODE_STATIC );
        return( true );
        
    }   /* createDevice() */
    
    /**
     * Closes a single device
     * 
     * @param dev_idx
     *  Index of the device to close
     *  
     * @return
     *  Returns true if the device was
     *  closed, and false otherwise
     */
    private boolean releaseDevice( int dev_idx )
    {
        /*-----------------------------
         * Check for valid index
         *---------------------------*/
        if( ( dev_idx < 0 ) || ( dev_idx >= MAX_DEVICES ) )
        {
            return( false );
        }
        
        /*-----------------------------
         * Close the device if it
         * isn't already closed
         *---------------------------*/
        if( out_devs[ dev_idx ] != null )
        {
            /*-------------------------
             * Stop the device
             *-----------------------*/
            switch( out_devs[ dev_idx ].getPlayState() )
            {
                case AudioTrack.PLAYSTATE_STOPPED:
                    break;
                
                case AudioTrack.PLAYSTATE_PAUSED:
                case AudioTrack.PLAYSTATE_PLAYING:
                    out_devs[ dev_idx ].stop();
                    break;
                    
                default:
                    Log.i( "DevOutLoader--stopDevs()", "Invalid play state" );
                    break;
            }
            
            /*-------------------------
             * Close the device
             *-----------------------*/
            out_devs[ dev_idx ].release();
            out_devs[ dev_idx ] = null;
            return( true );
        }
        
        return( true );
        
    }   /* releaseDevice() */
    
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

};  /* OutDevLoader */
