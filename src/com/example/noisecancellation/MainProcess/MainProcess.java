package com.example.noisecancellation.MainProcess;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import com.example.noisecancellation.Device.Mic.Mic;
import com.example.noisecancellation.Device.OutputDevice.OutputDevice;

public class MainProcess implements Runnable
{
    /*-----------------------------------------
     * Class variables
     *---------------------------------------*/
    private int          n;
    private Mic          m;
    private OutputDevice s;
    private boolean      paused;
    boolean              should_run;
    
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
    private byte   [] recorded_data;

    /**
     * Default constructor for the audio processing thread
     */
    public MainProcess()
    {
        m             = new Mic();
        s             = new OutputDevice();
        n             = m.getSuggestedBufferSize();
        paused        = true;
        should_run    = false;
        recorded_data = new byte[ n ];
        
        /*-----------------------------
         * Check if we're running
         * on the simulator
         *---------------------------*/
        use_dbg_info = Build.FINGERPRINT.contains( "generic" );
        if( use_dbg_info )
        {
            dbg_info   = new DBGInfo();
        }

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
        byte [] old_data = null;
        should_run = true;
        while( should_run )
        {
            if( !paused )
            {
                 m.getRecordData( recorded_data );
                 if( use_dbg_info )
                 {
                     if( null == old_data )
                     {
                         old_data = new byte[ recorded_data.length ];
                     }
                     for( int i = 0; i < old_data.length; ++i )
                     {
                         old_data[ i ] = recorded_data[  i  ];
                     }
                 }
                 
                 invert( recorded_data );
                 s.write( recorded_data );
                 
                 if( use_dbg_info )
                 {
                     dbg_info.write( old_data, recorded_data );
                 }
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
    private void tearDown()
    {
        m.close();
        s.close();
        
        if( use_dbg_info )
        {
            try
            {
                dbg_info.stop();
                Thread.sleep(500);
            }
            catch( Exception e )
            {
                Log.i( "MainProcess--tearDown()", "Restless thread" );
            }
        }
        
    }   /* tearDown() */

};  /* MainProcess */

/**
 * Class used for debugging. It's only available for
 * use in the emulator. It pretty much just writes a
 * bunch of stuff to a socket.
 */
class DBGInfo
{
    private static final int SOCK_PORT = 8575;
    private static final byte [] IP_ADDRESS = { 10, 0, 2, 15 };
    
    private volatile byte [] original;
    private volatile byte [] inverted;
    private volatile boolean should_stop;
    private volatile boolean connected;
    
    /**
     * Internal class for an asynchronous task.
     * 
     * An exception is triggered if there is
     * any network stuff happening on the main
     * thread. The way to get around that is to
     * create an asynchronous task, so that's
     * what was done.
     */
    private class SockTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground( Void ... voids )
        {
            int i;
            PrintWriter out;
            ServerSocket server_sock;
            Socket dbg_sock;
            
            try
            {
                connected = false;
                server_sock = new ServerSocket( SOCK_PORT, 1, InetAddress.getByAddress( IP_ADDRESS ) );
                Log.i( "run()", "Opened socket..." + server_sock.getLocalPort() + 
                                " " + server_sock.isBound() + " " + server_sock.getLocalSocketAddress() + 
                                " " + server_sock.getInetAddress());
                dbg_sock = server_sock.accept();
                Log.i( "run()", "Accepted socket connection" );
                out = new PrintWriter( dbg_sock.getOutputStream() );
            }
            catch( Exception e)
            {
                Log.i( "SocketThread", e.toString() ); 
                return( null ); 
            }
            
            connected = true;        
            should_stop = false;
            
            while( !should_stop )
            {
                if( ( original != null ) && ( inverted != null ) )
                {
                    out.printf( "%d\n%d\n", (int)original.length, (int)inverted.length );
                    for( i = 0; i < original.length; ++i )
                    {
                        out.printf( "%d\n", (int)original[ i ] );
                        out.printf( "%d\n", (int)inverted[ i ] );
                    }
                    
                    original = null;
                    inverted = null;
                }
            }
            
            try
            {
                out.close();
                dbg_sock.close();
                server_sock.close();
            }
            catch( Exception e )
            {
                Log.i( "Debug thread", "Failed to close sockets" );
            }
            
            return null;
        }
    };
    
    private SockTask sock_task;
    
    /**
     * Default constructor
     */
    public DBGInfo()
    {
        original = null;
        inverted = null;
        connected = false;
        should_stop = true;
        
        sock_task = new SockTask();
        sock_task.execute();
    }
    
    /**
     * Tells the socket task to close
     */
    public void stop()
    {
        should_stop = true;
    }
    
    /**
     * Writes data to an array. The array will be
     * written to the socket at a later time
     * 
     * @param orig
     *  Buffer containing the original signal (directly from Mic)
     *  
     * @param inv
     *  Buffer containing the inverted signal
     */
    public void write( byte [] orig, byte [] inv )
    {
        if( !connected )
        {
            return;
        }
        
        if( ( original != null ) || ( inverted != null ) )
        {
            return;
        }
        
        original = new byte[ orig.length ];
        inverted = new byte[ inv.length ];
        
        for( int i = 0; i < original.length; ++i )
        {    
            original[ i ] = orig[ i ];
            inverted[ i ] = inv[ i ];
        }
    }
    
}