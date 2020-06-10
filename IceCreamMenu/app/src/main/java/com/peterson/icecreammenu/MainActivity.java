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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

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

        // add sample flavor information using names from R.array.flavor_names_array
        String[] mFlavorNameArray = getResources().getStringArray(R.array.flavor_names_array);
        for (String s : mFlavorNameArray) {
            String desc = "description of " + s + " goes here";
            if (s.contains("Sorbet")) {
                addFlavor("Sorbet", s, desc);
            } else if (s.contains("Gelato")) {
                addFlavor("Gelato", s, desc);
            } else {
                addFlavor("Ice Cream", s, desc);
            }
        }

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

        // add a new flavor to the list
        if (resultCode == ADD_MODE) {
            assert data != null;
            //TODO -- refresh list from JSON file instead, perhaps create a separate method
            addFlavor(data.getStringExtra("TYPE"),
                    data.getStringExtra("NAME"),
                    data.getStringExtra("DESC"));
        }

        // edit an existing flavor
        else assert resultCode != EDIT_MODE || data != null;

        // otherwise do nothing
    }

    // ---------------------------------------------------------------------------------------------
    // adds a new flavor to the list of flavors using the given type, name, and description
    // ---------------------------------------------------------------------------------------------
    private void addFlavor(String type, String name, String desc) {
        // if the name field is blank, do nothing
        if (name.equals("")) return;

        // look for an image file based on the name of the flavor
        // (defaults to blank if no such file is found)
        // TODO -- add a proper response if no image found
        String drawableName = name.toLowerCase().replace("& ", "").replace(" ", "_");
        int imgID = getResources().getIdentifier(
                drawableName,
                "drawable",
                getApplicationContext().getPackageName()
        );

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
}
