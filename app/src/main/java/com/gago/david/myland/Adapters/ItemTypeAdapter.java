package com.gago.david.myland.Adapters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gago.david.myland.Models.PlantTypeObject;
import com.gago.david.myland.R;
import com.gago.david.myland.SettingsFragment.OnListFragmentInteractionListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlantTypeObject} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class ItemTypeAdapter extends RecyclerView.Adapter<ItemTypeAdapter.ViewHolder> {

    private final List<PlantTypeObject> mValues;
    private final OnListFragmentInteractionListener mListener;

    public ItemTypeAdapter(List<PlantTypeObject> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.plant_type_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.name.setText(mValues.get(position).name);
        holder.icon.setImageResource(mValues.get(position).icon);
        holder.icon.setColorFilter(Color.parseColor(mValues.get(position).color));
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getRootView().getContext());
                alertDialog.setTitle(holder.mItem.name);
                alertDialog.setMessage(R.string.remove_item);

                ColorFilter filter = new PorterDuffColorFilter(Color.parseColor(holder.mItem.color), PorterDuff.Mode.SRC_IN);
                Drawable icon = view.getRootView().getContext().getResources().getDrawable(holder.mItem.icon);
                icon.setColorFilter(filter);
                alertDialog.setIcon(icon);

                alertDialog.setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mListener.removeItem(holder.mItem);
                                mValues.remove(holder.mItem);
                                notifyDataSetChanged();
                            }
                        });

                alertDialog.setNegativeButton(R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertDialog.show();
            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.selectItem(holder.mItem);
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
        public final TextView name;
        public final CircleImageView icon;
        public final CircleImageView remove;
        public PlantTypeObject mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            name = view.findViewById(R.id.plant);
            icon = view.findViewById(R.id.icon);
            remove = view.findViewById(R.id.remove);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + name.getText() + "'";
        }
    }
}
