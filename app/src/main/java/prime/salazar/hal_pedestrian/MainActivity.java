package prime.salazar.hal_pedestrian;

import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;

import ca.uol.aig.fftpack.RealDoubleFFT;

public class MainActivity extends AppCompatActivity implements OnClickListener{

    boolean appStatus = false;

    int blockSize = 256;
    int sampleFreq = 48000;
    int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    GetAudioPeak getAudioPeak;
    RealDoubleFFT transformer;

    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) this.findViewById(R.id.button);
        startButton.setOnClickListener(this);

        transformer = new RealDoubleFFT(blockSize);
        Log.d("onCreate....","all good");
    }


    private class GetAudioPeak extends AsyncTask<Void, double[], Void> {

        @Override
        protected Void doInBackground(Void... params){
            try{

                int bufferSize = AudioRecord.getMinBufferSize(sampleFreq, channelConfig, audioEncoding);
                Log.d("Min Bufer Size:......", "" + bufferSize);

                AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                        sampleFreq, channelConfig, audioEncoding, bufferSize);

                short[] buffer = new short[bufferSize];
                double[] toTransform = new double[bufferSize];

                audioRecord.startRecording();

                while (appStatus){
                    int bufferRead = audioRecord.read(buffer,0, blockSize);

                    for(int i =0; i<blockSize && i<bufferRead; i++){
                        toTransform[i] = (double) buffer[i] / 32768.0;
                    }
                    transformer.ft(toTransform);
                    publishProgress(toTransform);
                }// while

                audioRecord.stop();
            } catch (Throwable t) {
                Log.e("AudioRecord......", "Recording Failed");
            }

            return null;
        }// doInBackground

        @Override
        protected void onProgressUpdate(double[]... toTransform)
        {
//            Log.v("Progress Update:...","" + toTransform);
        }//onProgressUpdate

    } // GetAudioPeak

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//
//        try {
//            audioRecord.stop();
//        } catch (IllegalStateException e) {
//            Log.e("Stop failed", e.toString());
//
//        }
//    }// onBackPressed

    public void onClick(View v)
    {
        Log.d("good1..............","");
        if(appStatus){
            appStatus = false;
            startButton.setText("Start");
            getAudioPeak.cancel(true);
        }
        else{
            appStatus = true;
            startButton.setText("End");
            getAudioPeak = new GetAudioPeak();
            getAudioPeak.execute();
        }
    }//onClick
} // MainActivity
