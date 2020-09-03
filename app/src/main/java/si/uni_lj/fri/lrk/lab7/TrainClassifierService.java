package si.uni_lj.fri.lrk.lab7;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.annotation.Nullable;

import si.uni_lj.fri.lrss.machinelearningtoolkit.MachineLearningManager;
import si.uni_lj.fri.lrss.machinelearningtoolkit.classifier.Classifier;
import si.uni_lj.fri.lrss.machinelearningtoolkit.utils.Instance;
import si.uni_lj.fri.lrss.machinelearningtoolkit.utils.MLException;
import si.uni_lj.fri.lrss.machinelearningtoolkit.utils.Value;

import static si.uni_lj.fri.lrk.lab7.MainActivity.ACTION_CLASSIFIER_TRAINING;



public class TrainClassifierService extends IntentService {

    public TrainClassifierService() {
        super("TrainClassifierService");
    }


    private static final String TAG = "TrainClassifierService";


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.d(TAG,"onHandleIntent");

        if (intent.getAction().equals(ACTION_CLASSIFIER_TRAINING))
        {
            float accMean = intent.getFloatExtra("accMean", 0);
            float accVar = intent.getFloatExtra("accVar", 0);
            float accMCR = intent.getFloatExtra("accMCR", 0);
            String label = intent.getStringExtra("label");
            trainClassifier(accMean, accVar, accMCR, label);
        }
    }


    void trainClassifier(Float mean, Float var, Float MCR, String label)
    {
        Log.d(TAG,"trainClassifier with "+mean+" "+var+" "+MCR+" "+label);

        // TODO: Train the classifier here; include mean, variance, and MCR
        ArrayList<Value> instanceValues = new ArrayList<Value>();
        Value meanValue = new Value((double)mean, Value.NUMERIC_VALUE);
        instanceValues.add(meanValue);
        Value varValue = new Value((double)var, Value.NUMERIC_VALUE);
        instanceValues.add(varValue);
        Value MCRValue = new Value((double)MCR, Value.NUMERIC_VALUE);
        instanceValues.add(MCRValue);
        // TODO: two other features should go here

        Value classValue = new Value(label, Value.NOMINAL_VALUE);
        instanceValues.add(classValue);
        Instance instance = new Instance(instanceValues);
        ArrayList<Instance> instances = new ArrayList<Instance>();

        try{
            instances.add(instance);
        }
        catch (Exception e) {
            Log.d(TAG,"TrainClassifierError "+e.getMessage());
        }

        // TODO: a try-catch block is needed here

        try {
            MachineLearningManager mManager = MachineLearningManager.getMLManager(getApplicationContext());
            Classifier c = mManager.getClassifier("movementClassifier");
            c.train(instances);
            c.printClassifierInfo();
        } catch (MLException e) {
            e.printStackTrace();
        }




    }



}
