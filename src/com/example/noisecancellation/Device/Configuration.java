package com.example.noisecancellation.Device;

import android.media.AudioFormat;
import android.media.MediaRecorder.AudioSource;

public class Configuration
{
    /*-----------------------------------------
     * Literal Constants
     *---------------------------------------*/
    private static final int DEFAULT_AUDIO_IN_SOURCE    = AudioSource.MIC;
    private static final int DEFAULT_AUDIO_OUT_SOURCE   = 0xFFFFFFFF;
    private static final int DEFAULT_SAMPLING_RATE      = 8000; /* in Hz */
    private static final int DEFAULT_CHANNEL_IN_CONFIG  = AudioFormat.CHANNEL_IN_MONO;
    private static final int DEFAULT_CHANNEL_OUT_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    private static final int DEFAULT_AUDIO_FORMAT       = AudioFormat.ENCODING_PCM_16BIT;

    public static final byte INPUT_DEVICE_CONFIGURATION  = 0;
    public static final byte OUTPUT_DEVICE_CONFIGURATION = 1;
    
    /*-----------------------------------------
     * Class Attributes
     *---------------------------------------*/
    private int audio_source;
    private int sampling_rate;
    private int channel_config;
    private int audio_format;

    /**
     * Default Constructor for an audio
     * device configuration. The default
     * configuration is for an input device.
     */
    public Configuration()
    {
        setAll( DEFAULT_AUDIO_IN_SOURCE,
                DEFAULT_SAMPLING_RATE,
                DEFAULT_CHANNEL_IN_CONFIG,
                DEFAULT_AUDIO_FORMAT       );
    }   /* Configuration() */

    /**
     * Non-Default constructor for an audio
     * device configuration.
     * 
     * @param config_type
     *  Determines the type of configuration
     *  that will be initialized.
     *  
     * @throws RuntimeException
     *  Throws a runtime exception if the
     *  supplied parameter is not valid.
     *  <br /><br />
     *  Valid configuration types are:
     *  <ul>
     *    <li>INPUT_DEVICE_CONFIGURATION</li>
     *    <li>OUTPUT_DEVICE_CONFIGURATION</li>
     *  </ul>
     */
    public Configuration( byte config_type )
    {
        if( INPUT_DEVICE_CONFIGURATION == config_type )
        {
            setAll( DEFAULT_AUDIO_IN_SOURCE,
                    DEFAULT_SAMPLING_RATE,
                    DEFAULT_CHANNEL_IN_CONFIG,
                    DEFAULT_AUDIO_FORMAT       );
        }
        else if( OUTPUT_DEVICE_CONFIGURATION == config_type )
        {
            setAll( DEFAULT_AUDIO_OUT_SOURCE,
                    DEFAULT_SAMPLING_RATE,
                    DEFAULT_CHANNEL_OUT_CONFIG,
                    DEFAULT_AUDIO_FORMAT       );
        }
        else
        {
            throw new RuntimeException( "Supplied configuration type is not valid." );
        }
        
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
