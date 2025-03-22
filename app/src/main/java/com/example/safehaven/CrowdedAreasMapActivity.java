package com.example.safehaven;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.DirectionsApi;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CrowdedAreasMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_CODE = 100;
    private final List<LatLng> crowdedAreas = new ArrayList<>();
    private GeoApiContext geoApiContext;
    private LatLng currentLocation = null; // Store user's current location

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crowded_areas_map);

        // Initialize Google Maps API
        geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyDR5Be7tIv1kztQ4udXedI_iKiDW1xLdfs")
                .build();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        } else {
            fetchCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Cannot show your location.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    updateMapWithCurrentLocation();
                } else {
                    Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateMapWithCurrentLocation() {
        if (mMap == null || currentLocation == null) return;

        // Add marker for user's location
        mMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .title("Your Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        // Move camera to user's location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

        // Fetch and display crowded areas
        fetchNearbyCrowdedAreas();
    }

    private void fetchNearbyCrowdedAreas() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String overpassQuery = "[out:json];" +
                        "(" +
                        "node[amenity=restaurant](around:5000," + currentLocation.latitude + "," + currentLocation.longitude + ");" +
                        "node[amenity=bar](around:5000," + currentLocation.latitude + "," + currentLocation.longitude + ");" +
                        "node[shop=mall](around:5000," + currentLocation.latitude + "," + currentLocation.longitude + ");" +
                        ");" +
                        "out body;";

                String encodedQuery = java.net.URLEncoder.encode(overpassQuery, "UTF-8");
                String urlString = "https://overpass-api.de/api/interpreter?data=" + encodedQuery;

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                connection.disconnect();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray elements = jsonResponse.getJSONArray("elements");

                List<LatLng> newCrowdedAreas = new ArrayList<>();
                for (int i = 0; i < elements.length(); i++) {
                    JSONObject element = elements.getJSONObject(i);
                    double lat = element.getDouble("lat");
                    double lon = element.getDouble("lon");
                    LatLng placeLatLng = new LatLng(lat, lon);

                    String name = element.has("tags") && element.getJSONObject("tags").has("name")
                            ? element.getJSONObject("tags").getString("name")
                            : "Unknown Place";

                    newCrowdedAreas.add(placeLatLng);
                    handler.post(() -> {
                        mMap.addMarker(new MarkerOptions()
                                .position(placeLatLng)
                                .title(name)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    });
                }

                handler.post(() -> {
                    crowdedAreas.clear();
                    crowdedAreas.addAll(newCrowdedAreas);
                    if (!crowdedAreas.isEmpty()) {
                        findNearestCrowdedArea();
                    } else {
                        Toast.makeText(CrowdedAreasMapActivity.this, "No crowded areas found nearby", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                handler.post(() -> Toast.makeText(CrowdedAreasMapActivity.this,
                        "Failed to fetch places: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void findNearestCrowdedArea() {
        LatLng nearest = null;
        float minDistance = Float.MAX_VALUE;

        for (LatLng area : crowdedAreas) {
            float[] results = new float[1];
            Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                    area.latitude, area.longitude, results);
            if (results[0] < minDistance) {
                minDistance = results[0];
                nearest = area;
            }
        }

        if (nearest != null) {
            drawRoute(nearest);
        }
    }

    private void drawRoute(LatLng destination) {
        // Route drawing logic here (same as before)
    }
}
