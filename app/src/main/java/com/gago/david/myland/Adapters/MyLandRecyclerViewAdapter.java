package com.gago.david.myland.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gago.david.myland.LandFragment.OnListFragmentInteractionListener;
import com.gago.david.myland.LandOpenHelper;
import com.gago.david.myland.Models.LandObject;
import com.gago.david.myland.Models.PriorityObject;
import com.gago.david.myland.Models.TaskTypeObject;
import com.gago.david.myland.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * {@link RecyclerView.Adapter} that can display a {@link LandObject} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyLandRecyclerViewAdapter extends RecyclerView.Adapter<MyLandRecyclerViewAdapter.ViewHolder> {

    private final List<LandObject> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final List<PriorityObject> priorities;

    public MyLandRecyclerViewAdapter(List<LandObject> items, OnListFragmentInteractionListener listener, List<PriorityObject> priorities) {
        mValues = items;
        mListener = listener;
        this.priorities = priorities;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_land, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(holder.mItem.name);
        holder.mContentView.setText(holder.mItem.Description);
        holder.landImage.setImageBitmap(new LandOpenHelper((Context) mListener).getImage(holder.mItem.imageUri));
        holder.number.setText(String.format("%d",holder.mItem.notifications));
        boolean colorChanged = false;
        for (PriorityObject p : priorities)
            if(p.p_order == holder.mItem.priority) {
                holder.notification.setImageDrawable(new ColorDrawable(Color.parseColor(p.color)));
                colorChanged = true;
                break;
            }
        if(!colorChanged)
            holder.notification.setImageDrawable(new ColorDrawable(Color.WHITE));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final CircleImageView landImage;
        public final CircleImageView notification;
        public final TextView number;
        public LandObject mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.id);
            mContentView = view.findViewById(R.id.content);
            landImage = view.findViewById(R.id.land_image);
            notification = view.findViewById(R.id.notification);
            number = view.findViewById(R.id.number);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
