package com.gago.david.myland.Adapters;

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.gago.david.myland.R;
import com.gago.david.myland.Models.TaskObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> implements Filterable {
    private SortedList<TaskObject> mValues;
    private CustomFilter mFilter;
    private ArrayList<TaskObject> originalList;


    public TaskListAdapter(ArrayList<TaskObject> items) {
        mValues = new SortedList<>(TaskObject.class, new SortedList.Callback<TaskObject>() {
            @Override
            public int compare(TaskObject a, TaskObject b) {
                if(a.priority.compareTo(b.priority) == 0)
                    return a.creationDate.compareTo(b.creationDate);
                return a.priority.compareTo(b.priority);
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(TaskObject oldItem, TaskObject newItem) {
                // return whether the items' visual representations are the same or not.
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areItemsTheSame(TaskObject item1, TaskObject item2) {
                return item1.equals(item2);
            }
        });
        originalList = items;
        mValues.addAll(items);
        mFilter = new CustomFilter(TaskListAdapter.this, mValues);
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).taskType);
        holder.mContentView.setText(mValues.get(position).observations);
        setFadeAnimation(holder.mView);
    }
    private void setFadeAnimation(View view) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500);
        view.startAnimation(anim);
    }
    @Override
    public int getItemCount() {
        return mValues.size();
    }
    @Override
    public Filter getFilter() {
        return mFilter;
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final CircleImageView mLogoView;
        public final CircleImageView mNotificationView;
        public TaskObject mItem;
        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.id);
            mContentView = view.findViewById(R.id.content);
            mLogoView = view.findViewById(R.id.logo_image);
            mNotificationView = view.findViewById(R.id.notification);
        }
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
    public class CustomFilter extends Filter {
        private TaskListAdapter mAdapter;
        private SortedList<TaskObject> filteredList;
        private CustomFilter(TaskListAdapter mAdapter, SortedList<TaskObject> list) {
            super();
            this.mAdapter = mAdapter;
            this.filteredList = list;
        }
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final FilterResults results = new FilterResults();
            ArrayList<TaskObject> filteredResults=new ArrayList<>();
            if (constraint.length() == 0) {
                filteredResults.addAll(originalList);
            } else {
                final String[] filterPattern = constraint.toString().split(" ");
                switch (filterPattern[0]){
                    case "all":
                        filteredResults.addAll(originalList);
                        break;

                    case "land":
                        for (int i = 0; i < originalList.size(); i++)
                            if (originalList.get(i).plantIndex==null) {
                                filteredResults.add(originalList.get(i));
                            }
                        break;
                    case "group":
                        List<Integer> idx = new ArrayList<>();
                        for(int i = 1; i < filterPattern.length; i++)
                            idx.add(Integer.valueOf(filterPattern[i]));
                        for (int i = 0; i < originalList.size(); i++)
                            if (idx.contains(originalList.get(i).plantIndex)) {
                                filteredResults.add(originalList.get(i));
                            }
                        break;
                    case "item":
                        for (int i = 0; i < originalList.size(); i++)
                            if (Integer.valueOf(filterPattern[1]).equals(originalList.get(i).plantIndex)) {
                                filteredResults.add(originalList.get(i));
                            }
                        break;

                }
            }
            Log.v("Count Number ", ""+filteredResults.size());
            results.values = filteredResults;
            results.count = filteredResults.size();
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList.clear();
            filteredList.addAll((ArrayList<TaskObject>)results.values);
            //System.out.println("Count Number 2 " + ((List<TaskObject>) results.values).size());
            this.mAdapter.notifyDataSetChanged();
        }

    }
}
