package com.peterson.icecreammenu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static Boolean TESTING = true;
    public static Boolean ADMIN = true;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter iceCreamAdapter;
    private RecyclerView.Adapter gelatoAdapter;
    private RecyclerView.LayoutManager layoutManager;
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

//        //Bitmap bitmap = null;
////        try {
////            bitmap = BitmapFactory.decodeStream(this.openFileInput("1.jpg"));
////        } catch (FileNotFoundException e) {
////            e.printStackTrace();
////        }
////        try {
////            bitmap = BitmapFactory.decodeStream(this.openFileInput("2.jpg"));
////        } catch (FileNotFoundException e) {
////            e.printStackTrace();
////        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        iceCreamAdapter = new MyAdapter(iceCreamFlavorList);
        gelatoAdapter = new MyAdapter(gelatoFlavorList);
        recyclerView.setAdapter(iceCreamAdapter);

        addFlavor("Ice Cream", "Vanilla", "plain stuff");
        addFlavor("Ice Cream", "Vanilla Cascade", "exciting stuff");
        addFlavor("Ice Cream", "Chocolate", "slightly less plain stuff");
        addFlavor("Gelato", "Vanilla Bean", "fancy stuff");
        addFlavor("Gelato", "Chocolate Mousse", "extra fancy stuff");
        addFlavor("Gelato", "Raspberry Truffle", "wow this is neat");

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
                launchAddFlavorActivity(v);
            }
        });
        if (ADMIN) {
            buttonAddNewFlavor.show();
        } else {
            buttonAddNewFlavor.hide();
        }
    }

    public void showOptions(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.view_options, popup.getMenu());
        popup.show();
    }


    private void launchAddFlavorActivity(View v) {
        Intent intent = new Intent(this, AddFlavorActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            addFlavor(data.getStringExtra("TYPE"),
                      data.getStringExtra("NAME"),
                      data.getStringExtra("DESC"));
        }
    }

    public void addFlavor(String type, String name, String desc) {
        if (name != "") {
            Bitmap bitmap = Bitmap.createBitmap(185, 185, Bitmap.Config.ARGB_4444);
            if (type == "Ice Cream") {
                iceCreamFlavorList.add(new FlavorItem(bitmap, name, desc));
                Collections.sort(iceCreamFlavorList);
                iceCreamAdapter.notifyDataSetChanged();
            } else {
                gelatoFlavorList.add(new FlavorItem(bitmap, name, desc));
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
        ADMIN = !ADMIN;
        if (ADMIN) {
            buttonAddNewFlavor.show();
        } else {
            buttonAddNewFlavor.hide();
        }
    }
}
