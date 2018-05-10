package com.gago.david.myland;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditTaskTypeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditTaskTypeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditTaskTypeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "name";
    private static final String ARG_PARAM2 = "create";

    // TODO: Rename and change types of parameters
    private TaskTypeObject task;
    private boolean create = false;

    @BindView(R.id.task_name) TextView nameView;
    @BindView(R.id.task_description) EditText descriptionView;
    @BindView(R.id.submit_button) FloatingActionButton button;

    private OnFragmentInteractionListener mListener;

    public EditTaskTypeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param task Parameter 1.
     * @return A new instance of fragment EditTaskTypeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditTaskTypeFragment newInstance(TaskTypeObject task, boolean create) {
        EditTaskTypeFragment fragment = new EditTaskTypeFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, task);
        args.putBoolean(ARG_PARAM2, create);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            task = (TaskTypeObject) getArguments().getSerializable(ARG_PARAM1);
            create = getArguments().getBoolean(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_task_type, container, false);
        ButterKnife.bind(this, view);
        nameView.setText(task.name);
        descriptionView.setText(task.description);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task.description = descriptionView.getText().toString();
                task.name = nameView.getText().toString();
                onButtonPressed(task);
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(TaskTypeObject taskType) {
        if (mListener != null) {
            if (create)
                mListener.addItem(task);
            else
                mListener.onFragmentInteraction(taskType);
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
        void onFragmentInteraction(TaskTypeObject taskType);
        long addItem(TaskTypeObject item);
    }
}
