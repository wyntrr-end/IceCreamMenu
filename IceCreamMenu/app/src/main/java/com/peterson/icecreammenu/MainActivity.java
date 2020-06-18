package com.peterson.icecreammenu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// =================================================================================================
// MainActivity
// =================================================================================================
public class MainActivity extends AppCompatActivity {
    public static Boolean TESTING = true;
    public static Boolean isAdmin = true;
    public static Boolean isGridView = false;
    public static int ADD_MODE = 1;
    public static int EDIT_MODE = 2;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter iceCreamAdapter;
    private RecyclerView.Adapter gelatoAdapter;

    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton btnAddNewFlavor;
    private ImageButton btnListView;
    private ImageButton btnGridView;

    private List<FlavorItem> iceCreamFlavorList;
    private List<FlavorItem> gelatoFlavorList;

    // ---------------------------------------------------------------------------------------------
    // perform setup tasks
    // ---------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // instantiate flavor lists
        iceCreamFlavorList = new ArrayList<>();
        gelatoFlavorList = new ArrayList<>();

        // setup recyclerView with two adapters, default to showing ice cream
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        iceCreamAdapter = new MyAdapter(this, iceCreamFlavorList);
        gelatoAdapter = new MyAdapter(this, gelatoFlavorList);
        recyclerView.setAdapter(iceCreamAdapter);

        // load sample flavor information
        loadSampleInfo();

        // allow toggling admin/user version when testing
        Button btnToggleAdmin = findViewById(R.id.btnToggleAdmin);
        if (TESTING) {
            btnToggleAdmin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleAdmin();
                }
            });
        } else {
            btnToggleAdmin.setVisibility(View.GONE);
        }

        // switch to list view when tapping on btnListView
        btnListView = findViewById(R.id.btnListView);
        btnListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isGridView) {
                    isGridView = false;
                    updateViewType();
                }
            }
        });

        // switch to grid view when tapping on btnGridView
        btnGridView = findViewById(R.id.btnGridView);
        btnGridView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isGridView) {
                    isGridView = true;
                    updateViewType();
                }
            }
        });

        // changing tabs switches the recyclerView adapter in order to
        // display appropriate information
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabLayout.setSelected(true);
                if (tab.getPosition() == 1) {
                    recyclerView.setAdapter(gelatoAdapter);
                } else {
                    recyclerView.setAdapter(iceCreamAdapter);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // allow adding new flavors if the user is Admin
        btnAddNewFlavor = findViewById(R.id.buttonAddNewFlavor);
        btnAddNewFlavor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAddFlavorActivity(v);
            }
        });
        if (!isAdmin) {
            btnAddNewFlavor.hide();
        }

        // call the method to update the displayed content when the user performs
        // a swipe-to-refresh gesture
        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("Refresh", "onRefresh called from SwipeRefreshLayout");
                        reloadContent();
                    }
                }
        );

        // make sure the recyclerView loads properly
        updateViewType();
    }

    // ---------------------------------------------------------------------------------------------
    // deals with switching view modes between list and grid view
    // ---------------------------------------------------------------------------------------------
    private void updateViewType() {
        // make sure the interface buttons are yellow when active and grey when not active
        btnListView.setImageResource(isGridView ? R.drawable.ic_view_list_grey_24dp : R.drawable.ic_view_list_yellow_24dp);
        btnGridView.setImageResource(isGridView ? R.drawable.ic_view_module_yellow_24dp : R.drawable.ic_view_module_grey_24dp);

        // save the current adapter for later
        RecyclerView.Adapter mAdapter = recyclerView.getAdapter();
        // set the layout manager according to the current view mode
        recyclerView.setLayoutManager(isGridView ? new GridLayoutManager(this, 4) : new LinearLayoutManager(this));
        // reset the adapter to refresh the layout
        recyclerView.setAdapter(mAdapter);

        // notify the user which view mode we are now in
        Toast.makeText(getApplicationContext(), (isGridView ? "View by Case" : "View Alphabetically"), Toast.LENGTH_LONG).show();
    }

    // ---------------------------------------------------------------------------------------------
    // launches AddEditFlavorActivity in Add mode
    // ---------------------------------------------------------------------------------------------
    private void launchAddFlavorActivity(View v) {
        Intent intent = new Intent(this, AddEditFlavorActivity.class);
        intent.putExtra("MODE", ADD_MODE);
        startActivityForResult(intent, 1);
    }

    // ---------------------------------------------------------------------------------------------
    // launches AddEditFlavorActivity in Edit mode, passing the name of the flavor to edit
    // ---------------------------------------------------------------------------------------------
    public void launchEditFlavorActivity(View v, String flavorName) {
        Intent intent = new Intent(this, AddEditFlavorActivity.class);
        intent.putExtra("MODE", EDIT_MODE);
        intent.putExtra("OLD_NAME", flavorName);
        startActivityForResult(intent, 1);
    }

    // ---------------------------------------------------------------------------------------------
    // deals with the result of AddEditFlavorActivity, adding/modifying the corresponding flavor
    // or doing nothing if the activity was cancelled
    // ---------------------------------------------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // if something has been changed, reload the content
        if (resultCode == ADD_MODE || resultCode == EDIT_MODE) {
            reloadContent();
        }

        // otherwise do nothing
    }

    // ---------------------------------------------------------------------------------------------
    // adds a new flavor to the list of flavors using the given type, name, and description
    // ---------------------------------------------------------------------------------------------
    private void addFlavor(JSONObject jsonFlavor) {
        String name = "";
        String type = "";
        String desc = "";

        try {
            name = jsonFlavor.getString("NAME");
            type = jsonFlavor.getString("TYPE");
            desc = jsonFlavor.getString("DESC");
        } catch (org.json.JSONException e) {
            Log.d("JSON", "Error parsing JSON flavor in addFlavor()");
            e.printStackTrace();
        }

        // if the name field is blank, do nothing
        if (name.equals("")) return;

        // look for an image file based on the name of the flavor
        String drawableName = name.toLowerCase().replace("& ", "").replace(" ", "_");
        int imgID = getResources().getIdentifier(
                drawableName,
                "drawable",
                getApplicationContext().getPackageName()
        );
        // if no image is found, use purple ice cream icon
        if (imgID == 0) {
            imgID = R.drawable.ic_icecream_vector_purple;
        }
        Log.d("Image", "flavor = " + name + " | imgID = " + imgID);

        // add the new flavor to the corresponding list of flavors, re-sort the list,
        // and refresh the adapter
        if (type.equals("Ice Cream")) {
            iceCreamFlavorList.add(new FlavorItem(imgID, name, desc));
            Collections.sort(iceCreamFlavorList);
            iceCreamAdapter.notifyDataSetChanged();
        } else {
            gelatoFlavorList.add(new FlavorItem(imgID, name, desc));
            Collections.sort(gelatoFlavorList);
            gelatoAdapter.notifyDataSetChanged();
        }

        // print the flavor lists to the log if testing
        if (TESTING) {
            Log.d("FlavorList", "iceCreamFlavorList = " + iceCreamFlavorList.toString());
            Log.d("FlavorList", "gelatoFlavorList = " + gelatoFlavorList.toString());
        }
    }

    // ---------------------------------------------------------------------------------------------
    // toggle the user's Admin status and make appropriate adjustments depending on
    // whether the user is Admin
    // ---------------------------------------------------------------------------------------------
    private void toggleAdmin() {
        isAdmin = !isAdmin;
        if (isAdmin) {
            btnAddNewFlavor.show();
        } else {
            btnAddNewFlavor.hide();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // reload all the flavors from the "flavors.json" file
    // ---------------------------------------------------------------------------------------------
    private void reloadContent() {
        // read in the "flavors.json" file and get an array of the contained flavor names
        File f = new File(getApplicationContext().getFilesDir(), "flavors.json");
        JSONObject jsonFlavors = JSONFileHandler.readJsonObjectFromFile(f);
        JSONArray jsonNames = jsonFlavors.names();

        if (jsonNames == null) {
            Log.d("JSON", "Error: no names in jsonFlavors object in reloadContent()");
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        try {
            Log.d("JSON", jsonFlavors.toString(2));
            Log.d("JSON", jsonNames.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // remember the old lists in case something fails
        List<FlavorItem> oldIceCreams = new ArrayList<>();
        for (FlavorItem item : iceCreamFlavorList) {
            oldIceCreams.add(item);
        }
        List<FlavorItem> oldGelatos = new ArrayList<>();
        for (FlavorItem item : gelatoFlavorList) {
            oldGelatos.add(item);
        }
        // clear the flavor lists so they can be rebuilt
        iceCreamFlavorList.clear();
        gelatoFlavorList.clear();

        // iteratively add each flavor to the flavor lists
        for (int i = 0; i < jsonNames.length(); i++) {
            try {
                // get the name at this index from the array of names
                String name = jsonNames.getString(i);

                // get the flavor corresponding with that name from the flavors object
                // and then add it to the flavor lists
                JSONObject jsonFlavor = jsonFlavors.getJSONObject(name);
                addFlavor(jsonFlavor);
            }
            // if stuff fails, reload the old lists and exit
            catch (org.json.JSONException e) {
                Log.d("JSON", "Error reloading flavors from json file");
                iceCreamFlavorList.clear();
                gelatoFlavorList.clear();
                for (FlavorItem item : oldIceCreams) {
                    iceCreamFlavorList.add(item);
                }
                for (FlavorItem item : oldGelatos) {
                    gelatoFlavorList.add(item);
                }
                e.printStackTrace();
                break;
            }
        }

        //updateViewType();
        swipeRefreshLayout.setRefreshing(false);
    }

    // ---------------------------------------------------------------------------------------------
    // build sample flavor info based on names from R.array.flavor_names_array and store the info
    // in both flavors.json and the internal flavor lists
    // ---------------------------------------------------------------------------------------------
    public void loadSampleInfo() {
        // load names from R.array.flavor_names_array
        String[] mFlavorNameArray = getResources().getStringArray(R.array.flavor_names_array);

        // setup "flavors.json" as destination file
        File flavorFile = new File(getApplicationContext().getFilesDir(), "flavors.json");
        JSONObject jsonAllFlavors = new JSONObject();

        // for each name in the array, build a sample flavor item and add it to the appropriate
        // list, in addition to adding it to jsonAllFlavors
        for (String name : mFlavorNameArray) {
            String desc = "description of " + name + " goes here";
            String type = "";
            if (name.contains("Sorbet")) {
                type = "Sorbet";
            } else if (name.contains("Gelato")) {
                type = "Gelato";
            } else {
                type = "Ice Cream";
            }

            // create JSONObject for this new Flavor and add it to the jsonAllFlavors object,
            // using the flavor name as the object name
            JSONObject jsonFlavor = new JSONObject();
            try {
                jsonFlavor.put("NAME", name);
                jsonFlavor.put("TYPE", type);
                jsonFlavor.put("DESC", desc);
                jsonFlavor.put("CASE", "");
                jsonFlavor.put("SLOT", "");
                Log.d("JSON", jsonFlavor.toString(2));

                jsonAllFlavors.put(name, jsonFlavor);
            } catch (JSONException e) {
                Log.d("JSON", "Error putting to JSON object.");
                e.printStackTrace();
            }

            // add this flavor to the flavorlists as well
            addFlavor(jsonFlavor);
        }

        // write the updated jsonAllFlavors to "flavors.json"
        JSONFileHandler.writeJsonObjectToFile(jsonAllFlavors, flavorFile);
        try {
            Log.d("JSON", jsonAllFlavors.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
