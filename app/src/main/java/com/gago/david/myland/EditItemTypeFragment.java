package com.gago.david.myland;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;

import java.util.ArrayList;

import br.com.bloder.magic.view.MagicButton;
import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditItemTypeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditItemTypeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditItemTypeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "name";
    private static final String ARG_PARAM2 = "create";

    // TODO: Rename and change types of parameters
    private PlantTypeObject item;
    private boolean create = false;
    private ArrayList<Integer> list;
    private String tintColor;
    private int drawable;

    @BindView(R.id.item_name) EditText nameView;
    @BindView(R.id.item_image) ImageView imageView;
    @BindView(R.id.submit_button) FloatingActionButton button;
    @BindView(R.id.edit_icon) MagicButton editIcon;
    @BindView(R.id.edit_color) MagicButton editColor;

    private OnFragmentInteractionListener mListener;

    public EditItemTypeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param item Parameter 1.
     * @return A new instance of fragment EditTaskTypeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditItemTypeFragment newInstance(PlantTypeObject item, boolean create) {
        EditItemTypeFragment fragment = new EditItemTypeFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, item);
        args.putBoolean(ARG_PARAM2, create);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            item = (PlantTypeObject) getArguments().getSerializable(ARG_PARAM1);
            create = getArguments().getBoolean(ARG_PARAM2);
            tintColor = item.color.equals("") ? "#669900" :  item.color;
            drawable = item.icon;
        }

        TypedArray array = getResources().obtainTypedArray(R.array.imageList);
        list = new ArrayList<>();
        for (int i = 0; i < 7; i++)
            list.add(array.getResourceId(i,-1));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_item_type, container, false);
        ButterKnife.bind(this, view);
        nameView.setText(item.name);
        imageView.setImageResource(drawable);
        imageView.setColorFilter(Color.parseColor(tintColor), PorterDuff.Mode.SRC_IN);

        editIcon.setMagicButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialog();
            }
        });

        editColor.setMagicButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorPickerDialog.newBuilder()
                        .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                        .setDialogId(0)
                        .setColor(Color.parseColor(tintColor))
                        .setShowAlphaSlider(false)
                        .show(getActivity());
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item.name = nameView.getText().toString();
                item.icon = drawable;
                item.color = tintColor;
                onButtonPressed(item);
            }
        });

        return view;
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("Choose image");


        ColorFilter filter = new PorterDuffColorFilter(Color.parseColor(tintColor), PorterDuff.Mode.SRC_IN);
        Drawable icon = getResources().getDrawable(drawable);
        icon.setColorFilter(filter);
        alertDialog.setIcon(icon);

        alertDialog.setAdapter(new ImageAdapter(getActivity(), list, Color.parseColor(tintColor)),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.v("image adapter", "adfngajn:"+i);
                        imageView.setImageResource(list.get(i));
                        drawable = list.get(i);
                    }
                });

        alertDialog.show();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(PlantTypeObject item) {
        if (mListener != null) {
            if (create)
                mListener.addItem(item);
            else
                mListener.onFragmentInteraction(item);
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

    public void setColor(String color){
        imageView.setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN);
        tintColor = color;
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
        void onFragmentInteraction(PlantTypeObject itemType);
        long addItem(PlantTypeObject item);
    }
}
