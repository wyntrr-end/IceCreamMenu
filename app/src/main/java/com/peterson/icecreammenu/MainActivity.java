package com.peterson.icecreammenu;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// =================================================================================================
// Main Activity which allows any user to view the lists of flavors.

// Users can:
//      - Switch between Ice Cream and Gelato/Sorbet lists
//      - Switch between list and grid view

// Admins can:
//      - Switch to Admin Edit Mode in order to manage flavors
// =================================================================================================
public class MainActivity extends AppCompatActivity {
    public static final boolean TESTING = true;
    public static final boolean VERBOSE = false;

    public static boolean hasCamera = false;
    public static boolean isAdmin = true;
    public static boolean INIT = true;

    public static final int VIEW_LIST = 0;
    public static final int VIEW_GRID = 1;
    public static final int VIEW_EDIT = 2;
    private int viewMode = VIEW_LIST;

    private RecyclerView recyclerView;
    private MyAdapter iceCreamAdapter;
    private MyAdapter otherFlavorAdapter;

    private Toolbar toolbar;
    private ImageButton btnEdit;
    private ImageButton btnViewMode;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    private FlavorList iceCreamFlavorList;
    private FlavorList otherFlavorList;

    // ---------------------------------------------------------------------------------------------
    // perform setup tasks
    // ---------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // instantiate flavor lists
        iceCreamFlavorList = new FlavorList(FlavorItem.ICE_CREAM);
        otherFlavorList = new FlavorList(FlavorList.OTHER_TYPE);

        // setup recyclerView with two adapters, default to showing ice cream
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        iceCreamAdapter = new MyAdapter(this, iceCreamFlavorList);
        otherFlavorAdapter = new MyAdapter(this, otherFlavorList);
        recyclerView.setAdapter(iceCreamAdapter);

        // load sample flavor information and check for an available camera
        // if the app is being initialised
        // ============================ WARNING ==============================
        // INIT will be true every time the app is launched
        // ===================================================================
        if (TESTING)
            Log.d("INIT", "INIT = " + INIT);
        if (INIT) {
            loadSampleInfo();
            hasCamera = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);

//            Intent intent = new Intent(this, AdminEditActivity.class);
//            startActivity(intent);
        }

        toolbar = findViewById(R.id.toolbarMain);
        if (!isAdmin) {
            toolbar.setTitle(R.string.main_header);
        }

        btnEdit = findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AdminEditActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        if (!isAdmin) {
            btnEdit.setVisibility(View.GONE);
        }

        // allow toggling admin/user version when testing
        Switch switchAdmin = findViewById(R.id.switchAdmin);
        switchAdmin.setChecked(isAdmin);
        if (TESTING) {
            switchAdmin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleAdmin();
                }
            });
        } else {
            switchAdmin.setVisibility(View.GONE);
        }

        // toggle grid view when tapping on btnViewMode
        btnViewMode = findViewById(R.id.btnViewMode);
        btnViewMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewMode = (viewMode + 1) % 2;
                updateViewType();
            }
        });

        // changing tabs switches the recyclerView adapter in order to
        // display appropriate information
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.i("MainActivity", "Changing tab...");
                tabLayout.setSelected(true);
                if (tab.getPosition() == 1) {
                    recyclerView.setAdapter(otherFlavorAdapter);
                } else {
                    recyclerView.setAdapter(iceCreamAdapter);
                }
                updateViewType();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // call the method to update the displayed content when the user performs
        // a swipe-to-refresh gesture
        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("MainActivity", "onRefresh called from SwipeRefreshLayout");
                        updateViewType();
                        reloadContent();
                    }
                }
        );

        // allow the user to reload all the sample data if in testing
        Button btnLoadSampleData = findViewById(R.id.btnLoadData);
        if (TESTING) {
            btnLoadSampleData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    loadSampleInfo();
                    reloadContent();
                }
            });
        } else {
            btnLoadSampleData.setVisibility(View.GONE);
        }

        // make sure the recyclerView loads properly
        updateViewType();
        reloadContent();

        INIT = false;
    }

    // ---------------------------------------------------------------------------------------------
    // deals with the result of AddEditFlavorActivity, adding/modifying the corresponding flavor
    // or doing nothing if the activity was cancelled
    // ---------------------------------------------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (TESTING)
            Log.d("MainActivity",
                    "Returned from AdminEditActivity with dataStatus=" + resultCode + "..."
            );

        // if info was changed, reload the content and update the view
        if (resultCode == AdminEditActivity.MODIFIED) {
            reloadContent();
            updateViewType();
        }

        // otherwise do nothing
    }

    // ---------------------------------------------------------------------------------------------
    // deals with switching modes between list, grid, and edit view
    // ---------------------------------------------------------------------------------------------
    private void updateViewType() {
        Log.i("MainActivity", "Updating view type...");

        // make sure the interface button displays the correct icon
        if (viewMode == VIEW_GRID) {
            btnViewMode.setImageResource(R.drawable.ic_view_module_yellow_24dp);
        } else {
            btnViewMode.setImageResource(R.drawable.ic_view_list_yellow_24dp);
        }

        // show/hide certian buttons when the user is admin
        if (isAdmin) {
            toolbar.setTitle(R.string.main_header_admin);
            btnEdit.setVisibility(View.VISIBLE);
        } else {
            toolbar.setTitle(R.string.main_header);
            btnEdit.setVisibility(View.GONE);
        }

        // update the view mode and get the current adapter
        iceCreamAdapter.setViewMode(viewMode);
        otherFlavorAdapter.setViewMode(viewMode);
        MyAdapter mAdapter = (MyAdapter) recyclerView.getAdapter();

        // set the layout manager according to the current view mode
        if (viewMode == VIEW_GRID) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        // reset the adapter to refresh the layout
        recyclerView.setAdapter(mAdapter);
    }

    // ---------------------------------------------------------------------------------------------
    // toggle the user's Admin status and make appropriate adjustments depending on
    // whether the user is Admin
    // ---------------------------------------------------------------------------------------------
    private void toggleAdmin() {
        isAdmin = !isAdmin;
        updateViewType();
    }

    // ---------------------------------------------------------------------------------------------
    // reload all the flavors from the "flavors.json" file
    // ---------------------------------------------------------------------------------------------
    private void reloadContent() {
        Log.i("MainActivity", "Reloading flavor lists...");

        // read in the "flavors.json" file and get an array of the contained flavor names
        File flavorFile = new File(getApplicationContext().getFilesDir(), getString(R.string.flavor_filename));

        iceCreamFlavorList.reloadFlavorsFromJSON(flavorFile);
        otherFlavorList.reloadFlavorsFromJSON(flavorFile);
        iceCreamAdapter.notifyDataChanged();
        otherFlavorAdapter.notifyDataChanged();

        if (TESTING)
            Log.d("MainActivity", "iceCreamFlavorList.size()=" + iceCreamFlavorList.size());

        swipeRefreshLayout.setRefreshing(false);
    }

    // ---------------------------------------------------------------------------------------------
    // build sample flavor info based on names from R.array.flavor_names_array and store the info
    // in both flavors.json and the internal flavor lists
    // -- this is only for use when testing
    // ---------------------------------------------------------------------------------------------
    public void loadSampleInfo() {
        Log.i("MainActivity", "Loading sample data...");

        // load names from R.array.flavor_names_array
        String[] mFlavorNameArray = getResources().getStringArray(R.array.flavor_names_array);

        // setup "flavors.json" as destination file (overwrite it if the file exists)
        File flavorFile = new File(getApplicationContext().getFilesDir(), "flavors.json");
        flavorFile.delete();
        JSONObject jsonAllFlavors = new JSONObject();

        // for each name in the array, build a sample flavor item and add it to the appropriate
        // list, in addition to adding it to jsonAllFlavors
        for (String name : mFlavorNameArray) {
            String desc = "description of " + name + " goes here";
            int type;
            if (name.contains("Sorbet")) {
                type = FlavorItem.SORBET;
            } else if (name.contains("Gelato")) {
                type = FlavorItem.GELATO;
            } else {
                type = FlavorItem.ICE_CREAM;
            }

            // find the appropriate drawable based on the flavor name and copy it to the app files
            // for use
            String imgName = name.toLowerCase().replace("& ", "").replace(" ", "_");
            File imgFile = new File(getApplicationContext().getFilesDir(), imgName + ".jpg");
            try {
                // get the drawable id of the file with the given imgName
                int imgID = getResources().getIdentifier(
                        imgName,
                        "drawable",
                        getApplicationContext().getPackageName()
                );
                // if such a drawable exists, copy it to app files using the same name
                if (imgID != 0) {
                    OutputStream out = new FileOutputStream(imgFile);
                    Drawable d = getDrawable(imgID);
                    Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.close();
                    if (TESTING)
                        Log.d("Image", "Wrote image to " + imgFile);
                    imgName = imgName + ".jpg";
                }
                // otherwise set imgName blank so a placeholder will be used
                else {
                    imgName = "";
                }
            } catch (IOException e) {
                Log.e("Image", "Error writing image.");
                imgName = "";
                e.printStackTrace();
            }

            // create a new FlavorItem with this info and add it to the appropriate flavor list
            FlavorItem flavor = new FlavorItem(imgName, name, type, desc);
            // availability will be true for all flavors except these 3
            if (!(name.equals("Almond Fudge")
                    || name.equals("Bananasplit")
                    || name.equals("Blueberry Yogurt"))) {
                flavor.setAvailability(true);
                iceCreamFlavorList.addFlavor(flavor);
                otherFlavorList.addFlavor(flavor);
            }

            // create JSONObject from this new FlavorItem and add it to the jsonAllFlavors object,
            // using the flavor name as the object name
            JSONObject jsonFlavor = flavor.toJSONObject();
            try {
                jsonAllFlavors.put(name, jsonFlavor);
            } catch (JSONException e) {
                Log.e("JSON", "Error putting to JSON object.");
                e.printStackTrace();
            }
        }

        // write the updated jsonAllFlavors to "flavors.json"
        JSONFileHandler.writeJsonObjectToFile(jsonAllFlavors, flavorFile);
        if (TESTING && VERBOSE) {
            try {
                Log.d("JSON", jsonAllFlavors.toString(2));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Toast.makeText(
                getApplicationContext(),
                "Loaded sample data",
                Toast.LENGTH_SHORT
        ).show();
    }
}
