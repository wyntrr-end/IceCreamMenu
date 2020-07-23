package com.peterson.icecreammenu;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.View;

import java.io.File;

// =================================================================================================
// Activity which allows admin users to modify the lists of flavors.
//
// Admins can:
//      - Switch between Ice Cream and Gelato/Sorbet lists
//      - Add new flavors
//      - Delete flavors
//      - Edit flavors
//      - Toggle the availability of flavors
//      - Return to MainActivity
//
// ** NOTE **
//      MUST set dataStatus to MODIFIED if any changes have been made before finishing
// =================================================================================================
public class AdminEditActivity extends AppCompatActivity {
    public static int MODIFIED = 1;
    private int dataStatus = 0;

    private RecyclerView recyclerView;
    private MyAdapter iceCreamAdapter;
    private MyAdapter otherFlavorAdapter;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    private FlavorList iceCreamFlavorList;
    private FlavorList otherFlavorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // instantiate flavor lists
        iceCreamFlavorList = new FlavorList(FlavorItem.ICE_CREAM);
        otherFlavorList = new FlavorList(FlavorList.OTHER_TYPE);

        // setup recyclerView with two adapters, default to showing ice cream
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        iceCreamAdapter = new MyAdapter(this, iceCreamFlavorList);
        otherFlavorAdapter = new MyAdapter(this, otherFlavorList);
        recyclerView.setAdapter(iceCreamAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToMainActivity();
            }
        });

        // changing tabs switches the recyclerView adapter in order to
        // display appropriate information
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d("AdminEditActivity", "Changing tab...");
                tabLayout.setSelected(true);
                if (tab.getPosition() == 1) {
                    recyclerView.setAdapter(otherFlavorAdapter);
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
                        Log.i("AdminEditActivity", "onRefresh called from SwipeRefreshLayout");
                        reloadContent();
                    }
                }
        );

        FloatingActionButton btnAddFlavor = findViewById(R.id.btnAddFlavor);
        btnAddFlavor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchAddFlavorActivity();
            }
        });

        reloadContent();
    }

    // ---------------------------------------------------------------------------------------------
    // launches AddEditFlavorActivity in Add mode
    // ---------------------------------------------------------------------------------------------
    private void launchAddFlavorActivity() {
        Intent intent = new Intent(this, AddEditFlavorActivity.class);
        intent.putExtra("MODE", AddEditFlavorActivity.ADD_MODE);
        startActivityForResult(intent, 1);
    }

    // ---------------------------------------------------------------------------------------------
    // launches AddEditFlavorActivity in Edit mode, passing the name of the flavor to edit
    // ---------------------------------------------------------------------------------------------
    public void launchEditFlavorActivity(String flavorName) {
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

        // if the activity was not cancelled, reload the content
        if (resultCode != AddEditFlavorActivity.CANCELLED) {
            reloadContent();
            dataStatus = MODIFIED;
        }

        // otherwise do nothing
    }

    @Override
    public void onBackPressed() {
        returnToMainActivity();
        super.onBackPressed();
    }

    private void returnToMainActivity() {
        if (MainActivity.TESTING)
            Log.d("AdminEditActivity",
                    "Returning to MainActivity with dataStatus=" + dataStatus + "..."
            );
        Intent intent = new Intent();
        setResult(dataStatus, intent);
        finishAfterTransition();
    }

    // ---------------------------------------------------------------------------------------------
    // reload all the flavors from the "flavors.json" file
    // ---------------------------------------------------------------------------------------------
    private void reloadContent() {
        Log.i("AdminEditActivity", "Reloading flavor lists...");

        // read in the "flavors.json" file and get an array of the contained flavor names
        File flavorFile = new File(getApplicationContext().getFilesDir(), getString(R.string.flavor_filename));

        iceCreamFlavorList.reloadFlavorsFromJSON(flavorFile);
        iceCreamAdapter.notifyDataSetChanged();
        otherFlavorList.reloadFlavorsFromJSON(flavorFile);
        otherFlavorAdapter.notifyDataSetChanged();

        swipeRefreshLayout.setRefreshing(false);
    }
}
