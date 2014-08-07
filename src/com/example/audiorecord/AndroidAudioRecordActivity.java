package com.example.audiorecord;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidAudioRecordActivity extends Activity {
	
	public int sum(ArrayList<Integer> a){
		int sum=0;
		for(int i=0;i<a.size();i++){
			sum=sum+a.get(i);
		}
		return sum;
	}
	
	public int next(int i){
		if(i<dur-1){
			return i+1;
		}
		else{
			return 0;
		}
	}
	
	private int count = 0;
	private TextView setIt;
	private double val=0;
	private int bufferSize = 0;
	private AudioRecord recorder = null;
	private boolean isRecording;
	private Thread recordingThread = null;
	private static ArrayList mem;
	private boolean israllowed= true;
	
	private static int threshold = 90;
	private static int dur = 4;
	private static int counter=0;
	private static final int RECORDER_SAMPLERATE = 16000;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	
	
	int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
	int BytesPerElement = 2; // 2 bytes in 16bit format
	
	MediaPlayer mediaPlayer;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		mediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.soho);
		super.onCreate(savedInstanceState);
        mem=new ArrayList();
        
        setContentView(R.layout.activity_android_audio_record);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        threshold=Integer.parseInt(prefs.getString("threshold", "90"));
        dur=Integer.parseInt(prefs.getString("time", "4"));
        for(int i=0;i<dur;i++){
            mem.add(0);
            }
        
        ((Button)findViewById(R.id.btnStart)).setOnClickListener(btnClick);
        ((Button)findViewById(R.id.btnStop)).setOnClickListener(btnClick);
    	for(int i=0;i<dur;i++){
    		mem.set(i,0);
    	}
        enableButtons(true, false);
        
        //get the minimum buffer size from AudioRecord using sampleRate, channels, and encoding
        //minbuffer required to create AudioRecord Object
         bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
    }
	
	protected void onResume(){
		super.onResume();
		MediaPlayer mediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.soho);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        threshold=Integer.parseInt(prefs.getString("threshold", "90"));
        dur=Integer.parseInt(prefs.getString("time", "4"));
        mem=new ArrayList();
        for(int i=0;i<dur;i++){
            mem.add(0);
            }
        
        counter=0;
	}
	
	public void LaunchConfigureScreen(View v){
		startActivity(new Intent(this,ConfigureScreen.class));
	}
	
	private View.OnClickListener btnClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()){
				case R.id.btnStart:{
					//AppLog.logString("Start Recording");
					enableButtons(false, true);
					startRecording();
							
					break;
				}
				case R.id.btnStop:{
					//AppLog.logString("Start Recording");
					enableButtons(true, false);
					stopRecording();
					
					break;
				}
			}
		}
	};  
	
	
	private void enableButtons(boolean start, boolean stop)
	{
		((Button)findViewById(R.id.btnStart)).setEnabled(start);
		((Button)findViewById(R.id.btnStop)).setEnabled(stop);
		
	}
	

	/*
	 * Converting short to bytes
	 */
	private byte[] short2byte(short[] sData)
	{
	    int shortArrsize= sData.length;
	    byte[] bytes = new byte[shortArrsize*2];
	
	    for (int i=0; i < shortArrsize; i++)
	    {
	        bytes[i*2] = (byte) (sData[i] & 0x00FF);
	        bytes[(i*2)+1] = (byte) (sData[i] >> 8);
	        sData[i] = 0;
	    }
	    return bytes;
	
	 }
	
	/*
	 * on clicking START recording will be started and audio Data is written to the 
	 * original file. DataToFile is done in a separate threads
	 */
	private void startRecording(){
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
						RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);
		recorder.startRecording();

		isRecording = true;
		
		recordingThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				//call computeEnergy function here
				//computeEnergy();
					StartListening();
				}
			
		},"AudioRecorder Thread");

		recordingThread.start();
	}
	/*
	 * on clicking STOP the recorder is set to null
	 * and also the temp file are saved to original file and 
	 * delete the temp files
	 */
	private void stopRecording(){
		if(null != recorder){
			isRecording = false;
			
			recorder.stop();
			recorder.release();
			
			recorder = null;
			recordingThread = null;
		}
	}
	
	/*
	 * this function is to write the audioData to original File.
	 * this function first gets the filename = TempFileName
	 * recorder.read(buffer, 0, size) stores the audioData from h/w to buffer
	 * and write that to FileOutputStream Object
	 * 
	 */
	private void StartListening() {
		short sData[] = new short[bufferSize];
		int read = 0;
			while(isRecording) {
				if(israllowed){
					recorder.read(sData, 0, bufferSize);
					//byte bData[] = short2byte(sData);
					computeEnergy(sData);
				}
				else {
					try {
						recordingThread.sleep(4000);
						israllowed = true;
						mem=new ArrayList();
				        for(int i=0;i<dur;i++){
				            mem.add(0);
				            }
				        
					} catch (InterruptedException e) {
						
					}
				}
//				recorder.read(sData, 2, BufferElements2Rec);
//				//bData = short2byte(sData);
//				computeEnergy(sData);
//				recorder.read(sData, 4, BufferElements2Rec);
//				//bData = short2byte(sData);
//				computeEnergy(sData);
//				recorder.read(sData, 6, BufferElements2Rec);
//				//bData = short2byte(sData);
//				computeEnergy(sData);
			}	
	}
	
	/*
	 * Computing the Energy
	 */
	private void computeEnergy(final short[] dat)
	{	
		setIt = (TextView) findViewById(R.id.editIt);;
		runOnUiThread(new Runnable() {
	        public void run() {
	            try{
	            	double val1=calculate(dat,0,BufferElements2Rec);
	            	double val2=calculate(dat,1*(BufferElements2Rec/4),BufferElements2Rec);
	            	double val3=calculate(dat,2*(BufferElements2Rec/4),BufferElements2Rec);
	            	double val4=calculate(dat,3*(BufferElements2Rec/4),BufferElements2Rec);
	            	val= (val1+val2+val3+val4)/4;
	            	if(val<threshold){
	            		mem.set(counter,0);
	            		counter=next(counter);
	            		if(sum(mem)<(dur*3)/4){
		            		setIt.setText("Level=" + sum(mem) +"\n("+val+")");
		            	}
		            	else{
		            		setIt.setText("Exceeded Level! =" + sum(mem) +"\n("+val+")");
		            		israllowed=false;
		            		mediaPlayer.start();
		            	}
	            	}
	            	else{
	            		mem.set(counter,1);
	            		counter=next(counter);
	            		if(sum(mem)<(dur*3)/4){
		            		setIt.setText("Level=" + sum(mem) +"\n("+val+")");
		            	}
		            	else{
		            		setIt.setText("Exceeded Level! =" + sum(mem) +"\n("+val+")");
		            		israllowed=false;
		            		mediaPlayer.start();
		            		
		            	}
	            	}
	            	
	            	
	            }catch (Exception e) {}
	        }
	    });	
	}
	
	/*
	 * Calculation Formula
	 */
    public final static double calculate(short[] sdata,int offset ,int samples){
    	   double sum = 0;
    	   for (int i = 0; i < samples; i++) {
	    	   double v = sdata[i+offset]*sdata[i+offset];
	    	   sum += v; 
    	   }
    	   return 10*Math.log10(sum); 
   }

	
}
