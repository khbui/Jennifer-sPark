package com.example.jennifers.jenniferspark;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class Navigation extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyBKAGRDeGnHhi0gN3tYCAVXEgiwVvXGcr8";
    private FirebaseAuth mAuth;
    private GoogleMap mMap;
    private LatLng origin;
    private String destination;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private boolean isFinish;
    private ProgressDialog progressDialog;
    private TextView distance, duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initialize();

    }

    private void initialize() {
        Bundle extras = getIntent().getExtras();
        try {
            destination = URLEncoder.encode(extras.getString("destination"), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        mAuth = FirebaseAuth.getInstance();
        isFinish = false;
        distance = (TextView) findViewById(R.id.tvDistance);
        duration = (TextView) findViewById(R.id.tvDuration);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Finding Path...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    //Inflate the menu on Activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sub_menu, menu);
        return true;
    }

    //Set up options for menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.Profile:
                startActivity(new Intent(this, Profile.class));
                return true;
            case R.id.SignOut:
                Toast.makeText(this, "You have signed out", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                startActivity(new Intent(this, Login.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        buildGoogleApiClient();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (!isFinish) {
            if (origin == null) {
                origin = latLng;
            } else {

                String url = DIRECTION_URL_API + "origin=" + origin.latitude + "," + origin.longitude + "&destination=" + destination + "&key=" + GOOGLE_API_KEY;

                new DownloadDirection().execute(url);
            }
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
    }

    /*
  *Build Google API on client side
  **/
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private class DownloadDirection extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String link = params[0];
            try {
                URL url = new URL(link);
                InputStream is = url.openConnection().getInputStream();
                StringBuffer buffer = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");

                }

                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            try {

                translationData(res);
                isFinish = true;
                progressDialog.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void translationData(String data) throws JSONException {
        if (data == null)
            return;

        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        JSONObject jsonRoute = jsonRoutes.getJSONObject(0);

        JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
        JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
        JSONObject jsonLeg = jsonLegs.getJSONObject(0);
        JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
        JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
        JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
//        JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

//            route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
//            route.duration = new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
//            route.endAddress = jsonLeg.getString("end_address");
//            route.startAddress = jsonLeg.getString("start_address");
//            route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
//            route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
//        duration.setText(jsonDuration.getString("text"));
//        distance.setText(jsonDistance.getString("text"));
        List<LatLng> polypoints = decodePolyLine(overview_polylineJson.getString("points"));
        PolylineOptions polylineOptions = new PolylineOptions().
                geodesic(true).
                width(10);
        for (LatLng l : polypoints) {
            polylineOptions.add(l);
        }

        mMap.addPolyline(polylineOptions);
        mMap.addMarker(new MarkerOptions().title(jsonLeg.getString("end_address")).position(new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"))));

    }

    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }
        return decoded;
    }
}