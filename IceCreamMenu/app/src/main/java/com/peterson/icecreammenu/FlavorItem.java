package com.peterson.icecreammenu;

import androidx.annotation.NonNull;

// =================================================================================================
// define the contents and properties of a FlavorItem
// =================================================================================================
public class FlavorItem implements Comparable<FlavorItem> {
    private String imageName;
    private String name;
    private String description;
    private Boolean available = false;

    public FlavorItem(String newImgName, String newName, String newDescription) {
        imageName = newImgName;
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

    public String getImageName() {
        return imageName;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Boolean isAvailable() {
        return available;
    }
    public void setAvailability(Boolean newAvail) {
        available = newAvail;
    }
}
