package com.gago.david.myland

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.gago.david.myland.fragments.AddLandDetailsDialogFragment
import com.gago.david.myland.models.LandObject
import com.gago.david.myland.utils.LocationPermissionHelper
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.turf.*
import java.lang.ref.WeakReference
import java.util.*

class AddLandActivity : AppCompatActivity() {
    private var mapView: MapView? = null
    private lateinit var mapboxMap: MapboxMap
    private var polygon: MutableList<Point> = mutableListOf()
    private var area: Double? = null
    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private lateinit var polylineAnnotationManager: PolylineAnnotationManager
    private lateinit var circleAnnotationManager: CircleAnnotationManager
    private var lastLocation: Point? = null
    private var hasStartedSnapshotGeneration = false

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapboxMap.setCamera(CameraOptions.Builder().center(it).build())
        mapView?.gestures?.focalPoint = mapView?.getMapboxMap()?.pixelForCoordinate(it)
    }

    private val saveLocationListener = OnIndicatorPositionChangedListener {
        lastLocation = it
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    private fun onCameraTrackingDismissed() {
        mapView?.location?.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView?.gestures?.removeOnMoveListener(onMoveListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_land)
        mapView = findViewById(R.id.mapView)
        polygon = LinkedList()
        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions {
            setupMap()
        }
        val addMarker = findViewById<FloatingActionButton>(R.id.place_object)
        addMarker.setOnClickListener { setVertex(mapboxMap.cameraState.center) }
        val removeMarker = findViewById<FloatingActionButton>(R.id.remove_object)
        removeMarker.setOnClickListener { removeLastMarker() }
        val goToLocation = findViewById<FloatingActionButton>(R.id.myLocationButton)
        goToLocation.setOnClickListener { resetLocation() }
        val saveButton = findViewById<FloatingActionButton>(R.id.saveButton)
        saveButton.setOnClickListener { save() }
    }

    private fun save() {
        if (!hasStartedSnapshotGeneration) {
            hasStartedSnapshotGeneration = true
            if (polygon.size > 3) {
                if (checkBoundary()) {
                    AddLandDetailsDialogFragment().show(
                        supportFragmentManager,
                        "add-land-details-dialog"
                    )
                } else {
                    hasStartedSnapshotGeneration = false
                    Toast.makeText(
                        baseContext,
                        R.string.markers_visibility_error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            hasStartedSnapshotGeneration = false
            Toast.makeText(
                baseContext,
                R.string.land_border_size_error,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun checkBoundary(): Boolean {
        val pixelsForCoordinates = mapboxMap.pixelsForCoordinates(polygon)
        return pixelsForCoordinates.all { it.x >= 0 && it.y >= 0 }
    }

    private fun startSnapShot(name: String, state: String) {
        mapboxMap.getStyle { style ->
            val latitude = mapboxMap.cameraState.center.latitude()
            val longitude = mapboxMap.cameraState.center.longitude()
            val zoom = mapboxMap.cameraState.zoom
            val bearing = mapboxMap.cameraState.bearing


            val polygonGeoJSON = Polygon.fromLngLats(listOf(polygon))
            area = TurfMeasurement.area(polygonGeoJSON)
            val snapshotter = buildSnapshotter(style, polygonGeoJSON)
            snapshotter.start {
                val snapshot = it?.bitmap()

                if (snapshot != null) {
                    val filename = addImage(snapshot)
                    val success = LandOpenHelper.addLand(
                        this,
                        LandObject(
                            name,
                            filename,
                            state,
                            area!!,
                            latitude,
                            longitude,
                            zoom,
                            bearing,
                            polygonGeoJSON.toJson()
                        )
                    )
                    if (!success) Toast.makeText(
                        this,
                        "Land already exists, choose a different name",
                        Toast.LENGTH_SHORT
                    ).show() else {
                        val intent = Intent(this, ScrollingActivity::class.java)
                        val b = Bundle()
                        b.putString("name", name)
                        intent.putExtras(b)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun buildSnapshotter(
        style: Style,
        polygonGeoJSON: Polygon
    ): Snapshotter {
        val mapSnapshotOptions =
            MapSnapshotOptions.Builder().pixelRatio(style.pixelRatio).size(mapboxMap.getSize())
                .resourceOptions(mapboxMap.getResourceOptions()).build()
        val snapshotter = Snapshotter(
            this,
            mapSnapshotOptions,
            SnapshotOverlayOptions(showLogo = false, showAttributes = false)
        )
        snapshotter.setStyleUri(Style.SATELLITE)
        snapshotter.setStyleListener(object : SnapshotStyleListener {
            override fun onDidFinishLoadingStyle(style: Style) {
                drawPolygon(style, polygonGeoJSON)
            }
        })
        snapshotter.setCamera(mapboxMap.cameraState.toCameraOptions())
        snapshotter.setSize(mapboxMap.getSize())
        snapshotter.setTileMode(false)
        return snapshotter
    }

    private fun drawPolygon(style: Style, polygonGeoJSON: Polygon) {
        style.addSource(GeoJsonSource.Builder("land").geometry(polygonGeoJSON).build())
        style.addLayer(lineLayer("area", "land") {
            lineColor("#3bb2d0")
            lineCap(LineCap.ROUND)
            lineJoin(LineJoin.ROUND)
            lineWidth(8.0)
        })
    }

    private fun resetLocation() {
        mapboxMap.setCamera(CameraOptions.Builder().center(lastLocation).build())
        mapView?.location?.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView?.gestures?.addOnMoveListener(onMoveListener)
    }

    private fun setupMap() {

        val prefs = getSharedPreferences(SettingsFragment.MY_PREFS_NAME, MODE_PRIVATE)
        mapboxMap = mapView?.getMapboxMap()!!
        mapboxMap.setCamera(CameraOptions.Builder().center(Point.fromLngLat(-9.198105, 38.666578)).zoom(14.0).build())
        val style = when (prefs.getInt("mapType", 0)) {
            0 -> Style.SATELLITE
            else -> Style.SATELLITE_STREETS
        }
        mapboxMap.loadStyleUri(
            style
        )
        // After the style is loaded, initialize the Location component.
        {
            initLocationComponent()
            setupGesturesListener()
        }

        // Create an instance of the Annotation API and get the CircleAnnotationManager.
        val annotationApi = mapView?.annotations
        polylineAnnotationManager = annotationApi!!.createPolylineAnnotationManager()
        circleAnnotationManager = annotationApi.createCircleAnnotationManager()

    }

    private fun setupGesturesListener() {
        mapView?.gestures?.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        mapView?.location?.updateSettings {
            enabled = true
            pulsingEnabled = true
        }
        mapView?.location?.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView?.location?.addOnIndicatorPositionChangedListener(saveLocationListener)
    }

    private fun addImage(bitmap: Bitmap): String {
        return LandOpenHelper.addImage(this, bitmap)
    }

    private fun updatePolygon() {
        polylineAnnotationManager.deleteAll()
        if (polygon.size >= 2) {
            val polylineAnnotationOptions: PolylineAnnotationOptions = PolylineAnnotationOptions()
                .withLineWidth(2.0)
                .withLineColor("#3bb2d0")
                .withPoints(polygon + polygon[0])
            polylineAnnotationManager.create(polylineAnnotationOptions)
        }
    }

    private fun addMarker(point: Point) {
        val circleAnnotationOptions: CircleAnnotationOptions = CircleAnnotationOptions()
            .withPoint(point)
            .withCircleRadius(6.0)
            .withCircleColor("#7cd3ea")
            .withCircleStrokeWidth(2.0)
            .withCircleStrokeColor("#3bb2d0")
        circleAnnotationManager.create(circleAnnotationOptions)
    }

    private fun removeLastMarker() {
        if (polygon.isNotEmpty()) {
            polygon.removeLast()
            updatePolygon()
            circleAnnotationManager.delete(circleAnnotationManager.annotations.last())
            Log.v("MAPBOX", "marker removed")
        }
    }

    private fun setVertex(point: Point): Boolean {
        polygon.add(point)
        updatePolygon()
        addMarker(point)
        Log.v("MAPBOX", "marker added")
        return true;
    }

    fun addLandDetailsCallback(name: String, state: String) {
        startSnapShot(name, state)
    }
}