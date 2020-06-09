package com.peterson.icecreammenu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    public static Boolean TESTING = true;
    public static Boolean isAdmin = true;
    public static Boolean isGridView = false;
    public static int ADD_MODE = 1;
    public static int EDIT_MODE = 2;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter iceCreamAdapter;
    private RecyclerView.Adapter gelatoAdapter;
    private RecyclerView.LayoutManager listLayoutManager;
    private RecyclerView.LayoutManager gridLayoutManager;

    private TabLayout tabLayout;
    private FloatingActionButton buttonAddNewFlavor;
    private ImageButton buttonOptions;
    private Button buttonToggleAdmin;

    private List<FlavorItem> iceCreamFlavorList;
    private List<FlavorItem> gelatoFlavorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iceCreamFlavorList = new ArrayList<>();
        gelatoFlavorList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        listLayoutManager = new LinearLayoutManager(this);
        gridLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(listLayoutManager);
        iceCreamAdapter = new MyAdapter(iceCreamFlavorList);
        gelatoAdapter = new MyAdapter(gelatoFlavorList);
        recyclerView.setAdapter(iceCreamAdapter);

        String[] mFlavorNameArray = getResources().getStringArray(R.array.flavor_names_array);
        for (String s : mFlavorNameArray) {
            String desc = "description of " + s + " goes here";
            if (s.contains("Sorbet")) {
                addFlavor("Sorbet", s, desc);
            }
            else if (s.contains("Gelato")) {
                addFlavor("Gelato", s, desc);
            }
            else {
                addFlavor("Ice Cream", s, desc);
            }
        }

        buttonToggleAdmin = findViewById(R.id.btnToggleAdmin);
        buttonToggleAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleAdmin();
            }
        });
        if (!TESTING) {
            buttonToggleAdmin.setVisibility(View.GONE);
        }

        buttonOptions = findViewById(R.id.btnOptions);
        buttonOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(MainActivity.this, view);
                popup.setOnMenuItemClickListener(MainActivity.this);
                popup.inflate(R.menu.view_options);
//                MenuInflater inflater = popup.getMenuInflater();
//                inflater.inflate(R.menu.view_options, popup.getMenu());
                popup.show();
            }
        });

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

        buttonAddNewFlavor = findViewById(R.id.buttonAddNewFlavor);
        buttonAddNewFlavor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAddEditFlavorActivity(v, ADD_MODE);
            }
        });
        if (isAdmin) {
            buttonAddNewFlavor.show();
        } else {
            buttonAddNewFlavor.hide();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        //TODO
        isGridView = (item.getItemId() == R.id.btnGridView);
        updateViewType();
        return true;
    }

    private void updateViewType() {
        //TODO
        Toast.makeText(getApplicationContext(), "updating view type... isGridView = " + isGridView, Toast.LENGTH_SHORT);
        if (isGridView) {
            recyclerView.setLayoutManager(gridLayoutManager);
        } else {
            recyclerView.setLayoutManager(listLayoutManager);
        }
        recyclerView.refreshDrawableState(); //TODO
    }

    private void launchAddEditFlavorActivity(View v, int mode) {
        Intent intent = new Intent(this, AddEditFlavorActivity.class);
        intent.putExtra("MODE", mode);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ADD_MODE) {
            assert data != null;
            addFlavor(data.getStringExtra("TYPE"),
                      data.getStringExtra("NAME"),
                      data.getStringExtra("DESC"));
        }
    }

    public void addFlavor(String type, String name, String desc) {
        if (!name.equals("")) {
            String drawableName = name.toLowerCase().replace("& ", "").replace(" ", "_");
            int imgID = getResources().getIdentifier(
                    drawableName,
                    "drawable",
                    getApplicationContext().getPackageName()
            );
            if (type.equals("Ice Cream")) {
                iceCreamFlavorList.add(new FlavorItem(imgID, name, desc));
                Collections.sort(iceCreamFlavorList);
                iceCreamAdapter.notifyDataSetChanged();
            } else {
                gelatoFlavorList.add(new FlavorItem(imgID, name, desc));
                Collections.sort(gelatoFlavorList);
                gelatoAdapter.notifyDataSetChanged();
            }
        }
        if (TESTING) {
            Log.d("FlavorList", "iceCreamFlavorList = " + iceCreamFlavorList.toString());
            Log.d("FlavorList", "gelatoFlavorList = " + gelatoFlavorList.toString());
        };
    }

    private void toggleAdmin() {
        isAdmin = !isAdmin;
        if (isAdmin) {
            buttonAddNewFlavor.show();
        } else {
            buttonAddNewFlavor.hide();
        }
    }
}
