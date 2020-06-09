package com.peterson.icecreammenu;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

public class FlavorItem implements Comparable<FlavorItem> {
    private int imageRefID;
    private String name;
    private String description = "";

    public FlavorItem(int newImgID, String newName) {
        imageRefID = newImgID;
        name = newName;
    }
    public FlavorItem(int newImgID, String newName, String newDescription) {
        imageRefID = newImgID;
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

    public int getImageRefID() {
        return imageRefID;
    }
    public String getName(){
        return name;
    }
    public String getDescription() {
        return description;
    }
}
