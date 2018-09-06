package com.example.jennifers.jenniferspark;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class Map extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    /**
     * CODE
     */
    private static final int LOCATION_PERMISSIONS_CODE = 1;
    private static final int PLACE_AUTO_COMPLETE_CODE = 11;
    /**
     * Google Map and related
     */
    private GoogleMap mMap;
    private Location mLocation;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    //List stores all markers on map
    private List<Marker> markerList;
    private Geocoder geocoder;
    /**
     * Firebase
     */
    private FirebaseAuth mAuth;
    /**
     * Android(Java)
     */
    private ImageButton imageButton;
    private Button exitsearchmodebtn;
    private User currentUser;
    //This variable keeps track area for parking lot information. If users' area change,
    //parking lot list is updated to retrieve new parking lot information from database for new area
    private ProgressDialog progressDialog;
    private String localcity;
    //List stores all parking lot in specific area
    private List<Parking> parkingList;
    //This variable keep track search mode
    private boolean isSearch;
    private boolean isFetched;
    private boolean isStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initialize();

    }

    @Override
    public void onPause() {
        super.onPause();
        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    /**
     * Manipulates the map once available. This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia. If Google Play services is not installed on the device,
     * the user will be prompted to install it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    /*
    * Create main menu and set up option for the menu
    **/
    //Inflate the menu on Activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
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
            case R.id.AddParkingLot:
                if (currentUser.getIsAdmin() == 0)
                    Toast.makeText(getApplicationContext(), "This feature requires administration permission", Toast.LENGTH_LONG).show();
                else
                    startActivity(new Intent(this, AddParkingLot.class));
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

    /*
    * HELPER METHODS
    * */
    //Get current authorized user information
    private void getCurrentUserInfo() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //Get parking lot around current location
    private void getParkingList(LatLng latLng) {

        try {
            List<Address> addresses;
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            String child = addresses.get(0).getLocality() + addresses.get(0).getAdminArea();
            parkingList.clear();
            for (Marker m : markerList) {
                m.remove();
            }
            markerList.clear();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Parkings").child(child);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        Parking temp = d.getValue(Parking.class);
                        parkingList.add(temp);
                    }
                    isFetched = true;
                    displayParkingLot();
                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void displayParkingLot() {

        List<Address> addresses;
        for (Parking p : parkingList) {
            try {
                addresses = geocoder.getFromLocationName(p.toString(), 1);
                Address location = addresses.get(0);
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                Marker pmaker = mMap.addMarker(new MarkerOptions().position(latLng).title(p.getTitle()).snippet("Tap for action"));
                markerList.add(pmaker);
                pmaker.setTag(p);
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        final Parking temp = (Parking) marker.getTag();
                        if (temp != null) {
                            AlertDialog.Builder confirmDialog = new AlertDialog.Builder(Map.this);
                            String tempaddress = temp.getAddress() + "\n" + temp.getCity() + ", " + temp.getState() + " " + temp.getZipcode();
                            confirmDialog.setTitle(temp.getTitle()).setMessage(tempaddress + "\n\n" + temp.getDescription());
                            confirmDialog.setCancelable(false);
                            confirmDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //Do nothing
                                }
                            });
                            confirmDialog.setPositiveButton("Direction", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Map.this, Navigation.class);
                                    intent.putExtra("destination", temp.toString());

                                    intent.putExtra("origin", mLocation);
                                    startActivity(intent);
                                }
                            });
                            Dialog dialogConfirm = confirmDialog.create();
                            dialogConfirm.show();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Initial setup when activity loaded
    private void initialize() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //Get current authorized user information. No authorized user, go to Login Activity
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(Map.this, Login.class));
        } else {
            getCurrentUserInfo();
        }
        //Set up geocoder to retrive address
        geocoder = new Geocoder(this, Locale.getDefault());
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();
        //Other components on Map Activity
        parkingList = new ArrayList<Parking>();
        markerList = new ArrayList<Marker>();
        localcity = "";
        isStarted = true;
        isSearch = false;
        isFetched = false;
        //Set up action on buttons
        exitsearchmodebtn = (Button) findViewById(R.id.exitsearchmodebtn);
        exitsearchmodebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(Map.this,Map.class));
            }
        });
        exitsearchmodebtn.setVisibility(View.GONE);
        imageButton = (ImageButton) findViewById(R.id.searchimgbtn);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(Map.this);
                    startActivityForResult(intent, PLACE_AUTO_COMPLETE_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    /*
    *OVERRIDE METHODS
    * */
    //Check permission to access device location
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(Map.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        LOCATION_PERMISSIONS_CODE);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSIONS_CODE);
            }
        }
    }

    //Respone to location access permission request
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    //Respone to search feature. User input an entry and search bar will autocomplete
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //autocompleteFragment.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTO_COMPLETE_CODE) {
            if (resultCode == RESULT_OK) {
                exitsearchmodebtn.setVisibility(View.VISIBLE);
                isSearch = true;
                Place place = PlaceAutocomplete.getPlace(this, data);
                LatLng latLng = place.getLatLng();
                //move map camera
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));

                try {
                    List<Address> addresses;

                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    String child = addresses.get(0).getLocality() + addresses.get(0).getAdminArea();

                    if (!localcity.equals(child)) {
                        isFetched = false;
                        localcity = child;
                    }
                    if (!isFetched) {
                        progressDialog.setMessage("Loading");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        getParkingList(latLng);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
            } else if (requestCode == RESULT_CANCELED) {

            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (!isSearch) {
            try {
                List<Address> addresses;
                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                String child = addresses.get(0).getLocality() + addresses.get(0).getAdminArea();

                if (!localcity.equals(child)) {
                    isFetched = false;
                    localcity = child;
                }
                if (!isFetched) {
                    getParkingList(latLng);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //move map camera
            if (isStarted) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
                isStarted = false;
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

}
