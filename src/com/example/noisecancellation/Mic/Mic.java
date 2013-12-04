package com.example.noisecancellation.Mic;

import com.example.noisecancellation.fft.*;

import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.media.AudioFormat;
import android.util.Log;

class Configuration
{
    /*-----------------------------------------
     * Literal Constants
     *---------------------------------------*/
    private final int DEFAULT_AUDIO_SOURCE   = AudioSource.MIC;
    private final int DEFAULT_SAMPLING_RATE  = 8000; /* in Hz */
    private final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private final int DEFAULT_AUDIO_FORMAT   = AudioFormat.ENCODING_PCM_16BIT;

    /*-----------------------------------------
     * Class Attributes
     *---------------------------------------*/
    private int audio_source;
    private int sampling_rate;
    private int channel_config;
    private int audio_format;

    /*-----------------------------------------
     * Default Constructor
     *---------------------------------------*/
    public Configuration()
    {
        setAll( DEFAULT_AUDIO_SOURCE,
                DEFAULT_SAMPLING_RATE,
                DEFAULT_CHANNEL_CONFIG,
                DEFAULT_AUDIO_FORMAT    );	
    }   /* Configuration() */

    /*-----------------------------------------
     * Helper function
     *---------------------------------------*/
    private void setAll( int aud_src, 
                         int s_rate, 
                         int ch_config, 
                         int aud_format )
    {
        audio_source   = aud_src;
        sampling_rate  = s_rate;
        channel_config = ch_config;
        audio_format   = aud_format;
        
    }   /* setAll() */

    /*-----------------------------------------
     * Class accessors
     *---------------------------------------*/
    public int getAudioSource()   { return( audio_source );   }
    public int getSamplingRate()  { return( sampling_rate );  }
    public int getChannelConfig() { return( channel_config ); }
    public int getAudioFormat()   { return( audio_format );   }

    /*-----------------------------------------
     * Class modifiers
     *---------------------------------------*/
    public void setAudioSource( int new_src )      { audio_source   = new_src;    }
    public void setSamplingRate( int new_rate )    { sampling_rate  = new_rate;   }
    public void setChannelConfig( int new_config ) { channel_config = new_config; }
    public void setAudioFormat( int new_format )   { audio_format   = new_format; }
	
};  /* Configuration */

public class Mic
{
    /*-----------------------------------------
     * Literal Constants
     *---------------------------------------*/
    private final int DEFAULT_BUFFER_SIZE = 1024;

    /*-----------------------------------------
     * Class Attributes
     *---------------------------------------*/
    private Configuration configuration;
    private AudioRecord   recorder;	
    private boolean       is_recording;
    private boolean       can_update;
    private FFT_Wrapper   fft;
    private int	          n;
    private int	          bytes_read;

    /*-----------------------------------------
     * Buffers used by this class.
     * 	    hz            - buffer containing frequencies 
     *                      of FFT transformed data
     * 	    db            - buffer containing decibel 
     *                      values of FFT transformed 
     *                      data
     *      magnitudes    - the magnitudes of
     *                      fft_data
     *      recorded_data - buffer containing the data
     *                      recorded during the last
     *                      call to getRecordData()
     *      cos_table     - a cosine lookup table
     *      window_data   - data obtained from
     *                      Hanning Window
     *      fft_data      - values returned from
     *                      performing FFT on
     *                      record_data 
     * 
     * NOTE: Maybe we should make these local
     *       to the getRecordData() function
     *---------------------------------------*/
    private int    [] hz;
    private int    [] db;
    private byte   [] magnitudes;
    private byte   [] recorded_data;
    private double [] cos_table;
    private double [] window_data;
    private double [] fft_data;


    /**
     * Default constructor for an FFT_Wrapper object. 
     */	
    public Mic()
    {
        bytes_read    = 0;
        is_recording  = false;
        can_update    = false;
        recorder      = null;
        configuration = new Configuration();
        fft           = new FFT_Wrapper( DEFAULT_BUFFER_SIZE );
        
        resetBuffers( DEFAULT_BUFFER_SIZE );
	
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
	
    }   /* getBufferSize() */


    /**
     * Returns the contents of the buffer containing
     * the decibel values of the recorded data.
     * 
     * @return 
     *  Returns an array of integers containing the
     *  decibel values of the last recorded data.
     */
    public int [] getDecibels()
    {
        return( db.clone() );
	
    }   /* getDecibels() */


    /**
     * Returns the contents of the buffer containing
     * the transformed representation of the recorded data.
     * 
     * @return 
     *  Returns an array of doubles containing the
     *  transformed representation of the last recorded data.
     */
    public double [] getFFTData()
    {
        return( fft_data.clone() );
	
    }   /* getFFTData() */


    /**
     * Returns the contents of the buffer containing
     * the frequencies of the recorded data.
     * 
     * @return 
     *  Returns an array of integers containing the
     *  frequencies of the last recorded data.
     */
    public int [] getFrequencies()
    {
        return( hz.clone() );
	
    }   /* getFrequencies() */


    /**
     * Returns the contents of the buffer containing
     * the magnitudes of the recorded data.
     * 
     * @return 
     *  Returns an array of bytes containing the
     *  magnitudes of the last recorded data.
     */
    public byte [] getMagnitudes()
    {
        return( magnitudes.clone() );
	
    }   /* getMagnitudes() */


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
	
    }   /* isRecording() */


    /**
     * Returns the contents of the buffer containing
     * the the recorded data.
     * 
     * @return 
     *  Returns an array of bytes containing the
     *  data last read by the microphone.
     */
    public byte [] getRecordedData()
    {
        return( recorded_data.clone() );
	
    }   /* getRecordedData() */


    /**
     * Is it safe to modify the settings of our microphone?
     * 
     * @return 
     *  Returns true if it is safe to update the
     *  microphone settings, and returns false otherwise.
     */
    public boolean canUpdate()
    {
        return( can_update );
	
    }   /* canUpdate() */


    /**
     * Resizes all of the buffers used by this class
     * to the size passed to the procedure from the caller.
     * 
     * @param new_size
     * 	      The new size of the buffers.
     */
    private void resetBuffers( int new_size )
    {
        n             = new_size; 
        hz            = new int[ new_size ];
        db            = new int[ new_size ];
        magnitudes    = new byte[ new_size ];
        recorded_data = new byte[ getSuggestedBufferSize() ];
        window_data   = new double[ new_size ];
        fft_data      = new double[ 2 * new_size ];
	
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
     * 	      The new size of the cosine lookup table.
     */
    private void resetCosTable( int new_size )
    {
        int i;
	
        cos_table = new double[ new_size ];
	
        for( i = 0; i < new_size; ++i )
        {
            cos_table[ i ] = Math.cos( 2.0 * Math.PI * (double)i / (double)new_size );
        }
	
    }   /* resetCosTable() */


    /**
     * This function opens a recording device. If a
     * device was successfully opened, the function
     * starts recording audio data.
     * 
     * If the function is, for some reason, unable 
     * to open a recording device, it throws an 
     * exception.
     * 
     * @throws IllegalStateException
     */
    public void start()
    {
        int buffer_size = recorded_data.length;
	
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

            if( AudioRecord.RECORDSTATE_RECORDING != recorder.getRecordingState() ) 
            {
                recorder.startRecording();
                Log.i( "info", "Recorder..............start...." );
                is_recording = true;
                Log.i( "info", "start............." );
                new RecordThread( this );	
            }			
        } 
        catch( IllegalStateException ie )
        {
            Log.i("error", "Could not create audio record object");
        }
	
    }   /* start() */


    /**
     * This function stops the device from
     * recording audio data.
     */
    public void stop() 
    {
        is_recording = false;		
        if( null != recorder ) 
        {
            if( AudioRecord.STATE_UNINITIALIZED != recorder.getState() ) 
            {
                recorder.stop();
                Log.i("info", "stop.............");
                recorder.release();
            }						
        } 
        else 
        {
            Log.i("error", "no audio record object");
        }
		      
    }   /* stop() */


    /**
     * This function attempts to apply a Hanning Window
     * to the provided data.
     * 
     * @param buf
     * 	      The data to which the Hanning Window will be
     *        applied.
     */
    private void applyWindow( final byte [] buf, int len )
    {
        int i;
	
        if( window_data.length != len )
        {
            throw new RuntimeException( "Window and buffer dimensions don't match. BT-Dub," +
                                        "this is Mr. Window. Mr. Hanning Window, to be precise." +
                                        " Call me back." );
        }
	
        for( i = 0; i < len; ++i )
        {
            window_data[ i ] = buf[ i ] * 0.5 * ( 1.0 - cos_table[ i ] );
        }
	
    }   /* applyWindow() */


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
     *  buffer.
     */
    public int getRecordData( byte [] buf )
    {
        can_update = false;
	
        Log.i("data", "reading recording data..............");
        bytes_read = recorder.read( recorded_data, 0, recorded_data.length );    	
        Log.i("data", "done recording data..............");
	
        can_update = true;    	    	    	
	
        buf = recorded_data;
        return( bytes_read );
	
    }   /* getRecordData() */


    /**
     * This function processes an audio buffer
     * and fills the instance of this class'
     * buffers with the processed data. The
     * buffers can be retrieved by using the
     * corresponding class accessors.
     * 
     * @param buf
     *        Buffer containing recorded audio data.
     *        The data in this buffer is processed by the
     *        function and used to fill the various buffers
     *        specific to this class.
     * @param len
     *        The number of elements to process. This needs
     *        to be less than or equal to the size of the
     *        supplied buffer.
     * 
     * @throws RuntimeException
     *  This function throws a runtime exception if the
     *  len parameter is greater than the size of the buffer.
     *  Providing a value for len that is greater than the
     *  size of the provided data buffer is telling the function
     *  to process more elements than are actually present.
     *  This can't happen.
     */
    public void processData( final byte [] buf, final int len )
    {
        double re;
        double im;
        int buffer_size = buf.length;
        int sampling_rate = configuration.getSamplingRate();
	
        if( buf.length < len )
        {
            throw new RuntimeException( "The number of elements to process cannot be " +
                                        "greater than the size of the buffer" );
        }

        if( len != n )
        {
            resetBuffers( len );
        }

        Log.i( "data", "processing recorded data.............." );

        //apply Hanning Window
        applyWindow( buf, len );

        fft_data = fft.fft( window_data );
        for( int i = 0; i < len; ++i )
        {    			
            re = fft_data[ i << 1 ];
            im = fft_data[ ( i << 1 ) + 1 ];
	
            magnitudes[ i ] = (byte)Math.ceil( Math.hypot( re, im ) ); 
            db[ i ] = (int)( 10.0 * Math.log10( (double)magnitudes[ i ] ) );
            hz[ i ] = (int)( i * sampling_rate * ( 1.0 / buffer_size ) );	
        }

        Log.i( "data", "finished processing recorded data.............." );
	
    }   /* processData() */
	
};  /* Mic */


class RecordThread implements Runnable
{
    private Thread t;
    private Mic m;
	
    RecordThread( Mic mic )
    {
        m = mic;
        t = new Thread( this, "recording thread" );
        t.start();

    }   /* RecordThread() */

    public void run()
    {
        int len;
        byte [] data = new byte[ m.getSuggestedBufferSize() ];
	
        while( m.isRecording() )
        {
            len = m.getRecordData( data );
            m.processData( data, len );
        }
	
    }   /* run() */
	
};  /* RecordThread */

