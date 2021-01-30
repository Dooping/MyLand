package com.gago.david.myland

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.Toast
import com.gago.david.myland.utils.LatLngInterpolator.Linear
import com.gago.david.myland.utils.SphericalUtil
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.constants.Style
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMapLongClickListener
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import id.arieridwan.lib.PageLoader
import java.util.*

class AddLandActivity : AppCompatActivity(), OnMapReadyCallback, OnMapLongClickListener, LocationEngineListener, PermissionsListener {
    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null
    private var locationLayerPlugin: LocationLayerPlugin? = null
    private var locationEngine: LocationEngine? = null
    private var permissionsManager: PermissionsManager? = null
    private var poligon: LinkedList<LatLng>? = null
    private var pageLoader: PageLoader? = null
    private var area: Double? = null
    private val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10
    private val MY_PERMISSIONS_REQUEST_LOCATION = 20
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_add_land)
        mapView = findViewById(R.id.mapView)
        pageLoader = findViewById(R.id.pageloader)
        pageLoader?.startProgress()
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
        poligon = LinkedList()
    }

    @SuppressLint("RestrictedApi")
    override fun onMapReady(mapboxMap: MapboxMap) {
        pageLoader!!.stopProgress()
        Log.v("MAPBOX", "onMapReady")
        this@AddLandActivity.mapboxMap = mapboxMap
        val prefs = getSharedPreferences(SettingsFragment.MY_PREFS_NAME, MODE_PRIVATE)
        val mapTypePosition = prefs.getInt("mapType", 0)
        if (mapTypePosition == 0) mapboxMap.setStyle(Style.SATELLITE) else mapboxMap.setStyle(Style.SATELLITE_STREETS)
        mapboxMap.uiSettings.isTiltGesturesEnabled = false
        //        mapboxMap.addOnMapLongClickListener(this);
        enableLocationPlugin()
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
        mapboxMap.setOnMarkerClickListener { marker ->
            Log.v("Marker", marker.position.toString())
            poligon!!.remove(marker.position)
            marker.remove()
            if (mapboxMap.polygons.size > 0) mapboxMap.polygons[0].points = poligon
            true
        }
        if (poligon != null) {
            mapboxMap.addPolygon(PolygonOptions()
                    .addAll(poligon)
                    .strokeColor(Color.parseColor("#3bb2d0"))
                    .fillColor(Color.parseColor("#7cd3ea"))
                    .alpha(0.7f))
            for (p in poligon!!) mapboxMap.addMarker(MarkerOptions()
                    .position(p))
        }
        userLocationFAB()
        val saveButton = findViewById<FloatingActionButton>(R.id.saveButton)
        saveButton.visibility = View.VISIBLE
        saveButton.setOnClickListener {
            pageLoader!!.startProgress()
            //                locationLayerPlugin.setLocationLayerEnabled(false);
            var cancel = false
            if (poligon!!.size > 0) {
                val latLngBounds = mapboxMap.projection.visibleRegion.latLngBounds
                for (p in poligon!!) if (!latLngBounds.contains(p)) {
                    cancel = true
                    Toast.makeText(baseContext, R.string.markers_visibility_error, Toast.LENGTH_SHORT).show()
                    pageLoader!!.stopProgress()
                    //                            locationLayerPlugin.setLocationLayerEnabled(true);
                }
                if (!cancel) {
                    mapboxMap.clear()
                    poligon!!.add(poligon!!.first)
                    mapboxMap.addPolyline(PolylineOptions()
                            .addAll(poligon)
                            .color(Color.parseColor("#3bb2d0"))).width = 3.0f
                    area = SphericalUtil.computeArea(poligon)
                    Log.i("AREA", "computeArea " + SphericalUtil.computeArea(poligon) + " m2")
                }
            }
            if (!cancel) {
                val myHandler = Handler()
                myHandler.postDelayed({
                    mapboxMap.snapshot(MapboxMap.SnapshotReadyCallback { snapshot ->
                        val filename = addImage(snapshot)
                        if (filename != null) {
                            val data = Intent()
                            //---set the data to pass back---
                            data.putExtra("name", filename)
                            data.putExtra("area", area)
                            Log.v("MAPBOX", "fileUri: $filename")
                            setResult(RESULT_OK, data)
                            //---close the activity---
                            finish()
                        }
                    })
                }, 50)
            }
        }
        val addMarker = findViewById<FloatingActionButton>(R.id.add_marker)
        addMarker.visibility = View.VISIBLE
        addMarker.setOnClickListener { onMapLongClick(mapboxMap.cameraPosition.target) }
        val removeMarker = findViewById<FloatingActionButton>(R.id.remove_marker)
        removeMarker.visibility = View.VISIBLE
        removeMarker.setOnClickListener {
            if (poligon!!.size > 0) {
                poligon!!.removeLast()
                val marker = mapboxMap.markers[mapboxMap.markers.size - 1]
                marker.remove()
                if (mapboxMap.polygons.size > 0) mapboxMap.polygons[0].points = poligon
            }
        }
    }

    private fun enableLocationPlugin() {
//        if (ContextCompat.checkSelfPermission( this,android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
//        {
//            ActivityCompat.requestPermissions(
//                    this,
//                    new String [] { android.Manifest.permission.ACCESS_COARSE_LOCATION },
//                    LocationService.MY_PERMISSION_ACCESS_COURSE_LOCATION
//            );
//        }


        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Create a location engine instance
            initializeLocationEngine()
            locationLayerPlugin = LocationLayerPlugin(mapView!!, mapboxMap!!, locationEngine)
            //            locationLayerPlugin.setLocationLayerEnabled(true);
            lifecycle.addObserver(locationLayerPlugin!!)
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager!!.requestLocationPermissions(this)
        }
    }

    private fun initializeLocationEngine() {
        locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
        locationEngine!!.priority = LocationEnginePriority.HIGH_ACCURACY
        locationEngine!!.addLocationEngineListener(this)
        locationEngine!!.activate()
        //        Location lastLocation = locationEngine.getLastLocation();
//        if (lastLocation != null) {
//            setCameraPosition(lastLocation);
//        } else {
//            locationEngine.addLocationEngineListener(this);
//        }
    }

    private fun setCameraPosition(location: Location) {
        mapboxMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(location.latitude, location.longitude), 16.0), 3000)
    }

    private fun addImage(bitmap: Bitmap): String? {
        val name = LandOpenHelper.addImage(this, bitmap)
        if (name == null) Toast.makeText(this, R.string.image_add_error, Toast.LENGTH_SHORT).show()
        return name
    }

    override fun onConnected() {
//        if (locationEngine != null)
//            locationEngine.requestLocationUpdates();
    }

    override fun onLocationChanged(location: Location) {
        mapboxMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(location.latitude, location.longitude), 16.0), 3000)
        if (locationEngine != null) locationEngine!!.removeLocationEngineListener(this)
    }

    @SuppressLint("RestrictedApi")
    private fun userLocationFAB() {
        val FAB = findViewById<FloatingActionButton>(R.id.myLocationButton)
        FAB.visibility = View.VISIBLE
        FAB.setOnClickListener {
            if (locationLayerPlugin!!.lastKnownLocation != null) { // Check to ensure coordinates aren't null, probably a better way of doing this...
                mapboxMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(locationLayerPlugin!!.lastKnownLocation), 16.0), 3000)
            }
        }
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    public override fun onStart() {
        super.onStart()
        mapView!!.onStart()
        //        if (locationEngine != null) {
//            locationEngine.requestLocationUpdates();
//            locationEngine.addLocationEngineListener(this);
//        }
    }

    public override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
        if (locationEngine != null) {
            locationEngine!!.removeLocationEngineListener(this)
            locationEngine!!.removeLocationUpdates()
        }
        pageLoader!!.stopProgress()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
        if (locationEngine != null) {
            locationEngine!!.deactivate()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable("polygon", poligon)
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        poligon = savedInstanceState.getSerializable("polygon") as LinkedList<LatLng>
        mapView!!.getMapAsync(this)
    }

    override fun onMapLongClick(point: LatLng) {
        val marker = mapboxMap!!.addMarker(MarkerOptions()
                .position(point))
        val bearing = mapboxMap!!.cameraPosition.bearing
        val start: LatLng = SphericalUtil.computeOffset(marker.position, 5.0, bearing)
        marker.position = start
        val interpolator = Linear()
        val bounceInterpolator = BounceInterpolator()
        val markerAnimator: ValueAnimator = ObjectAnimator.ofObject(marker, "position",
                TypeEvaluator<LatLng> { fraction: Float, a: LatLng?, b: LatLng? -> interpolator.interpolate(fraction, a, b) }, marker.position, point)
        markerAnimator.interpolator = bounceInterpolator
        markerAnimator.duration = 1000
        markerAnimator.start()
        poligon!!.add(point)
        if (mapboxMap!!.polygons.size > 0) mapboxMap!!.polygons[0].addPoint(point) else mapboxMap!!.addPolygon(PolygonOptions()
                .addAll(poligon)
                .strokeColor(Color.parseColor("#3bb2d0"))
                .fillColor(Color.parseColor("#7cd3ea"))
                .alpha(0.7f))
        Log.v("MAPBOX", "marker added")
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        //
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            Log.v("Permission", "granted")
            enableLocationPlugin()
        } else {
            Toast.makeText(this, "ahsd", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionsManager!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}