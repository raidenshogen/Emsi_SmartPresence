package com.example.emsismartpresence;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.ViewHolder> {
    private List<SchoolLocation> locations;
    private OnLocationClickListener listener;

    public interface OnLocationClickListener {
        void onLocationClick(SchoolLocation location);
    }

    public LocationsAdapter(List<SchoolLocation> locations, OnLocationClickListener listener) {
        this.locations = locations;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SchoolLocation location = locations.get(position);
        holder.locationName.setText(location.getName());
        holder.locationAddress.setText(location.getAddress());
        holder.viewOnMapButton.setOnClickListener(v -> listener.onLocationClick(location));
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView locationName;
        TextView locationAddress;
        Button viewOnMapButton;

        ViewHolder(View view) {
            super(view);
            locationName = view.findViewById(R.id.locationName);
            locationAddress = view.findViewById(R.id.locationAddress);
            viewOnMapButton = view.findViewById(R.id.viewOnMapButton);
        }
    }
}