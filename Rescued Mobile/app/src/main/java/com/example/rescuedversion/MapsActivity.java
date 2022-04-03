package com.example.rescuedversion;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.rescuedversion.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.twitter.sdk.android.tweetui.TweetTimelineRecyclerViewAdapter;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraMoveListener, GoogleMap.OnMarkerClickListener, View.OnClickListener, StoreListAdapter.onRecyclerViewUpdatedListener {

    /** Called when the user clicks on anything */
    public void sendExampleCity(View view) {
        Intent intent = new Intent(this, ExampleCity.class);
        startActivity(intent);
    }

    private GoogleMap mMap;

    private static final String TAG = "MapActivity";


    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mDetails = new ArrayList<>();
    private ArrayList<ArrayList<String>> mImageUrls = new ArrayList<>();
    private ArrayList<String> imageUrls = new ArrayList<>();
    private ArrayList<ArrayList<String>> mFoodDetails = new ArrayList<>();
    private ArrayList<String> foodDetails = new ArrayList<>();
    private ArrayList<ArrayList<String>> mFoodNames = new ArrayList<>();
    private ArrayList<String> foodNames = new ArrayList<>();
    private ArrayList<LatLng> mPoints = new ArrayList<>();

    private BottomNavigationView bnv;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        findViewById(R.id.reset_view).setOnClickListener(this);

        hideSystemUI();

        bnv = findViewById(R.id.bnv);
        bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.explore) {
                    startActivity(new Intent(MapsActivity.this, MapsActivity.class));
                    return false;
                } else if (item.getItemId() == R.id.check_in) {
                    startActivity(new Intent(MapsActivity.this, CheckInActivity.class));
                    return false;
                } else {
                    startActivity(new Intent(MapsActivity.this, SettingsActivity.class));
                    return false;
                }
            }
        });

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                updateUI(mAuth.getCurrentUser());
            }
        });

        updateUI(mAuth.getCurrentUser());

        initMap();
        getLocationPermission();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM);

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }


    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        initRecyclerView();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermission();
        }

        mMap.setOnCameraMoveListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setMyLocationEnabled(true);

        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        findViewById(R.id.reset_view).callOnClick();

        // Add a marker in Naperville and move the camera
        BitmapDescriptor icon2 = BitmapDescriptorFactory.fromResource(R.drawable.rescued_marker_primary_resized);
        LatLng storeLocation = new LatLng(41.712002143875374, -88.20353417832028);
        mPoints.add(storeLocation);
        mMap.addMarker(new MarkerOptions().position(storeLocation).icon(bitmapFromVector(getApplicationContext(), R.drawable.rescued_marker_primary_resized))).setTag(1);
    }

    private BitmapDescriptor bitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicWidth(), Bitmap.Config.ARGB_8888);
        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);
        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);
        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void initRecyclerView() {
        imageUrls.add("https://images-na.ssl-images-amazon.com/images/I/81GB1i1ZQvS.AC_SL240_.jpg");
        imageUrls.add("https://images-na.ssl-images-amazon.com/images/I/51r0fKoGNFL.AC_SL240_.jpg");
        imageUrls.add("https://images-na.ssl-images-amazon.com/images/I/81jG4U2tQeL.AC_SL240_.jpg");
        imageUrls.add("https://m.media-amazon.com/images/I/41x8Lij6oJL._AC_SY250_.jpg");

        mImageUrls.add(imageUrls);
        mNames.add("Walmart 75th Street");
        mDetails.add("1 mi away · Open today until 9:00 PM");
        imageUrls = new ArrayList<>();

        imageUrls.add("https://images-na.ssl-images-amazon.com/images/I/71mUTZL++DL.AC_SL240_.jpg");
        imageUrls.add("https://images-na.ssl-images-amazon.com/images/I/81LKLCmdAQL.AC_SL240_.jpg");
        imageUrls.add("https://images-na.ssl-images-amazon.com/images/I/71MzPgrIaoL.AC_SL240_.jpg");
        imageUrls.add("https://images-na.ssl-images-amazon.com/images/I/81qa4AyQMfL.AC_SL240_.jpg");

        mImageUrls.add(imageUrls);
        mNames.add("Jewel Osco Route 59");
        mDetails.add("1 mi away · Open today until 9:00 PM");

        foodDetails.add("Best before Nov 3, 2021");
        foodDetails.add("Best before Nov 7, 2021");
        foodDetails.add("Best before Nov 1, 2021");
        foodDetails.add("Best before Nov 2, 2021");

        mFoodDetails.add(foodDetails);
        foodDetails = new ArrayList<>();

        foodDetails.add("Best before Nov 5, 2021");
        foodDetails.add("Best before Nov 1, 2021");
        foodDetails.add("Best before Nov 4, 2021");
        foodDetails.add("Best before Nov 8, 2021");

        mFoodDetails.add(foodDetails);

        foodNames.add("Ball Park Hot Dog Buns");
        foodNames.add("Organic Pineapple");
        foodNames.add("Little Bites Chocolate Chip Muffins");
        foodNames.add("Organic Bartlett Pears");

        mFoodNames.add(foodNames);
        foodNames = new ArrayList<>();

        foodNames.add("Brown Cage Free Eggs");
        foodNames.add("Organic Hass Avocados");
        foodNames.add("Organic Granny Smith Apples");
        foodNames.add("Mission Soft Taco Flour Tortillas");

        mFoodNames.add(foodNames);

        RelativeLayout layout = findViewById(R.id.maps_activity_container);

        SmoothLinearLayoutManager layoutManager = new SmoothLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.storesView);
        recyclerView.setLayoutManager(layoutManager);
        StoreListAdapter adapter = new StoreListAdapter(this, this, layout, layoutManager, mMap, mPoints, mNames, mDetails, mImageUrls, mFoodDetails, mFoodNames);
        recyclerView.setAdapter(adapter);

        SnapHelper helper = new LinearSnapHelper();
        helper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onCameraMove() {
        VisibleRegion vr = mMap.getProjection().getVisibleRegion();
        double left = vr.latLngBounds.southwest.longitude;
        double top = vr.latLngBounds.northeast.latitude;
        double right = vr.latLngBounds.northeast.longitude;
        double bottom = vr.latLngBounds.southwest.latitude;

        Location center = new Location("center");
        center.setLatitude(vr.latLngBounds.getCenter().latitude);
        center.setLongitude(vr.latLngBounds.getCenter().longitude);

        Location middleLeftCornerLocation = new Location("center");
        middleLeftCornerLocation.setLatitude(center.getLatitude());
        middleLeftCornerLocation.setLongitude(left);

        int dis = Math.round(center.distanceTo(middleLeftCornerLocation) / 1000);
        TextView radius = findViewById(R.id.radius);
        if (dis != 0) radius.setText(Integer.toString(dis) + "mi");
        else radius.setText("<1mi");
    }

    private void resetMarkerPoints() {
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        int id = (int) marker.getTag();
        RecyclerView recyclerView = findViewById(R.id.storesView);
        recyclerView.smoothScrollToPosition(id);
        resetMarkerPoints();
        marker.setIcon(bitmapFromVector(getApplicationContext(), R.drawable.rescued_marker_secondary));
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.reset_view) {// Get the center of the Map.
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                getLocationPermission();
                return;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                        .zoom(15)
                                        .bearing(0)
                                        .tilt(0)
                                        .build();
                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            }
                        }
                    });
        }
    }

    private void updateUI(FirebaseUser user)
    {
        if (user == null)
        {
            Toast.makeText(getApplicationContext(), "Session Timed Out", Toast.LENGTH_SHORT).show();
            Intent logoutIntent = new Intent(getApplicationContext(), PathwayActivity.class);
            startActivity(logoutIntent);
        }
    }

    @Override
    public void onExpanded() {
        findViewById(R.id.reset_view).setVisibility(View.GONE);
        RecyclerView recyclerView = findViewById(R.id.storesView);
        recyclerView.getLayoutParams().height = RecyclerView.LayoutParams.MATCH_PARENT;
    }

    @Override
    public void onMinimized() {
        findViewById(R.id.reset_view).setVisibility(View.VISIBLE);
        RecyclerView recyclerView = findViewById(R.id.storesView);
        recyclerView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.recyclerview_height);
    }
}