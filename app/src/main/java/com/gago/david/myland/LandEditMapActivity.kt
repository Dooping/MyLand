package com.gago.david.myland

import android.animation.Animator
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.gago.david.myland.adapters.PopupMenuAdapter
import com.gago.david.myland.fragments.AddItemDialogFragment
import com.gago.david.myland.models.LandObject
import com.gago.david.myland.models.PlantObject
import com.gago.david.myland.models.PlantTypeObject
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.toCameraOptions
import com.mapbox.turf.TurfMeasurement
import java.util.ArrayList


class LandEditMapActivity : AppCompatActivity(), PopupMenuAdapter.OnMenuItemInteractionListener {
    private lateinit var mapView: MapView
    private lateinit var placeObject: FloatingActionButton
    private lateinit var removeObject: FloatingActionButton
    private lateinit var deleteObject: FloatingActionButton
    private lateinit var moveObject: FloatingActionButton
    private var alertDialog : AlertDialog? = null
    private var land: LandObject? = null
    private lateinit var polylineAnnotationManager: PolylineAnnotationManager
    private lateinit var annotationManager: PointAnnotationManager
    private var selectedObjectType: PlantTypeObject? = null
    private var addedObjects: List<PlantObject> = emptyList()
    private var addedAnnotations: MutableList<PointAnnotation> = mutableListOf()
    private var selectedObject: PlantObject? = null
    private var movingObject = false
    private var allObjectAnnotations: Map<Int, PointAnnotation> = emptyMap()
    private lateinit var crosshair: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_land_edit_map)

        mapView = findViewById(R.id.mapView)
        mapView.getMapboxMap().loadStyleUri(Style.SATELLITE)
        val annotationApi = mapView.annotations
        polylineAnnotationManager = annotationApi.createPolylineAnnotationManager()
        annotationManager = annotationApi.createPointAnnotationManager()

        val b = intent.extras
        val name = b!!.getString("name")
        land = LandOpenHelper.readLandWithArea(name, this)

        setCameraPosition(land!!)
        setLandBorders(land!!)
        setExistingObjects(land!!.plants)

        mapView.getMapboxMap().addOnMapClickListener {
            selectClosestObject(it)
            setExistingObjects(land!!.plants)
            true
        }

        Toast.makeText(baseContext, R.string.helper_edit_land_long_press, Toast.LENGTH_LONG).show()

        placeObject = findViewById(R.id.place_object)
        removeObject = findViewById(R.id.remove_object)
        deleteObject = findViewById(R.id.delete_object)
        moveObject = findViewById(R.id.move_object)
        crosshair = findViewById(R.id.crosshair)

        placeObject.setOnLongClickListener {
            showObjectTypeDialog()
            false
        }

        placeObject.setOnClickListener {
            if (selectedObjectType == null)
                showObjectTypeDialog()
            else
                AddItemDialogFragment(
                    selectedObjectType!!,
                    mapView.getMapboxMap().cameraState.center).show(supportFragmentManager, "add-item-dialog")
        }

        removeObject.setOnClickListener {
            removeLastObject()
            checkDeleteButtonVisibility()
            checkRemoveButtonVisibility()
        }

        deleteObject.setOnClickListener {
            selectedObject?.let {
                deleteObject(it)
            } ?: Toast.makeText(this, getString(R.string.no_object_selected), Toast.LENGTH_SHORT).show()
        }

        moveObject.setOnClickListener {
            selectedObject?.let {
                moveObject(it)
            } ?: Toast.makeText(this, getString(R.string.no_object_selected), Toast.LENGTH_SHORT).show()
        }

        checkRemoveButtonVisibility()
        checkDeleteButtonVisibility()
    }

    private fun moveObject(plantObject: PlantObject) {
        val objectTypes = LandOpenHelper.readPlantTypes(this)
        if (!movingObject) {
            startObjectMoving(plantObject, objectTypes)
        }  else {
            stopObjectMoving(objectTypes, plantObject)
        }

        checkAddButtonVisibility()
        checkDeleteButtonVisibility()
        checkRemoveButtonVisibility()
    }

    private fun stopObjectMoving(
        objectTypes: ArrayList<PlantTypeObject>,
        plantObject: PlantObject
    ) {
        movingObject = false
        mapView.getMapboxMap().cameraState.center
        val type = objectTypes.find { type -> type.name == plantObject.plantType }!!
        val icon = getObjectIconPainted(type, true)
        val center = mapView.getMapboxMap().cameraState.center

        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
            .withPoint(center)
            .withIconImage(icon)
        val newAnnotation = annotationManager.create(pointAnnotationOptions)
        plantObject.lat = center.latitude()
        plantObject.lon = center.longitude()
        allObjectAnnotations = allObjectAnnotations + (plantObject.id to newAnnotation)
        LandOpenHelper.updatePlant(this, plantObject, land!!.name)
        selectClosestObject(center)
        crosshair.setImageResource(R.drawable.red_marker)
    }

    private fun startObjectMoving(
        plantObject: PlantObject,
        objectTypes: ArrayList<PlantTypeObject>
    ) {
        movingObject = true
        val mapbox = mapView.getMapboxMap()
        mapbox.flyTo(
            mapbox.cameraState.toCameraOptions().toBuilder()
                .center(Point.fromLngLat(plantObject.lon, plantObject.lat)).build(),
            mapAnimationOptions {
                animatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {

                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        allObjectAnnotations[plantObject.id]?.let {
                            annotationManager.delete(it)
                            Toast.makeText(
                                this@LandEditMapActivity,
                                R.string.edit_object_position,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        val type = objectTypes.find { type -> type.name == plantObject.plantType }!!
                        val icon = getObjectIconPainted(type, true)
                        crosshair.setImageBitmap(icon)
                    }

                    override fun onAnimationCancel(animation: Animator?) {

                    }

                    override fun onAnimationRepeat(animation: Animator?) {

                    }
                })
            }
        )
    }

    private fun deleteObject(objectToDelete: PlantObject) {
        val alert = AlertDialog.Builder(this)
            .setMessage(R.string.remove_tree)
            .setCancelable(false)
            .setPositiveButton(R.string.yes) { _, _ ->
                LandOpenHelper.deletePlantObject(this, objectToDelete)
                land!!.removePlant(objectToDelete)
                selectedObject = null
                setExistingObjects(land!!.plants)
                checkDeleteButtonVisibility()
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }.create()
        alert.show()
    }

    private fun selectClosestObject(click: Point) {
        val min = land!!.plants.minByOrNull { plantObject -> TurfMeasurement.distance(Point.fromLngLat(plantObject.lon, plantObject.lat), click) }
        min?.let {
            selectedObject = it
            checkDeleteButtonVisibility()
        }
    }

    private fun setExistingObjects(objects: List<PlantObject>) {
        annotationManager.deleteAll()
        val objectTypes = LandOpenHelper.readPlantTypes(this)
        objects.forEach {
            val type = objectTypes.find { type -> type.name == it.plantType }!!
            val icon = getObjectIconPainted(type, selectedObject == it)

            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(it.lon, it.lat))
                .withIconImage(icon)
            val newAnnotation = annotationManager.create(pointAnnotationOptions)
            allObjectAnnotations = allObjectAnnotations + (it.id to newAnnotation)
        }
    }

    private fun removeLastObject() {
        if (addedObjects.isNotEmpty()) {
            val last = addedObjects.last()
            addedObjects = addedObjects.dropLast(1)
            land!!.removePlant(last)
            LandOpenHelper.deletePlantObject(this, last)
            annotationManager.delete(allObjectAnnotations[last.id]!!)
            allObjectAnnotations = allObjectAnnotations - last.id
            if (last == selectedObject) {
                selectedObject = null
            }
        }
    }

    private fun addObject(center: Point, p: PlantObject) {
        val icon = getObjectIconPainted(selectedObjectType!!)

        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
            .withPoint(center)
            .withIconImage(icon)
        val newAnnotation = annotationManager.create(pointAnnotationOptions)
        allObjectAnnotations = allObjectAnnotations + (p.id to newAnnotation)
        land!!.addPlant(p)
        addedObjects = addedObjects + p
        checkRemoveButtonVisibility()
        LandOpenHelper.addPlant(this, p, land!!.name)
    }

    fun addItemDialogOkButton(description: String, plantTypeName: String, center: Point) {
        val p = PlantObject(plantTypeName, description, center.latitude(), center.longitude())
        addObject(center, p)
    }

    private fun checkRemoveButtonVisibility() {
        val isEnabled = addedObjects.isNotEmpty() && !movingObject
        removeObject.isEnabled = isEnabled
        removeObject.backgroundTintList = ColorStateList.valueOf(if (isEnabled) Color.RED else Color.GRAY)
    }

    private fun checkDeleteButtonVisibility() {
        val isEnabled = selectedObject != null && !movingObject
        deleteObject.isEnabled = isEnabled
        deleteObject.backgroundTintList = ColorStateList.valueOf(if (isEnabled) Color.RED else Color.GRAY)
    }

    private fun checkAddButtonVisibility() {
        val isEnabled = !movingObject
        placeObject.isEnabled = isEnabled
        resources.getColor(R.color.colorAccent, theme)
        placeObject.backgroundTintList = ColorStateList.valueOf(if (isEnabled)
            resources.getColor(R.color.colorAccent, theme) else Color.GRAY)
    }

    private fun getObjectIconPainted(type: PlantTypeObject, selected: Boolean = false): Bitmap {
        val myIcon = ContextCompat.getDrawable(this, type.icon)
        val bitmap = (myIcon as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val paint = Paint()
        val filter: ColorFilter = PorterDuffColorFilter(
            Color.parseColor(type.color),
            PorterDuff.Mode.SRC_IN
        )
        paint.colorFilter = filter

        val canvas = Canvas(bitmap)
        canvas.drawBitmap(bitmap, 0.0F, 0.0F, paint)
        if (selected) {
            val mPaint = Paint()
            mPaint.color = Color.RED
            mPaint.style = Paint.Style.STROKE
            mPaint.strokeWidth = 2.5f
            mPaint.isAntiAlias = true
            canvas.drawCircle(canvas.width.toFloat() / 2, canvas.height.toFloat() / 2, 50f, mPaint)
        }
        return bitmap
    }

    private fun showObjectTypeDialog() {
        val sortList = LandOpenHelper.readPlantTypes(this)
        val adapter = PopupMenuAdapter(this, sortList)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setAdapter(adapter) { dialog, _ -> dialog.dismiss() }
        alertDialog = alertDialogBuilder.show()
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
        selectedObjectType = item!!
        changeButtonIcon(item)
        alertDialog?.dismiss()
    }

    private fun changeButtonIcon(item: PlantTypeObject) {
        placeObject.foreground = ContextCompat.getDrawable(this, item.icon)
    }
}
