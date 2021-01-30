package com.gago.david.myland

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import br.com.bloder.magic.view.MagicButton
import butterknife.BindView
import butterknife.ButterKnife
import com.gago.david.myland.adapters.ImageAdapter
import com.gago.david.myland.models.PlantTypeObject
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [EditItemTypeFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [EditItemTypeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditItemTypeFragment : Fragment() {
    private lateinit var item: PlantTypeObject
    private var create = false
    private var list: ArrayList<Int>? = null
    private var tintColor: String? = null
    private var drawable = 0

    @JvmField
    @BindView(R.id.item_name)
    var nameView: EditText? = null

    @JvmField
    @BindView(R.id.item_image)
    var imageView: ImageView? = null

    @JvmField
    @BindView(R.id.submit_button)
    var button: FloatingActionButton? = null

    @JvmField
    @BindView(R.id.edit_icon)
    var editIcon: MagicButton? = null

    @JvmField
    @BindView(R.id.edit_color)
    var editColor: MagicButton? = null
    private var mListener: OnFragmentInteractionListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            item = arguments!!.getSerializable(ARG_PARAM1) as PlantTypeObject
            create = arguments!!.getBoolean(ARG_PARAM2)
            tintColor = if (item.color == "") "#669900" else item.color
            drawable = item.icon
        }
        val array = resources.obtainTypedArray(R.array.imageList)
        list = ArrayList()
        for (i in 0..6) list!!.add(array.getResourceId(i, -1))
        Log.v("images", list.toString())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_item_type, container, false)
        ButterKnife.bind(this, view)
        nameView!!.setText(item.name)
        imageView!!.setImageResource(drawable)
        imageView!!.setColorFilter(Color.parseColor(tintColor), PorterDuff.Mode.SRC_IN)
        editIcon!!.setMagicButtonClickListener { showAlertDialog() }
        editColor!!.setMagicButtonClickListener {
            ColorPickerDialog.newBuilder()
                    .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                    .setDialogId(0)
                    .setColor(Color.parseColor(tintColor))
                    .setShowAlphaSlider(false)
                    .show(activity)
        }
        button!!.setOnClickListener {
            item.name = nameView!!.text.toString()
            item.icon = drawable
            item.color = tintColor!!
            onButtonPressed(item)
        }
        return view
    }

    private fun showAlertDialog() {
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle("Choose image")
        val filter: ColorFilter = PorterDuffColorFilter(Color.parseColor(tintColor), PorterDuff.Mode.SRC_IN)
        val icon = resources.getDrawable(drawable)
        icon.colorFilter = filter
        alertDialog.setIcon(icon)
        alertDialog.setAdapter(ImageAdapter(activity!!, list!!, Color.parseColor(tintColor))
        ) { _, i ->
            imageView!!.setImageResource(list!![i])
            drawable = list!![i]
        }
        alertDialog.show()
    }

    private fun onButtonPressed(item: PlantTypeObject) {
        if (mListener != null) {
            if (create) mListener!!.addItem(item) else mListener!!.onFragmentInteraction(item)
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

    fun setColor(color: String?) {
        imageView!!.setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN)
        tintColor = color
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
        fun onFragmentInteraction(itemType: PlantTypeObject)
        fun addItem(item: PlantTypeObject)
    }

    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "name"
        private const val ARG_PARAM2 = "create"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param item Parameter 1.
         * @return A new instance of fragment EditTaskTypeFragment.
         */
        @JvmStatic
        fun newInstance(item: PlantTypeObject?, create: Boolean): EditItemTypeFragment {
            val fragment = EditItemTypeFragment()
            val args = Bundle()
            args.putSerializable(ARG_PARAM1, item)
            args.putBoolean(ARG_PARAM2, create)
            fragment.arguments = args
            return fragment
        }
    }
}