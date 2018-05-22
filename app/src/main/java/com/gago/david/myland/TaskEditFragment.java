package com.gago.david.myland;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gago.david.myland.Models.PriorityObject;
import com.gago.david.myland.Models.TaskObject;
import com.gago.david.myland.Models.TaskTypeObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TaskEditFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TaskEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskEditFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "task";

    // TODO: Rename and change types of parameters
    private TaskObject task;
    private ArrayList<TaskTypeObject> taskTypes;
    ArrayList<PriorityObject> priorities;
    private Calendar myCalendar;
    private boolean first = true;
    private boolean deleted = false;
    private boolean completed = false;

    private OnFragmentInteractionListener mListener;
    @BindView(R.id.task_type_description) TextView taskTypeDescription;
    @BindView(R.id.spinner) Spinner taskSpinner;
    @BindView(R.id.priority_spinner) Spinner prioritySpinner;
    @BindView(R.id.task_description) EditText taskDescription;
    @BindView(R.id.targetDate) EditText targetDate;

    public TaskEditFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param task Task to edit.
     * @return A new instance of fragment TaskEditFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TaskEditFragment newInstance(TaskObject task) {
        TaskEditFragment fragment = new TaskEditFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, task);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            task = (TaskObject) getArguments().getSerializable(ARG_PARAM1);
        }
        taskTypes = LandOpenHelper.readTaskTypes(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task_edit, container, false);
        ButterKnife.bind(this, view);

        ArrayList<String> list = new ArrayList<>();
        int index = 0;
        for(int i = 0; i < taskTypes.size(); i++) {
            list.add(taskTypes.get(i).name);
            if(taskTypes.get(i).name.equals(task.taskType))
                index = i;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, list);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        taskSpinner.setAdapter(adapter);
        taskSpinner.setOnItemSelectedListener(this);
        taskSpinner.setSelection(index);

        priorities = LandOpenHelper.readPriorities(getContext());
        ArrayList<String> list2 = new ArrayList<>();
        int index2 = 0;
        for (int i = 0; i < priorities.size(); i++) {
            list2.add(priorities.get(i).name);
            if(priorities.get(i).p_order == task.priority)
                index2 = i;
        }
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, list2);
        adapter2.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(adapter2);
        prioritySpinner.setSelection(index2);

        myCalendar = Calendar.getInstance();

        if(task.targetDate != null) {
            myCalendar.setTime(task.targetDate);
            updateLabel();
        }
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        targetDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(getContext(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        taskDescription.setText(task.observations);

        return view;
    }

    private void updateLabel(){
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
        String s = dateFormat.format(myCalendar.getTime());

        targetDate.setText(s);
    }

    public TaskObject closeTask(){
        completed = true;
        //onButtonPressed();
        return task;
    }

    public TaskObject deleteTask(){
        boolean success = LandOpenHelper.deleteTask(task, getContext());
        deleted = true;
        if(success)
            Toast.makeText(getContext(), R.string.delete_task_success, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getContext(), R.string.delete_task_error, Toast.LENGTH_SHORT).show();
        return task;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed() {
        if (mListener != null && !deleted) {
            TaskObject task = this.task.clone();
            boolean changed = false;
            if ( !taskTypes.get(taskSpinner.getSelectedItemPosition()).name.equals(task.taskType)){
                changed = true;
                task.taskType = taskTypes.get(taskSpinner.getSelectedItemPosition()).name;
            }
            if ( priorities.get(prioritySpinner.getSelectedItemPosition()).p_order != task.priority ){
                changed = true;
                task.priority = priorities.get(prioritySpinner.getSelectedItemPosition()).p_order;
            }
            if ( !taskDescription.getText().toString().equals(task.observations) ){
                changed = true;
                task.observations = taskDescription.getText().toString();
            }
            if (( !targetDate.getText().toString().equals("") && task.targetDate != null && myCalendar.getTime().compareTo(task.targetDate) != 0)
                || ( !targetDate.getText().toString().equals("") && task.targetDate == null)){
                changed = true;
                task.targetDate = myCalendar.getTime();
            }
            if ( completed ) {
                changed = true;
                task.completed = true;
            }
            if ( changed )
                mListener.updateTask(task, this.task);
            else
                mListener.notUpdateTask();
        }
    }

    @Override
    public void onDestroyView() {
        onButtonPressed();
        if (mListener!=null)
            mListener.notUpdateTask();
        super.onDestroyView();
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, final int i, long l) {
        if (taskTypes != null && !first) {
            int measuredTextHeight = AddTaskFragment.getHeight(getContext(), taskTypes.get(i).description, 14, taskTypeDescription.getWidth(), Typeface.DEFAULT);
            ValueAnimator anim = ValueAnimator.ofInt(taskTypeDescription.getMeasuredHeight(), measuredTextHeight);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = taskTypeDescription.getLayoutParams();
                    layoutParams.height = val;
                    taskTypeDescription.setLayoutParams(layoutParams);
                }
            });
            anim.addListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    taskTypeDescription.setText(taskTypes.get(i).description);
                    AlphaAnimation fadeIn = new AlphaAnimation(0.0f , 1.0f ) ;
                    taskTypeDescription.startAnimation(fadeIn);
                    fadeIn.setDuration(500);
                    fadeIn.setFillAfter(true);
                    fadeIn.setStartOffset(500);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    AlphaAnimation fadeOut = new AlphaAnimation( 1.0f , 0.0f ) ;
                    fadeOut.setDuration(500);
                    fadeOut.setFillAfter(true);
                    taskTypeDescription.startAnimation(fadeOut);
                }
            });
            anim.setDuration(500);
            anim.start();
        }
        else if(taskTypes != null) {
            first = false;
            taskTypeDescription.setText(taskTypes.get(i).description);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.v("item", "not selected");
        taskTypeDescription.setText("");
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
        void selectTask(TaskObject task);
        void updateTask(TaskObject newTask, TaskObject oldTask);
        void notUpdateTask();
    }
}
