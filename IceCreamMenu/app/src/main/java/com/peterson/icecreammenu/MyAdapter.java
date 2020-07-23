package com.peterson.icecreammenu;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

// =================================================================================================
// Custom implementation of a RecyclerView Adapter which defines how FlavorItems are displayed
// in the main RecyclerView.
// =================================================================================================
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.FlavorHolder> {
    private FlavorList mFlavorList;
    private FlavorList allFlavors;
    private FlavorList availableFlavors;
    private Context mContext;
    private int viewMode = MainActivity.VIEW_LIST;
    private File flavorFile;
    private int offset = 0;

    private AdminEditActivity mAdminEditActivity;

    // ---------------------------------------------------------------------------------------------
    // basic constructor
    // ---------------------------------------------------------------------------------------------
    public MyAdapter(Context context, FlavorList flavorList) {
        mContext = context;
        allFlavors = flavorList;
        availableFlavors = new FlavorList(flavorList.getType());
        flavorFile = new File(mContext.getFilesDir(), "flavors.json");
        refreshFlavorLists();
    }
    // ---------------------------------------------------------------------------------------------
    // constructor called from AdminEditActivity to allow editing functions
    // ---------------------------------------------------------------------------------------------
    public MyAdapter(AdminEditActivity adminEditActivity, FlavorList flavorList) {
        mAdminEditActivity = adminEditActivity;
        mContext = adminEditActivity.getApplicationContext();
        allFlavors = flavorList;
        availableFlavors = new FlavorList(flavorList.getType());
        viewMode = MainActivity.VIEW_EDIT;
        flavorFile = new File(mContext.getFilesDir(), "flavors.json");
        refreshFlavorLists();
    }

    // ---------------------------------------------------------------------------------------------
    // create new views (invoked by the layout manager)
    // ---------------------------------------------------------------------------------------------
    @Override
    public FlavorHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view using the appropriate layout (grid, list, or edit list layout)
        int layout;
        if (viewMode == MainActivity.VIEW_GRID) {
            layout = R.layout.recycler_grid_item;
        } else if (viewMode == MainActivity.VIEW_EDIT) {
            layout = R.layout.recycler_list_edit_item;
        } else {
            layout = R.layout.recycler_list_item;
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new FlavorHolder(v);
    }

    // ---------------------------------------------------------------------------------------------
    // set the contents of a view (invoked by the layout manager)
    // ---------------------------------------------------------------------------------------------
    @Override
    public void onBindViewHolder(final FlavorHolder holder, final int position) {
        // get the FlavorItem at this position
        FlavorItem tmpFlavor = mFlavorList.get(position + offset);
        if (viewMode != MainActivity.VIEW_EDIT) {
            while (!tmpFlavor.isAvailable() && position + offset + 1 < mFlavorList.size()) {
                offset++;
                tmpFlavor = mFlavorList.get(position + offset);
            }
        }
        final FlavorItem flavor = tmpFlavor;

        if (MainActivity.TESTING)
            Log.d("MyAdapter",
                    "Flavor at position " + position + " is " + flavor.getName()
            );

        // replace the contents of the view with values appropriate for that FlavorItem
        String flavorImgName = flavor.getImgName();
        if (!flavorImgName.equals("")) {
            // if an image name is given, use that to set the image
            File flavorImg = new File(mContext.getFilesDir(), flavorImgName);
            holder.imageView.setImageURI(Uri.fromFile(flavorImg));
        } else {
            // if no image name is given, use the placeholder icon
            holder.imageView.setImageResource(R.drawable.ic_icecream_vector_purple);
        }
        holder.nameTextView.setText(flavor.getName());
        holder.descTextView.setText(flavor.getDescription());

        // if we're in admin edit mode, set additional functionality
        if (viewMode == MainActivity.VIEW_EDIT) {

            //set the availability checkbox appropriately
            holder.chAvailable.setChecked(flavor.isAvailable());

            // when the item is clicked, toggle the availability
            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.chAvailable.setChecked(!holder.chAvailable.isChecked());
                    flavor.setAvailability(holder.chAvailable.isChecked());
                    mFlavorList.get(position).setAvailability(holder.chAvailable.isChecked());
                    flavor.writeToJSONFile(flavorFile);
                    refreshFlavorLists();
                }
            });

            // when the edit button is clicked, launch EditFlavorActivity
            if (holder.btnEditItem != null)
                holder.btnEditItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(
                                "Adapter",
                                "launchEditFlavorActivity for flavor " + holder.nameTextView.getText()
                        );
                        mAdminEditActivity.launchEditFlavorActivity(holder.nameTextView.getText().toString());
                    }
                });
        }
//        else {
//            if (flavor.isAvailable()) {
//                holder.itemView.setVisibility(View.VISIBLE);
//                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//            } else {
//                Log.d(
//                        "MyAdapter",
//                        "flavor unavailable: " + holder.nameTextView.getText()
//                );
//                holder.itemView.setVisibility(View.GONE);
//                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
//            }
//        }
    }

    // ---------------------------------------------------------------------------------------------
    // clear the list of available flavors and populate it with every available flavor from the
    // main flavor list
    // ---------------------------------------------------------------------------------------------
    private void refreshFlavorLists() {
        availableFlavors.clear();
        for (int i = 0; i < allFlavors.size(); i++) {
            FlavorItem flavor = allFlavors.get(i);
            if (flavor.isAvailable())
                availableFlavors.addFlavor(flavor);
        }
        mFlavorList = (viewMode == MainActivity.VIEW_EDIT) ? allFlavors.clone() : availableFlavors.clone();
    }

    // ---------------------------------------------------------------------------------------------
    // Return the size of the dataset (invoked by the layout manager)
    // ---------------------------------------------------------------------------------------------
    @Override
    public int getItemCount() {
        return mFlavorList.size();
    }

    // ---------------------------------------------------------------------------------------------
    // class which defines a custom ViewHolder for FlavorItems
    // ---------------------------------------------------------------------------------------------
    static class FlavorHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        ImageView imageView;
        TextView nameTextView;
        TextView descTextView;
        CheckBox chAvailable;
        ImageButton btnEditItem;

        FlavorHolder(View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.rvLayout);
            imageView = itemView.findViewById(R.id.rvImage);
            nameTextView = itemView.findViewById(R.id.rvName);
            descTextView = itemView.findViewById(R.id.rvDesc);
            chAvailable = itemView.findViewById(R.id.chAvailable);
            btnEditItem = itemView.findViewById(R.id.btnEditItem);
        }
    }

    public void setViewMode(int newMode) {
        viewMode = newMode;
    }
}