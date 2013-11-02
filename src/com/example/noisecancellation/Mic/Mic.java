package com.example.equalizer.Mic;


import com.example.equalizer.FFT.Complex;

import android.annotation.SuppressLint;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.media.AudioFormat;
import android.util.Log;


@SuppressLint("NewApi")
public class Mic {
	private int audioSource = AudioSource.MIC;
	private int samplingRate = 8000; /* in Hz*/
	private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	
	public AudioRecord recorder;
	
	public int bufferSize; 	
	public byte[] recordData;	
	public byte[] windowData;
	public boolean isRecording;
	public boolean canUpdate = false;
	public int session;
	public Complex[] complexData;
	public Complex[] fftData;
	public byte[] data;
	public int[] db;
	public int[] hz;
	
	
	
	public void start() {
		bufferSize = AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat);
		bufferSize = 1024;
		complexData = null;
		recordData = new byte[bufferSize];
		//if (bufferSize < 4096) {
		//	bufferSize = 4096;
		//} else {
		//	bufferSize = 8192;
		//}
		try {
			if (recorder == null) {
				recorder = new AudioRecord(audioSource, samplingRate, channelConfig, audioFormat, bufferSize);	
				Log.i("info", "Recorder..............created....");
			}
			if(recorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
				
				Log.i("info", "Recorder..............initialize....");
				recordData = new byte[bufferSize];
				recorder = new AudioRecord(audioSource, samplingRate, channelConfig, audioFormat, bufferSize);	
			}
			if(recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
				
				recorder.startRecording();
				Log.i("info", "Recorder..............start....");
				isRecording = true;
				Log.i("info", "start.............");
				new recordThread(this);
				
			}
						
		} catch(IllegalStateException ie) {
			Log.i("error", "Could not create audio record object");
			
			
		}        
   	};  
	
	public void stop() {
		isRecording = false;		
		if(recorder != null) {
			if(recorder.getState() != AudioRecord.STATE_UNINITIALIZED) {
				recorder.stop();
				Log.i("info", "stop.............");
				recorder.release();
			}						
		} else {
			Log.i("error", "no audio record object");
		}
		
				      
    };    
    
    public byte[] getRecordData() {	
    
    	recorder.read(recordData, 0, bufferSize);
    	Log.i("data", "reading recording data..............");
    	do{		   
    		int size = recorder.read(recordData, 0, bufferSize);
    		
    		
    		windowData = new byte[size];
    		//apply hanning window 
    		for (int i = 0; i < size; i++)
    	    {
    			windowData[i] = (byte) (recordData[i] * 0.5 * (1.0 - Math.cos(2.0 * Math.PI * i / size)));
    	    }
    	  
    		complexData = this.getComplexData();
    		
    		fftData = com.example.equalizer.FFT.FFT.fft(complexData);
    		data = new byte[fftData.length];
    		db = new int[fftData.length];
    		hz = new int[fftData.length];
    		for (int i=0; i<fftData.length; i++) {
    			
    			double re = fftData[i].re();
    			double im = fftData[i].im();
    			data[i] = (byte)Math.sqrt((re * re) + (im * im));
    			int dbValue = (int) (10 * Math.log10(data[i]));
    			db[i] = dbValue;
    			
    			int freq = (i * samplingRate/bufferSize);
    			hz[i] = freq;
    	
    		 			
    			
    		}
    				
    		canUpdate = true;  	
    			
    		
    	}
    	while(isRecording);
    	canUpdate = false;
    	Log.i("data", "done recording data..............");
    	    	    	    	
    	return recordData;
	};
	
	public Complex[] getComplexData() {
		
		
		complexData = new Complex[windowData.length];
		for (int i = 0; i < windowData.length; i++) {
		    complexData[i] = new Complex(windowData[i], 0);
		}
		return complexData;
		
	}
	
	
};

class recordThread implements Runnable {
	Thread t;
	public byte[] data;
	public Complex[] cData;
	private Mic m;
	recordThread(Mic mic) {
		m = mic;
		t = new Thread(this, "recording thread");
		t.start();
	
	}
	public void run() {
		data = m.getRecordData();
						
	}
	
}

