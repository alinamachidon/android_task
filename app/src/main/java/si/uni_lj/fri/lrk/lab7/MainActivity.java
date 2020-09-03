package si.uni_lj.fri.lrk.lab7;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import si.uni_lj.fri.lrss.machinelearningtoolkit.MachineLearningManager;
import si.uni_lj.fri.lrss.machinelearningtoolkit.classifier.Classifier;
import si.uni_lj.fri.lrss.machinelearningtoolkit.utils.ClassifierConfig;
import si.uni_lj.fri.lrss.machinelearningtoolkit.utils.Constants;
import si.uni_lj.fri.lrss.machinelearningtoolkit.utils.Feature;
import si.uni_lj.fri.lrss.machinelearningtoolkit.utils.FeatureNominal;
import si.uni_lj.fri.lrss.machinelearningtoolkit.utils.FeatureNumeric;
import si.uni_lj.fri.lrss.machinelearningtoolkit.utils.Instance;
import si.uni_lj.fri.lrss.machinelearningtoolkit.utils.MLException;
import si.uni_lj.fri.lrss.machinelearningtoolkit.utils.Signature;
import si.uni_lj.fri.lrss.machinelearningtoolkit.utils.Value;

import static si.uni_lj.fri.lrk.lab7.MainActivity.AccBroadcastReceiver.ACTION_SENSING_RESULT;
import static si.uni_lj.fri.lrss.machinelearningtoolkit.utils.Value.*;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    public static final String ACTION_CLASSIFIER_TRAINING = "si.uni_lj.fri.lrk.lab7.TRAIN_CLASSIFIER";
    public static final String ACTION_CLASSIFIER_QUERY = "si.uni_lj.fri.lrk.lab7.QUERY_CLASSIFIER";

    AccBroadcastReceiver mBcastRecv;
    private Timer mTimer = null;

    // TODO: Uncomment MachineLearningManager declaration
    MachineLearningManager mManager;

    Handler handler;

    Boolean mSensing;
    Boolean mTraining;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBcastRecv = new AccBroadcastReceiver();

        this.handler = new Handler();

        final Button controlButton = findViewById(R.id.btn_control);

        mSensing = false;
        mTraining = false;

        controlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSensing) {
                    controlButton.setText(R.string.txt_start);
                    stopSensing();
                } else {
                    controlButton.setText(R.string.txt_stop);
                    startSensing();
                }
            }
        });

        Switch sw = findViewById(R.id.sw_training);

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mTraining = true;

                    findViewById(R.id.tv_select).setVisibility(View.VISIBLE);
                    findViewById(R.id.radioGroup).setVisibility(View.VISIBLE);
                    findViewById(R.id.tv_result).setVisibility(View.INVISIBLE);
                } else {
                    mTraining = false;

                    findViewById(R.id.tv_select).setVisibility(View.INVISIBLE);
                    findViewById(R.id.radioGroup).setVisibility(View.INVISIBLE);
                    findViewById(R.id.tv_result).setVisibility(View.VISIBLE);
                }
            }
        });

        initClassifier();

    }


    @Override
    protected void onStart() {
        super.onStart();

        // TODO: Register local broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SENSING_RESULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBcastRecv, filter);
    }


    @Override
    protected void onStop() {
        super.onStop();

        // TODO: Unregister local broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBcastRecv);

    }


    void startSensing() {
        Log.d(TAG, "startSensing()");

        mSensing = true;

        // TODO: set Handler to run AccSenseService every five seconds

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                startService(new Intent(MainActivity.this, AccSenseService.class));
            }
        };

        handler.postDelayed(runnable, 5000);
    }

    void stopSensing()
    {

        Log.d(TAG,"stopSensing()");

        mSensing = false;

        this.handler.removeMessages(0);

        View v  = findViewById(R.id.container);
        v.setBackgroundColor(Color.WHITE);
    }


    void initClassifier()
    {
        Log.d(TAG,"initClassifier");

        mManager = null;
        // TODO: Instantiate the classifier

        Feature accMean = new FeatureNumeric("accMean");
        Feature accVariance = new FeatureNumeric("accVariance");
        Feature accMCR = new FeatureNumeric("accMCR");
        // TODO: two more features should be defined here

        ArrayList<String> classValues = new ArrayList<String>();
        classValues.add(getResources().getString(R.string.txt_gesture_1));
        classValues.add(getResources().getString(R.string.txt_gesture_2));
        classValues.add(getResources().getString(R.string.txt_gesture_3));
        // TODO: one more class value should be added here

        Feature movement = new FeatureNominal("movement", classValues);
        ArrayList<Feature> features = new ArrayList<Feature>();
        features.add(accMean);
        features.add(accVariance);
        features.add(accMCR);
        // TODO: two other features should go here

        features.add(movement);
        Signature signature = new Signature(features, features.size()-1);

        // TODO: a try-catch block is needed here
        try {
            mManager = MachineLearningManager.getMLManager(getApplicationContext());
            mManager.addClassifier(Constants.TYPE_NAIVE_BAYES, signature,
                    new ClassifierConfig(),"movementClassifier");
        } catch (MLException e) {
            e.printStackTrace();
        }

    }


    public void recordAccData(float mean, float variance, float MCR) {

        Log.d(TAG, "recordAccData Intensity: " + mean + " var " + variance + " MCR " + MCR);

        Switch s = findViewById(R.id.sw_training);

        if (s.isChecked()) {

            // TODO: get the label of the selected radio button
            RadioGroup rg = findViewById(R.id.radioGroup);
            int selectedId = rg.getCheckedRadioButtonId();
            RadioButton rb = (RadioButton) findViewById(selectedId);
            String label = rb.getText().toString();

            // TODO: send data to TrainClassifierService
            Intent mIntent = new Intent(MainActivity.this,
                    TrainClassifierService.class);
            mIntent.putExtra("accMean", mean);
            mIntent.putExtra("accVar", variance);
            mIntent.putExtra("accMCR", MCR);
            mIntent.putExtra("label", label);
            mIntent.setAction(ACTION_CLASSIFIER_TRAINING);
            startService(mIntent);


        } else {
            // send data to QueryClassifierService
            Intent qIntent = new Intent(MainActivity.this,
                    QueryClassifierService.class);
            qIntent.putExtra("accMean", mean);
            qIntent.putExtra("accVar", variance);
            qIntent.putExtra("accMCR", MCR);
            qIntent.setAction(ACTION_CLASSIFIER_QUERY);
            startService(qIntent);
/*
            // TODO: Do the inference (classification) and set the result (also screen background colour)
            try {
                // TODO: a try-catch block is needed here
                //mManager = MachineLearningManager.getMLManager(getApplicationContext());
                Classifier c = mManager.getClassifier("movementClassifier");

                ArrayList<Value> instanceValues = new ArrayList<Value>();
                Value meanValue = new Value((double) mean, NUMERIC_VALUE);
                instanceValues.add(meanValue);
                Value varValue = new Value((double) variance, NUMERIC_VALUE);
                instanceValues.add(varValue);
                Value MCRValue = new Value((double) MCR, NUMERIC_VALUE);
                instanceValues.add(MCRValue);
                // TODO: add two more features here

                Instance instance1 = new Instance(instanceValues);

                Value inference = c.classify(instance1);

                String sv = inference.getValue().toString();
                Log.d(TAG, "classifier result: " + sv);

                TextView tvResult = (TextView) findViewById(R.id.tv_result);
                tvResult.setVisibility(View.VISIBLE);
                tvResult.setText(sv);

                View v = findViewById(R.id.container);

                if (sv.equals(getResources().getString(R.string.txt_gesture_1))) {
                    v.setBackgroundColor(Color.BLUE);
                }
                if (sv.equals(getResources().getString(R.string.txt_gesture_2))) {
                    v.setBackgroundColor(Color.RED);
                }
                if (sv.equals(getResources().getString(R.string.txt_gesture_3))) {
                    v.setBackgroundColor(Color.GREEN);
                }
            }
            catch(MLException e)
            {
                e.printStackTrace();
            }
*/

        }
    }


    public class AccBroadcastReceiver extends BroadcastReceiver {

        public static final String ACTION_SENSING_RESULT = "si.uni_lj.fri.lrk.lab7.SENSING_RESULT";
        public static final String MEAN = "mean";
        public static final String VARIANCE = "variance";
        public static final String MCR = "MCR";

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, " AccBroadcastReceiver onReceive...");

            float mean = intent.getFloatExtra(MEAN, 0);
            float variance = intent.getFloatExtra(VARIANCE, 0);
            float mcr = intent.getFloatExtra(MCR, 0);

            Log.d(TAG, "recordAccData Intensity: " + mean + " var " + variance + " MCR " + mcr);
            recordAccData(mean, variance, mcr);
        }
    }

    private BroadcastReceiver  bReceiver = new BroadcastReceiver(){
        public static final String QCLASS = "class";
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, " QueryBroadcastReceiver onReceive...");

            String queryResult = intent.getStringExtra(QCLASS);
            Log.d(TAG, "queryResult: " + queryResult);

            TextView tvResult = (TextView) findViewById(R.id.tv_result);
            tvResult.setVisibility(View.VISIBLE);
            tvResult.setText(queryResult);

            View v = findViewById(R.id.container);

            if (queryResult.equals(getResources().getString(R.string.txt_gesture_1))) {
                v.setBackgroundColor(Color.BLUE);
            }
            if (queryResult.equals(getResources().getString(R.string.txt_gesture_2))) {
                v.setBackgroundColor(Color.RED);
            }
            if (queryResult.equals(getResources().getString(R.string.txt_gesture_3))) {
                v.setBackgroundColor(Color.GREEN);
            }

        }
    };

    protected void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter("ClassifierResult"));
    }

    protected void onPause (){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
    }

}
