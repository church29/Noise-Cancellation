package com.example.noisecancellation.MainProcess;

import android.util.Log;
import com.example.noisecancellation.Device.Mic.Mic;
import com.example.noisecancellation.Device.OutputDevice.OutputDevice;
import com.example.noisecancellation.fft.*;

public class MainProcess implements Runnable
{
    /*-----------------------------------------
     * Class variables
     *---------------------------------------*/
    private int          n;
    private Mic          m;
    private OutputDevice s;
    private FFT_Wrapper  fft;
    private boolean      paused;
    boolean              should_run;

    /*-----------------------------------------
     * Buffers used by this class.
     *      recorded_data - buffer containing the data
     *                      recorded during the last
     *                      call to getRecordData()
     *      cos_table     - a cosine lookup table
     *      window_data   - data obtained from
     *                      Hanning Window
     *---------------------------------------*/
    private byte   [] recorded_data;
    private double [] cos_table;
    private double [] window_data;

    /**
     * Default constructor for the audio processing thread
     */
    public MainProcess()
    {
        m             = new Mic();
        s             = new OutputDevice();
        n             = m.getSuggestedBufferSize();
        fft           = new FFT_Wrapper();
        paused        = true;
        should_run    = false;
        recorded_data = new byte[ n ];

        resetBuffers( n );
        setUp();

    }   /* MainProcess() */

    /**
     * Sets a flag telling the thread that
     * the audio processing should be
     * temporarily paused.
     */
    public void pause()
    {
        paused = true;
        m.stop();
        s.stop();

    }   /* pause() */

    /**
     * Sets a flag telling the thread that
     * the audio processing should resume.
     */
    public void resume()
    {
        m.start();
        s.start();
        paused = false;

    }   /* resume() */

    @Override
    /**
     * The function that the thread will run.
     * Why else would we call it run?
     */
    public void run()
    {
        should_run = true;
        while( should_run )
        {
            if( !paused )
            {
                 m.getRecordData( recorded_data );
                 invert( recorded_data );
                 s.write( recorded_data );
            }
        }

        tearDown();

    }   /* run() */

    /**
     * Sets a flag for the processing thread so that
     * it sees that it needs to stop processing
     * audio data.
     */
    public void stopProcessing()
    {
        should_run = false;

    }   /* stopProcessing() */

    /**
     * This function attempts to apply a Hanning Window
     * to the provided data.
     *
     * @param buf
     *  The data to which the Hanning Window will be
     *  applied.
     */
    private void applyWindow( final byte [] buf, final int len )
    {
        int i;

        if( window_data.length != len )
        {
            throw new RuntimeException( "Window and buffer dimensions don't match." );
        }

        for( i = 0; i < len; ++i )
        {
            window_data[ i ] = buf[ i ] * 0.5 * ( 1.0 - cos_table[ i ] );
        }

    }   /* applyWindow() */

    /**
     * Inverts the audio obtained from the microphone.
     * 16 bit pcm is in little endian format
     * We first convert a 16bit buffer into a short
     * If the short is equal to -32768, which would be
     * greater than max(short) when inverted, we add
     * one to the short.
     * After that, we convert the short back into
     * a little endian 16bit.
     * 
     * @param buf
     *  Buffer containing audio data
     */
    private void invert( byte [] buf )
    {
        int   i;
        short invert;
        for( i = 0; i < buf.length - 1; i += 2 ) 
        {
            invert = (short)( buf[ i ] | ( (short)buf[ i + 1 ] << 8 ) );
            if( invert < -Short.MAX_VALUE ) 
            {
                invert = -Short.MAX_VALUE;
            }
            invert       = (short)(  -invert );
            buf[ i ]     = (byte) (   invert & 0x00FF );
            buf[ i + 1 ] = (byte) ( ( invert >>> 8  ) );
        }

    }   /* invert() */

    /**
     * Resizes all of the buffers used by this class
     * to the size passed to the procedure from the caller.
     *
     * @param new_size
     *  The new size of the buffers.
     */
    private void resetBuffers( final int new_size )
    {
        n             = new_size;
        window_data   = new double[ new_size ];

        resetCosTable( new_size );

        System.gc();

    }   /* resetBuffers() */

    /**
     * Resets the cosine lookup table used in the
     * Hanning Window calculation. Supposedly, this
     * saves computation time since this only has
     * to be recalculated if the size of the
     * buffers change.
     *
     * @param new_size
     *  The new size of the cosine lookup table.
     */
    private void resetCosTable( final int new_size )
    {
        int i;

        cos_table = new double[ new_size ];

        for( i = 0; i < new_size; ++i )
        {
            cos_table[ i ] = Math.cos( 2.0 * Math.PI * (double)i / (double)new_size );
        }

    }   /* resetCosTable() */

    /**
     * Sets up the microphone and
     * output device for the audio
     * processing.
     */
    private void setUp()
    {
        m.open();
        s.open();

    }   /* setUp() */

    /**
     * Closes the microphone and
     * output device used by
     * the audio processing
     * thread
     */
    private void tearDown()
    {
        m.close();
        s.close();

    }   /* tearDown() */

};  /* MainProcess */
