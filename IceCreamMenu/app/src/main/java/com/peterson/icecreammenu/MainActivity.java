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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// =================================================================================================
// MainActivity
// =================================================================================================
public class MainActivity extends AppCompatActivity {
    public static final Boolean TESTING = true;
    public static Boolean hasCamera = false;
    public static Boolean isAdmin = true;
    public static Boolean INIT = true;
    public static final int VIEW_LIST = 0;
    public static final int VIEW_GRID = 1;
    public static final int VIEW_EDIT = 2;
    private int viewMode = VIEW_LIST;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter iceCreamAdapter;
    private RecyclerView.Adapter gelatoAdapter;

    private ImageButton btnBack;
    private Toolbar toolbar;
    private ImageButton btnEdit;
    private Switch switchAdmin;
    private ImageButton btnViewMode;
    private TextView txtAutosave;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton btnAddFlavor;

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

        // load sample flavor information and check for an available camera
        // if the app is being initialised
        // ============================ WARNING ==============================
        // INIT will be true every time the app is launched
        // ===================================================================
        Log.d("INIT", "INIT = " + INIT);
        if (INIT) {
            loadSampleInfo();
            hasCamera = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
        }

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewMode = VIEW_LIST;
                updateViewType();
            }
        });
        btnBack.setVisibility(View.GONE);

        toolbar = findViewById(R.id.toolbarMain);
        if (!isAdmin) {
            toolbar.setTitle(R.string.main_header);
        }

        btnEdit = findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewMode = VIEW_EDIT;
                updateViewType();
            }
        });
        if (!isAdmin) {
            btnEdit.setVisibility(View.GONE);
        }

        // allow toggling admin/user version when testing
        switchAdmin = findViewById(R.id.switchAdmin);
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

        txtAutosave = findViewById(R.id.txtAutosave);

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

        // set up a button to add a new flavor
        btnAddFlavor = findViewById(R.id.btnAddFlavor);
        btnAddFlavor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAddFlavorActivity(v);
            }
        });

        // make sure the recyclerView loads properly
        updateViewType();
        if (!INIT) reloadContent();

        INIT = false;
    }

    // ---------------------------------------------------------------------------------------------
    // deals with switching modes between list, grid, and edit view
    // ---------------------------------------------------------------------------------------------
    private void updateViewType() {
        // make sure the interface button displays the correct icon, and hide it
        // when in admin edit mode
        if (viewMode == VIEW_GRID) {
            btnViewMode.setImageResource(R.drawable.ic_view_module_yellow_24dp);
        } else if (viewMode == VIEW_LIST){
            btnViewMode.setImageResource(R.drawable.ic_view_list_yellow_24dp);
        } else {
            btnViewMode.setVisibility(View.GONE);
        }

        // show/hide certian buttons in admin edit mode
        if (viewMode == VIEW_EDIT) {
            btnBack.setVisibility(View.VISIBLE);
            toolbar.setTitle(R.string.admin_edit_mode_header);
            switchAdmin.setVisibility(View.GONE);
            btnEdit.setVisibility(View.GONE);
            txtAutosave.setVisibility(View.VISIBLE);
            btnAddFlavor.show();
        } else {
            btnBack.setVisibility(View.GONE);
            if (isAdmin) toolbar.setTitle(R.string.main_header_admin);
            else toolbar.setTitle(R.string.main_header);
            switchAdmin.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.VISIBLE);
            btnViewMode.setVisibility(View.VISIBLE);
            txtAutosave.setVisibility(View.GONE);
            btnAddFlavor.hide();
        }

        // get the current adapter and update the view mode
        MyAdapter mAdapter = (MyAdapter) recyclerView.getAdapter();
        mAdapter.setViewMode(viewMode); // TODO -- does this actually change anything?

        // set the layout manager according to the current view mode
        if (viewMode == VIEW_GRID) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        } else {
            // both VIEW_LIST and VIEW_EDIT use a linear layout manager
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        // reset the adapter to refresh the layout
        recyclerView.setAdapter(mAdapter);
    }

    // ---------------------------------------------------------------------------------------------
    // launches AddEditFlavorActivity in Add mode
    // ---------------------------------------------------------------------------------------------
    private void launchAddFlavorActivity(View v) {
        Intent intent = new Intent(this, AddEditFlavorActivity.class);
        intent.putExtra("MODE", AddEditFlavorActivity.ADD_MODE);
        startActivityForResult(intent, 1);
    }

    // ---------------------------------------------------------------------------------------------
    // launches AddEditFlavorActivity in Edit mode, passing the name of the flavor to edit
    // ---------------------------------------------------------------------------------------------
    public void launchEditFlavorActivity(View v, String flavorName) {
        Intent intent = new Intent(this, AddEditFlavorActivity.class);
        intent.putExtra("MODE", AddEditFlavorActivity.EDIT_MODE);
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
        if (resultCode == AddEditFlavorActivity.ADD_MODE || resultCode == AddEditFlavorActivity.EDIT_MODE) {
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
        String img = "";

        // get the fields from the JSONObject
        try {
            name = jsonFlavor.getString("NAME");
            type = jsonFlavor.getString("TYPE");
            desc = jsonFlavor.getString("DESC");
            img = jsonFlavor.getString("IMG");
        } catch (org.json.JSONException e) {
            Log.e("JSON", "Error parsing JSON flavor in addFlavor()");
            e.printStackTrace();
        }

        // if the name or type fields are blank, do nothing
        if (name.equals("") || type.equals("")) return;

        // add the new flavor to the corresponding list of flavors, re-sort the list,
        // and refresh the adapter
        if (type.equals("Ice Cream")) {
            iceCreamFlavorList.add(new FlavorItem(img, name, desc));
            Collections.sort(iceCreamFlavorList);
            iceCreamAdapter.notifyDataSetChanged();
        } else {
            gelatoFlavorList.add(new FlavorItem(img, name, desc));
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
            btnEdit.setVisibility(View.VISIBLE);
            toolbar.setTitle(R.string.main_header_admin);
        } else {
            btnEdit.setVisibility(View.GONE);
            toolbar.setTitle(R.string.main_header);
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

        // make sure there are names in the jsonFlavors object
        if (jsonNames == null) {
            Log.e("JSON", "Error: no names in jsonFlavors object in reloadContent()");
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        if (TESTING) {
            try {
                Log.d("JSON", jsonFlavors.toString(2));
                Log.d("JSON", jsonNames.toString(2));
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
                Log.e("JSON", "Error reloading flavors from json file");
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

        swipeRefreshLayout.setRefreshing(false);
    }

    // ---------------------------------------------------------------------------------------------
    // build sample flavor info based on names from R.array.flavor_names_array and store the info
    // in both flavors.json and the internal flavor lists
    // -- this is only for use when testing
    // ---------------------------------------------------------------------------------------------
    public void loadSampleInfo() {
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
            String type = "";
            if (name.contains("Sorbet")) {
                type = "Sorbet";
            } else if (name.contains("Gelato")) {
                type = "Gelato";
            } else {
                type = "Ice Cream";
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
                    Log.d("Image", "Wrote image to " + imgFile);
                    imgName = imgName + ".jpg";
                }
                // otherwise set imgName blank so a placeholder will be used
                else {
                    imgName = "";
                }
            }
            catch (IOException e){
                Log.e("Image", "Error writing image.");
                imgName = "";
                e.printStackTrace();
            }

            // create JSONObject for this new Flavor and add it to the jsonAllFlavors object,
            // using the flavor name as the object name
            JSONObject jsonFlavor = new JSONObject();
            try {
                jsonFlavor.put("NAME", name);
                jsonFlavor.put("TYPE", type);
                jsonFlavor.put("DESC", desc);
                jsonFlavor.put("IMG", imgName);
                jsonFlavor.put("CASE", "");
                jsonFlavor.put("SLOT", "");
                Log.d("JSON", jsonFlavor.toString(2));

                jsonAllFlavors.put(name, jsonFlavor);
            } catch (JSONException e) {
                Log.e("JSON", "Error putting to JSON object.");
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

        Toast.makeText(
                getApplicationContext(),
                "Loaded sample data",
                Toast.LENGTH_SHORT
        ).show();
    }
}
