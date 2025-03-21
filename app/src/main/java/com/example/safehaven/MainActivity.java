package com.example.safehaven;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int SMS_PERMISSION_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private static final float SHAKE_THRESHOLD = 12.0f;
    private static final int TIME_THRESHOLD = 1000;

    private SharedPreferences sharedPreferences;
    private LocationManager locationManager;
    private List<String> selectedContacts;
    private Location currentLocation;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;
    private float lastX, lastY, lastZ;

    private Handler holdHandler = new Handler();
    private Runnable sendSOSRunnable;
    private boolean isShakeDetectionActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();

        Button sosButton = findViewById(R.id.sos_button);
        Button stopShakingButton = findViewById(R.id.stop_shaking_service);
        Button btnSelectContact = findViewById(R.id.btn_set_sos);
        LinearLayout basicLaw = findViewById(R.id.four);
        LinearLayout selfDefense = findViewById(R.id.two);
        LinearLayout fakeCall = findViewById(R.id.one);

        btnSelectContact.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SelectContactsActivity.class)));
        basicLaw.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LawsActivity.class)));
        selfDefense.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DefenseActivity.class)));
        fakeCall.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FakeCallActivity.class)));

        // Tap SOS button -> Start Shake Detection
        sosButton.setOnClickListener(v -> startShakeDetectionService());

        // Long Press on SOS button -> Send SOS
        sosButton.setOnLongClickListener(v -> {
            sendSOSMessage();
            return true;
        });

        // Stop Shake Detection
        stopShakingButton.setOnClickListener(v -> stopShakeDetectionService());

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            currentLocation = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    public void sendSOSMessage() {
        sharedPreferences = getSharedPreferences("SafeHavenContacts", Context.MODE_PRIVATE);
        selectedContacts = new ArrayList<>(sharedPreferences.getStringSet("sos_contacts", new HashSet<>()));

        if (selectedContacts.isEmpty()) {
            Toast.makeText(this, "No SOS contacts selected. Please set emergency contacts.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentLocation != null) {
            double latitude = currentLocation.getLatitude();
            double longitude = currentLocation.getLongitude();
            String message = "SOS: I am in trouble, I need help! My location: http://maps.google.com/?q=" + latitude + "," + longitude;

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                SmsManager smsManager = SmsManager.getDefault();
                try {
                    for (String contact : selectedContacts) {
                        smsManager.sendTextMessage(contact, null, message, null, null);
                    }
                    Toast.makeText(this, "SOS message sent to selected contacts", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to send message!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Permission not granted to send SMS.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Location not available. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startShakeDetectionService() {
        if (!isShakeDetectionActive) {
            isShakeDetectionActive = true;
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            }
            Toast.makeText(this, "Shake detection started", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopShakeDetectionService() {
        if (isShakeDetectionActive) {
            isShakeDetectionActive = false;
            sensorManager.unregisterListener(this);
            Toast.makeText(this, "Shake detection stopped", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isShakeDetectionActive && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float deltaX = Math.abs(x - lastX);
            float deltaY = Math.abs(y - lastY);
            float deltaZ = Math.abs(z - lastZ);

            if (deltaX > SHAKE_THRESHOLD || deltaY > SHAKE_THRESHOLD || deltaZ > SHAKE_THRESHOLD) {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastShakeTime) > TIME_THRESHOLD) {
                    lastShakeTime = currentTime;
                    sendSOSMessage();
                }
            }

            lastX = x;
            lastY = y;
            lastZ = z;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
