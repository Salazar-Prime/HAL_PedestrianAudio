/**
 *
 * Author: Varun Aggarwal
 * Date of last update: 7/13/17
*/

package prime.salazar.hal_pedestrian;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import static java.lang.Integer.parseInt;
import ca.uol.aig.fftpack.RealDoubleFFT;

public class MainActivity extends Activity implements View.OnClickListener {

    int frequency = 48000;
    int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    private RealDoubleFFT transformer;
    int blockSize = 256;
    int markerFrequency = 19900;
    int markerX = (int)(2*blockSize*markerFrequency/frequency);
    int continuousFalseCount = 0;
    boolean freqFound = false;
//    double threshold;

    Button startStopButton;
    boolean started = false;
    LinearLayout bgElement;
    TextView stopSign;
//    EditText thresholdTextBox;
    RecordAudio recordTask;
    //
    ImageView imageView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    Paint paintBlock;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startStopButton = (Button) this.findViewById(R.id.StartStopButton);
        startStopButton.setOnClickListener(this);

        transformer = new RealDoubleFFT(blockSize);
        bgElement = (LinearLayout) findViewById(R.id.container);
        stopSign = (TextView) findViewById(R.id.textView);
//        thresholdTextBox = (EditText) findViewById(R.id.editText);
        imageView = (ImageView) this.findViewById(R.id.ImageView01);
        bitmap = Bitmap.createBitmap((int) 256, (int) 100,
                Bitmap.Config.ARGB_8888);
//        threshold = parseInt(thresholdTextBox.getText().toString());
        canvas = new Canvas(bitmap);
        paint = new Paint();
        paintBlock = new Paint();
        paintBlock.setColor(Color.RED);
        paint.setColor(Color.GREEN);
        imageView.setImageBitmap(bitmap);
    }

    private class RecordAudio extends AsyncTask<Void, double[], Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                int bufferSize = AudioRecord.getMinBufferSize(frequency,
                        channelConfiguration, audioEncoding);
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        // WORK on UI thread here
//
//                    }
//                });

                AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, frequency,
                        channelConfiguration, audioEncoding, bufferSize);

                short[] buffer = new short[blockSize];
                double[] toTransform = new double[blockSize];

                audioRecord.startRecording();

                while (started) {
                    int bufferReadResult = audioRecord.read(buffer, 0,
                            blockSize);

                    for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                        toTransform[i] = (double) buffer[i] / 32768.0; // signed 16 bit\
                    }

                    transformer.ft(toTransform);
                    publishProgress(toTransform);
                }

                audioRecord.stop();
            } catch (Throwable t) {
                Log.e("AudioRecord", "Recording Failed");
            }

            return null;
        }

        protected void onProgressUpdate(double[]... toTransform) {
//            threshold = parseInt(thresholdTextBox.getText().toString());
            canvas.drawColor(Color.BLACK);
            canvas.drawLine(markerX, 50, markerX, 100, paintBlock);
            if(continuousFalseCount > 250)
                stopSign.setVisibility(View.INVISIBLE);

            for (int i = 0; i < toTransform[0].length; i++) {
                int x = i;
                int downy = (int) (100 - (toTransform[0][i] * 10));
                int upy = 100;
                if(i>markerX && toTransform[0][i] > 0.25) {
//                    bgElement.setBackgroundColor(Color.RED);
                    stopSign.setVisibility(View.VISIBLE);
                    continuousFalseCount = 0;
                    freqFound = true;
//                    canvas.drawColor(Color.RED);
                }// if
                canvas.drawLine(x, downy, x, upy, paint);
            }// for(i)
            if(!freqFound)
                continuousFalseCount++;
            freqFound = false;
            imageView.invalidate();
        }//onProgressUpdate
    }// RecordAudio

    public void onClick(View v) {
        if (started) {
            started = false;
            startStopButton.setText("Start");
            recordTask.cancel(true);
        } else {
            started = true;
            startStopButton.setText("Stop");
            recordTask = new RecordAudio();
            recordTask.execute();
        }
    }
}