package com.gago.david.myland

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gago.david.myland.adapters.TaskHistoryRecyclerViewAdapter
import com.gago.david.myland.adapters.TaskListAdapter
import com.gago.david.myland.fragments.DeleteItemDialogFragment
import com.gago.david.myland.models.*
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.lantouzi.wheelview.WheelView
import com.lantouzi.wheelview.WheelView.OnWheelItemSelectedListener
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.turf.TurfMeasurement
import java.text.MessageFormat
import java.text.NumberFormat
import kotlin.math.roundToInt


class ScrollingActivity : AppCompatActivity(), AddTaskFragment.OnFragmentInteractionListener, NavigationView.OnNavigationItemSelectedListener, TaskEditFragment.OnFragmentInteractionListener {
    private lateinit var taskHistory: ArrayList<TaskObject>
    private lateinit var taskHistoryFiltered: MutableList<TaskObject>
    private var wheel: WheelView? = null
    private var land: LandObject? = null
    private var name: String? = null
    private var first = true
    private lateinit var toolbarLayout: CollapsingToolbarLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var emptyTaskList: View
    private var plantTypeList: ArrayList<PlantTypeObject>? = null
    private var selected = 0
    private var editButton: FloatingActionButton? = null
    private var addTaskButton: FloatingActionButton? = null
    private lateinit var removeButton: MenuItem
    private var editLandButton: MenuItem? = null
    private var doneButton: FloatingActionButton? = null
    private var deleteButton: FloatingActionButton? = null
    private var mAdapter: TaskListAdapter? = null
    private var tasks = ArrayList<TaskObject>()
    private var description: TextView? = null
    private var state: TextView? = null
    private var landTitle: TextView? = null
    private var descriptionLayout: LinearLayout? = null
    private var fragment: Fragment? = null
    private var priorities: List<PriorityObject>? = null
    private var plantGroups: Map<String, List<PlantObject>>? = null
    private lateinit var mapView: MapView
    private lateinit var polylineAnnotationManager: PolylineAnnotationManager
    private lateinit var annotationManager: PointAnnotationManager
    private var selectedObjects: List<PlantObject> = emptyList()
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = intent.extras
        name = b!!.getString("name")
        if (name == null) finish()
        land = LandOpenHelper.readLandWithArea(name, this)
        priorities = LandOpenHelper.readPriorities(this)
        setContentView(R.layout.activity_scrolling)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        val prefs = getSharedPreferences(SettingsFragment.MY_PREFS_NAME, MODE_PRIVATE)
        val user = prefs.getString("user", "")
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        val headerView = navigationView.getHeaderView(0)
        val userView = headerView.findViewById<TextView>(R.id.user)
        userView.text = user
        val navLayout = headerView.findViewById<LinearLayout>(R.id.nav_layout)
        navLayout.setOnClickListener {
            val intent = Intent(this@ScrollingActivity, Login::class.java)
            startActivity(intent)
        }
        editButton = findViewById(R.id.fab)
        editButton?.setOnClickListener {
            editLand()
        }
        doneButton = findViewById(R.id.done)
        deleteButton = findViewById(R.id.delete)
        doneButton?.setOnClickListener { view: View ->
            val alertDialog = AlertDialog.Builder(view.rootView.context)
            alertDialog.setTitle(R.string.close)
            alertDialog.setMessage(R.string.close_task_ask)
            alertDialog.setPositiveButton(R.string.yes
            ) { _: DialogInterface?, _: Int ->
                if (fragment is TaskEditFragment) {
                    val task = (fragment as TaskEditFragment).closeTask()
                    supportFragmentManager.popBackStack()
                    tasks.remove(task)
                    filter()
                    doneButton?.visibility = View.GONE
                    deleteButton?.visibility = View.GONE
                }
            }
            alertDialog.setNegativeButton(R.string.no
            ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            alertDialog.show()
        }
        deleteButton?.setOnClickListener { view: View ->
            val alertDialog = AlertDialog.Builder(view.rootView.context)
            alertDialog.setTitle(R.string.delete)
            alertDialog.setMessage(R.string.delete_task)
            alertDialog.setPositiveButton(R.string.yes
            ) { _: DialogInterface?, _: Int ->
                if (fragment is TaskEditFragment) {
                    val task = (fragment as TaskEditFragment).deleteTask()
                    supportFragmentManager.popBackStack()
                    tasks.remove(task)
                    filter()
                    doneButton?.visibility = View.GONE
                    deleteButton?.visibility = View.GONE
                }
            }
            alertDialog.setNegativeButton(R.string.no
            ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            alertDialog.show()
        }
        addTaskButton = findViewById(R.id.add_task_button)
        addTaskButton?.setOnClickListener { addTaskPressed() }
        toolbarLayout = findViewById(R.id.toolbar_layout)
        mapView = findViewById(R.id.mapView)
        val annotationApi = mapView.annotations
        polylineAnnotationManager = annotationApi.createPolylineAnnotationManager()
        annotationManager = annotationApi.createPointAnnotationManager()

        val coordinatorLayout: CoordinatorLayout = findViewById(R.id.coordinatorLayout)
        mapView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> coordinatorLayout.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> coordinatorLayout.requestDisallowInterceptTouchEvent(
                    false
                )
            }
            super.onTouchEvent(event)
        }
        mapView.getMapboxMap().addOnMapClickListener {
            selectClosestObject(it)
            setExistingObjects(land!!.plants)
            true
        }

        toolbarLayout.viewTreeObserver?.addOnGlobalLayoutListener {
            if (first) {
                filter()
                drawTrees()
                first = false
            }
        }
        appBarLayout = findViewById(R.id.app_bar)
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            if (verticalOffset == 0) {
                editButton?.visibility = View.VISIBLE
                editLandButton?.isVisible = false
            }
            else {
                editButton?.visibility = View.INVISIBLE
                editLandButton?.isVisible = true
            }
        })
        val recyclerView = findViewById<RecyclerView>(R.id.task_list)
        recyclerView.layoutManager =
            LinearLayoutManager(this)
        tasks = LandOpenHelper.readTasks(this, land!!.name)
        emptyTaskList = findViewById<View>(R.id.emptyTaskList)
        mAdapter = TaskListAdapter(tasks, this, priorities)
        recyclerView.adapter = mAdapter
        wheel = findViewById(R.id.wheelview)
        wheel?.setOnWheelItemSelectedListener(object : OnWheelItemSelectedListener {
            override fun onWheelItemChanged(wheelView: WheelView, position: Int) {
                selected = position
                filter()
                drawTrees()
                updateHistory()
                updateProgressBar()
            }

            override fun onWheelItemSelected(wheelView: WheelView, position: Int) {}
        })
        plantTypeList = LandOpenHelper.readPlantTypes(this)
        val unit = prefs.getInt("unit", 0) //0 is the default value.
        val area = when (unit) {
            0 -> " (" + NumberFormat.getInstance().format(land!!.area.roundToInt()) + "m\u00B2)"
            else -> " (" + NumberFormat.getInstance().format(land!!.area / 10000) + "ha)"
        }
        title = ""
        landTitle = findViewById(R.id.land_title_size)
        landTitle?.text = MessageFormat.format("{0}{1}", land!!.name, if (land!!.area == 0.0) "" else area)
        description = findViewById(R.id.scrolling_description)
        description?.text = land!!.description
        descriptionLayout = findViewById(R.id.description_layout)
        descriptionLayout?.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this@ScrollingActivity)
            val input = EditText(this@ScrollingActivity)
            if (selected < plantGroups?.keys!!.size + 2) {
                alertDialog.setTitle(R.string.edit_land)
                input.setText(land!!.description)
            } else {
                alertDialog.setTitle(R.string.edit_item)
                input.setText(land!!.plants[selected - 2 - plantGroups?.keys!!.size].description)
            }
            alertDialog.setMessage(R.string.state)
            val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT)
            input.layoutParams = lp
            alertDialog.setView(input)
            val filter: ColorFilter = PorterDuffColorFilter(resources.getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN)
            val icon = resources.getDrawable(R.drawable.ic_action_edit, theme)
            icon.colorFilter = filter
            alertDialog.setIcon(icon)
            alertDialog.setPositiveButton("OK"
            ) { dialog: DialogInterface?, which: Int ->
                description?.text = input.text
                if (selected < plantGroups?.keys!!.size + 2) {
                    land!!.description = input.text.toString()
                    updateLand()
                } else {
                    land!!.plants[selected - 2 - plantGroups?.keys!!.size].description = input.text.toString()
                    updateItem(land!!.plants[selected - 2 - plantGroups?.keys!!.size])
                }
            }
            alertDialog.setNegativeButton(resources.getString(R.string.cancel)
            ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            alertDialog.show()
        }
        state = findViewById(R.id.state)

        taskHistory = LandOpenHelper.readTaskHistory(this, land!!.name)
        taskHistoryFiltered = taskHistory.toMutableList()
        findViewById<RecyclerView>(R.id.task_history_list).apply {
            layoutManager =
                LinearLayoutManager(context)
            adapter = TaskHistoryRecyclerViewAdapter(taskHistoryFiltered)
        }
        progressBar = findViewById(R.id.land_progress)
        updateProgressBar()
    }

    private fun selectClosestObject(click: Point) {
        val min = land!!.plants.minByOrNull { plantObject -> TurfMeasurement.distance(Point.fromLngLat(plantObject.lon, plantObject.lat), click) }
        min?.let {
            selectedObjects = listOf(it)
            val indexOfObject = land?.plants?.indexOf(it)!! + 2 + plantGroups?.keys!!.size
            wheel?.smoothSelectIndex(indexOfObject)
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

    private fun setExistingObjects(objects: List<PlantObject>) {
        annotationManager.deleteAll()
        val objectTypes = LandOpenHelper.readPlantTypes(this)
        objects.forEach {
            val type = objectTypes.find { type -> type.name == it.plantType }!!
            val selected = selectedObjects.contains(it)
            val icon = getObjectIconPainted(type, selected)

            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(it.lon, it.lat))
                .withIconImage(icon)
            annotationManager.create(pointAnnotationOptions)
        }
    }

    private fun getObjectIconPainted(type: PlantTypeObject, selected: Boolean = false): Bitmap {
        val myIcon = ContextCompat.getDrawable(this, type.icon)
        val bitmap = (myIcon as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true);
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

    private fun editLand() {
        val intent = Intent(applicationContext, LandEditMapActivity::class.java)
        val b1 = Bundle()
        b1.putString("name", name)
        intent.putExtras(b1)
        startActivity(intent)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateHistory() {
        taskHistoryFiltered.clear()
        when {
            selected == 0 -> taskHistoryFiltered.addAll(taskHistory)
            selected == 1 -> taskHistoryFiltered.addAll(taskHistory.filter { it.plantIndex == null })
            selected < plantGroups?.keys!!.size + 2 -> {
                val plantType = plantGroups!!.keys.toList()[selected - 2]
                val plantIndexes = land!!.plants.filter { it.plantType == plantType }.map { it.id }
                taskHistoryFiltered.addAll(taskHistory.filter { plantIndexes.contains(it.plantIndex)})
            }
            else -> {
                val plantIndex = land!!.plants[selected - 2 - plantGroups?.keys!!.size].id
                taskHistoryFiltered.addAll(taskHistory.filter { it.plantIndex == plantIndex})
            }
        }
        findViewById<RecyclerView>(R.id.task_history_list).adapter?.notifyDataSetChanged()
    }

    private fun updateProgressBar() {
        val tasksSize = when {
            selected == 0 -> tasks.size
            selected == 1 -> tasks.filter { it.plantIndex == null }.size
            selected < plantGroups?.keys!!.size + 2 -> {
                val plantType = plantGroups!!.keys.toList()[selected - 2]
                val plantIndexes = land!!.plants.filter { it.plantType == plantType }.map { it.id }
                tasks.filter { plantIndexes.contains(it.plantIndex)}.size
            }
            else -> {
                val plantIndex = land!!.plants[selected - 2 - plantGroups?.keys!!.size].id
                tasks.filter { it.plantIndex == plantIndex}.size
            }
        }
        val taskHistorySize = taskHistoryFiltered.size
        val progress = taskHistorySize.toFloat() / (taskHistorySize + tasksSize) * 100
        progressBar.setProgress(progress.toInt(), true)

        //TODO: refactor
        emptyTaskList.visibility = if ((taskHistorySize + tasksSize) == 0)  View.VISIBLE else View.GONE
    }

    override fun setContentView(view: View) {
        super.setContentView(view)

        //scrollView.fullScroll(NestedScrollView.FOCUS_UP);
    }

    @SuppressLint("RestrictedApi")
    private fun filter() {
        selectedObjects = emptyList()
        when {
            selected == 0 -> {
                mAdapter!!.filter.filter("all")
                selectedObjects = land?.plants?.toList()!!
            }
            selected == 1 -> mAdapter!!.filter.filter("land")

            selected < plantGroups?.keys!!.size + 2 -> {
                var filter = "group"
                for (p in land!!.plants)
                    if (plantGroups!!.keys.toList()[selected - 2] == p.plantType) {
                        filter = filter + " " + p.id
                        selectedObjects = selectedObjects + p
                    }
                mAdapter!!.filter.filter(filter)
            }
            else -> {
                mAdapter!!.filter.filter("item " + land!!.plants[selected - 2 - plantGroups?.keys!!.size].id)
                selectedObjects = listOf(land!!.plants[selected - 2 - plantGroups?.keys!!.size])
            }
        }
        if (!first)
            removeButton.isEnabled = selected >= plantGroups?.keys!!.size + 2
        if (selected < 1)
            addTaskButton!!.visibility = View.GONE
        else addTaskButton!!.visibility = View.VISIBLE
        if (selected < plantGroups?.keys!!.size + 2 && description!!.text.toString() != land!!.description)
            setDescription(land!!.description, R.string.land_state)
        else if (selected >= plantGroups?.keys!!.size + 2 && description!!.text.toString() != land!!.plants[selected - 2 - plantGroups?.keys!!.size].description) setDescription(land!!.plants[selected - 2 - plantGroups?.keys!!.size].description, R.string.item_state)
    }

    @SuppressLint("RestrictedApi")
    private fun addTaskPressed() {
        val fragment = AddTaskFragment()
        val args = Bundle()
        args.putString("land", land!!.name)
        var type = "all"
        val list = ArrayList<Int>()
        when {
            wheel!!.selectedPosition == 0 -> for (p in land!!.plants) list.add(p.id)
            wheel!!.selectedPosition == 1 -> type = "land"
            wheel!!.selectedPosition - 2 < plantGroups?.keys!!.size -> {
                for (p in land!!.plants)
                    if (plantGroups!!.keys.toList()[wheel!!.selectedPosition - 2] == p.plantType) list.add(p.id)
                type = "group"
            }
            else -> {
                list.add(land!!.plants[wheel!!.selectedPosition - 2 - plantGroups?.keys!!.size].id)
                type = "item"
            }
        }
        Log.v("ScrollingActivity", "type:$type list:$list")
        args.putString("type", type)
        args.putIntegerArrayList("plandIndex", list)
        fragment.arguments = args
        val manager = supportFragmentManager
        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_vertical, R.anim.exit_vertical, R.anim.pop_enter_vertical, R.anim.pop_exit_vertical)
                .add(R.id.add_fragment_container, fragment, LandFragment::class.java.name)
                .addToBackStack(fragment.javaClass.name)
                .commit()
        editButton!!.visibility = View.VISIBLE
        addTaskButton!!.visibility = View.GONE
        removeButton.isEnabled = false
    }

    private fun drawTrees() {
        setExistingObjects(land!!.plants)
    }

    fun removeTree(view: View) {
        val p = land!!.plants[selected - 2 - plantGroups?.keys!!.size]
        var plantTypeObject: PlantTypeObject? = null
        for (pl in plantTypeList!!) if (pl.name == p.plantType) {
            plantTypeObject = pl
            break
        }
        DeleteItemDialogFragment(plantTypeObject!!).show(supportFragmentManager, "delete-item-dialog")
    }

    fun deleteSelectedItem() {
        val plant: PlantObject = land!!.plants[selected - 2 - plantGroups?.keys!!.size]
        LandOpenHelper.deletePlantObject(this, plant)
        val id = plant.id
        selected--
        val iterator = tasks.iterator()
        while (iterator.hasNext()) {
            val t = iterator.next()
            if (t.plantIndex != null && t.plantIndex == id) iterator.remove()
        }
        filter()
        initiateStuff()
    }

    private fun deleteLand(view: View) {
        val alertDialog = AlertDialog.Builder(view.rootView.context)
        alertDialog.setTitle(land!!.name)
        alertDialog.setMessage(R.string.remove_land)
        alertDialog.setPositiveButton(R.string.yes
        ) { _: DialogInterface?, _: Int ->
            LandOpenHelper.deleteLand(land!!, this@ScrollingActivity)
            finish()
        }
        alertDialog.setNegativeButton(R.string.no
        ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        alertDialog.show()
    }

    override fun onResume() {
        super.onResume()
        initiateStuff()
    }

    private fun initiateStuff() {
        land = LandOpenHelper.readLandWithArea(name, this)
        val strings: MutableList<String> = ArrayList(land!!.plants.size)
        val plants = land!!.plants.toList()
        plantGroups = plants.groupBy { it.plantType }
        strings.add(resources.getString(R.string.all_tasks))
        strings.add(resources.getString(R.string.land))
        for (p in plantGroups!!.keys)
            if (plantGroups!!.containsKey(p))
                strings.add(p + " (" + plantGroups!!.getOrDefault(p, listOf()).size + ")")
        for (p in land!!.plants)
                strings.add(p.plantType)
        wheel!!.items = strings
        wheel!!.minSelectableIndex = 0
        wheel!!.maxSelectableIndex = strings.size - 1

        setCameraPosition(land!!)
        setLandBorders(land!!)

        drawTrees()
    }

    @SuppressLint("RestrictedApi")
    fun showButtons() {
        editButton!!.visibility = View.VISIBLE
        addTaskButton!!.visibility = View.VISIBLE
        if (selected > plantGroups?.keys!!.size + 1) removeButton.isEnabled = true
    }

    override fun onFragmentInteraction(tasks: ArrayList<TaskObject>?) {
        val fragment: Fragment = AddTaskFragment()
        val manager = supportFragmentManager
        val trans = manager.beginTransaction()
        trans.remove(fragment)
        trans.commit()
        manager.popBackStack()
        Log.v("tasks", tasks.toString())
        addTaskQuery(tasks!!)
        filter()
    }

    private fun addTaskQuery(tasks: ArrayList<TaskObject>) {
        val mDbHelper = LandOpenHelper(this)
        var success = true

        // Gets the data repository in write mode
        val db = mDbHelper.writableDatabase
        val prefs = getSharedPreferences(SettingsFragment.MY_PREFS_NAME, MODE_PRIVATE)
        val user = prefs.getString("user", "")
        for (t in tasks) {
            val values = ContentValues()
            values.put("Land", t.land)
            values.put(LandContract.TaskEntry.COLUMN_USER, user)
            values.put("PlantIndex", t.plantIndex)
            values.put("TaskType", t.taskType)
            values.put("Priority", t.priority)
            values.put("CreationDate", t.creationDate.time)
            if (t.targetDate != null) values.put("ExpirationDate", t.targetDate!!.time)
            values.put("Archived", t.archived)
            values.put("Completed", t.completed)
            values.put("Observations", t.observations)

// Insert the new row, returning the primary key value of the new row
            val newRowId = db.insert("Tasks", null, values)
            if (newRowId == -1L) success = false else {
                Log.v("AddTask", "row inserted: $newRowId")
                this.tasks.add(t)
                t.rowid = newRowId
            }
        }
        if (!success) Toast.makeText(this, R.string.task_add_error, Toast.LENGTH_SHORT).show() else Toast.makeText(this, R.string.task_added, Toast.LENGTH_SHORT).show()
        db.close()
    }

    private fun updateLand() {
        val mDbHelper = LandOpenHelper(this)

        // Gets the data repository in write mode
        val db = mDbHelper.writableDatabase

// Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put("Name", land!!.name)
        values.put("ImageUri", land!!.imageUri)
        values.put("Description", land!!.description)

// Insert the new row, returning the primary key value of the new row
        val whereClause = "Name = ?"
        val whereArgs = arrayOf(land!!.name)
        val newRowId = db.update("Lands", values, whereClause, whereArgs).toLong()
        Log.v("Update Detail", "row updated: $newRowId")
        if (newRowId == -1L) Toast.makeText(this, R.string.land_update_error, Toast.LENGTH_SHORT).show() else {
            Toast.makeText(this, R.string.land_update_success, Toast.LENGTH_SHORT).show()
            //meter na layer
        }
        db.close()
    }

    private fun updateItem(p: PlantObject) {
        val mDbHelper = LandOpenHelper(this)

        // Gets the data repository in write mode
        val db = mDbHelper.writableDatabase

// Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put("Id", p.id)
        values.put("Land", land!!.name)
        values.put("description", p.description)
        values.put("PlantType", p.plantType)
        values.put("lat", p.lat)
        values.put("lon", p.lon)

// Insert the new row, returning the primary key value of the new row
        val whereClause = "Id = ?"
        val whereArgs = arrayOf("" + p.id)
        val newRowId = db.update("Plants", values, whereClause, whereArgs).toLong()
        Log.v("Update item", "row updated: $newRowId")
        if (newRowId == -1L) Toast.makeText(this, R.string.item_update_error, Toast.LENGTH_SHORT).show() else {
            Toast.makeText(this, R.string.item_update_success, Toast.LENGTH_SHORT).show()
            //meter na layer
        }
        db.close()
    }

    private fun setDescription(s: String, title: Int) {
        Log.v("height", s)
        val measuredTextHeight = AddTaskFragment.getHeight(this, s, 14, description!!.width, Typeface.DEFAULT)
        val anim = ValueAnimator.ofInt(description!!.measuredHeight, measuredTextHeight)
        anim.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams = description!!.layoutParams
            layoutParams.height = `val`
            description!!.layoutParams = layoutParams
        }
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                description!!.text = s
                state!!.setText(title)
                val fadeIn = AlphaAnimation(0.0f, 1.0f)
                descriptionLayout!!.startAnimation(fadeIn)
                fadeIn.duration = 300
                fadeIn.fillAfter = true
                fadeIn.startOffset = 300
            }

            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                val fadeOut = AlphaAnimation(1.0f, 0.0f)
                fadeOut.duration = 300
                fadeOut.fillAfter = true
                descriptionLayout!!.startAnimation(fadeOut)
            }
        })
        anim.duration = 300
        anim.start()
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_scrolling, menu)
        removeButton = menu!!.findItem(R.id.menu_delete_item)
        editLandButton = menu.findItem(R.id.menu_edit_land)
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_delete_land -> deleteLand(findViewById(R.id.menu_delete_land))
            R.id.menu_delete_item -> removeTree(findViewById(R.id.menu_delete_item))
            R.id.menu_edit_land -> editLand()
        }
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.lands -> {
                finishAfterTransition()
            }
            R.id.settings -> {
                val data = Intent()
                data.putExtra("menu", "settings")
                setResult(RESULT_OK, data)
                finish()
            }
            R.id.exit -> {
                val homeIntent = Intent(Intent.ACTION_MAIN)
                homeIntent.addCategory(Intent.CATEGORY_HOME)
                homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(homeIntent)
            }
        }
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    @SuppressLint("RestrictedApi")
    override fun selectTask(task: TaskObject) {
        val localFragment = TaskEditFragment.newInstance(task)
        fragment = localFragment
        val manager = supportFragmentManager
        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_vertical, R.anim.exit_vertical, R.anim.pop_enter_vertical, R.anim.pop_exit_vertical)
                .add(R.id.add_fragment_container, localFragment, LandFragment::class.java.name)
                .addToBackStack(localFragment.javaClass.name)
                .commit()
        addTaskButton!!.visibility = View.GONE
        doneButton!!.visibility = View.VISIBLE
        deleteButton!!.visibility = View.VISIBLE
        editButton!!.visibility = View.GONE
        removeButton.isEnabled = false
    }

    override fun updateTask(newTask: TaskObject, oldTask: TaskObject) {
        if (newTask.completed) {
            val success = LandOpenHelper.updateTask(newTask, this@ScrollingActivity)
            if (success) {
                Toast.makeText(this@ScrollingActivity, R.string.task_close_success, Toast.LENGTH_SHORT).show()
                taskHistory.add(newTask)
                updateHistory()
                updateProgressBar()
            } else Toast.makeText(this@ScrollingActivity, R.string.task_close_error, Toast.LENGTH_SHORT).show()
            filter()
        } else {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(R.string.update)
            alertDialog.setMessage(R.string.save_changes)
            alertDialog.setPositiveButton(R.string.yes
            ) { _: DialogInterface?, _: Int ->
                val success = LandOpenHelper.updateTask(newTask, this@ScrollingActivity)
                if (success) {
                    Toast.makeText(this@ScrollingActivity, R.string.update_task_success, Toast.LENGTH_SHORT).show()
                    tasks[tasks.indexOf(oldTask)] = newTask
                } else Toast.makeText(this@ScrollingActivity, R.string.update_task_error, Toast.LENGTH_SHORT).show()
                filter()
            }
            alertDialog.setNegativeButton(R.string.no
            ) { dialog: DialogInterface, _: Int ->
                dialog.cancel()
                filter()
            }
            alertDialog.show()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun notUpdateTask() {
        doneButton!!.visibility = View.GONE
        deleteButton!!.visibility = View.GONE
        editButton!!.visibility = View.VISIBLE
        mAdapter!!.notifyDataSetChanged()
    }

//    override fun onTouchEvent(ev: MotionEvent): Boolean {
//        Toast.makeText(this, "onTouch", Toast.LENGTH_SHORT).show()
//        return true
//    }

}