package com.gago.david.myland;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.gago.david.myland.Utils.SphericalUtil;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import id.arieridwan.lib.PageLoader;


public class AddLandActivity extends AppCompatActivity implements OnMapReadyCallback,
        MapboxMap.OnMapLongClickListener, LocationEngineListener, PermissionsListener {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private LocationLayerPlugin locationLayerPlugin;
    private LocationEngine locationEngine;
    private PermissionsManager permissionsManager;
    private LinkedList<LatLng> poligon;
    private PageLoader pageLoader;
    private Double area;
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10;
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_add_land);

        mapView = findViewById(R.id.mapView);
        pageLoader = findViewById(R.id.pageloader);
        pageLoader.startProgress();
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        poligon = new LinkedList<>();

    }

    @Override
    public void onMapReady(final MapboxMap mapboxMap) {
        pageLoader.stopProgress();
        Log.v("MAPBOX", "onMapReady");
        AddLandActivity.this.mapboxMap = mapboxMap;
        mapboxMap.getUiSettings().setTiltGesturesEnabled(false);
        mapboxMap.addOnMapLongClickListener(this);
        enableLocationPlugin();
//
//        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
//        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
//        locationEngine.setFastestInterval(1000);
//        locationEngine.addLocationEngineListener(this);
//        locationEngine.activate();
//
//        int[] padding;
//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            padding = new int[] {0, 750, 0, 0};
//        } else {
//            padding = new int[] {0, 250, 0, 0};
//        }
//        LocationLayerOptions options = LocationLayerOptions.builder(this)
//                .padding(padding)
//                .build();
//
//        locationLayerPlugin = new LocationLayerPlugin(mapView, mapboxMap, locationEngine, options);
        //locationLayerPlugin.addOnLocationClickListener(this);
        //locationLayerPlugin.addOnCameraTrackingChangedListener(this);

//        getLifecycle().addObserver(locationLayerPlugin);
//
//        if(locationLayerPlugin.getLastKnownLocation() != null) { // Check to ensure coordinates aren't null, probably a better way of doing this...
//            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationLayerPlugin.getLastKnownLocation()),16), 3000);
//        }



        //mapboxMap.setMyLocationEnabled(true);
        mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                Log.v("Marker", marker.getPosition().toString());
                poligon.remove(marker.getPosition());
                marker.remove();
                if ( mapboxMap.getPolygons().size()>0)
                    mapboxMap.getPolygons().get(0).setPoints(poligon);
                return true;
            }
        });
        if (poligon != null){
            mapboxMap.addPolygon(new PolygonOptions()
                    .addAll(poligon)
                    .strokeColor(Color.parseColor("#3bb2d0"))
                    .fillColor(Color.parseColor("#7cd3ea"))
                    .alpha(0.7f));
            for(LatLng p: poligon)
                mapboxMap.addMarker(new MarkerOptions()
                        .position(p));
        }
        userLocationFAB();
        FloatingActionButton saveButton = findViewById(R.id.saveButton);
        saveButton.setVisibility(View.VISIBLE);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageLoader.startProgress();
                locationLayerPlugin.setLocationLayerEnabled(false);
                if(poligon.size()>0) {
                    mapboxMap.clear();
                    /*for (Polygon p : mapboxMap.getPolygons())
                        mapboxMap.removePolygon(p);*/
                    poligon.add(poligon.getFirst());
                    mapboxMap.addPolyline(new PolylineOptions()
                            .addAll(poligon)
                            .color(Color.parseColor("#3bb2d0"))).setWidth(3.0f);
                    area = SphericalUtil.computeArea(poligon);
                    Log.i("AREA", "computeArea " + SphericalUtil.computeArea(poligon)+ " m2");
                }

                Handler myHandler = new Handler();

                myHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mapboxMap.snapshot(new MapboxMap.SnapshotReadyCallback() {
                            @Override
                            public void onSnapshotReady(Bitmap snapshot) {
                                String filename = addImage(snapshot);
                                Intent data = new Intent();
                                //---set the data to pass back---
                                data.putExtra("name", filename);
                                data.putExtra("area", area);
                                Log.v("MAPBOX", "fileUri: "+filename);
                                setResult(RESULT_OK, data);
                                //---close the activity---
                                finish();
                            }
                        });
                    }
                }, 50);

            }
        });

        FloatingActionButton addMarker = findViewById(R.id.add_marker);
        addMarker.setVisibility(View.VISIBLE);
        addMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMapLongClick(mapboxMap.getCameraPosition().target);
            }
        });
        FloatingActionButton removeMarker = findViewById(R.id.remove_marker);
        removeMarker.setVisibility(View.VISIBLE);
        removeMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(poligon.size() > 0) {
                    poligon.removeLast();
                    Marker marker = mapboxMap.getMarkers().get(mapboxMap.getMarkers().size() - 1);
                    marker.remove();
                    if (mapboxMap.getPolygons().size() > 0)
                        mapboxMap.getPolygons().get(0).setPoints(poligon);
                }
            }
        });

    }

    private void enableLocationPlugin() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Create a location engine instance
            initializeLocationEngine();
            locationLayerPlugin = new LocationLayerPlugin(mapView, mapboxMap, locationEngine);
            locationLayerPlugin.setLocationLayerEnabled(true);
            getLifecycle().addObserver(locationLayerPlugin);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    private void initializeLocationEngine() {
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.addLocationEngineListener(this);
        locationEngine.activate();
        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }
    private void setCameraPosition(Location location) {
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 16), 3000);
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    public String addImage(Bitmap image) {
        LandOpenHelper mDbHelper = new LandOpenHelper(this);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String name = UUID.randomUUID().toString()+".png";

        ContentValues values = new ContentValues();
        values.put("Name", name);
        values.put("Image", getBitmapAsByteArray(image));

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert("Images", null, values);
        if (newRowId == -1) {
            Toast.makeText(this,R.string.image_add_error, Toast.LENGTH_SHORT).show();
            Log.v("Add Image", "Failed to insert item: " + name);
        }
        else {
            Log.v("Add Image", "row inserted: " + newRowId);
        }

        db.close();
        return name;
    }

    @Override
    public void onConnected() {
        if (locationEngine != null)
            locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 16), 3000);
        if (locationEngine != null)
            locationEngine.removeLocationEngineListener(this);
    }

    private void userLocationFAB(){
        FloatingActionButton FAB = findViewById(R.id.myLocationButton);
        FAB.setVisibility(View.VISIBLE);
        FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(locationLayerPlugin.getLastKnownLocation() != null) { // Check to ensure coordinates aren't null, probably a better way of doing this...
                    mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationLayerPlugin.getLastKnownLocation()),16), 3000);
                }
            }
        });
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onStart(){
        super.onStart();
        mapView.onStart();
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
            locationEngine.addLocationEngineListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onStop(){
        super.onStop();
        mapView.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationEngineListener(this);
            locationEngine.removeLocationUpdates();
        }
        pageLoader.stopProgress();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("polygon", poligon);
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        poligon = (LinkedList<LatLng>) savedInstanceState.getSerializable("polygon");
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapLongClick(@NonNull LatLng point) {
        mapboxMap.addMarker(new MarkerOptions()
                .position(point));
        poligon.add(point);
        if ( mapboxMap.getPolygons().size()>0)
            mapboxMap.getPolygons().get(0).addPoint(point);
        else
            mapboxMap.addPolygon(new PolygonOptions()
                    .addAll(poligon)
                    .strokeColor(Color.parseColor("#3bb2d0"))
                    .fillColor(Color.parseColor("#7cd3ea"))
                    .alpha(0.7f));
        Log.v("MAPBOX", "marker added");
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        //
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            Log.v("Permission", "granted");
            enableLocationPlugin();
        } else {
            Toast.makeText(this, "ahsd", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
