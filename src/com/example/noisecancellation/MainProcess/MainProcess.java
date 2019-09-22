package com.example.noisecancellation.MainProcess;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import com.example.noisecancellation.Device.Mic.Mic;
import com.example.noisecancellation.Device.OutputDevice.OutputDevice;

public class MainProcess
{
    private static final int READ_DATA = 0;
    private static final int WRITE_DATA = 1;
    
    /*-----------------------------------------
     * Class variables
     *---------------------------------------*/
    private int                 n;
    private Mic                 m;
    private OutputDevice        s;
    private boolean             paused;
    private volatile boolean    should_run;
    private volatile boolean    cancelling;
    private float               amplitude_adjust;
    //private LMSFilter           filter;
    //private Filter              filter;
    private Timer mic_timer;
    private Timer out_timer;
    private InTask mic_task;
    private OutTask out_task;
    private volatile boolean locked;
    private int mic_delay;
    private int out_delay;
    private volatile boolean dirty;
    private Context parent;
    
    /*-----------------------------------------
     * Debug socket stuff for visual depiction
     * of the waves
     *---------------------------------------*/
    private DBGInfo dbg_info;
    private boolean use_dbg_info;
    
    /*-----------------------------------------
     * Buffers used by this class.
     *      recorded_data - buffer containing the data
     *                      recorded during the last
     *                      call to getRecordData()
     *---------------------------------------*/
    private short [] mic_data;
    
    private class OutTask extends TimerTask
    {

        @Override
        public void run()
        {
            if( !cancelling )
            {
                return;
            }
            
            int i;
            short [] adj_mic = new short [ m.getSuggestedBufferSize() >> 1 ];
            short [] out_data = new short [ m.getSuggestedBufferSize() >> 1 ];
            
            if( !dirty )
            {
                return;
            }
            
            update_mic_data( adj_mic, READ_DATA );

            for( i = 0; i < adj_mic.length; ++i )
            {
                out_data[ i ] = (short)( -adj_mic[ i ] );
            }
                
            s.write( out_data );
            if( use_dbg_info )
            {
                Log.i("write timer", "WRITING TO SOCKET");
                byte [] temp1 = new byte [ n ];
                byte [] temp2 = new byte [ n ];
                for( i = 0; i < temp1.length; i += 2 )
                {
                    temp1[ i ]     = (byte) (   adj_mic[i>>1] & 0x000000FF );
                    temp1[ i + 1 ] = (byte) ( ( adj_mic[i>>1] >>> 8  ) & 0x000000FF );
                    temp2[ i ]     = (byte) (   out_data[i>>1] & 0x000000FF );
                    temp2[ i + 1 ] = (byte) ( ( out_data[i>>1] >>> 8  ) & 0x000000FF );
                }
                dbg_info.write( temp1, temp2 );
            }            
        } 
    }
    
    private class InTask extends TimerTask
    {
        @Override
        public void run()
        {
            float amp_adj = amplitude_adjust;
            byte [] mic_data = new byte [ m.getSuggestedBufferSize() ];
            short [] adj_data = new short [ m.getSuggestedBufferSize() >> 1 ];

            if( cancelling )
            {
                if( dirty )
                {
                    return;
                }
                
                m.getRecordData( mic_data );
                int temp_idx;
                for( int i = 0; i < mic_data.length; i += 2 )
                {
                    temp_idx = i >> 1;
                    adj_data[ temp_idx ] = (short)( ( (short)mic_data[ i ] & 0x000000FF) | ( (short)mic_data[ i + 1 ] << 8 ) );
                    adj_data[ temp_idx ] = (short)( adj_data[ temp_idx ] * amp_adj );
                }
                
                update_mic_data( adj_data, WRITE_DATA );
            }            
        }        
    }
    /**
     * Default constructor for the audio processing thread
     */
    public MainProcess( Context parent )
    {
        m             = new Mic();
        s             = new OutputDevice(m.getSamplingRate());
        n             = m.getSuggestedBufferSize();
        paused        = true;
        cancelling    = false;
        should_run    = false;
        mic_data = new short [ n >> 1 ];
        amplitude_adjust = 1.0f;
        dirty = false;
        locked = false;
        this.parent = parent;
        //filter = new Filter( n );
        
        use_dbg_info = Build.FINGERPRINT.contains( "generic" );
        if( use_dbg_info )
        {
            dbg_info   = new DBGInfo();
            Log.i( "MainProcess--init", "DEBUGGING ENABLED" );
        }

        setUp();
        initTimer();
    }   /* MainProcess() */
    
    public void setAmplitude( float val )
    {
        amplitude_adjust = val;
    }
    
    private void createTasks()
    {
        mic_task = new InTask();        
        out_task = new OutTask();
    }
    
    private void initTimer()
    {
        out_timer = new Timer();
        mic_timer = new Timer();
        createTasks();
                
        mic_delay = 20;
        out_delay = 20;
        
        out_timer.scheduleAtFixedRate( out_task, mic_delay + ( mic_delay >> 1 ), out_delay );
        mic_timer.scheduleAtFixedRate( mic_task,  mic_delay, mic_delay );
    }

    /**
     * Sets a flag telling the thread that
     * the audio processing should be
     * temporarily paused.
     */
    public void pause()
    {
        cancelling = false;
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
        cancelling = true;
    }   /* resume() */

    /**
     * Sets a flag for the processing thread so that
     * it sees that it needs to stop processing
     * audio data.
     */
    public void stopProcessing()
    {
        should_run = false;

    }   /* stopProcessing() */
    
    public short [] getMicData()
    {
        short [] ret = new short [ mic_data.length ];
        
        while( locked );
        locked = true;
        for( int i = 0; i < mic_data.length; ++i )
        {
            ret[ i ] = mic_data[ i ];
        }
        locked = false;
        return( ret );
    }

    private synchronized void update_mic_data( short [] buf, int mod_code ) throws ArrayIndexOutOfBoundsException, NullPointerException
    {
        int i;
        switch( mod_code )
        {
            case READ_DATA:
                if( mic_data.length != buf.length )
                {
                    mic_data = new short [ buf.length ];
                }
                
                for( i = 0; i < mic_data.length; ++i )
                {
                    buf[ i ] = mic_data[ i ];
                }
                dirty = false;
                break;
                
            case WRITE_DATA:
                if( mic_data.length != buf.length )
                {
                    mic_data = new short [ buf.length ];
                }
                
                for( i = 0; i < buf.length; ++i )
                {
                    mic_data[ i ] = buf[ i ];
                }
                dirty = true;
                break;
                
            default:
                throw new RuntimeException();
        }
    }
    
    public void setMicDelay( int val )
    {
        mic_task.cancel();
        out_task.cancel();
        mic_timer.purge();
        out_timer.purge();
        mic_delay = val;
        createTasks();
        mic_timer.scheduleAtFixedRate( mic_task, mic_delay, mic_delay );
        out_timer.scheduleAtFixedRate( out_task, mic_delay + ( mic_delay >> 1 ), out_delay );        
    }
    
    public void setOutputDelay( int val )
    {
        mic_task.cancel();
        out_task.cancel();
        mic_timer.purge();
        out_timer.purge();
        out_delay = val;
        createTasks();
        mic_timer.scheduleAtFixedRate( mic_task, mic_delay, mic_delay );
        out_timer.scheduleAtFixedRate( out_task, mic_delay + ( mic_delay >> 1 ), out_delay );        
    }
    
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
            /*-------------------------
             * If buf[i] is negative,
             * Java will sign-extend
             * it when we perform the
             * bitwise-or. When this
             * happens, all of the
             * bits in the most 
             * significant byte will
             * be ones, so we need
             * to get rid of the sign
             * extension before it can 
             * hurt us--thus the bitwise-
             * and cleverly inserted in
             * the code.
             *------------------------*/
            invert = (short)( ( (short)buf[ i ] & 0x000000FF) | ( (short)buf[ i + 1 ] << 8 ) );
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
    public void tearDown()
    {
        m.close();
        s.close();
        
        if( use_dbg_info )
        {
            try
            {
                mic_task.cancel();
                out_task.cancel();
                out_timer.cancel();
                out_timer.purge();
                mic_timer.cancel();
                mic_timer.purge();
                //player.tearDown();
                dbg_info.stop();
                Thread.sleep(500);
            }
            catch( Exception e )
            {
                Log.i( "MainProcess--tearDown()", "Restless thread" );
            }
        }

    }   /* tearDown() */
/*
    @Override
    public IBinder onBind( Intent arg0 )
    {
        // TODO Auto-generated method stub
        return null;
    }
*/
};  /* MainProcess */