package com.gago.david.myland;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class PopupMenuAdapter extends ArrayAdapter<PlantTypeObject> {

    private final OnMenuItemInteractionListener mListener;

    public PopupMenuAdapter(@NonNull Context context, @NonNull List<PlantTypeObject> objects) {
        super(context, R.layout.menu_item, objects);
        mListener = (OnMenuItemInteractionListener)context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final PlantTypeObject plant = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.menu_item, parent, false);
        }
        // Lookup view for data population
        ImageView icon = convertView.findViewById(R.id.icon);
        TextView plantText = convertView.findViewById(R.id.plant);
        // Populate the data into the template view using the data object
        icon.setImageResource(plant.icon);
        icon.setColorFilter(Color.parseColor(plant.color));
        plantText.setText(plant.name);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onMenuItemInteraction(plant);
                }
            }
        });
        // Return the completed view to render on screen
        return convertView;
    }


    public interface OnMenuItemInteractionListener {
        // TODO: Update argument type and name
        void onMenuItemInteraction(PlantTypeObject item);
    }
}
