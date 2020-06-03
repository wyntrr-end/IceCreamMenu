package com.peterson.icecreammenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private FloatingActionButton buttonAddNewFlavor;

    private List<FlavorItem> flavorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flavorList = new ArrayList<>();

        Bitmap bitmap = null;
//        try {
//            bitmap = BitmapFactory.decodeStream(this.openFileInput("1.jpg"));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        flavorList.add(new FlavorItem(bitmap, "Vanilla", "plain stuff"));
//        try {
//            bitmap = BitmapFactory.decodeStream(this.openFileInput("2.jpg"));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        flavorList.add(new FlavorItem(bitmap, "Chocolate", "slightly less plain stuff"));
        Collections.sort(flavorList);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MyAdapter(flavorList);
        recyclerView.setAdapter(mAdapter);

        buttonAddNewFlavor = findViewById(R.id.buttonAddNewFlavor);
        buttonAddNewFlavor.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addFlavor(v);
            }
        });
    }

    public void addFlavor(View v) {
        Intent intent = new Intent(this, AddFlavorActivity.class);
        startActivity(intent);
    }
}
