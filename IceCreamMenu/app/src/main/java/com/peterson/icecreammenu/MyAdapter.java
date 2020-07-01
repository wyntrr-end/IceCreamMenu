package com.peterson.icecreammenu;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private List<FlavorItem> mFlavorItemList;
    private MainActivity mMainActivity;

    // ---------------------------------------------------------------------------------------------
    // basic constructor
    // ---------------------------------------------------------------------------------------------
    public MyAdapter(MainActivity mainActivity, List<FlavorItem> flavorItemList) {
        mMainActivity = mainActivity;
        mFlavorItemList = flavorItemList;
    }

    // ---------------------------------------------------------------------------------------------
    // create new views (invoked by the layout manager)
    // ---------------------------------------------------------------------------------------------
    @Override
    public FlavorHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view using the appropriate layout (grid or list layout)
        View v = LayoutInflater.from(parent.getContext()).inflate((
                MainActivity.isGridView ?
                        R.layout.recycler_grid_item :
                        R.layout.recycler_list_item
        ), parent, false);
        return new FlavorHolder(v);
    }

    // ---------------------------------------------------------------------------------------------
    // set the contents of a view (invoked by the layout manager)
    // ---------------------------------------------------------------------------------------------
    @Override
    public void onBindViewHolder(final FlavorHolder holder, int position) {
        // get the FlavorItem at this position
        final FlavorItem flavor = mFlavorItemList.get(position);

        // replace the contents of the view with values appropriate for that FlavorItem
        String flavorImgName = flavor.getImageName();
        if (!flavorImgName.equals("")) {
            // if an image name is given, use that to set the image
            File flavorImg = new File(mMainActivity.getApplicationContext().getFilesDir(), flavorImgName);
            holder.imageView.setImageURI(Uri.fromFile(flavorImg));
        } else {
            // if no image name is given, use the placeholder icon
            holder.imageView.setImageResource(R.drawable.ic_icecream_vector_purple);
        }
        holder.nameTextView.setText(flavor.getName());
        holder.descTextView.setText(flavor.getDescription());

        // add an onClickListener to launch an appropriate instance of AddEditFlavorActivity
        // in Edit mode if the user is Admin
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.isAdmin) {
                    Log.d(
                            "Adapter",
                            "launchEditFlavorActivity for flavor " + holder.nameTextView.getText()
                    );
                    mMainActivity.launchEditFlavorActivity(
                            mMainActivity.getCurrentFocus(),
                            holder.nameTextView.getText().toString()
                    );
                }
            }
        });
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

        FlavorHolder(View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.rvLayout);
            imageView = itemView.findViewById(R.id.rvImage);
            nameTextView = itemView.findViewById(R.id.rvName);
            descTextView = itemView.findViewById(R.id.rvDesc);
        }
    }
}