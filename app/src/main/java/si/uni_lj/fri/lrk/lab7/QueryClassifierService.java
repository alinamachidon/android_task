package si.uni_lj.fri.lrk.lab7;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;

import si.uni_lj.fri.lrss.machinelearningtoolkit.MachineLearningManager;
import si.uni_lj.fri.lrss.machinelearningtoolkit.classifier.Classifier;
import si.uni_lj.fri.lrss.machinelearningtoolkit.utils.Instance;
import si.uni_lj.fri.lrss.machinelearningtoolkit.utils.MLException;
import si.uni_lj.fri.lrss.machinelearningtoolkit.utils.Value;

import static si.uni_lj.fri.lrk.lab7.MainActivity.ACTION_CLASSIFIER_QUERY;



public class QueryClassifierService extends IntentService {

    public QueryClassifierService() {
        super("QueryClassifierService");
    }


    private static final String TAG = "QueryClassifierService";


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.d(TAG,"onHandleIntent");

        if (intent.getAction().equals(ACTION_CLASSIFIER_QUERY))
        {
            float accMean = intent.getFloatExtra("accMean", 0);
            float accVar = intent.getFloatExtra("accVar", 0);
            float accMCR = intent.getFloatExtra("accMCR", 0);
            queryClassifier(accMean, accVar, accMCR);
        }
    }


    void queryClassifier(Float mean, Float var, Float MCR)
    {
        Log.d(TAG,"queryClassifier with "+mean+" "+var+" "+MCR);

        
        ArrayList<Value> instanceValues = new ArrayList<Value>();
        Value meanValue = new Value((double)mean, Value.NUMERIC_VALUE);
        instanceValues.add(meanValue);
        Value varValue = new Value((double)var, Value.NUMERIC_VALUE);
        instanceValues.add(varValue);
        Value MCRValue = new Value((double)MCR, Value.NUMERIC_VALUE);
        instanceValues.add(MCRValue);
        // TODO: two other features should go here

        
        Instance instance = new Instance(instanceValues);
        
        // TODO: a try-catch block is needed here
        try {
            MachineLearningManager mManager = MachineLearningManager.getMLManager(getApplicationContext());
            Classifier c = mManager.getClassifier("movementClassifier");
            Value inference = c.classify(instance);

			String sv = inference.getValue().toString();
			Log.d(TAG, "classifier result: " + sv);

            Intent intent = new Intent ("ClassifierResult");
            intent.putExtra("class", sv);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        } catch (MLException e) {
            e.printStackTrace();
        }




    }



}
