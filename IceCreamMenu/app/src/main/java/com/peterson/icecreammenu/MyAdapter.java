package com.peterson.icecreammenu;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.FlavorHolder> {
    private List<FlavorItem> mflavors;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class FlavorHolder extends RecyclerView.ViewHolder {
        public LinearLayout linearLayout;
        public ImageView imageView;
        public TextView nameTextView;
        public TextView descTextView;

        public FlavorHolder(View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.rvLayout);
            imageView = itemView.findViewById(R.id.rvImage);
            nameTextView = itemView.findViewById(R.id.rvName);
            descTextView = itemView.findViewById(R.id.rvDesc);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(List<FlavorItem> flavors) {
        mflavors = flavors;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FlavorHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = null;
        if (MainActivity.isGridView) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_grid_item, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_list_item, parent, false);
        }
        FlavorHolder vh = new FlavorHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final FlavorHolder holder, int position) {
        // - get element from your dataset at this position
        final FlavorItem flavor = mflavors.get(position);
        // - replace the contents of the view with that element
        holder.imageView.setImageResource(flavor.getImageRefID());
        holder.nameTextView.setText(flavor.getName());
        holder.descTextView.setText(flavor.getDescription());

        if (MainActivity.isAdmin) {
            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("onClick", "item \"" + holder.nameTextView.getText() + "\" clicked");
                }
            });
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mflavors.size();
    }
}