package com.jcrawley.simplecompass;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private ImageView compassImageView;
    private float startingDegrees =0f;
    private TextView degreesTextView;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private float[] gravity;
    private float[] geomagnetic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compassImageView = findViewById(R.id.compassImageView);
        degreesTextView = findViewById(R.id.degreesTextView);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }


    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }


    public void onSensorChanged(SensorEvent event) {
        assignGravityAndGeomagneticValues(event);
        float azimuth = getAzimuthFromSensorReadings();
        float degrees = getDegreesFromAzimuth(azimuth);
        rotateGraphic(degrees);
        updateTextView(degrees);
    }


    private void assignGravityAndGeomagneticValues(SensorEvent event){
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            gravity = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values;
        }
    }


    private float getDegreesFromAzimuth(float azimuth){
        return (float)Math.toDegrees(azimuth) + 180;
    }


    private float getAzimuthFromSensorReadings(){
        if (gravity != null && geomagnetic != null) {
            float[] r = new float[9];
            if (SensorManager.getRotationMatrix(r, new float[9], gravity, geomagnetic)) {
                float[] orientationArray = new float[3];
                SensorManager.getOrientation(r, orientationArray);
                return orientationArray[0]; // orientation contains: azimuth, pitch and roll
            }
        }
        return 0f;
    }


    private void rotateGraphic(float currentDegreesReading){
        RotateAnimation ra = new RotateAnimation(
                startingDegrees,
                -currentDegreesReading,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        ra.setFillAfter(true);
        ra.setDuration(210);
        compassImageView.startAnimation(ra);
        startingDegrees = -currentDegreesReading;
    }


    private void updateTextView(float degrees){
        int currentDegreesReading = Math.round(degrees);
        degreesTextView.setText(getString(R.string.degrees_string, currentDegreesReading));
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

}