package com.gago.david.myland.Adapters;

import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.gago.david.myland.R;

import java.util.List;

public class ImageItemAdapter extends RecyclerView.Adapter<ImageItemAdapter.ViewHolder> {

    private final List<Integer> mValues;
    private final int colotTint;

    public ImageItemAdapter(List<Integer> items, int colorTint) {
        mValues = items;
        this.colotTint = colorTint;
    }

    @Override
    public ImageItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_item, parent, false);
        return new ImageItemAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ImageItemAdapter.ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setImageResource(mValues.get(position));
        holder.mIdView.setColorFilter(colotTint, PorterDuff.Mode.SRC_IN);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }*/
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mIdView;
        public int mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.image);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mIdView.toString() + "'";
        }
    }
}