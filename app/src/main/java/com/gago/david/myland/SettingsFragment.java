package com.gago.david.myland;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.gago.david.myland.Adapters.ItemTypeAdapter;
import com.gago.david.myland.Adapters.TaskTypeAdapter;
import com.gago.david.myland.Models.PlantTypeObject;
import com.gago.david.myland.Models.TaskTypeObject;

import java.io.IOException;
import java.util.ArrayList;

import br.com.bloder.magic.view.MagicButton;
import butterknife.BindView;
import butterknife.ButterKnife;
import id.arieridwan.lib.PageLoader;
import lib.kingja.switchbutton.SwitchMultiButton;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 20;
    public static final String MY_PREFS_NAME = "MyLandSettings";

    ItemTypeAdapter itemAdapter;
    TaskTypeAdapter taskAdapter;

    private OnListFragmentInteractionListener mListener;
    private OnTaskListFragmentInteractionListener mListener2;

    ArrayList<PlantTypeObject> items;
    ArrayList<TaskTypeObject> tasks;

    @BindView(R.id.import_db) MagicButton importDB;
    @BindView(R.id.export_db) MagicButton exportDB;
    @BindView(R.id.unit) SwitchMultiButton unitSwitch;
    @BindView(R.id.pageloader) PageLoader pageLoader;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        items = LandOpenHelper.readPlantTypes(getContext());
        tasks = readTaskTypes();
        askReadingPermission();
        askWritingPermission();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        if (view instanceof FrameLayout) {
            Context context = view.getContext();
            RecyclerView recyclerView = view.findViewById(R.id.item_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setNestedScrollingEnabled(false);
            recyclerView.setHasFixedSize(false);
            itemAdapter = new ItemTypeAdapter(items, mListener);
            recyclerView.setAdapter(itemAdapter);
            RecyclerView recyclerView2 = view.findViewById(R.id.task_type_list);
            recyclerView2.setLayoutManager(new LinearLayoutManager(context));
            recyclerView2.setNestedScrollingEnabled(false);
            recyclerView2.setHasFixedSize(false);
            taskAdapter = new TaskTypeAdapter(tasks, mListener2);
            recyclerView2.setAdapter(taskAdapter);
            FloatingActionButton addTask = view.findViewById(R.id.add_task_type);
            addTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener2.addTaskType(taskAdapter, tasks);
                }
            });
            FloatingActionButton addPlant = view.findViewById(R.id.add_item_type);
            addPlant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.addItem(itemAdapter, items);
                }
            });
            ButterKnife.bind(this, view);
            importDB.setMagicButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent i = new Intent(Intent.ACTION_GET_CONTENT );
                    i.addCategory(Intent.CATEGORY_DEFAULT);
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(Intent.createChooser(i, "Choose file"), 9998);
                }
            });
            exportDB.setMagicButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    i.setType("application/octet-stream");
                    //i.putExtra(Intent.EXTRA_TITLE, "myland.db");
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(Intent.createChooser(i, "Choose directory"), 9999);
                }
            });
            SharedPreferences prefs = getContext().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            int unit = prefs.getInt("unit", 0); //0 is the default value.
            unitSwitch.setSelectedTab(unit);
            unitSwitch.setOnSwitchListener(new SwitchMultiButton.OnSwitchListener() {
                @Override
                public void onSwitch(int position, String tabText) {
                    //Toast.makeText(getContext(), tabText, Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = getContext().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putInt("unit", position);
                    editor.apply();
                }
            });
        }

        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if(resultCode == Activity.RESULT_OK)
            switch(requestCode) {
                case 9999:
                    Log.i("Test", "Result 9999 URI " + data.getData().getPath());
                    //exportDB(data.getData());
                    new ExportDB(getContext()).execute(data.getData());
                    break;
                case 9998:
                    Log.i("Test", "Result 9998 URI " + data.getData().getPath());
                    //importDB(data.getData());
                    new ImportDB(getContext()).execute(data.getData());
                    break;
            }
    }

    @Override
    public void onResume() {
        super.onResume();
        InputMethodManager inputManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View currentFocusedView = getActivity().getCurrentFocus();
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
            if(context instanceof OnTaskListFragmentInteractionListener)
                mListener2 = (OnTaskListFragmentInteractionListener) context;
            else
                throw new RuntimeException(context.toString()
                        + " must implement OnTaskListFragmentInteractionListener");
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mListener2 = null;
    }

    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void selectItem(PlantTypeObject item);
        boolean removeItem(PlantTypeObject item);
        void addItem(ItemTypeAdapter itemAdapter, ArrayList<PlantTypeObject> items);
    }

    public interface OnTaskListFragmentInteractionListener {
        // TODO: Update argument type and name
        void selectItem(TaskTypeObject item);
        boolean removeItem(TaskTypeObject item);
        void addTaskType(TaskTypeAdapter taskAdapter, ArrayList<TaskTypeObject> tasks);
    }



    private ArrayList<TaskTypeObject> readTaskTypes(){
        LandOpenHelper mDbHelper = new LandOpenHelper(getContext());

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                "Name",
                "Description"
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = null;

        Cursor cursor = db.query(
                "TaskTypes",   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        ArrayList<TaskTypeObject> taskTypes = new ArrayList<>();

        while(cursor.moveToNext()) {
            TaskTypeObject o = new TaskTypeObject(cursor.getString(0), cursor.getString(1));
            taskTypes.add(o);
        }

        cursor.close();

        return taskTypes;
    }

    private void importDB(Uri s){
        LandImporterHelper mDbHelper = new LandImporterHelper(getContext());
        try {
            if (mDbHelper.importDatabase(s))
                Toast.makeText(getContext(), R.string.import_success, Toast.LENGTH_SHORT);
            else
                Toast.makeText(getContext(), R.string.import_error, Toast.LENGTH_SHORT);

        } catch (IOException e) {
            e.printStackTrace();
        }
        getActivity().deleteDatabase(LandImporterHelper.LAND_TABLE_NAME);
    }

    private void exportDB(Uri s){
        LandExporterHelper mDbHelper = new LandExporterHelper(getContext());
        try {
            if (mDbHelper.exportDatabase(s))
                Toast.makeText(getContext(), R.string.export_success, Toast.LENGTH_SHORT);
            else
                Toast.makeText(getContext(), R.string.export_error, Toast.LENGTH_SHORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        getActivity().deleteDatabase(LandExporterHelper.LAND_TABLE_NAME);
    }

    private void askWritingPermission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    private void askReadingPermission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    private class ExportDB extends AsyncTask<Uri, Void, Boolean> {
        private Context mContext;

        public ExportDB(Context context){
            this.mContext = context;
        }

        protected void onPreExecute (){
            pageLoader.startProgress();
        }

        protected Boolean doInBackground(Uri... path) {
            boolean success = false;
            LandExporterHelper mDbHelper = new LandExporterHelper(getContext());
            try {
                if (mDbHelper.exportDatabase(path[0]))
                    success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return success;
        }

        protected void onPostExecute(Boolean result) {
            if (result)
                Toast.makeText(mContext, R.string.export_success, Toast.LENGTH_SHORT);
            else
                Toast.makeText(mContext, R.string.export_error, Toast.LENGTH_SHORT);
            mContext.deleteDatabase(LandExporterHelper.LAND_TABLE_NAME);
            Log.v("Export", "aqui");
            pageLoader.stopProgress();
        }
    }

    private class ImportDB extends AsyncTask<Uri, Void, Boolean> {
        private Context mContext;

        public ImportDB(Context context){
            this.mContext = context;
        }

        protected void onPreExecute (){
            pageLoader.startProgress();
        }

        protected Boolean doInBackground(Uri... path) {
            boolean success = false;
            LandImporterHelper mDbHelper = new LandImporterHelper(getContext());
            try {
                if (mDbHelper.importDatabase(path[0]))
                    success=true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return success;
        }

        protected void onPostExecute(Boolean result) {
            if (result)
                Toast.makeText(mContext, R.string.import_success, Toast.LENGTH_SHORT);
            else
                Toast.makeText(mContext, R.string.import_error, Toast.LENGTH_SHORT);
            mContext.deleteDatabase(LandImporterHelper.LAND_TABLE_NAME);
            Log.v("Import", "aqui");
            pageLoader.stopProgress();
        }
    }

}
