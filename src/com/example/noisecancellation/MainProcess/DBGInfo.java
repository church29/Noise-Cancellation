package com.example.noisecancellation.MainProcess;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Class used for debugging. It's only available for
 * use in the emulator. It pretty much just writes a
 * bunch of stuff to a socket.
 */
public class DBGInfo
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
