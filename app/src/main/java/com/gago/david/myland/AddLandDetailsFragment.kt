package com.gago.david.myland

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.gago.david.myland.models.LandObject

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [AddLandDetailsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [AddLandDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddLandDetailsFragment : Fragment() {
    private var mParam1: String? = null
    private var mParam2: String? = null

    lateinit var image: ImageView
    lateinit var name: EditText
    lateinit var description: EditText
    lateinit var button: FloatingActionButton

    private var imageUri: String? = null
    private var area: Double? = null
    private var created = false
    private var mListener: OnFragmentInteractionListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        imageUri = requireArguments().getString("filename")
        area = requireArguments().getDouble("area")
        Log.v("ADDDETAIL", imageUri!!)
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_land_details, container, false)
        image = view.findViewById(R.id.land_detail_image)
        name = view.findViewById(R.id.land_name)
        description = view.findViewById(R.id.land_description)
        button = view.findViewById(R.id.next_button)
        image.setImageBitmap(LandOpenHelper.getImage(requireContext(), imageUri))
        button.setOnClickListener { addLandQuery() }
        return view
    }

    private fun addLandQuery() {
        val success = LandOpenHelper.addLand(requireContext(), LandObject(name.text.toString(), imageUri!!, description.text.toString(), area!!))
        if (!success) Toast.makeText(context, "Land already exists, choose a different name", Toast.LENGTH_SHORT).show() else {
            created = true
            val intent = Intent(context, ScrollingActivity::class.java)
            val b = Bundle()
            b.putString("name", name.text.toString()) //Your id
            intent.putExtras(b) //Put your id to your next Intent
            startActivity(intent)
            (activity as MainActivity?)!!.removeLandDetails()
        }
    }

    fun onButtonPressed(uri: Uri?) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
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

    override fun onDestroyView() {
        if (!created) LandOpenHelper.deleteImage(imageUri!!, requireContext())
        super.onDestroyView()
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
        fun onFragmentInteraction(uri: Uri?)
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
         * @return A new instance of fragment AddLandDetailsFragment.
         */
        fun newInstance(param1: String?, param2: String?): AddLandDetailsFragment {
            val fragment = AddLandDetailsFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}