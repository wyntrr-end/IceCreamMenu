package com.peterson.icecreammenu;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

public class FlavorItem implements Comparable<FlavorItem> {
    private Bitmap image;
    private String name;
    private String description = "";

    public FlavorItem(Bitmap newImg, String newName) {
        image = newImg;
        name = newName;
    }
    public FlavorItem(Bitmap newImg, String newName, String newDescription) {
        image = newImg;
        name = newName;
        description = newDescription;
    }

    @Override
    public int compareTo(FlavorItem f) {
        return this.name.compareTo(f.name);
    }

    @NonNull
    @Override
    public String toString() {
        return name + ": " + description;
    }

    public Bitmap getImage() {
        return image;
    }
    public String getName(){
        return name;
    }
    public String getDescription() {
        return description;
    }
}
