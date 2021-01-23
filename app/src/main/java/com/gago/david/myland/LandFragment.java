package com.gago.david.myland;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.gago.david.myland.adapters.MyLandRecyclerViewAdapter;
import com.gago.david.myland.models.LandObject;
import com.gago.david.myland.models.PriorityObject;
import com.gago.david.myland.models.TaskTypeObject;


import java.util.ArrayList;
import java.util.List;

import id.arieridwan.lib.PageLoader;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class LandFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
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

        lands = LandOpenHelper.readLands(getContext());
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
                Double area = data.getDoubleExtra("area", 0.0);
                ((MainActivity)getActivity()).addLandDetails(filename, area);
            }
        }
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
        lands.addAll(LandOpenHelper.readLands(getContext()));
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
        void onListFragmentInteraction(LandObject item);
    }
}
