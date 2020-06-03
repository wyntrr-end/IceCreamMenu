package com.peterson.icecreammenu;

import android.graphics.Bitmap;

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
