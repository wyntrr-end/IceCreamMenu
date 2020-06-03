package com.peterson.icecreammenu;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private List<FlavorItem> mflavors;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView nameTextView;
        public TextView descTextView;

        public MyViewHolder(ImageView img, TextView name, TextView desc) {
            super(img);
            imageView = img;
            nameTextView = name;
            descTextView = desc;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(List<FlavorItem> flavors) {
        mflavors = flavors;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        ImageView img = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);
        TextView name = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);
        TextView desc = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);
        MyViewHolder vh = new MyViewHolder(img, name, desc);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.imageView.setImageBitmap(mflavors.get(position).getImage());
        holder.nameTextView.setText(mflavors.get(position).getName());
        holder.descTextView.setText(mflavors.get(position).getDescription());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mflavors.size();
    }
}