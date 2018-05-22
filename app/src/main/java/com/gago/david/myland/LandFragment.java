package com.gago.david.myland;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.gago.david.myland.Adapters.MyLandRecyclerViewAdapter;
import com.gago.david.myland.Models.LandObject;
import com.gago.david.myland.Models.PriorityObject;
import com.gago.david.myland.Models.TaskTypeObject;


import java.util.ArrayList;
import java.util.List;

import id.arieridwan.lib.PageLoader;

import static android.app.Activity.RESULT_OK;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class LandFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private MyLandRecyclerViewAdapter adapter;

    private PageLoader pageLoader;

    private List<LandObject> lands;
    private List<PriorityObject> priorities;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LandFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static LandFragment newInstance(int columnCount) {
        LandFragment fragment = new LandFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        priorities = LandOpenHelper.readPriorities(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_land_list, container, false);

        lands = readLands();
        if (view instanceof FrameLayout) {
            Context context = view.getContext();
            RecyclerView recyclerView = view.findViewById(R.id.list);
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            adapter = new MyLandRecyclerViewAdapter(lands, mListener, priorities);
            recyclerView.setAdapter(adapter);
        }
        //lands = new ArrayList<>();

        // Set the adapter

        FloatingActionButton btn = (FloatingActionButton) view.findViewById(R.id.add_land_button);

        pageLoader = (PageLoader) view.findViewById(R.id.pageloader);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("LAND_LIST","carregou no botao");
                Intent intent = new Intent(getContext(), AddLandActivity.class);
                startActivityForResult(intent,1);
                pageLoader.startProgress();
            }
        });
        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String filename = data.getStringExtra("name");
                ((MainActivity)getActivity()).addLandDetails(filename);
            }
        }
    }

    private List<LandObject> readLands(){
        LandOpenHelper mDbHelper = new LandOpenHelper(getContext());

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                "Lands.Name as 'Name'",
                "Lands.ImageUri as 'ImageUri'",
                "Lands.Description as 'Description'",
                "count('Tasks'.Land) as 'Notification'",
                "min('Tasks'.Priority) as 'Priority'"
        };

        // How you want the results sorted in the resulting Cursor

        Cursor cursor = db.query(
                "Lands left outer join (select * from tasks where completed = 0) as 'Tasks' on Lands.Name = 'Tasks'.Land",   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                "Name",                   // don't group the rows
                null,                   // don't filter by row groups
                "Lands.rowid asc"               // The sort order
        );
        /*Cursor cursor = db.rawQuery("select Name, ImageUri, Description, count(Tasks.Land) as 'Notification' \n" +
                "from Lands left outer join Tasks on Lands.Name = Tasks.Land\n" +
                "where Priority is null or Priority = 1\n" +
                "group by Tasks.Land", null
        );*/

        List<LandObject> lands = new ArrayList<>();

        while(cursor.moveToNext()) {
            int priority = cursor.isNull(cursor.getColumnIndex("Priority")) ? 0 : cursor.getInt(cursor.getColumnIndex("Priority"));
            LandObject o = new LandObject(cursor.getString(cursor.getColumnIndex("Name"))
                    , cursor.getString(cursor.getColumnIndex("ImageUri"))
                    , cursor.getString(cursor.getColumnIndex("Description"))
                    , cursor.getInt(cursor.getColumnIndex("Notification"))
                    , priority);
            lands.add(o);
        }

        Log.v("Lands", lands.toString());

        cursor.close();
        db.close();

        return lands;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStop(){
        super.onStop();
        pageLoader.stopProgress();
    }

    @Override
    public void onResume() {
        super.onResume();
        lands.clear();
        lands.addAll(readLands());
        adapter.notifyDataSetChanged();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(LandObject item);
    }
}
