package com.peterson.icecreammenu;

import androidx.annotation.NonNull;

// =================================================================================================
// define the contents and properties of a FlavorItem
// =================================================================================================
public class FlavorItem implements Comparable<FlavorItem> {
    private int imageRefID;
    private String name;
    private String description;

    public FlavorItem(int newImgID, String newName, String newDescription) {
        imageRefID = newImgID;
        name = newName;
        description = newDescription;
    }

    @Override
    public int compareTo(FlavorItem f) {
        return this.name.toLowerCase().compareTo(f.name.toLowerCase());
    }

    @NonNull
    @Override
    public String toString() {
        return name + ": " + description;
    }

    public int getImageRefID() {
        return imageRefID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
