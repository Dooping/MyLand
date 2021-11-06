package com.gago.david.myland

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.gago.david.myland.LandFragment.OnListFragmentInteractionListener
import com.gago.david.myland.adapters.MyLandRecyclerViewAdapter
import com.gago.david.myland.models.LandObject
import com.gago.david.myland.models.PriorityObject
import id.arieridwan.lib.PageLoader

/**
 * A fragment representing a list of Items.
 *
 *
 * Activities containing this fragment MUST implement the [OnListFragmentInteractionListener]
 * interface.
 */
class LandFragment
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
    : Fragment() {
    private var mColumnCount = 1
    private var mListener: OnListFragmentInteractionListener? = null
    private var adapter: MyLandRecyclerViewAdapter? = null
    private var pageLoader: PageLoader? = null
    private lateinit var lands: MutableList<LandObject>
    private lateinit var priorities: List<PriorityObject>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mColumnCount = requireArguments().getInt(ARG_COLUMN_COUNT)
        }
        priorities = LandOpenHelper.readPriorities(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_land_list, container, false)
        lands = LandOpenHelper.readLands(requireContext())
        if (view is FrameLayout) {
            val context = view.getContext()
            val recyclerView: RecyclerView = view.findViewById(R.id.list)
            recyclerView.layoutManager =
                LinearLayoutManager(context)
            val emptyListView = view.findViewById<View>(R.id.land_list_empty)
            adapter = MyLandRecyclerViewAdapter(lands, mListener, priorities, emptyListView)
            recyclerView.adapter = adapter
        }
        //lands = new ArrayList<>();

        // Set the adapter
        val btn = view.findViewById<View>(R.id.add_land_button) as FloatingActionButton
        pageLoader = view.findViewById<View>(R.id.pageloader) as PageLoader
        btn.setOnClickListener {
            val intent = Intent(context, AddLandActivity::class.java)
            startActivityForResult(intent, 1)
            pageLoader!!.startProgress()
        }
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val filename = data!!.getStringExtra("name")
                val area = data.getDoubleExtra("area", 0.0)
                (activity as MainActivity?)!!.addLandDetails(filename, area)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnListFragmentInteractionListener) {
            context
        } else {
            throw RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onStop() {
        super.onStop()
        pageLoader!!.stopProgress()
    }

    override fun onResume() {
        super.onResume()
        lands.clear()
        lands.addAll(LandOpenHelper.readLands(requireContext()))
        adapter!!.notifyDataSetChanged()
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
    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(item: LandObject)
    }

    companion object {
        private const val ARG_COLUMN_COUNT = "column-count"
        fun newInstance(columnCount: Int): LandFragment {
            val fragment = LandFragment()
            val args = Bundle()
            args.putInt(ARG_COLUMN_COUNT, columnCount)
            fragment.arguments = args
            return fragment
        }
    }
}