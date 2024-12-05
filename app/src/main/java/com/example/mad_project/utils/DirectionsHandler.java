package com.example.mad_project.utils;

import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DirectionsHandler {

    private static final String TAG = "DirectionsHandler";
    private final GoogleMap mMap;
    private final FragmentActivity context;
    private final String apiKey;
    private final ExecutorService executorService;
    private List<LatLng> currentRoutePoints;

    public DirectionsHandler(GoogleMap map, FragmentActivity context, String apiKey) {
        this.mMap = map;
        this.context = context;
        this.apiKey = apiKey;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void displayRoute(String origin, String destination) {
        String urlString = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin +
                "&destination=" + destination +
                "&key=" + apiKey;

        FetchDirectionsTask fetchDirectionsTask = new FetchDirectionsTask(urlString);
        Future<List<LatLng>> future = executorService.submit(fetchDirectionsTask);

        executorService.execute(() -> {
            try {
                List<LatLng> routePoints = future.get();
                context.runOnUiThread(() -> onPostExecute(routePoints));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching directions: ", e);
            }
        });
    }

    private void onPostExecute(List<LatLng> routePoints) {
        if (!routePoints.isEmpty()) {
            drawRoute(routePoints);

            LatLngBounds bounds = getRouteBounds();
            int padding = 100; // Padding around the route
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

            Toast.makeText(context, "Route displayed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "No route found", Toast.LENGTH_SHORT).show();
        }
    }

    public LatLngBounds getRouteBounds() {
        if (currentRoutePoints == null || currentRoutePoints.isEmpty()) {
            return null;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : currentRoutePoints) {
            builder.include(point);
        }
        return builder.build();
    }

    private void drawRoute(List<LatLng> routePoints) {
        if (routePoints != null && !routePoints.isEmpty()) {
            this.currentRoutePoints = routePoints;  // Store the route points
            
            // Draw polyline
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.addAll(routePoints);
            mMap.addPolyline(polylineOptions);

            // Add markers for origin and destination
            LatLng originLatLng = routePoints.get(0);
            LatLng destinationLatLng = routePoints.get(routePoints.size() - 1);
            mMap.addMarker(new MarkerOptions()
                .position(originLatLng)
                .title("Origin")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mMap.addMarker(new MarkerOptions()
                .position(destinationLatLng)
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }

    private record FetchDirectionsTask(String urlString) implements Callable<List<LatLng>> {

        @Override
            public List<LatLng> call() {
                List<LatLng> routePoints = new ArrayList<>();

                try {
                    JSONArray routes = getJsonArray();

                    if (routes.length() > 0) {
                        JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");

                        for (int i = 0; i < legs.length(); i++) {
                            JSONArray steps = legs.getJSONObject(i).getJSONArray("steps");
                            for (int j = 0; j < steps.length(); j++) {
                                JSONObject step = steps.getJSONObject(j);
                                String polyline = step.getJSONObject("polyline").getString("points");
                                routePoints.addAll(decodePolyline(polyline));
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching directions: ", e);
                }

                return routePoints;
            }

        private JSONArray getJsonArray() throws IOException, JSONException {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            InputStream in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String json = sb.toString();

            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.getJSONArray("routes");
        }

        private List<LatLng> decodePolyline(String encodedPath) {
                List<LatLng> poly = new ArrayList<>();
                int len = encodedPath.length();
                int index = 0;
                int lat = 0, lng = 0;

                while (index < len) {
                    int b, shift = 0, result = 0;
                    do {
                        b = encodedPath.charAt(index++) - 63;
                        result |= (b & 0x1f) << shift;
                        shift += 5;
                    } while (b >= 0x20);
                    int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                    lat += dlat;

                    shift = 0;
                    result = 0;
                    do {
                        b = encodedPath.charAt(index++) - 63;
                        result |= (b & 0x1f) << shift;
                        shift += 5;
                    } while (b >= 0x20);
                    int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                    lng += dlng;

                    LatLng p = new LatLng((lat / 1E5), (lng / 1E5));
                    poly.add(p);
                }
                return poly;
            }
        }
}