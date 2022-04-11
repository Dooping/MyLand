package com.gago.david.myland

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.gago.david.myland.adapters.PopupMenuAdapter
import com.gago.david.myland.models.LandObject
import com.gago.david.myland.models.PlantTypeObject
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager

lateinit var mapView: MapView
lateinit var placeObject: FloatingActionButton
lateinit var removeObject: FloatingActionButton
var alertDialog : AlertDialog? = null
var land: LandObject? = null
private lateinit var polylineAnnotationManager: PolylineAnnotationManager

class LandEditMapActivity : AppCompatActivity(), PopupMenuAdapter.OnMenuItemInteractionListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_land_edit_map)

        mapView = findViewById(R.id.mapView)
        mapView.getMapboxMap().loadStyleUri(Style.SATELLITE)
        val annotationApi = mapView.annotations
        polylineAnnotationManager = annotationApi.createPolylineAnnotationManager()

        val b = intent.extras
        val name = b!!.getString("name")
        land = LandOpenHelper.readLandWithArea(name, this)

        setCameraPosition(land!!)
        setLandBorders(land!!)

        Toast.makeText(baseContext, R.string.helper_edit_land_long_press, Toast.LENGTH_LONG).show()

        placeObject = findViewById(R.id.place_object)
        removeObject = findViewById(R.id.remove_object)

        placeObject.setOnLongClickListener {
            val sortList = LandOpenHelper.readPlantTypes(this)
            val adapter = PopupMenuAdapter(this, sortList)
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setAdapter(adapter) { dialog, _ -> dialog.dismiss() }
            alertDialog = alertDialogBuilder.show()
            false
        }

        placeObject.setOnClickListener {
            Toast.makeText(baseContext, R.string.helper_edit_land_long_press, Toast.LENGTH_SHORT).show()
        }


    }

    private fun setCameraPosition(land: LandObject) {
        val point = Point.fromLngLat(land.lon, land.lat)
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(point).bearing(land.bearing).zoom(land.zoom).build())
    }

    private fun setLandBorders(land: LandObject) {
        val polygon = Polygon.fromJson(land.polygon!!).coordinates()[0]
        if (polygon.size >= 2) {
            val polylineAnnotationOptions: PolylineAnnotationOptions = PolylineAnnotationOptions()
                .withLineWidth(2.0)
                .withLineColor("#3bb2d0")
                .withPoints((polygon + polygon[0]) as List<Point>)
            polylineAnnotationManager.create(polylineAnnotationOptions)
        }
    }

    override fun onMenuItemInteraction(item: PlantTypeObject?) {
        Toast.makeText(baseContext, item?.name, Toast.LENGTH_SHORT).show()
        alertDialog?.dismiss()
    }
}
