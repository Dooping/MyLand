package com.gago.david.myland;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;


public class AddLandActivity extends AppCompatActivity implements OnMapReadyCallback,
        MapboxMap.OnMapLongClickListener{

    private MapView mapView;
    private MapboxMap mapboxMap;
    private LinkedList<LatLng> poligon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_add_land);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        poligon = new LinkedList<>();

    }

    @Override
    public void onMapReady(final MapboxMap mapboxMap) {
        Log.v("MAPBOX", "onMapReady");
        AddLandActivity.this.mapboxMap = mapboxMap;
        mapboxMap.setOnMapLongClickListener(this);
        mapboxMap.setMyLocationEnabled(true);
        mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
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
                mapboxMap.addMarker(new MarkerViewOptions()
                        .position(p));
        }
        userLocationFAB();
        FloatingActionButton saveButton = (FloatingActionButton) findViewById(R.id.saveButton);
        saveButton.setVisibility(View.VISIBLE);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(Polygon p: mapboxMap.getPolygons())
                    mapboxMap.removePolygon(p);
                poligon.add(poligon.getFirst());
                mapboxMap.addPolyline(new PolylineOptions()
                        .addAll(poligon)
                        .color(Color.parseColor("#3bb2d0"))).setWidth(3.0f);
                Handler myHandler = new Handler();

                myHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mapboxMap.snapshot(new MapboxMap.SnapshotReadyCallback() {
                            @Override
                            public void onSnapshotReady(Bitmap snapshot) {
                                String filename = UUID.randomUUID().toString()+".png";
                                Log.v("MAPBOX", "filename: "+filename);
                                FileOutputStream out = null;
                                try {
                                    File file = new File(getFilesDir(),filename);
                                    out = new FileOutputStream(file);
                                    snapshot.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                                    // PNG is a lossless format, the compression factor (100) is ignored

                                    Intent data = new Intent();
                                    //---set the data to pass back---
                                    data.setData(Uri.fromFile(file));
                                    Log.v("MAPBOX", "fileUri: "+Uri.fromFile(file));
                                    setResult(RESULT_OK, data);
                                    //---close the activity---
                                    finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    try {
                                        if (out != null) {
                                            out.close();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                }, 50);

            }
        });

    }

    private void userLocationFAB(){
        FloatingActionButton FAB = (FloatingActionButton) findViewById(R.id.myLocationButton);
        FAB.setVisibility(View.VISIBLE);
        FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mapboxMap.getMyLocation() != null) { // Check to ensure coordinates aren't null, probably a better way of doing this...
                    mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mapboxMap.getMyLocation()),16), 3000);
                }
            }
        });
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onStart(){
        super.onStart();
        mapView.onStart();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
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
        mapboxMap.addMarker(new MarkerViewOptions()
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
}
