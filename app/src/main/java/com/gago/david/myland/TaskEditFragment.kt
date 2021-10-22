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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import com.gago.david.myland.AddTaskFragment.Companion.getHeight
import com.gago.david.myland.models.PriorityObject
import com.gago.david.myland.models.TaskObject
import com.gago.david.myland.models.TaskTypeObject
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TaskEditFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [TaskEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TaskEditFragment : Fragment(), OnItemSelectedListener {
    private lateinit var task: TaskObject
    private lateinit var taskTypes: ArrayList<TaskTypeObject>
    lateinit var priorities: ArrayList<PriorityObject>
    private lateinit var myCalendar: Calendar
    private var first = true
    private var deleted = false
    private var completed = false
    private var mListener: OnFragmentInteractionListener? = null

    lateinit var taskTypeDescription: TextView
    lateinit var taskSpinner: Spinner
    lateinit var prioritySpinner: Spinner
    lateinit var taskDescription: EditText
    lateinit var targetDate: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            task = arguments!!.getSerializable(ARG_PARAM1) as TaskObject
        }
        taskTypes = LandOpenHelper.readTaskTypes(context!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_task_edit, container, false)

        taskSpinner = view.findViewById(R.id.taskSpinner)
        taskTypeDescription = view.findViewById(R.id.taskTypeDescription)
        prioritySpinner = view.findViewById(R.id.prioritySpinner)
        taskDescription = view.findViewById(R.id.taskDescription)
        targetDate = view.findViewById(R.id.targetDate)

        val list = ArrayList<String>()
        var index = 0
        for (i in taskTypes.indices) {
            list.add(taskTypes[i].name)
            if (taskTypes[i].name == task.taskType) index = i
        }
        val adapter = ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item, list)
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        taskSpinner.adapter = adapter
        taskSpinner.onItemSelectedListener = this
        taskSpinner.setSelection(index)
        priorities = LandOpenHelper.readPriorities(context!!)
        val list2 = ArrayList<String>()
        var index2 = 0
        for (i in priorities.indices) {
            list2.add(priorities[i].name)
            if (priorities[i].p_order == task.priority) index2 = i
        }
        val adapter2 = ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item, list2)
        adapter2.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        prioritySpinner.adapter = adapter2
        prioritySpinner.setSelection(index2)
        myCalendar = Calendar.getInstance()
        if (task.targetDate != null) {
            myCalendar.time = task.targetDate!!
            updateLabel()
        }
        val date = OnDateSetListener { _: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
        }
        targetDate.setOnClickListener { v: View? ->
            DatePickerDialog(context!!, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        taskDescription.setText(task.observations)
        return view
    }

    private fun updateLabel() {
        val dateFormat = DateFormat.getDateFormat(context)
        val s = dateFormat.format(myCalendar.time)
        targetDate.setText(s)
    }

    fun closeTask(): TaskObject {
        completed = true
        //onButtonPressed();
        return task
    }

    fun deleteTask(): TaskObject {
        val success = LandOpenHelper.deleteTask(task, context!!)
        deleted = true
        if (success) Toast.makeText(context, R.string.delete_task_success, Toast.LENGTH_SHORT).show() else Toast.makeText(context, R.string.delete_task_error, Toast.LENGTH_SHORT).show()
        return task
    }

    fun onButtonPressed() {
        if (mListener != null && !deleted) {
            val task = task.clone()
            var changed = false
            if (taskTypes[taskSpinner.selectedItemPosition].name != task.taskType) {
                changed = true
                task.taskType = taskTypes[taskSpinner.selectedItemPosition].name
            }
            if (priorities[prioritySpinner.selectedItemPosition].p_order != task.priority) {
                changed = true
                task.priority = priorities[prioritySpinner.selectedItemPosition].p_order
            }
            if (taskDescription.text.toString() != task.observations) {
                changed = true
                task.observations = taskDescription.text.toString()
            }
            if (targetDate.text.toString() != "" && task.targetDate != null && myCalendar.time.compareTo(task.targetDate) != 0
                    || targetDate.text.toString() != "" && task.targetDate == null) {
                changed = true
                task.targetDate = myCalendar.time
            }
            if (completed) {
                changed = true
                task.completed = true
                task.completedDate = Date()
            }
            if (changed) mListener!!.updateTask(task, this.task) else mListener!!.notUpdateTask()
        }
    }

    override fun onDestroyView() {
        onButtonPressed()
        if (mListener != null) mListener!!.notUpdateTask()
        super.onDestroyView()
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
        if (!first) {
            val measuredTextHeight = getHeight(context, taskTypes[i].description, 14, taskTypeDescription.width, Typeface.DEFAULT)
            val anim = ValueAnimator.ofInt(taskTypeDescription.measuredHeight, measuredTextHeight)
            anim.addUpdateListener { valueAnimator: ValueAnimator ->
                val `val` = valueAnimator.animatedValue as Int
                val layoutParams = taskTypeDescription.layoutParams
                layoutParams.height = `val`
                taskTypeDescription.layoutParams = layoutParams
            }
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    taskTypeDescription.text = taskTypes[i].description
                    val fadeIn = AlphaAnimation(0.0f, 1.0f)
                    taskTypeDescription.startAnimation(fadeIn)
                    fadeIn.duration = 500
                    fadeIn.fillAfter = true
                    fadeIn.startOffset = 500
                }

                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    val fadeOut = AlphaAnimation(1.0f, 0.0f)
                    fadeOut.duration = 500
                    fadeOut.fillAfter = true
                    taskTypeDescription.startAnimation(fadeOut)
                }
            })
            anim.duration = 500
            anim.start()
        } else {
            first = false
            taskTypeDescription.text = taskTypes[i].description
        }
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) {
        Log.v("item", "not selected")
        taskTypeDescription.text = ""
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
        fun selectTask(task: TaskObject)
        fun updateTask(newTask: TaskObject, oldTask: TaskObject)
        fun notUpdateTask()
    }

    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "task"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param task Task to edit.
         * @return A new instance of fragment TaskEditFragment.
         */
        fun newInstance(task: TaskObject?): TaskEditFragment {
            val fragment = TaskEditFragment()
            val args = Bundle()
            args.putSerializable(ARG_PARAM1, task)
            fragment.arguments = args
            return fragment
        }
    }
}