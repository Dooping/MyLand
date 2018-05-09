package com.gago.david.myland;


import android.content.ClipData;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.ArrayList;


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

    ItemTypeAdapter itemAdapter;
    TaskTypeAdapter taskAdapter;

    private OnListFragmentInteractionListener mListener;
    private OnTaskListFragmentInteractionListener mListener2;

    ArrayList<PlantTypeObject> items;
    ArrayList<TaskTypeObject> tasks;

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
        items = readPlantTypes();
        tasks = readTaskTypes();
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
            itemAdapter = new ItemTypeAdapter(items, mListener);
            recyclerView.setAdapter(itemAdapter);
            RecyclerView recyclerView2 = view.findViewById(R.id.task_type_list);
            recyclerView2.setLayoutManager(new LinearLayoutManager(context));
            taskAdapter = new TaskTypeAdapter(tasks, mListener2);
            recyclerView2.setAdapter(taskAdapter);
        }

        return view;
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
        long addItem(PlantTypeObject item);
    }

    public interface OnTaskListFragmentInteractionListener {
        // TODO: Update argument type and name
        void selectItem(TaskTypeObject item);
        boolean removeItem(TaskTypeObject item);
        long addItem(TaskTypeObject item);
    }

    private ArrayList<PlantTypeObject> readPlantTypes(){
        LandOpenHelper mDbHelper = new LandOpenHelper(getContext());

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                "Name",
                "Icon",
                "Color"
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = null;

        Cursor cursor = db.query(
                "PlantTypes",   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        ArrayList<PlantTypeObject> plants = new ArrayList<>();

        while(cursor.moveToNext()) {
            PlantTypeObject o = new PlantTypeObject(cursor.getString(0), cursor.getInt(1), cursor.getString(2));
            plants.add(o);
        }

        cursor.close();
        db.close();
        return plants;
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

}
