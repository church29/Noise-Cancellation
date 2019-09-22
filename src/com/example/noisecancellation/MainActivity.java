package com.example.noisecancellation;

import java.io.File;
import java.util.TimerTask;

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
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ToggleButton;

import com.example.noisecancellation.MainProcess.MainProcess;

public class MainActivity extends Activity
{ 
    private static final int MAX_INPUT_DELAY = 100;
    private static final int MIN_INPUT_DELAY = 1;
    private static final int MAX_OUTPUT_DELAY = 100;
    private static final int MIN_OUTPUT_DELAY = 1;
    private static final int MAX_AMPLITUDE = 1000;
    private static final int INITIAL_AMPLITUDE = 300;
    private static final int INITIAL_INPUT_DELAY = 50;
    private static final int INITIAL_OUTPUT_DELAY = 50;
    
    private MainProcess      work_process;
    private boolean          headphones_used;
    private AudioManager     man;
    private HeadphoneMonitor headmon;
    private volatile boolean changing_headphone_state;
	private boolean          music_paused;
	private Intent player_intent;
	private volatile boolean graphing;
	private DrawerTask graph_task;
	private Graph graph;
	
	private class DrawerTask extends TimerTask
	{
        @Override
        public void run()
        {
            //graph.graphData( work_process.getMicData() );
        }
	}
	
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
    
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /*---------------------------------------
         * Create a new main process to handle
         * all of the audio processing
         *-------------------------------------*/
        work_process = new MainProcess( this );        
        
        changing_headphone_state = false;
        music_paused = true;
        graphing = false;
        player_intent = null;      
        
        SeekBar s = (SeekBar)findViewById( R.id.amplitude_adjust );
        s.setMax( MAX_AMPLITUDE );
        s.setProgress( INITIAL_AMPLITUDE );
        work_process.setAmplitude( 1.0f );
        s.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged( SeekBar arg0, int arg1, boolean arg2 )
            {
                work_process.setAmplitude( ( arg1 + INITIAL_AMPLITUDE ) / (0.5f * MAX_AMPLITUDE ) );
            }

            @Override
            public void onStartTrackingTouch( SeekBar seekBar )
            {
            }

            @Override
            public void onStopTrackingTouch( SeekBar seekBar )
            {
                work_process.setAmplitude( ( seekBar.getProgress() + ( 0.5f * MAX_AMPLITUDE ) - INITIAL_AMPLITUDE ) / (0.5f * MAX_AMPLITUDE ) );                
            }
        });
        
        s = (SeekBar)findViewById( R.id.mdelay_adjust );
        s.setMax( MAX_INPUT_DELAY - MIN_INPUT_DELAY );
        s.setProgress( INITIAL_INPUT_DELAY );
        work_process.setMicDelay( INITIAL_INPUT_DELAY );
        s.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged( SeekBar arg0, int arg1, boolean arg2 )
            {
                work_process.setMicDelay( arg1 + MIN_INPUT_DELAY );
            }

            @Override
            public void onStartTrackingTouch( SeekBar seekBar )
            {
            }

            @Override
            public void onStopTrackingTouch( SeekBar seekBar )
            {
                work_process.setMicDelay( seekBar.getProgress() + MIN_INPUT_DELAY );                
            }
        });
        
        s = (SeekBar)findViewById( R.id.hdelay_adjust );
        s.setMax( MAX_OUTPUT_DELAY - MIN_OUTPUT_DELAY );
        s.setProgress( INITIAL_OUTPUT_DELAY );
        work_process.setOutputDelay( INITIAL_OUTPUT_DELAY );
        s.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged( SeekBar arg0, int arg1, boolean arg2 )
            {
                work_process.setOutputDelay( arg1 + MIN_OUTPUT_DELAY );
            }

            @Override
            public void onStartTrackingTouch( SeekBar seekBar )
            {
            }

            @Override
            public void onStopTrackingTouch( SeekBar seekBar )
            {
                work_process.setOutputDelay( seekBar.getProgress() + MIN_OUTPUT_DELAY );                
            }
        });
        
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
        
        if( ( savedInstanceState != null ) && ( savedInstanceState.containsKey( "ButtonState" ) ) )
        {
            restoreState( savedInstanceState );
        }
        
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
    
    public void onRestoreClicked( View view )
    {
        SeekBar s = (SeekBar)findViewById( R.id.amplitude_adjust );
        s.setProgress( INITIAL_AMPLITUDE );
        work_process.setAmplitude( 1.0f );
        
        s = (SeekBar)findViewById( R.id.mdelay_adjust );
        s.setProgress( INITIAL_INPUT_DELAY );
        work_process.setMicDelay( INITIAL_INPUT_DELAY );
        
        s = (SeekBar)findViewById( R.id.hdelay_adjust );
        s.setProgress( INITIAL_OUTPUT_DELAY );
        work_process.setOutputDelay( INITIAL_OUTPUT_DELAY );
    }
    
    public void onMusicButtonClicked( View view )
    {
        if( null == player_intent )
        {
            player_intent = new Intent("android.intent.action.MUSIC_PLAYER");
        }
        startActivity(player_intent);        
    }
    
    public void onGraphClicked( View view )
    {
        toggleGraphMode();
    }
    
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
        work_process.tearDown();
        
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

    @Override
    public void onSaveInstanceState( Bundle b )
    {
        super.onSaveInstanceState( b );
        ToggleButton btn = (ToggleButton)findViewById( R.id.togglebutton );
        b.putBoolean( "ButtonState", btn.isChecked() );
        
        btn = (ToggleButton)findViewById( R.id.graphbutton );
        b.putBoolean( "GraphState", btn.isChecked() );
        
        SeekBar bar = (SeekBar)findViewById( R.id.amplitude_adjust );
        b.putInt( "Amplitude", bar.getProgress() );
        
        bar = (SeekBar)findViewById( R.id.hdelay_adjust );
        b.putInt( "OutDelay", bar.getProgress() );
        
        bar = (SeekBar)findViewById( R.id.mdelay_adjust );
        b.putInt( "InDelay", bar.getProgress() );
    }
    
    @Override
    public void onRestoreInstanceState( Bundle b )
    {
        super.onRestoreInstanceState( b );
        restoreState( b ); 
    }
    
    private void restoreState( Bundle b )
    {
        int temp;
        boolean prev_state = b.getBoolean( "ButtonState" );
        ToggleButton btn = (ToggleButton)findViewById( R.id.togglebutton );
        btn.setChecked( prev_state );
        if( prev_state )
        {
            work_process.resume();
        }
        
        prev_state = b.getBoolean( "GraphState" );
        btn = (ToggleButton)findViewById( R.id.graphbutton );
        btn.setChecked( prev_state );
        if( prev_state )
        {
            toggleGraphMode();
        }
        
        SeekBar bar = (SeekBar)findViewById( R.id.amplitude_adjust );
        temp = b.getInt( "Amplitude" );
        bar.setProgress( temp );
        work_process.setAmplitude( temp );
        
        bar = (SeekBar)findViewById( R.id.hdelay_adjust );
        temp = b.getInt( "OutDelay" );
        bar.setProgress( temp );
        work_process.setOutputDelay( temp );
        
        bar = (SeekBar)findViewById( R.id.mdelay_adjust );
        temp = b.getInt( "InDelay" );
        bar.setProgress( temp );
        work_process.setMicDelay( temp );
    }
    
    private void toggleGraphMode()
    {
        if( graphing )
        {
            graphing = false;
        }
        else
        {
            graphing = true;
        }
    }

};