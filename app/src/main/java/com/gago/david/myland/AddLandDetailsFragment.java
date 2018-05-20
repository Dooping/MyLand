package com.gago.david.myland;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddLandDetailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddLandDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddLandDetailsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    @BindView(R.id.land_detail_image) ImageView image;
    @BindView(R.id.land_name) EditText name;
    @BindView(R.id.land_description) EditText description;
    @BindView(R.id.next_button) FloatingActionButton button;

    String imageUri;

    private OnFragmentInteractionListener mListener;

    public AddLandDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddLandDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddLandDetailsFragment newInstance(String param1, String param2) {
        AddLandDetailsFragment fragment = new AddLandDetailsFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        imageUri = getArguments().getString("filename");
        Log.v("ADDDETAIL", imageUri);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_land_details, container, false);
        ButterKnife.bind(this, view);
        image.setImageBitmap(new LandOpenHelper(getContext()).getImage(imageUri));

        button.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          addLandQuery();
                                      }
                                  });

        return view;
    }

    private void addLandQuery(){
        LandOpenHelper mDbHelper = new LandOpenHelper(getContext());

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put("Name", name.getText().toString());
        values.put("ImageUri", imageUri);
        values.put("Description", description.getText().toString());

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert("Lands", null, values);
        Log.v("ADDDETAIL", "row inserted: "+newRowId);
        if (newRowId == -1)
            Toast.makeText(getContext(),"Land already exists, choose a different name", Toast.LENGTH_SHORT).show();
        else {
            Intent intent = new Intent(getContext(), ScrollingActivity.class);
            Bundle b = new Bundle();
            b.putString("name", name.getText().toString()); //Your id
            intent.putExtras(b); //Put your id to your next Intent
            startActivity(intent);
            ((MainActivity)getActivity()).removeLandDetails();
        }
        db.close();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
