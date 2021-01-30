package com.gago.david.myland

import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.gago.david.myland.models.TaskTypeObject

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [EditTaskTypeFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [EditTaskTypeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditTaskTypeFragment : Fragment() {
    private var task: TaskTypeObject? = null
    private var create = false

    @JvmField
    @BindView(R.id.task_name)
    var nameView: TextView? = null

    @JvmField
    @BindView(R.id.task_description)
    var descriptionView: EditText? = null

    @JvmField
    @BindView(R.id.submit_button)
    var button: FloatingActionButton? = null
    private var mListener: OnFragmentInteractionListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            task = arguments!!.getSerializable(ARG_PARAM1) as TaskTypeObject?
            create = arguments!!.getBoolean(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_task_type, container, false)
        ButterKnife.bind(this, view)
        nameView!!.text = task!!.name
        descriptionView!!.setText(task!!.description)
        button!!.setOnClickListener { view1: View? ->
            task!!.description = descriptionView!!.text.toString()
            task!!.name = nameView!!.text.toString()
            onButtonPressed(task)
        }
        return view
    }

    fun onButtonPressed(taskType: TaskTypeObject?) {
        if (mListener != null) {
            if (create) mListener!!.addItem(task) else mListener!!.onFragmentInteraction(taskType)
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
        fun onFragmentInteraction(taskType: TaskTypeObject?)
        fun addItem(item: TaskTypeObject?)
    }

    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "name"
        private const val ARG_PARAM2 = "create"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param task Parameter 1.
         * @return A new instance of fragment EditTaskTypeFragment.
         */
        @JvmStatic
        fun newInstance(task: TaskTypeObject?, create: Boolean): EditTaskTypeFragment {
            val fragment = EditTaskTypeFragment()
            val args = Bundle()
            args.putSerializable(ARG_PARAM1, task)
            args.putBoolean(ARG_PARAM2, create)
            fragment.arguments = args
            return fragment
        }
    }
}