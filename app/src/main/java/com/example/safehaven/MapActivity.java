package com.example.safehaven;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static GoogleMap mMap;
    private double currentLatitude = 17.385044;  // Placeholder: Use the current location
    private double currentLongitude = 78.486671;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize Google Map
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Fetch nearest police stations
        fetchNearbyPoliceStations();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set initial camera position to user's location
        LatLng userLocation = new LatLng(currentLatitude, currentLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12));
    }

    // Method to fetch nearest police stations using Overpass API
    private void fetchNearbyPoliceStations() {
        String overpassUrl = "http://overpass-api.de/api/interpreter?data=[out:json];(node[amenity=police](around:1000," + currentLatitude + "," + currentLongitude + "););out;";

        // Make HTTP request to Overpass API
        new FetchNearbyPoliceStationsTask().execute(overpassUrl);
    }

    // Static AsyncTask class to avoid memory leaks
    private static class FetchNearbyPoliceStationsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String overpassUrl = params[0];
            try {
                URL url = new URL(overpassUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Log.d("Overpass API Response", result);  // Log the response for debugging

                try {
                    // Parse JSON response from Overpass API
                    JSONObject jsonResponse = new JSONObject(result);
                    JSONArray elements = jsonResponse.getJSONArray("elements");

                    if (elements.length() == 0) {
                        Log.d("Nearby Police Stations", "No police stations found in the area.");
                    }

                    // Loop through the elements and add markers for police stations to the map
                    for (int i = 0; i < elements.length(); i++) {
                        JSONObject element = elements.getJSONObject(i);
                        double lat = element.getDouble("lat");
                        double lon = element.getDouble("lon");
                        String name = element.optString("tags", "Police Station");

                        // Add a marker for each nearby police station
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lat, lon))
                                .title(name));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("Overpass API Error", "No response from the API");
            }
        }
    }
}
