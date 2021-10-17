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
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.gago.david.myland.adapters.TaskListAdapter
import com.gago.david.myland.models.*
import com.lantouzi.wheelview.WheelView
import com.lantouzi.wheelview.WheelView.OnWheelItemSelectedListener
import java.text.DecimalFormat
import java.text.MessageFormat
import java.util.*
import kotlin.math.roundToInt

class ScrollingActivity : AppCompatActivity(), AddTaskFragment.OnFragmentInteractionListener, NavigationView.OnNavigationItemSelectedListener, TaskEditFragment.OnFragmentInteractionListener {
    private var wheel: WheelView? = null
    private var land: LandObject? = null
    private var name: String? = null
    private var first = true
    private lateinit var layers: Array<Drawable?>
    private var toolbarLayout: CollapsingToolbarLayout? = null
    private var plantTypeList: ArrayList<PlantTypeObject>? = null
    private var selected = 0
    private var editButton: FloatingActionButton? = null
    private var addTaskButton: FloatingActionButton? = null
    private var removeButton: FloatingActionButton? = null
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
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = intent.extras
        name = b!!.getString("name")
        if (name == null) finish()
        land = readLand(name)
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
            //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            //        .setAction("Action", null).show();
            val intent = Intent(applicationContext, LandEditActivity::class.java)
            val b1 = Bundle()
            b1.putString("name", name) //Your id
            intent.putExtras(b1) //Put your id to your next Intent
            startActivity(intent)
        }
        removeButton = findViewById(R.id.remove)
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
        layers = arrayOfNulls(2)
        layers[0] = BitmapDrawable(resources, LandOpenHelper.getImage(land!!.imageUri))
        toolbarLayout?.background = layers[0]
        /*try {
            InputStream inputStream = getContentResolver().openInputStream(Uri.parse(land.imageUri));
            Drawable yourDrawable = Drawable.createFromStream(inputStream, land.imageUri );
            layers[0] = yourDrawable;
            toolbarLayout.setBackground(yourDrawable);
        } catch (FileNotFoundException e) {
            //yourDrawable = getResources().getDrawable(R.drawable.default_image);
        }*/toolbarLayout?.viewTreeObserver?.addOnGlobalLayoutListener {
            if (first) {
                drawTrees()
                first = false
            }
        }
        val recyclerView = findViewById<RecyclerView>(R.id.task_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        tasks = LandOpenHelper.readTasks(this, land!!.name)
        mAdapter = TaskListAdapter(tasks, this, priorities)
        recyclerView.adapter = mAdapter
        wheel = findViewById(R.id.wheelview)
        wheel?.setOnWheelItemSelectedListener(object : OnWheelItemSelectedListener {
            override fun onWheelItemChanged(wheelView: WheelView, position: Int) {
                selected = position
                drawTrees()
                filter()
            }

            override fun onWheelItemSelected(wheelView: WheelView, position: Int) {}
        })
        plantTypeList = readPlantTypes()
        val unit = prefs.getInt("unit", 0) //0 is the default value.
        val df = DecimalFormat("#.#")
        val area = if (unit == 0) " (" + land!!.area.roundToInt() + "m\u00B2)" else " (" + df.format(land!!.area / 10000) + "ha)"
        title = ""
        landTitle = findViewById(R.id.land_title_size)
        landTitle?.text = MessageFormat.format("{0}{1}", land!!.name, if (land!!.area == 0.0) "" else area)
        description = findViewById(R.id.scrolling_description)
        description?.text = land!!.Description
        descriptionLayout = findViewById(R.id.description_layout)
        descriptionLayout?.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this@ScrollingActivity)
            val input = EditText(this@ScrollingActivity)
            if (selected < plantGroups?.keys!!.size + 2) {
                alertDialog.setTitle(R.string.edit_land)
                input.setText(land!!.Description)
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
            val icon = resources.getDrawable(R.drawable.ic_action_edit)
            icon.colorFilter = filter
            alertDialog.setIcon(icon)
            alertDialog.setPositiveButton("OK"
            ) { dialog: DialogInterface?, which: Int ->
                description?.text = input.text
                if (selected < plantGroups?.keys!!.size + 2) {
                    land!!.Description = input.text.toString()
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
    }

    override fun setContentView(view: View) {
        super.setContentView(view)

        //scrollView.fullScroll(NestedScrollView.FOCUS_UP);
    }

    @SuppressLint("RestrictedApi")
    private fun filter() {
        if (selected == 0)
            mAdapter!!.filter.filter("all")
        else if (selected == 1)
            mAdapter!!.filter.filter("land")
        else if (selected < plantGroups?.keys!!.size + 2) {
            var filter = "group"
            for (p in land!!.plants)
                if (plantGroups!!.keys.toList()[selected - 2] == p.plantType)
                    filter = filter + " " + p.id
            mAdapter!!.filter.filter(filter)
        } else
            mAdapter!!.filter.filter("item " + land!!.plants[selected - 2 - plantGroups?.keys!!.size].id)
        if (selected < plantGroups?.keys!!.size + 2)
            removeButton!!.visibility = View.GONE
        else removeButton!!.visibility = View.VISIBLE
        if (selected < 1)
            addTaskButton!!.visibility = View.GONE
        else addTaskButton!!.visibility = View.VISIBLE
        if (selected < plantGroups?.keys!!.size + 2 && description!!.text.toString() != land!!.Description)
            setDescription(land!!.Description, R.string.land_state)
        else if (selected >= plantGroups?.keys!!.size + 2 && description!!.text.toString() != land!!.plants[selected - 2 - plantGroups?.keys!!.size].description) setDescription(land!!.plants[selected - 2 - plantGroups?.keys!!.size].description, R.string.item_state)
    }

    @SuppressLint("RestrictedApi")
    private fun addTaskPressed() {
        val fragment = AddTaskFragment()
        val args = Bundle()
        args.putString("land", land!!.name)
        var type = "all"
        val list = ArrayList<Int>()
        if (wheel!!.selectedPosition == 0)
            for (p in land!!.plants) list.add(p.id)
        else if (wheel!!.selectedPosition == 1)
            type = "land"
        else if (wheel!!.selectedPosition - 2 < plantGroups?.keys!!.size) {
            for (p in land!!.plants)
                if (plantGroups!!.keys.toList()[wheel!!.selectedPosition - 2] == p.plantType) list.add(p.id)
                    type = "group"
        } else {
            list.add(land!!.plants[wheel!!.selectedPosition - 2 - plantGroups?.keys!!.size].id)
            type = "item"
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
        editButton!!.visibility = View.GONE
        addTaskButton!!.visibility = View.GONE
        removeButton!!.visibility = View.GONE
    }

    private fun drawTrees() {
        val r = 15
        val mPaint = Paint()
        mPaint.color = Color.RED
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 2.5f
        mPaint.isAntiAlias = true
        val xRatio = toolbarLayout!!.measuredWidth.toFloat() / layers[0]!!.intrinsicWidth.toFloat()
        val yRatio = toolbarLayout!!.measuredHeight.toFloat() / layers[0]!!.intrinsicHeight.toFloat()
        val xSize: Int
        val ySize: Int
        if (xRatio < yRatio) {
            xSize = Math.round(xRatio * layers[0]!!.intrinsicWidth)
            ySize = Math.round(xRatio * layers[0]!!.intrinsicHeight)
        } else {
            xSize = Math.round(yRatio * layers[0]!!.intrinsicWidth)
            ySize = Math.round(yRatio * layers[0]!!.intrinsicHeight)
        }
        val bitmap = Bitmap.createBitmap(xSize, ySize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        if (plantGroups != null)
            for (i in land!!.plants.indices){
                val p = land!!.plants[i]
            //for (group in plantGroups!!.keys){
                val plantType = plantTypeList!!.stream().filter { it.name == p.plantType } .findAny().get()
                val d = ContextCompat.getDrawable(this, plantType.icon)
                d!!.setBounds(Math.round(p.x * canvas.width) - d.intrinsicWidth / 8, Math.round(p.y * canvas.height - d.intrinsicHeight / 8),
                        Math.round(p.x * canvas.width) + d.intrinsicWidth / 8, Math.round(p.y * canvas.height) + d.intrinsicHeight / 8)
                d.colorFilter = PorterDuffColorFilter(Color.parseColor(plantType.color), PorterDuff.Mode.SRC_IN)
                //And draw it...
                d.draw(canvas)
                if (i + 2 + plantGroups!!.keys.size == selected || selected == 0 || selected - 2 < plantGroups!!.keys.size && selected > 1 && p.plantType == plantGroups!!.keys.toList()[selected - 2])
                    canvas.drawCircle(p.x * canvas.width, p.y * canvas.height, r.toFloat(), mPaint)


            }
        /*for (i in land!!.plants.indices) {
            val p = land!!.plants[i]
            for (type in plantTypeList!!)
                if (type.name == p.plantType) {
                    val d = ContextCompat.getDrawable(this, type.icon)
                    d!!.setBounds(Math.round(p.x * canvas.width) - d.intrinsicWidth / 8, Math.round(p.y * canvas.height - d.intrinsicHeight / 8),
                            Math.round(p.x * canvas.width) + d.intrinsicWidth / 8, Math.round(p.y * canvas.height) + d.intrinsicHeight / 8)
                    d.colorFilter = PorterDuffColorFilter(Color.parseColor(type.color), PorterDuff.Mode.SRC_IN)
                    //And draw it...
                    d.draw(canvas)
                    if (i + 2 + plantTypeList!!.size == selected || selected == 0 || selected - 2 < plantTypeList!!.size && selected > 1 && p.plantType == plantTypeList!![selected - 2].name)
                        canvas.drawCircle(p.x * canvas.width, p.y * canvas.height, r.toFloat(), mPaint)
                    break
                }
        }*/
        val drawable = BitmapDrawable(resources, bitmap)
        layers[1] = drawable
        toolbarLayout!!.background = LayerDrawable(layers)
    }

    fun removeTree(view: View) {
        val p = land!!.plants[selected - 2 - plantGroups?.keys!!.size]
        var plantTypeObject: PlantTypeObject? = null
        for (pl in plantTypeList!!) if (pl.name == p.plantType) {
            plantTypeObject = pl
            break
        }
        val alertDialog = AlertDialog.Builder(view.rootView.context)
        alertDialog.setTitle(p.plantType)
        alertDialog.setMessage(R.string.remove_tree)
        val filter: ColorFilter = PorterDuffColorFilter(Color.parseColor(plantTypeObject!!.color), PorterDuff.Mode.SRC_IN)
        val icon = view.rootView.context.resources.getDrawable(plantTypeObject.icon)
        icon.colorFilter = filter
        alertDialog.setIcon(icon)
        alertDialog.setPositiveButton(R.string.yes
        ) { _: DialogInterface?, _: Int ->
            val plant:PlantObject = land!!.plants[selected - 2 - plantGroups?.keys!!.size]
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
        alertDialog.setNegativeButton(R.string.no
        ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        alertDialog.show()
    }

    fun deleteLand(view: View) {
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

    private fun readPlantTypes(): ArrayList<PlantTypeObject> {
        val mDbHelper = LandOpenHelper(applicationContext)
        val db = mDbHelper.readableDatabase

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        val projection = arrayOf(
                "Name",
                "Icon",
                "Color"
        )

        // How you want the results sorted in the resulting Cursor
        val sortOrder: String? = null
        val cursor = db.query(
                "PlantTypes",  // The table to query
                projection,  // The array of columns to return (pass null to get all)
                null,  // The columns for the WHERE clause
                null,  // The values for the WHERE clause
                null,  // don't group the rows
                null,  // don't filter by row groups
                sortOrder // The sort order
        )
        val plants = ArrayList<PlantTypeObject>()
        while (cursor.moveToNext()) {
            val o = PlantTypeObject(cursor.getString(0), cursor.getInt(1), cursor.getString(2))
            plants.add(o)
        }
        cursor.close()
        db.close()
        return plants
    }

    private fun readLand(landName: String?): LandObject {
        val mDbHelper = LandOpenHelper(applicationContext)
        val db = mDbHelper.readableDatabase

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        val projection = arrayOf(
                "Name",
                "ImageUri",
                "Description",
                "Area"
        )

        // Filter results WHERE "title" = 'My Title'
        val selection = "Name" + " = ?"
        val selectionArgs = arrayOf(landName)

        // How you want the results sorted in the resulting Cursor
        val sortOrder: String? = null
        val cursor = db.query(
                "Lands",  // The table to query
                projection,  // The array of columns to return (pass null to get all)
                selection,  // The columns for the WHERE clause
                selectionArgs,  // The values for the WHERE clause
                null,  // don't group the rows
                null,  // don't filter by row groups
                sortOrder // The sort order
        )
        cursor.moveToFirst()
        val l = LandObject(landName!!, cursor.getString(1), cursor.getString(2), cursor.getDouble(cursor.getColumnIndex("Area")))
        cursor.close()
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        val projection2 = arrayOf(
                "Id",
                "Land",
                "PlantType",
                "Description",
                "x",
                "y"
        )

        // Filter results WHERE "title" = 'My Title'
        val selection2 = "Land" + " = ?"

        // How you want the results sorted in the resulting Cursor
        val sortOrder2 = "Id ASC"
        val cursor2 = db.query(
                "Plants",  // The table to query
                projection2,  // The array of columns to return (pass null to get all)
                selection2,  // The columns for the WHERE clause
                selectionArgs,  // The values for the WHERE clause
                null,  // don't group the rows
                null,  // don't filter by row groups
                sortOrder2 // The sort order
        )
        while (cursor2.moveToNext()) l.addPlant(PlantObject(cursor2.getInt(cursor2.getColumnIndex("Id")), cursor2.getString(2), cursor2.getString(3), cursor2.getFloat(4), cursor2.getFloat(5)))
        Log.v("ScrollingActivity", "" + cursor2.getColumnIndex("Id"))
        cursor.close()
        cursor2.close()
        db.close()
        return l
    }

    override fun onResume() {
        super.onResume()
        initiateStuff()
    }

    private fun initiateStuff() {
        land = readLand(name)
        val strings: MutableList<String> = ArrayList(land!!.plants.size)
        val plants = land!!.plants.toList()
        plantGroups = plants.groupBy { it.plantType }
        strings.add(resources.getString(R.string.all_tasks))
        strings.add(resources.getString(R.string.land))
        for (p in plantTypeList!!)
            if (plantGroups!!.containsKey(p.name))
                strings.add(p.name + " (" + plantGroups!!.getOrDefault(p.name, listOf()).size + ")")
        for (p in land!!.plants)
                strings.add(p.plantType)
        wheel!!.items = strings
        wheel!!.minSelectableIndex = 0
        wheel!!.maxSelectableIndex = strings.size - 1

        if (!first) drawTrees()
    }

    @SuppressLint("RestrictedApi")
    fun showButtons() {
        editButton!!.visibility = View.VISIBLE
        addTaskButton!!.visibility = View.VISIBLE
        if (selected > plantGroups?.keys!!.size + 1) removeButton!!.visibility = View.VISIBLE
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
        values.put("Description", land!!.Description)

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
        values.put("x", p.x)
        values.put("y", p.y)

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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.lands) {
            finishAfterTransition()
        } else if (id == R.id.settings) {
            val data = Intent()
            data.putExtra("menu", "settings")
            setResult(RESULT_OK, data)
            finish()
        } else if (id == R.id.exit) {
            val homeIntent = Intent(Intent.ACTION_MAIN)
            homeIntent.addCategory(Intent.CATEGORY_HOME)
            homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(homeIntent)
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
        removeButton!!.visibility = View.GONE
    }

    override fun updateTask(newTask: TaskObject, oldTask: TaskObject) {
        if (newTask.completed) {
            val success = LandOpenHelper.updateTask(newTask, this@ScrollingActivity)
            if (success) {
                Toast.makeText(this@ScrollingActivity, R.string.task_close_success, Toast.LENGTH_SHORT).show()
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
}