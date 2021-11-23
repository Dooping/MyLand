package com.gago.david.myland

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.Handler
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.*
import com.gago.david.myland.adapters.PopupMenuAdapter
import com.gago.david.myland.adapters.PopupMenuAdapter.OnMenuItemInteractionListener
import com.gago.david.myland.models.LandContract
import com.gago.david.myland.models.LandObject
import com.gago.david.myland.models.PlantObject
import com.gago.david.myland.models.PlantTypeObject
import com.github.chrisbanes.photoview.PhotoView
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class LandEditActivity : AppCompatActivity(), OnMenuItemInteractionListener {
    private val mHideHandler = Handler()
    private var lastX = 0.0f
    private var lastY = 0.0f
    private var tempX = 0f
    private var tempY = 0f
    var popupWindow: PopupWindow? = null
    var land: LandObject? = null
    private lateinit var layers: Array<Drawable?>
    var photo: PhotoView? = null
    private var first = true
    private lateinit var plantTypeList: ArrayList<PlantTypeObject>
    private var addedPlants: Stack<PlantObject> = Stack()
    private var mContentView: View? = null
    private lateinit var removePlantButton: CircleImageView
    private val mHidePart2Runnable = Runnable { // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        mContentView!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
    private var mControlsView: View? = null
    private val mShowPart2Runnable = Runnable { // Delayed display of UI elements
        val actionBar = supportActionBar
        actionBar?.show()
        mControlsView!!.visibility = View.VISIBLE
    }
    private var mVisible = false
    private val mHideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val mDelayHideTouchListener = OnTouchListener { view, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        view.performClick()
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_land_edit)
        mVisible = true
        mControlsView = findViewById(R.id.fullscreen_content_controls)
        mContentView = findViewById(R.id.photo_view)
        val layout = findViewById<View>(R.id.frame_layout)
        layout.setOnTouchListener { view: View?, motionEvent: MotionEvent ->
            if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                lastX = motionEvent.x
                lastY = motionEvent.y
            }
            view!!.performClick()
            false
        }
        photo = mContentView as PhotoView?
        val b = intent.extras
        val name = b!!.getString("name")
        land = LandOpenHelper.readLandWithArea(name, this)
        plantTypeList = LandOpenHelper.readPlantTypes(this)

        layers = arrayOfNulls(2)
        layers[0] = BitmapDrawable(resources, LandOpenHelper.getImage(this, land!!.imageUri))
        layers[1] = ColorDrawable(Color.TRANSPARENT)
        photo!!.setImageDrawable(LayerDrawable(layers))
        photo!!.viewTreeObserver.addOnGlobalLayoutListener {
            if (first) {
                drawTrees()
                first = false
            }
        }


        // Set up the user interaction to manually show or hide the system UI.
        mContentView!!.setOnClickListener { toggle() }
        mContentView!!.setOnLongClickListener { view: View ->
            popupWindowsort().showAtLocation(view, Gravity.NO_GRAVITY, lastX.roundToInt(), lastY.roundToInt())
            Log.v("EDIT", "should be showing at x:" + lastX.roundToInt() + " y:" + lastY.roundToInt())
            val viewX = lastX - photo!!.left
            val viewY = lastY - photo!!.top
            Log.v("photo", "$viewX:$viewY")
            val r = photo!!.displayRect
            tempX = (viewX - r.left) / r.width()
            tempY = (viewY - r.top) / r.height()
            Log.v("rect", r.toString())
            Log.v("coords", "$tempX:$tempY")
            false
        }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById<View>(R.id.exit_button).setOnClickListener { finish() }
        setTitle(R.string.edit_land_title)
        removePlantButton = findViewById(R.id.remove_last_plant)
        removePlantButton.circleBackgroundColor = Color.GRAY
        removePlantButton.setOnClickListener {
            if (addedPlants.empty()) {
                Toast.makeText(baseContext, R.string.plant_remove_empty, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val deletedPlant = addedPlants.pop()
            val success = LandOpenHelper.deletePlantObject(this, deletedPlant)
            if (success) {
                land!!.removePlant(deletedPlant)
                drawTrees()
                if (addedPlants.isEmpty()) (it as CircleImageView).circleBackgroundColor = Color.GRAY
            } else
                Toast.makeText(baseContext, R.string.plant_remove_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawTrees() {
        val r = 5
        val mPaint = Paint()
        mPaint.color = Color.RED
        val xRatio = (photo!!.measuredWidth / layers[0]!!.intrinsicWidth).toFloat()
        val yRatio = (photo!!.measuredHeight / layers[0]!!.intrinsicHeight).toFloat()
        val xSize: Int
        val ySize: Int
        if (xRatio < yRatio) {
            xSize = (xRatio * layers[0]!!.intrinsicWidth).roundToInt()
            ySize = (xRatio * layers[0]!!.intrinsicHeight).roundToInt()
        } else {
            xSize = (yRatio * layers[0]!!.intrinsicWidth).roundToInt()
            ySize = (yRatio * layers[0]!!.intrinsicHeight).roundToInt()
        }
        val bitmap = Bitmap.createBitmap(xSize, ySize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        for (p in land!!.plants) for (type in plantTypeList!!) if (type!!.name == p.plantType) {
            val d = ContextCompat.getDrawable(this, type.icon)
            d!!.setBounds(
                (p.x * canvas.width).roundToInt() - d.intrinsicWidth / 4,
                (p.y * canvas.height - d.intrinsicHeight / 4).roundToInt(),
                    (p.x * canvas.width).roundToInt() + d.intrinsicWidth / 4, (p.y * canvas.height).roundToInt() + d.intrinsicHeight / 4)
            d.colorFilter = PorterDuffColorFilter(Color.parseColor(type.color), PorterDuff.Mode.SRC_IN)
            //And draw it...
            d.draw(canvas)
            break
        }
        val drawable = BitmapDrawable(resources, bitmap)
        layers[1] = drawable
        photo!!.setImageDrawable(LayerDrawable(layers))
    }

    private fun popupWindowsort(): PopupWindow {

        // initialize a pop up window type
        popupWindow = PopupWindow(applicationContext)
        val sortList = LandOpenHelper.readPlantTypes(this)
        val adapter = PopupMenuAdapter(this, sortList)
        // the drop down list is a list view
        val listViewSort = ListView(this)

        // set our adapter and pass our pop up window contents
        listViewSort.adapter = adapter

        // set on item selected
        //listViewSort.setOnItemClickListener(onItemClickListener());


        // set the listview as popup content
        popupWindow!!.contentView = listViewSort

        // some other visual settings for popup window
        popupWindow!!.isFocusable = true
        listViewSort.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        popupWindow!!.width = listViewSort.measuredWidth + 50
        // popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.background));
        popupWindow!!.height = WindowManager.LayoutParams.WRAP_CONTENT
        return popupWindow!!
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        val actionBar = supportActionBar
        actionBar?.hide()
        mControlsView!!.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    @SuppressLint("InlinedApi")
    private fun show() {
        // Show the system bar
        mContentView!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    override fun onMenuItemInteraction(item: PlantTypeObject?) {
        popupWindow!!.dismiss()
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(item!!.name)
        alertDialog.setMessage(R.string.state)
        val input = EditText(this)
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        input.layoutParams = lp
        alertDialog.setView(input)
        val filter: ColorFilter = PorterDuffColorFilter(Color.parseColor(item.color), PorterDuff.Mode.SRC_IN)
        val icon = resources.getDrawable(item.icon)
        icon.colorFilter = filter
        alertDialog.setIcon(icon)
        alertDialog.setPositiveButton("OK"
        ) { _, _ ->
            val p = PlantObject(item.name, input.text.toString(), tempX, tempY)
            land!!.addPlant(p)
            addedPlants.push(p)
            addPlantQuery(p)
            drawTrees()
            removePlantButton.circleBackgroundColor = Color.RED
            Log.v("plant", land.toString())
        }
        alertDialog.setNegativeButton(resources.getString(R.string.cancel)
        ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        alertDialog.show()
    }

    private fun addPlantQuery(p: PlantObject) {
        val mDbHelper = LandOpenHelper(this)

        // Gets the data repository in write mode
        val db = mDbHelper.writableDatabase
        val prefs = getSharedPreferences(SettingsFragment.MY_PREFS_NAME, MODE_PRIVATE)
        val user = prefs.getString("user", "")

// Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put("Land", land!!.name)
        values.put(LandContract.ItemEntry.COLUMN_USER, user)
        values.put("PlantType", p.plantType)
        values.put("Description", p.description)
        values.put("x", p.x)
        values.put("y", p.y)

// Insert the new row, returning the primary key value of the new row
        val newRowId = db.insert("Plants", null, values)
        p.id = newRowId.toInt()
        Log.v("ADDDETAIL", "row inserted: $newRowId")
        Log.v("ADDDETAIL", "row inserted: " + p.id)
        if (newRowId == -1L) Toast.makeText(this, R.string.item_add_error, Toast.LENGTH_SHORT).show() else {
            Toast.makeText(this, R.string.item_added, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [.AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = false

        /**
         * If [.AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}