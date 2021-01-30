package com.gago.david.myland

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.gago.david.myland.EditItemTypeFragment.Companion.newInstance
import com.gago.david.myland.EditTaskTypeFragment.Companion.newInstance
import com.gago.david.myland.MainActivity
import com.gago.david.myland.SettingsFragment.OnTaskListFragmentInteractionListener
import com.gago.david.myland.adapters.ItemTypeAdapter
import com.gago.david.myland.adapters.TaskTypeAdapter
import com.gago.david.myland.models.LandObject
import com.gago.david.myland.models.PlantTypeObject
import com.gago.david.myland.models.TaskTypeObject
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, LandFragment.OnListFragmentInteractionListener, AddLandDetailsFragment.OnFragmentInteractionListener, SettingsFragment.OnListFragmentInteractionListener, OnTaskListFragmentInteractionListener, EditTaskTypeFragment.OnFragmentInteractionListener, EditItemTypeFragment.OnFragmentInteractionListener, ColorPickerDialogListener {
    private var logout = false
    private var tasks: ArrayList<TaskTypeObject>? = null
    private var taskTypeAdapter: TaskTypeAdapter? = null
    private var items: ArrayList<PlantTypeObject>? = null
    private var itemTypeAdapter: ItemTypeAdapter? = null
    private var fragment: Fragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //deleteDatabase("myland.db");
        //Log.v("tree", ""+R.drawable.ic_tree);
        val user = intent.getStringExtra(INTENT_USER)
                ?: throw IllegalStateException("field " + INTENT_USER + " missing in Intent")
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        val headerView = navigationView.getHeaderView(0)
        val userView = headerView.findViewById<TextView>(R.id.user)
        userView.text = user
        val navLayout = headerView.findViewById<LinearLayout>(R.id.nav_layout)
        navLayout.setOnClickListener {
            val intent = Intent(this@MainActivity, Login::class.java)
            startActivity(intent)
        }
        val fragment = LandFragment()
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                .add(R.id.fragment_container, fragment, LandFragment::class.java.name)
                .commit()
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            if (supportFragmentManager.backStackEntryCount == 0 && !logout) {
                Toast.makeText(this, R.string.leave_message, Toast.LENGTH_LONG).show()
                logout = true
                val handler = Handler()
                handler.postDelayed({ logout = false }, 5000) // 5000ms delay
            } else if (supportFragmentManager.backStackEntryCount == 0) {
                finish()
            } else if (supportFragmentManager.backStackEntryCount > 0) supportFragmentManager.popBackStack() else super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        if (id == R.id.lands) {
            val fragment = LandFragment()
            replaceFragmentFromMenu(fragment)
        } else if (id == R.id.settings) {
            val fragment = SettingsFragment()
            replaceFragmentFromMenu(fragment)
        } else if (id == R.id.exit) {
            finish()
        }
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun replaceFragmentFromMenu(fragment: Fragment) {
        val backStateName = fragment.javaClass.name
        val manager = supportFragmentManager
        if (backStateName == LandFragment::class.java.name) manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE) else {
            manager.popBackStack(backStateName, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            val ft = manager.beginTransaction()
            ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
            ft.replace(R.id.fragment_container, fragment)
            ft.addToBackStack(backStateName)
            ft.commit()
        }
        logout = false
    }

    fun setActionBarTitle(title: String?) {
        setTitle(title)
    }

    fun addLandDetails(filename: String?, area: Double?) {
        setActionBarTitle("Land Details")
        val fragment: Fragment = AddLandDetailsFragment()
        val args = Bundle()
        args.putString("filename", filename)
        args.putDouble("area", area!!)
        fragment.arguments = args
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment).addToBackStack("main").commit()
    }

    fun removeLandDetails() {
        val fragment: Fragment = AddLandDetailsFragment()
        val manager = supportFragmentManager
        val trans = manager.beginTransaction()
        trans.remove(fragment)
        trans.commit()
        manager.popBackStack()
    }

    override fun onFragmentInteraction(uri: Uri?) {}
    override fun onListFragmentInteraction(item: LandObject) {
        val intent = Intent(this, ScrollingActivity::class.java)
        val b = Bundle()
        b.putString("name", item.name) //Your id
        intent.putExtras(b) //Put your id to your next Intent
        startActivityForResult(intent, 2)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                val menu = data!!.getStringExtra("menu")
                if (menu == "settings") {
                    val fragment = SettingsFragment()
                    val backStateName = fragment.javaClass.name
                    val manager = supportFragmentManager
                    val ft = manager.beginTransaction()
                    ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                    ft.replace(R.id.fragment_container, fragment)
                    ft.addToBackStack(backStateName)
                    ft.commit()
                }
            }
        }
    }

    override fun selectItem(item: PlantTypeObject) {
        fragment = newInstance(item, false)
        replaceFragmentFromMenu(fragment!!)
    }

    override fun removeItem(item: PlantTypeObject): Boolean {
        val mDbHelper = LandOpenHelper(this)

        // Gets the data repository in write mode
        val db = mDbHelper.writableDatabase
        val whereClause = "Name = ?"
        val whereArgs = arrayOf(item.name)
        val i = db.delete("PlantTypes", whereClause, whereArgs)
        Log.v("Remove item", "$i rows removed")
        db.close()
        return i > 0
    }

    override fun addItem(itemAdapter: ItemTypeAdapter, items: ArrayList<PlantTypeObject>) {
        itemTypeAdapter = itemAdapter
        this.items = items
        val item = PlantTypeObject("", R.drawable.ic_tree, "")
        fragment = newInstance(item, true)
        replaceFragmentFromMenu(fragment!!)
    }

    override fun selectItem(item: TaskTypeObject) {
        replaceFragmentFromMenu(newInstance(item, false))
    }

    override fun removeItem(item: TaskTypeObject): Boolean {
        val mDbHelper = LandOpenHelper(this)

        // Gets the data repository in write mode
        val db = mDbHelper.writableDatabase
        val whereClause = "Name = ?"
        val whereArgs = arrayOf(item.name)
        val i = db.delete("TaskTypes", whereClause, whereArgs)
        Log.v("Remove TaskType", "$i rows removed")
        db.close()
        return i > 0
    }

    override fun addTaskType(taskAdapter: TaskTypeAdapter, tasks: ArrayList<TaskTypeObject>) {
        this.tasks = tasks
        taskTypeAdapter = taskAdapter
        val item = TaskTypeObject("", "")
        replaceFragmentFromMenu(newInstance(item, true))
    }

    override fun addItem(item: TaskTypeObject?) {
        val success = LandOpenHelper.addTaskType(this, item)
        if (!success) {
            Toast.makeText(this, R.string.task_type_add_error, Toast.LENGTH_SHORT).show()
            Log.v("Add TaskType", "Failed to insert task type: " + item.toString())
        } else {
            if (taskTypeAdapter != null && tasks != null) {
                tasks!!.add(item!!)
                taskTypeAdapter!!.notifyDataSetChanged()
            }
            Toast.makeText(this, R.string.task_type_add_success, Toast.LENGTH_SHORT).show()
            Log.v("Add TaskType", "row inserted")
            val fragment: Fragment = EditTaskTypeFragment()
            val manager = supportFragmentManager
            val trans = manager.beginTransaction()
            trans.remove(fragment)
            trans.commit()
            manager.popBackStack()
        }
    }

    override fun onFragmentInteraction(taskType: TaskTypeObject?) {
        val mDbHelper = LandOpenHelper(this)

        // Gets the data repository in write mode
        val db = mDbHelper.writableDatabase

// Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put("Name", taskType!!.name)
        values.put("Description", taskType.description)

// Insert the new row, returning the primary key value of the new row
        val whereClause = "Name = ?"
        val whereArgs = arrayOf(taskType.name)
        val newRowId = db.update("TaskTypes", values, whereClause, whereArgs).toLong()
        Log.v("Update TaskType", "row updated: $newRowId")
        if (newRowId == -1L) Toast.makeText(this, R.string.task_type_update_error, Toast.LENGTH_SHORT).show() else {
            Toast.makeText(this, R.string.task_type_update_success, Toast.LENGTH_SHORT).show()
        }
        db.close()
        val fragment: Fragment = EditTaskTypeFragment()
        val manager = supportFragmentManager
        val trans = manager.beginTransaction()
        trans.remove(fragment)
        trans.commit()
        manager.popBackStack()
    }

    override fun onFragmentInteraction(itemType: PlantTypeObject) {
        val mDbHelper = LandOpenHelper(this)

        // Gets the data repository in write mode
        val db = mDbHelper.writableDatabase

// Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put("Name", itemType.name)
        values.put("Icon", itemType.icon)
        values.put("Color", itemType.color)

// Insert the new row, returning the primary key value of the new row
        val whereClause = "Name = ?"
        val whereArgs = arrayOf(itemType.name)
        val newRowId = db.update("PlantTypes", values, whereClause, whereArgs).toLong()
        Log.v("Update PlantType", "row updated: $newRowId")
        if (newRowId == -1L) Toast.makeText(this, R.string.item_type_update_error, Toast.LENGTH_SHORT).show() else {
            Toast.makeText(this, R.string.item_type_update_success, Toast.LENGTH_SHORT).show()
        }
        db.close()
        val fragment: Fragment = EditItemTypeFragment()
        val manager = supportFragmentManager
        val trans = manager.beginTransaction()
        trans.remove(fragment)
        trans.commit()
        manager.popBackStack()
    }

    override fun addItem(item: PlantTypeObject) {
        val success = LandOpenHelper.addItemType(this, item)
        if (!success) {
            Toast.makeText(this, R.string.item_type_add_error, Toast.LENGTH_SHORT).show()
            Log.v("Add Item", "Failed to insert item: $item")
        } else {
            if (itemTypeAdapter != null && items != null) {
                items!!.add(item)
                itemTypeAdapter!!.notifyDataSetChanged()
            }
            Toast.makeText(this, R.string.item_type_add_success, Toast.LENGTH_SHORT).show()
            Log.v("Add Item", "row inserted")
            val fragment: Fragment = EditItemTypeFragment()
            val manager = supportFragmentManager
            val trans = manager.beginTransaction()
            trans.remove(fragment)
            trans.commit()
            manager.popBackStack()
        }
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        val strColor = String.format("#%06X", 0xFFFFFF and color)
        if (fragment is EditItemTypeFragment) {
            (fragment as EditItemTypeFragment).setColor(strColor)
        }
    }

    override fun onDialogDismissed(dialogId: Int) {}

    companion object {
        private const val INTENT_USER = "user"
        fun newIntent(context: Context?, user: String?): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(INTENT_USER, user)
            return intent
        }
    }
}