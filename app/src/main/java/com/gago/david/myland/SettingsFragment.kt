package com.gago.david.myland

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import br.com.bloder.magic.view.MagicButton
import com.gago.david.myland.adapters.ItemTypeAdapter
import com.gago.david.myland.adapters.TaskTypeAdapter
import com.gago.david.myland.models.PlantTypeObject
import com.gago.david.myland.models.TaskTypeObject
import lib.kingja.switchbutton.SwitchMultiButton
import java.io.IOException
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {
    var itemAdapter: ItemTypeAdapter? = null
    var taskAdapter: TaskTypeAdapter? = null
    private var mListener: OnListFragmentInteractionListener? = null
    private var mListener2: OnTaskListFragmentInteractionListener? = null
    lateinit var items: ArrayList<PlantTypeObject>
    lateinit var tasks: ArrayList<TaskTypeObject>

    private lateinit var importDB: MagicButton
    lateinit var exportDB: MagicButton
    lateinit var deleteUser: MagicButton
    private lateinit var unitSwitch: SwitchMultiButton
    lateinit var mapType: SwitchMultiButton
    lateinit var startNewSeasonButton: Button

    private var mParam1: String? = null
    private var mParam2: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }
        items = LandOpenHelper.readPlantTypes(requireContext())
        tasks = readTaskTypes()
        askReadingPermission()
        askWritingPermission()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        if (view is FrameLayout) {
            val context = view.getContext()
            val recyclerView: RecyclerView = view.findViewById(R.id.item_list)
            recyclerView.layoutManager =
                LinearLayoutManager(context)
            recyclerView.isNestedScrollingEnabled = false
            recyclerView.setHasFixedSize(false)
            itemAdapter = ItemTypeAdapter(items, mListener)
            recyclerView.adapter = itemAdapter
            val recyclerView2: RecyclerView = view.findViewById(R.id.task_type_list)
            recyclerView2.layoutManager =
                LinearLayoutManager(context)
            recyclerView2.isNestedScrollingEnabled = false
            recyclerView2.setHasFixedSize(false)
            taskAdapter = TaskTypeAdapter(tasks, mListener2)
            recyclerView2.adapter = taskAdapter
            val addTask: FloatingActionButton = view.findViewById(R.id.add_task_type)
            addTask.setOnClickListener { mListener2!!.addTaskType(taskAdapter!!, tasks) }
            val addPlant: FloatingActionButton = view.findViewById(R.id.add_item_type)
            addPlant.setOnClickListener { mListener!!.addItem(itemAdapter!!, items) }

            importDB = view.findViewById(R.id.import_db)
            exportDB = view.findViewById(R.id.export_db)
            deleteUser = view.findViewById(R.id.delete_user)
            unitSwitch = view.findViewById(R.id.unit)
            mapType = view.findViewById(R.id.map_type)
            startNewSeasonButton = view.findViewById(R.id.startNewSeasonButton)

            startNewSeasonButton.setOnClickListener { startNewSeason() }
            importDB.setMagicButtonClickListener {
                val intent = Intent()
                    .setType("*/*")
                    .setAction(Intent.ACTION_GET_CONTENT)

                startActivityForResult(Intent.createChooser(intent, "Select a file"), 9998)
                /*val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    //type = ".db"
                }
                val i = Intent(Intent.ACTION_OPEN_DOCUMENT)
                i.addCategory(Intent.CATEGORY_DEFAULT)
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                startActivityForResult(Intent.createChooser(intent, "Choose file"), 9998)*/
            }
            exportDB.setMagicButtonClickListener {
                val i = Intent(Intent.ACTION_CREATE_DOCUMENT)
                i.type = "application/octet-stream"
                //i.putExtra(Intent.EXTRA_TITLE, "myland.db");
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                startActivityForResult(Intent.createChooser(i, "Choose directory"), 9999)
            }
            val prefs = requireContext().getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)
            val unit = prefs.getInt("unit", 0) //0 is the default value.
            unitSwitch.selectedTab = unit
            unitSwitch.setOnSwitchListener { position: Int, _: String? ->
                val editor = requireContext().getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit()
                editor.putInt("unit", position)
                editor.apply()
            }
            val mapTypePosition = prefs.getInt("mapType", 0)
            mapType.selectedTab = mapTypePosition
            mapType.setOnSwitchListener { position: Int, _: String? ->
                val editor = requireContext().getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit()
                editor.putInt("mapType", position)
                editor.apply()
            }
            val user = prefs.getString("user", "")
            deleteUser.setMagicButtonClickListener { view13: View ->
                val alertDialog = AlertDialog.Builder(view13.rootView.context)
                alertDialog.setTitle(R.string.delete_user)
                alertDialog.setMessage(R.string.delete_user_message)
                alertDialog.setPositiveButton(R.string.yes
                ) { _: DialogInterface?, _: Int ->
                    if (LandOpenHelper.deleteUser(requireContext(), user!!)) {
                        val intent = Intent(getContext(), Login::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
                alertDialog.setNegativeButton(R.string.no
                ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
                alertDialog.show()
            }
        }
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) when (requestCode) {
            9999 -> {
                Log.i("Test", "Result 9999 URI " + data!!.data!!.path)
                //exportDB(data.getData());
                ExportDB(activity).execute(data.data)
            }
            9998 -> {
                Log.i("Test", "Result 9998 URI " + data!!.data!!.path)
                //importDB(data.getData());
                ImportDB(activity).execute(data.data)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val inputManager = requireActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // check if no view has focus:
        val currentFocusedView = requireActivity().currentFocus
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            mListener = context
            mListener2 = if (context is OnTaskListFragmentInteractionListener) context else throw RuntimeException(context.toString()
                    + " must implement OnTaskListFragmentInteractionListener")
        } else {
            throw RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
        mListener2 = null
    }

    interface OnListFragmentInteractionListener {
        fun selectItem(item: PlantTypeObject)
        fun removeItem(item: PlantTypeObject): Boolean
        fun addItem(itemAdapter: ItemTypeAdapter, items: ArrayList<PlantTypeObject>)
    }

    interface OnTaskListFragmentInteractionListener {
        fun selectItem(item: TaskTypeObject)
        fun removeItem(item: TaskTypeObject): Boolean
        fun addTaskType(taskAdapter: TaskTypeAdapter, tasks: ArrayList<TaskTypeObject>)
    }

    private fun readTaskTypes(): ArrayList<TaskTypeObject> {
        val mDbHelper = LandOpenHelper(requireContext())
        val db = mDbHelper.readableDatabase

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        val projection = arrayOf(
                "Name",
                "Description"
        )

        // How you want the results sorted in the resulting Cursor
        val sortOrder: String? = null
        val cursor = db.query(
                "TaskTypes",  // The table to query
                projection,  // The array of columns to return (pass null to get all)
                null,  // The columns for the WHERE clause
                null,  // The values for the WHERE clause
                null,  // don't group the rows
                null,  // don't filter by row groups
                sortOrder // The sort order
        )
        val taskTypes = ArrayList<TaskTypeObject>()
        while (cursor.moveToNext()) {
            val o = TaskTypeObject(cursor.getString(0), cursor.getString(1))
            taskTypes.add(o)
        }
        cursor.close()
        return taskTypes
    }

    private fun startNewSeason() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle(R.string.dialog_new_season_title)
        alertDialog.setMessage(R.string.dialog_new_season_body)
        alertDialog.setPositiveButton(R.string.yes
        ) { _: DialogInterface?, _: Int ->
            LandOpenHelper.archiveOldTasks(requireContext())
            Toast.makeText(requireContext(), R.string.toast_new_season_success, Toast.LENGTH_SHORT).show()
        }
        alertDialog.setNegativeButton(R.string.no
        ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        alertDialog.show()
    }

    private fun askWritingPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    private fun askReadingPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    private inner class ExportDB(private val mContext: Context?) : AsyncTask<Uri?, Void?, Boolean>() {
        override fun onPreExecute() {
        }

        override fun doInBackground(vararg path: Uri?): Boolean {
            var success = false
            val mDbHelper = LandExporterHelper(context!!)
            try {
                if (mDbHelper.exportDatabase(path[0])) success = true
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return success
        }

        override fun onPostExecute(result: Boolean) {
            mContext!!.deleteDatabase(LandExporterHelper.LAND_TABLE_NAME)
            if (result) Toast.makeText(mContext, R.string.export_success, Toast.LENGTH_SHORT).show() else Toast.makeText(mContext, R.string.export_error, Toast.LENGTH_SHORT).show()
        }
    }

    private inner class ImportDB(private val mContext: Context?) : AsyncTask<Uri?, Void?, Boolean>() {
        override fun onPreExecute() {
        }

        override fun doInBackground(vararg path: Uri?): Boolean {
            var success = false
            val mDbHelper = LandImporterHelper(context!!)
            try {
                Log.v("IMPORT", "pathSize: ${path.size}")
                if (mDbHelper.importDatabase(path[0]!!)) success = true
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return success
        }

        override fun onPostExecute(result: Boolean) {
            mContext!!.deleteDatabase(LandImporterHelper.LAND_TABLE_NAME)
            if (result) Toast.makeText(mContext, R.string.import_success, Toast.LENGTH_SHORT).show() else Toast.makeText(mContext, R.string.import_error, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        private const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10
        private const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 20
        const val MY_PREFS_NAME = "MyLandSettings"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingsFragment.
         */
        fun newInstance(param1: String?, param2: String?): SettingsFragment {
            val fragment = SettingsFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}