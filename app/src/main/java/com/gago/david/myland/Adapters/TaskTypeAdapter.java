package com.gago.david.myland.Adapters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gago.david.myland.R;
import com.gago.david.myland.Models.TaskTypeObject;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import com.gago.david.myland.SettingsFragment.OnTaskListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link TaskTypeObject} and makes a call to the
 * specified {@link OnTaskListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class TaskTypeAdapter extends RecyclerView.Adapter<TaskTypeAdapter.ViewHolder> {

    private final List<TaskTypeObject> mValues;
    private final OnTaskListFragmentInteractionListener mListener;

    public TaskTypeAdapter(List<TaskTypeObject> items, OnTaskListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_type_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.task.setText(mValues.get(position).name);
        holder.description.setText(mValues.get(position).description);
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getRootView().getContext());
                alertDialog.setTitle(holder.mItem.name);
                alertDialog.setMessage(R.string.remove_task);

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
        public final TextView task;
        public final TextView description;
        public final CircleImageView remove;
        public TaskTypeObject mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            task = view.findViewById(R.id.task);
            description = view.findViewById(R.id.description);
            remove = view.findViewById(R.id.remove);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + task.getText() + "'";
        }
    }
}