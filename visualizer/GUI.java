import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.*;
import javax.swing.*;

import java.net.UnknownHostException;
import java.net.Socket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;

/**
 * The main GUI class
 */
public class GUI extends JFrame
{   
    private static final byte [] CLIENT_IP = {127, 0, 0, 1};
    private static final int SOCKET_PORT = 8576;
    private static final long serialVersionUID = 6411499808530678723L;
    private static final int TIMER_DELAY = 1000;
    
    private Timer t;
    private Grapher g;
    private short [] short_orig;
    private short [] short_inv;
    private volatile boolean updating;
    private volatile boolean modified;
    private volatile boolean stop_thread;
    private Thread loader_thread;
    
    /**
     * A class for our socket thread
     * 
     * Opens socket, reads from socket,
     * updates GUI's graph data, rinse and 
     * repeat
     * 
     * Protocol:
     *   - First value read from socket is the number of
     *     elements in the first buffer (the buffer
     *     containing the original Mic data)
     *   - Second value read from the socket is the
     *     number of elements in the inverted buffer
     *   - Subsequent values alternate between original
     *     data and inverted data. For example,
     *       3rd read -> original[0]
     *       4th read -> inverted[0]
     *       ...
     *   - Transmission ends when both buffers have
     *     been filled
     */
    private Runnable loader = new Runnable() {
        private static final int TIMEOUT = 5000;
        
        public void run() 
        {
            Socket in_sock;
            BufferedReader in;
            int orig_size = 0;
            int invert_size = 0;
            int i;
            byte [] original = null;
            byte [] inverted = null;
            String temp = null;
            
            try
            {
                in_sock = new Socket();
                System.out.println("Connected to socket...");
                in_sock.connect( new InetSocketAddress( InetAddress.getByAddress( CLIENT_IP ), SOCKET_PORT ), TIMEOUT );
                in = new BufferedReader( new InputStreamReader( in_sock.getInputStream() ) );
                while( !in.ready() );
            }
            catch( Exception e )
            {
                try
                {
                    System.err.println( InetAddress.getByName( "localhost" ) );
                }
                catch( Exception e2 )
                {
                    System.err.println( e2 );
                }
                
                System.err.println( e );
                throw new RuntimeException();
            }
            
            try 
            { 
                temp = in.readLine(); 
            } 
            catch( Exception e ) 
            {
                System.err.println( e );
                throw new RuntimeException();
            }
            
            while( null != temp )
            {
                orig_size = Integer.valueOf( temp );
                try 
                { 
                    invert_size = Integer.valueOf( in.readLine() ); 
                } 
                catch( Exception e ) 
                {
                    System.err.println( e );
                    throw new RuntimeException();
                }
                
                if( ( null == original ) || ( null == inverted ) || ( original.length != orig_size ) || ( inverted.length != invert_size ) )
                {   
                    original = new byte[ orig_size ];
                    inverted = new byte[ invert_size ];
                }
                
                for( i = 0; i < orig_size; ++i )
                {
                    try
                    {
                        original[ i ] = Byte.valueOf( in.readLine() );
                        inverted[ i ] = Byte.valueOf( in.readLine() );
                    }
                    catch( Exception e )
                    {
                        System.err.println( e );
                        throw new RuntimeException();
                    }
                }
                
                processData( original, inverted );
                
                if( stop_thread ) 
                {
                    break;
                }
                
                try 
                { 
                    temp = in.readLine(); 
                } 
                catch( Exception e ) 
                {
                    System.err.println( e );
                    throw new RuntimeException();
                }
            }
            
            try
            {
                in.close();
                in_sock.close();
            }
            catch( Exception e )
            {
                System.err.println( e );
                return;
            }
        }
    };

    /**
     * Default constructor
     */
    public GUI()
    {
        super();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        updating = false;
        stop_thread = false;
        modified = false;
        t = null;
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds( size.width / 40, size.height / 20, 19 * size.width / 20, 9 * size.height / 10 );
        
        g = new Grapher();
        
        add(g);
        validate();
        
        loader_thread = new Thread( loader );
        loader_thread.start();
        startTimer();
    }
    
    /**
     * Starts a timer. When the timer goes off,
     * the graphs are redrawn.
     */
    public void startTimer()
    {
        if( t != null )
        {
            return;
        }
        
        System.out.println("Setting up timer...\n");
        t = new Timer( TIMER_DELAY, null );
        
        ActionListener task_performer = new ActionListener() {
            public void actionPerformed( ActionEvent e )
            {
                if( !modified ) 
                {
                    return;
                }
                
                while(updating);
                updating = true;
                
                g.graph( short_orig, short_inv );
                g.repaint();
                t.restart();
                modified = false;
                
                updating = false;
            }
        };
        
        t.addActionListener( task_performer );
        t.setRepeats( true );
        t.start();
    }
    
    /**
     * Processes data
     * 
     * This used to do more, but right now it just copies everything
     * to this class' buffers
     * 
     * @param orig
     *  Buffer containing original mic data
     * 
     * @param inv
     *  Buffer containing inverted mic data
     */
    public void processData( byte [] orig, byte [] inv )
    {
        if( updating )
        {
            return;
        }
        
        updating = true;
        
        if( orig == null || short_orig == null || orig.length != short_orig.length )
        {
            short_orig = new short[ orig.length >> 1 ];
            short_inv = new short[ inv.length >> 1 ];
            
            System.gc();
        }
        
        for( int i = 0; i < orig.length; i += 2 )
        {
            /*-----------------------------
             * Convert from little-endian
             * to 16-bit short
             *---------------------------*/
            short_orig[ i >> 1 ] = (short)( ( (short)orig[ i ] & 0x000000FF) | ( (short)orig[ i + 1 ] << 8 ) );
            short_inv[ i >> 1 ] = (short)( ( (short)inv[ i ] & 0x000000FF) | ( (short)inv[ i + 1 ] << 8 ) );
            if( short_orig[ i >> 1 ] != -short_inv[ i >> 1 ] )  
            {
                System.err.println( short_orig[ i >> 1 ] + "\t" + short_inv[ i >> 1 ] + "\t" + orig[ i + 1 ] + "\t" + orig[ i ]+ "\t" + inv[ i + 1 ] + "\t" + inv[ i ] );
            }
        }
        
        modified = true;
        
        updating = false;
    }
    
    /**
     * The cleanup function--this is called
     * when the exit button on the window is
     * pressed.
     * 
     * @throws something
     */
    protected void finalize() throws Throwable
    {
        try 
        {
            stop_thread = true;
            Thread.sleep(500);
            loader_thread.join(); 
        } 
        catch( Exception e ) 
        {
            System.err.println( e );
            throw new RuntimeException();
        }
        
        super.finalize();
    }
};
