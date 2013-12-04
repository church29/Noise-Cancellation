package com.example.noisecancellation;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
//import com.example.noisecancellation.fft.*;
import com.example.noisecancellation.Mic.*;

public class MainActivity extends Activity
{

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        Mic m = new Mic();
        m.start();
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
    
}
