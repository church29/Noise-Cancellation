package com.example.noisecancellation;

import android.media.AudioManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ToggleButton;

import com.example.noisecancellation.MainProcess.MainProcess;

public class MainActivity extends Activity
{
	private MainProcess      work_process;
	private Thread           t;
	private boolean          headphones_used;
	private AudioManager     man;
	private HeadphoneMonitor headmon;
	private volatile boolean changing_headphone_state;
	
	/**
	 * Private class that makes sure that a
	 * head set is being used with our app.
	 */
	private class HeadphoneMonitor extends BroadcastReceiver {
        @Override
        public void onReceive( Context context, Intent intent )
        {
            /*-----------------------------------
             * Set this flag so that we don't
             * crash if the button is pressed
             * immediately after the headphones
             * are disconnected.
             * 
             * This actually was an issue for
             * me. Don't laugh.
             *---------------------------------*/
            changing_headphone_state = true;
            
            boolean disconnected = false;
            String action = intent.getAction();
            if( action.equals( Intent.ACTION_HEADSET_PLUG ) )
            {
                switch( intent.getIntExtra( "state", -1 ) )
                {
                    case 0:
                        headphones_used = false;
                        Log.i( "HeadphoneMonitor--onReceive()", "Headphone state: unplugged" );
                        disconnected = true;                        
                        break;
                        
                    case 1:
                        headphones_used = true;
                        Log.i( "HeadphoneMonitor--onReceive()", "Headphone state: plugged" );
                        break;
                        
                    default:
                        Log.i( "HeadphoneMonitor--onReceive()", "Headphone state: unknown" );
                        break;
                }
            }
            
            /*-----------------------------------
             * Check if bluetooth headphones 
             * are attached
             *---------------------------------*/
            else if( man.isBluetoothA2dpOn() )
            {
                Log.i( "HeadphoneMonitor--onReceive()", "Headphone state: bluetooth" );
                headphones_used = true;
            }
            
            changing_headphone_state = false;
            
            if( disconnected )
            {
                /*-----------------------
                 * If the button is
                 * currently pressed (i.e.,
                 * we are currently canceling)
                 * then we tell the
                 * button to not be
                 * pressed. In calling
                 * performClick(), we
                 * also ensure that 
                 * the audio inverter
                 * is paused.
                 *---------------------*/
                ToggleButton b = (ToggleButton)findViewById( R.id.togglebutton );
                if( b.isChecked() )
                {
                    b.performClick();
                }
            }
            
        }   /* onReceive() */	    
	};
	
    @SuppressWarnings( "deprecation" )
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /*---------------------------------------
         * Create a new main process to handle
         * all of the audio processing
         *-------------------------------------*/
        work_process = new MainProcess();
        t = new Thread( work_process, "work" );
        t.start();
        
        changing_headphone_state = false;
        
        /*---------------------------------------
         * Set up the headphone state listener
         * 
         * NOTE: isWiredHeadsetOn() is
         *       deprecated, but I wanted to
         *       be sure that the headphone
         *       state is set correctly when
         *       our app is first started 
         *-------------------------------------*/
        man = (AudioManager)getSystemService( Context.AUDIO_SERVICE );
        headphones_used = man.isBluetoothA2dpOn() || man.isWiredHeadsetOn();
        headmon = new HeadphoneMonitor();
        
        IntentFilter filter = new IntentFilter( Intent.ACTION_HEADSET_PLUG );
        registerReceiver( headmon, filter );
        
    }   /* onCreate() */

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        /*---------------------------------------
         * Inflate the menu; this adds items 
         * to the action bar if it is present.
         *-------------------------------------*/
        getMenuInflater().inflate(R.menu.main, menu);
        return( true );
        
    }   /* onCreateOptionsMenu() */

    public void onToggleClicked(View view) 
    {        
        /*---------------------------------------
         * Is the toggle on?
         *-------------------------------------*/
        boolean on = ((ToggleButton) view).isChecked();
        
        /*---------------------------------------
         * If we're changing state, then we
         * should just return after reverting
         * the button back to its previous
         * state
         *-------------------------------------*/
        if( changing_headphone_state )
        {
            ((ToggleButton)view).setChecked( !on );
            return;
        }
        
        /*---------------------------------------
         * Check if bluetooth is enabled
         * 
         * The listener class won't update
         * itself if bluetooth devices
         * are connected, so we perform
         * an additional check here.
         *-------------------------------------*/
        if( man.isBluetoothA2dpOn() )
        {
            headphones_used = true;
        }

        if( on ) 
        {
            /*-----------------------------------
             * If headphones are not
             * connected, show a dialog
             * and set the button's state to
             * unchecked.
             *---------------------------------*/
            if( !headphones_used )
            {
                AlertDialog.Builder b = new AlertDialog.Builder( view.getContext() );
                b.setTitle( "Error" );
                b.setMessage( "Headphones are currently unplugged. Please plug in a wired headset or connect a bluetooth headset" );
                b.setPositiveButton( "Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        dialog.dismiss();                    
                    }
                } );
                
                b.create().show();
                
                ( (ToggleButton)view ).setChecked( false );
                return;
            }
            
            /*-----------------------------------
             * Resume our audio processing
             *---------------------------------*/
            work_process.resume();
        } 
        else 
        {        	
        	/*-----------------------------------
        	 * Pause our audio processing
        	 *---------------------------------*/
        	work_process.pause();
        }
        
    }   /* onToggleClicked() */
    
    @Override
    public void onDestroy()
    {
        /*---------------------------------------
         * Tell the thread to stop processing
         * audio data and close the thread.
         *-------------------------------------*/
        work_process.stopProcessing();
        try
        {
            t.join();
        }
        catch( InterruptedException ie )
        {
            Log.i( "MainActivity--onToggleClicked()", "Failed to clean up after myself." );
            throw new RuntimeException( "Unable to clean up after myself" );
        }
        
        try
        {
            unregisterReceiver( headmon );
        }
        catch( Exception e )
        {
            Log.i( "MainActivity--onDestroy()", e.toString() );
        }
        
        /*---------------------------------------
         * Tell our parent to destroy itself
         *-------------------------------------*/
        super.onDestroy();
        
    }   /* onDestroy() */
    
    @Override
    public void onPause()
    {
        try
        {
            unregisterReceiver( headmon );
        }
        catch( Exception e )
        {
            Log.i( "MainActivity--onPause()", e.toString() );
        }
        
        super.onPause();
        
    }   /* onPause() */
    
    @Override
    public void onResume()
    {
        IntentFilter filter = new IntentFilter( Intent.ACTION_HEADSET_PLUG );
        registerReceiver( headmon, filter );
        super.onResume();
        
    }   /* onResume() */

};