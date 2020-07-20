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
import java.util.List;

// =================================================================================================
// class which defines how FlavorItems are displayed in the main RecyclerView
// =================================================================================================
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.FlavorHolder> {
    private FlavorList mFlavorItemList;
    private Context mContext;
    private int viewMode = MainActivity.VIEW_LIST;
    private File flavorFile;

    private AdminEditActivity mAdminEditActivity;

    // ---------------------------------------------------------------------------------------------
    // basic constructor
    // ---------------------------------------------------------------------------------------------
    public MyAdapter(Context context, FlavorList flavorItemList) {
        mContext = context;
        mFlavorItemList = flavorItemList;
        flavorFile = new File(mContext.getFilesDir(), "flavors.json");
    }
    // ---------------------------------------------------------------------------------------------
    // basic constructor
    // ---------------------------------------------------------------------------------------------
    public MyAdapter(AdminEditActivity adminEditActivity, FlavorList flavorItemList) {
        mAdminEditActivity = adminEditActivity;
        mContext = adminEditActivity.getApplicationContext();
        mFlavorItemList = flavorItemList;
        viewMode = MainActivity.VIEW_EDIT;
        flavorFile = new File(mContext.getFilesDir(), "flavors.json");
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
        final FlavorItem flavor = new FlavorItem();
        flavor.readFromJSONFile(flavorFile, mFlavorItemList.get(position).getName());

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
                    // TODO -- deal with toggling and saving the availability
                    holder.chAvailable.setChecked(!holder.chAvailable.isChecked());
                    flavor.setAvailability(holder.chAvailable.isChecked());
                    mFlavorItemList.get(position).setAvailability(holder.chAvailable.isChecked());
                    flavor.writeToJSONFile(flavorFile);
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
        else {
            if (flavor.isAvailable()) {
                holder.itemView.setVisibility(View.VISIBLE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            } else {
                Log.d(
                        "Adapter",
                        "flavor unavailable: " + holder.nameTextView.getText()
                );
                holder.itemView.setVisibility(View.GONE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Return the size of the dataset (invoked by the layout manager)
    // ---------------------------------------------------------------------------------------------
    @Override
    public int getItemCount() {
        return mFlavorItemList.size();
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