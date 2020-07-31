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

// =================================================================================================
// Custom implementation of a RecyclerView Adapter which defines how FlavorItems are displayed
// in the main RecyclerView.
// =================================================================================================
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.FlavorHolder> {
    private final FlavorList mFlavorList;
    private FlavorList availFlavorList;
    private final Context mContext;
    private int viewMode = MainActivity.VIEW_LIST;
    private final File flavorFile;

    private AdminEditActivity mAdminEditActivity;

    // ---------------------------------------------------------------------------------------------
    // basic constructor
    // ---------------------------------------------------------------------------------------------
    public MyAdapter(Context context, FlavorList flavorList) {
        if (MainActivity.TESTING)
            Log.d("MyAdapter", "Created new MyAdapter from generic context");
        mContext = context;
        mFlavorList = flavorList;
        availFlavorList = new FlavorList(mFlavorList.getType());
        refreshAvailableFlavors();

        flavorFile = new File(mContext.getFilesDir(), "flavors.json");
    }
    // ---------------------------------------------------------------------------------------------
    // constructor called from AdminEditActivity to allow editing functions
    // ---------------------------------------------------------------------------------------------
    public MyAdapter(AdminEditActivity adminEditActivity, FlavorList flavorList) {
        mAdminEditActivity = adminEditActivity;
        mContext = adminEditActivity.getApplicationContext();
        mFlavorList = flavorList;
        viewMode = MainActivity.VIEW_EDIT;
        flavorFile = new File(mContext.getFilesDir(), "flavors.json");
    }

    // ---------------------------------------------------------------------------------------------
    // custom data changed notification method to ensure the lists are updated
    // ---------------------------------------------------------------------------------------------
    public void notifyDataChanged() {
        if (MainActivity.TESTING)
            Log.d("MyAdapter", "Notifying data changed");
        refreshAvailableFlavors();
    }

    // ---------------------------------------------------------------------------------------------
    // discard and rebuild the list of available flavors
    // ---------------------------------------------------------------------------------------------
    private void refreshAvailableFlavors() {
        if (availFlavorList == null) {
            Log.e("MyAdapter", "refreshAvailableFlavors called when availFlavorList is null");
            return;
        }
        availFlavorList.clear();
        for (int i = 0; i < mFlavorList.size(); i++) {
            FlavorItem f = mFlavorList.get(i);
            if (f.isAvailable())
                availFlavorList.addFlavor(f);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // create new view holders for each item (invoked by the layout manager)
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
    // set the contents of an item view holder (invoked by the layout manager)
    // ---------------------------------------------------------------------------------------------
    @Override
    public void onBindViewHolder(final FlavorHolder holder, final int position) {
        FlavorList flavors = (viewMode == MainActivity.VIEW_EDIT) ? mFlavorList : availFlavorList;

        // get the FlavorItem at this position
        final FlavorItem flavor = new FlavorItem();
        flavor.readFromJSONFile(flavorFile, flavors.get(position).getName());

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

            // when the item is clicked, toggle the availability of the flavor and save the
            // change in the file
            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.chAvailable.setChecked(!holder.chAvailable.isChecked());

                    // we can use the position in mFlavorList because this code will only be reached
                    // in Edit mode, when the position comes from mFlavorList
                    mFlavorList.get(position).setAvailability(holder.chAvailable.isChecked());

                    flavor.setAvailability(holder.chAvailable.isChecked());
                    flavor.writeToJSONFile(flavorFile);
                }
            });

            // when the edit button is clicked, launch EditFlavorActivity
            if (holder.btnEditItem != null)
                holder.btnEditItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(
                                "MyAdapter",
                                "launchEditFlavorActivity for flavor " + holder.nameTextView.getText()
                        );
                        mAdminEditActivity.launchEditFlavorActivity(holder.nameTextView.getText().toString());
                    }
                });
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Return the size of the current dataset (invoked by the layout manager)
    // ---------------------------------------------------------------------------------------------
    @Override
    public int getItemCount() {
        if (viewMode == MainActivity.VIEW_EDIT)
            return mFlavorList.size();
        return availFlavorList.size();
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