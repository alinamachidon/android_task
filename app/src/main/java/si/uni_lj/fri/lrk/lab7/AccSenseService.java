package si.uni_lj.fri.lrk.lab7;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class AccSenseService extends IntentService implements SensorEventListener {

    public static final String TAG = "AccSenseService";

    private SensorManager mSensorManager;

    protected ArrayList<float[]> mSensorReadings;

    public AccSenseService() {
        super(TAG);
        Log.d(TAG, "AccSenseService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Log.d(TAG, "onCreate");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
            mSensorReadings = new ArrayList<>();
        }
    }

    private void getAccelerometer(SensorEvent event) {

        float[] values = event.values;
        float x = values[0];
        float y = values[1];
        float z = values[2];

        mSensorReadings.add(new float[]{x,y,z});
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "onSensorChanged");

        getAccelerometer(event);

        if (mSensorReadings.size() > 49) {

            // If there are 50 samples, we calculate mean, variance and mean crossing rate

            mSensorManager.unregisterListener(this);

            float mean = 0;
            float variance = 0;
            float mcr = 0;

            float totalIntensity = 0;
            float intensityList[] = new float[mSensorReadings.size()];

            for (int i=0; i<mSensorReadings.size(); i++) {
                float[] sample = mSensorReadings.get(i);
                float intensity = (float) Math.sqrt(Math.pow(sample[0], 2) + Math.pow(sample[1], 2) + Math.pow(sample[2], 2));
                intensityList[i] = intensity;
                totalIntensity += intensity;
            }
            if (mSensorReadings.size() > 0) {
                mean = totalIntensity/mSensorReadings.size();

                for (int i=0; i<mSensorReadings.size(); i++){
                    variance += Math.pow(intensityList[i] - mean, 2);

                    Log.d(TAG, "var one: "+Math.pow(intensityList[i] - mean, 2)+ " intensityList[i] "+intensityList[i]+" mean: "+mean);
                    if (i>0) {
                        if ((intensityList[i] - mean) * (intensityList[i-1] - mean) < 0){
                            mcr++;
                        }
                    }
                }

                variance = variance/mSensorReadings.size();

                if (mSensorReadings.size() > 1) {
                    mcr = mcr/(mSensorReadings.size()-1);
                }
            }

            // TODO: Broadcast the results (mean, variance, and MCR) to MainActivity by using
            //  a local broadcast with ACTION_SENSING_RESULT action

            Intent localIntent = new Intent(MainActivity.AccBroadcastReceiver.ACTION_SENSING_RESULT);
            localIntent.putExtra(MainActivity.AccBroadcastReceiver.MEAN, mean);
            localIntent.putExtra(MainActivity.AccBroadcastReceiver.VARIANCE, variance);
            localIntent.putExtra(MainActivity.AccBroadcastReceiver.MCR, mcr);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

            Log.d(TAG, "Broadcasting sensing results "+String.valueOf(mean) + ", "+String.valueOf(variance) + ", "+String.valueOf(mcr));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
