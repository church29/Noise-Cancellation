package com.example.noisecancellation;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ToggleButton;

import com.example.noisecancellation.MainProcess.MainProcess;

public class MainActivity extends Activity
{
	private MainProcess      work_process;
	private Thread           t;
		
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
        
        if( ( savedInstanceState != null ) && ( savedInstanceState.containsKey( "ButtonState" ) ) )
        {
            restoreButtonState( savedInstanceState.getBoolean( "ButtonState" ) );
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

    public void onToggleClicked(View view) 
    {        
        /*---------------------------------------
         * Is the toggle on?
         *-------------------------------------*/
        boolean on = ((ToggleButton) view).isChecked();
        
        if( on ) 
        {
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
        
        /*---------------------------------------
         * Tell our parent to destroy itself
         *-------------------------------------*/
        super.onDestroy();
        
    }   /* onDestroy() */
        
    @Override
    public void onSaveInstanceState( Bundle b )
    {
        super.onSaveInstanceState( b );
        ToggleButton btn = (ToggleButton)findViewById( R.id.togglebutton );
        b.putBoolean( "ButtonState", btn.isChecked() );
    }
    
    @Override
    public void onRestoreInstanceState( Bundle b )
    {
        super.onRestoreInstanceState( b );
        restoreButtonState( b.getBoolean( "ButtonState" ) ); 
    }
    
    private void restoreButtonState( boolean prev_state )
    {
        ToggleButton btn = (ToggleButton)findViewById( R.id.togglebutton );
        btn.setChecked( prev_state );
        if( prev_state )
        {
            work_process.resume();
        }
    }

};