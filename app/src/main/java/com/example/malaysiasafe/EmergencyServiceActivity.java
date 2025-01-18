package com.example.malaysiasafe;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class EmergencyServiceActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView orientationDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_emergency_service);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize orientation display TextView
        orientationDisplay = findViewById(R.id.orientation_display);

        // Initialize SensorManager and Accelerometer
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Set onClick listeners for call buttons
        setupCallButton(R.id.call_pdrm, "tel:999"); // Polis Diraja Malaysia
        setupCallButton(R.id.call_bomba, "tel:994"); // Bomba Malaysia
        setupCallButton(R.id.call_apam, "tel:991"); // Angkatan Pertahanan Awam
        setupCallButton(R.id.call_rela, "tel:112"); // Jabatan Sukarelawan

        // Add intent for Reporting button
        Button reportButton = findViewById(R.id.go_report);
        reportButton.setOnClickListener(v -> {
            Intent intent = new Intent(EmergencyServiceActivity.this, DisplayReportActivity.class);
            startActivity(intent);
        });
    }

    private void setupCallButton(int buttonId, String phoneNumber) {
        Button callButton = findViewById(buttonId);
        callButton.setOnClickListener(v -> {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse(phoneNumber));
            startActivity(dialIntent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the accelerometer listener
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the accelerometer listener
        if (accelerometer != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0]; // Acceleration along x-axis
            float y = event.values[1]; // Acceleration along y-axis

            if (Math.abs(y) > Math.abs(x)) {
                if (y > 0) {
                    orientationDisplay.setText("Orientation: Portrait (Normal)");
                } else {
                    orientationDisplay.setText("Orientation: Portrait (Upside Down)");
                }
            } else {
                if (x > 0) {
                    orientationDisplay.setText("Orientation: Landscape (Right Side Up)");
                } else {
                    orientationDisplay.setText("Orientation: Landscape (Left Side Up)");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used in this implementation
    }
}
