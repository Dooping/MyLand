package com.gago.david.myland;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

public class ImageAdapter extends ArrayAdapter<Integer> {
    private final Context context;
    private final ArrayList<Integer> list;
    private final int tintColor;

    private static class ViewHolder {
        ImageView image;
    }

    public ImageAdapter(Context context, ArrayList<Integer> list, int tintColor) {
        super(context, R.layout.image_item, list);
        this.context = context;
        this.list = list;
        this.tintColor = tintColor;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        int dataModel = list.get(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag


        if (convertView == null) {
            Log.v("adapter", "null");

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.image_item, parent, false);
            viewHolder.image = convertView.findViewById(R.id.image);


            convertView.setTag(viewHolder);
        } else {
            Log.v("adapter", "not null");
            viewHolder = (ViewHolder) convertView.getTag();
        }


        Log.v("adapter", " "+dataModel);
        viewHolder.image.setImageResource(dataModel);
        viewHolder.image.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
        // Return the completed view to render on screen
        return convertView;
    }
}
