package com.example.geofencing;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity {
    private GeofencingClient geofencingClient;
    private TextView statusTextView;
    private TextView gpsstatusTextView;
    private LocationManager locationManager;
    private BroadcastReceiver gpsStatusReceiver;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusTextView = findViewById(R.id.statusTextView);
        gpsstatusTextView = findViewById(R.id.gpsstatusTextView);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        updateGpsStatus();

        gpsStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateGpsStatus();
            }
        };

        registerReceiver(gpsStatusReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

        geofencingClient = LocationServices.getGeofencingClient(this);

        Geofence geofence = new Geofence.Builder()
                .setRequestId("myGeofenceID")
                .setCircularRegion(
                        37.4219983, -122.084,
                        100
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

        Intent intent = new Intent(this, GeofenceReceiver.class);
        intent.setAction("com.example.ACTION_GEOFENCE_EVENT");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling ActivityCompat#requestPermissions
            return;
        }

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(aVoid -> {
                    statusTextView.setText("Geofence añadido");
                })
                .addOnFailureListener(e -> {
                    statusTextView.setText("Error al añadir Geofence: " + e.getMessage());
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gpsStatusReceiver != null) {
            unregisterReceiver(gpsStatusReceiver);
        }
    }

    private void updateGpsStatus() {
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGpsEnabled) {
            gpsstatusTextView.setText("GPS habilitado");
        } else {
            gpsstatusTextView.setText("GPS deshabilitado");
        }
    }
}
