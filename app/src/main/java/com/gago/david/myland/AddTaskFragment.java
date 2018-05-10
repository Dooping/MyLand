package com.gago.david.myland;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddTaskFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddTaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddTaskFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView taskDescription;
    private Spinner spinner;
    private ArrayList<TaskTypeObject> taskTypes;
    private Calendar myCalendar;
    private EditText targetDate;
    private boolean first = true;
    private ArrayList<Integer> plantIndexes;
    private String type;
    private String land;
    private EditText description;

    private OnFragmentInteractionListener mListener;

    public AddTaskFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddTaskFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddTaskFragment newInstance(String param1, String param2) {
        AddTaskFragment fragment = new AddTaskFragment();
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

        plantIndexes = getArguments() != null ? getArguments().getIntegerArrayList("plandIndex") : null;
        type = getArguments().getString("type", "");
        land = getArguments().getString("land", "");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_task, container, false);

        spinner = view.findViewById(R.id.spinner);
        taskTypes = readTaskTypes();

        ArrayList<String> list = new ArrayList<>();
        for(TaskTypeObject task : taskTypes)
            list.add(task.name);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, list);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        //((ViewGroup) view.findViewById(R.id.task_type_layout)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        taskDescription = view.findViewById(R.id.task_type_description);

        final ArrayList<PriorityObject> priorities = readPriorities();
        final Spinner prioritySpinner = view.findViewById(R.id.priority_spinner);
        ArrayList<String> list2 = new ArrayList<>();
        for (PriorityObject p : priorities)
            list2.add(p.name);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, list2);
        adapter2.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(adapter2);

        myCalendar = Calendar.getInstance();

        targetDate = view.findViewById(R.id.targetDate);
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

        description = view.findViewById(R.id.task_description);

        Button addTask = view.findViewById(R.id.add_task);
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date time = targetDate.getText().toString().equals("") ? null : myCalendar.getTime();
                ArrayList<TaskObject> tasks = new ArrayList<>();
                switch (type){
                    case "land":
                        tasks.add(new TaskObject(land, null, taskTypes.get(spinner.getSelectedItemPosition()).name
                                , priorities.get(prioritySpinner.getSelectedItemPosition()).p_order, new Date(), time
                                , false, description.getText().toString()));
                        onButtonPressed(tasks);
                        break;
                    case "all":
                    case "group":
                    case "item":
                        if(plantIndexes.size() == 0) {
                            Toast.makeText(getContext(), "No items selected!", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        for (Integer i: plantIndexes)
                            tasks.add(new TaskObject(land, Integer.valueOf(i), taskTypes.get(spinner.getSelectedItemPosition()).name
                                    , priorities.get(prioritySpinner.getSelectedItemPosition()).p_order, new Date(), time
                                    , false, description.getText().toString()));

                        onButtonPressed(tasks);
                        break;
                    default:
                        Toast.makeText(getContext(),"Something went wrong!!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return view;
    }

    private void updateLabel(){
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
        String s = dateFormat.format(myCalendar.getTime());

        targetDate.setText(s);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(ArrayList<TaskObject> tasks) {
        if (mListener != null) {
            mListener.onFragmentInteraction(tasks);
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, final int i, long l) {
        if (taskTypes != null && !first) {
            int measuredTextHeight = getHeight(getContext(), taskTypes.get(i).description, 14, taskDescription.getWidth(), Typeface.DEFAULT);
            ValueAnimator anim = ValueAnimator.ofInt(taskDescription.getMeasuredHeight(), measuredTextHeight);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = taskDescription.getLayoutParams();
                    layoutParams.height = val;
                    taskDescription.setLayoutParams(layoutParams);
                }
            });
            anim.addListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    taskDescription.setText(taskTypes.get(i).description);
                    AlphaAnimation fadeIn = new AlphaAnimation(0.0f , 1.0f ) ;
                    taskDescription.startAnimation(fadeIn);
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
                    taskDescription.startAnimation(fadeOut);
                }
            });
            anim.setDuration(500);
            anim.start();
        }
        else if(taskTypes != null) {
            first = false;
            taskDescription.setText(taskTypes.get(i).description);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.v("item", "not selected");
        taskDescription.setText("");
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
        void onFragmentInteraction(ArrayList<TaskObject> tasks);
    }

    @Override
    public void onResume() {
        super.onResume();
        taskTypes = readTaskTypes();

        ArrayList<String> list = new ArrayList<>();
        for(TaskTypeObject task : taskTypes)
            list.add(task.name);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, list);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
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

    private ArrayList<PriorityObject> readPriorities(){
        LandOpenHelper mDbHelper = new LandOpenHelper(getContext());

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                "Name",
                "P_order",
                "Color"
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = "P_order ASC";

        Cursor cursor = db.query(
                "Priorities",   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        ArrayList<PriorityObject> priorities = new ArrayList<>();

        while(cursor.moveToNext()) {
            PriorityObject o = new PriorityObject(cursor.getString(0), cursor.getInt(1), cursor.getString(2));
            priorities.add(o);
        }

        cursor.close();

        return priorities;
    }

    public static int getHeight(Context context, CharSequence text, int textSize, int deviceWidth, Typeface typeface) {
        TextView textView = new TextView(context);
        textView.setWidth(deviceWidth);
        textView.setTypeface(typeface);
        textView.setText(text, TextView.BufferType.SPANNABLE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        textView.measure(widthMeasureSpec, heightMeasureSpec);
        return textView.getMeasuredHeight();
    }

    @Override
    public void onStop() {
        super.onStop();
        ((ScrollingActivity)getActivity()).showButtons();
    }
}
