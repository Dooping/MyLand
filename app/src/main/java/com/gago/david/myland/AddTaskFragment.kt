package com.gago.david.myland

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.format.DateFormat
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import com.gago.david.myland.models.TaskObject
import com.gago.david.myland.models.TaskTypeObject
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [AddTaskFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [AddTaskFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddTaskFragment : Fragment(), OnItemSelectedListener {
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var taskDescription: TextView? = null
    private var spinner: Spinner? = null
    private var taskTypes: ArrayList<TaskTypeObject>? = null
    private var myCalendar: Calendar? = null
    private var targetDate: EditText? = null
    private var first = true
    private var plantIndexes: ArrayList<Int>? = null
    private var type: String? = null
    private var land: String? = null
    private var description: EditText? = null
    private var mListener: OnFragmentInteractionListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        plantIndexes = if (arguments != null) arguments!!.getIntegerArrayList("plandIndex") else null
        type = arguments!!.getString("type", "")
        land = arguments!!.getString("land", "")

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_task, container, false)
        spinner = view.findViewById(R.id.spinner)
        taskTypes = LandOpenHelper.readTaskTypes(context)
        val list = ArrayList<String>()
        for (task in taskTypes!!) list.add(task.name)
        val adapter = ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item, list)
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        spinner!!.adapter = adapter
        spinner!!.onItemSelectedListener = this
        taskDescription = view.findViewById(R.id.task_type_description)
        val priorities = LandOpenHelper.readPriorities(context)
        val prioritySpinner = view.findViewById<Spinner>(R.id.priority_spinner)
        val list2 = ArrayList<String>()
        for (p in priorities) list2.add(p.name)
        val adapter2 = ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item, list2)
        adapter2.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        prioritySpinner.adapter = adapter2
        myCalendar = Calendar.getInstance()
        targetDate = view.findViewById(R.id.targetDate)
        val date = OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            myCalendar!!.set(Calendar.YEAR, year)
            myCalendar!!.set(Calendar.MONTH, monthOfYear)
            myCalendar!!.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
        }
        targetDate!!.setOnClickListener {
            DatePickerDialog(context!!, date, myCalendar!!
                    .get(Calendar.YEAR), myCalendar!!.get(Calendar.MONTH),
                    myCalendar!!.get(Calendar.DAY_OF_MONTH)).show()
        }
        description = view.findViewById(R.id.task_description)
        val addTask = view.findViewById<Button>(R.id.add_task)
        addTask.setOnClickListener {
            val time = if (targetDate!!.text.toString() == "") null else myCalendar!!.time
            val tasks = ArrayList<TaskObject>()
            when (type) {
                "land" -> {
                    tasks.add(TaskObject(land!!, null, taskTypes!!.get(spinner!!.selectedItemPosition).name, priorities[prioritySpinner.selectedItemPosition].p_order, Date(), time, false, description!!.text.toString()))
                    onButtonPressed(tasks)
                }
                "all", "group", "item" -> {
                    if (plantIndexes!!.size == 0) {
                        Toast.makeText(context, "No items selected!", Toast.LENGTH_SHORT).show()
                    }
                    for (i in plantIndexes!!)
                        tasks.add(TaskObject(land!!, Integer.valueOf(i), taskTypes!![spinner!!.selectedItemPosition].name, priorities[prioritySpinner.selectedItemPosition].p_order, Date(), time, false, description!!.text.toString()))
                    onButtonPressed(tasks)
                }
                else -> Toast.makeText(context, "Something went wrong!!", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    private fun updateLabel() {
        val dateFormat = DateFormat.getDateFormat(context)
        val s = dateFormat.format(myCalendar!!.time)
        targetDate!!.setText(s)
    }

    fun onButtonPressed(tasks: ArrayList<TaskObject>?) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(tasks)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnFragmentInteractionListener) {
            context
        } else {
            throw RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
        if (taskTypes != null && !first) {
            val measuredTextHeight = getHeight(context, taskTypes!![i].description, 14, taskDescription!!.width, Typeface.DEFAULT)
            val anim = ValueAnimator.ofInt(taskDescription!!.measuredHeight, measuredTextHeight)
            anim.addUpdateListener { valueAnimator ->
                val `val` = valueAnimator.animatedValue as Int
                val layoutParams = taskDescription!!.layoutParams
                layoutParams.height = `val`
                taskDescription!!.layoutParams = layoutParams
            }
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    taskDescription!!.text = taskTypes!![i].description
                    val fadeIn = AlphaAnimation(0.0f, 1.0f)
                    taskDescription!!.startAnimation(fadeIn)
                    fadeIn.duration = 500
                    fadeIn.fillAfter = true
                    fadeIn.startOffset = 500
                }

                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    val fadeOut = AlphaAnimation(1.0f, 0.0f)
                    fadeOut.duration = 500
                    fadeOut.fillAfter = true
                    taskDescription!!.startAnimation(fadeOut)
                }
            })
            anim.duration = 500
            anim.start()
        } else if (taskTypes != null) {
            first = false
            taskDescription!!.text = taskTypes!![i].description
        }
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) {
        Log.v("item", "not selected")
        taskDescription!!.text = ""
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(tasks: ArrayList<TaskObject>?)
    }

    override fun onResume() {
        super.onResume()
        taskTypes = LandOpenHelper.readTaskTypes(context)
        val list = ArrayList<String>()
        for (task in taskTypes!!) list.add(task.name)
        val adapter = ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item, list)
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        spinner!!.adapter = adapter
    }

    override fun onStop() {
        super.onStop()
        (activity as ScrollingActivity?)!!.showButtons()
    }

    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddTaskFragment.
         */
        fun newInstance(param1: String?, param2: String?): AddTaskFragment {
            val fragment = AddTaskFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }

        @JvmStatic
        fun getHeight(context: Context?, text: CharSequence?, textSize: Int, deviceWidth: Int, typeface: Typeface?): Int {
            val textView = TextView(context)
            textView.width = deviceWidth
            textView.typeface = typeface
            textView.setText(text, TextView.BufferType.SPANNABLE)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
            val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST)
            val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            textView.measure(widthMeasureSpec, heightMeasureSpec)
            return textView.measuredHeight
        }
    }
}