package com.example.noisecancellation;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ToggleButton;

import com.example.equalizer.Mic.Mic;
//import com.example.noisecancellation.fft.*;

public class MainActivity extends Activity
{
	private Mic m;
	
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        m = new Mic();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return( true );
    }
    
    public void onToggleClicked(View view) {
        // Is the toggle on?
        boolean on = ((ToggleButton) view).isChecked();
        
        if (on) {
            m.start();
        } else {
        	m.stop();
        }
    }

}
