package com.peterson.icecreammenu;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

// =================================================================================================
// define the contents and properties of a FlavorItem
// =================================================================================================
public class FlavorItem implements Comparable<FlavorItem> {
    public static final int SUCCESSFUL = 1;
    public static final int UNSUCCESSFUL = -1;
    public static final int DUPLICATE = 0;

    public static final int ICE_CREAM = 1;
    public static final int GELATO = 2;
    public static final int SORBET = 3;

    private String imgName;
    private String name;
    private int type;
    private String description;
    private boolean available = false;

    private String oldName = "";

    public FlavorItem() {
        imgName = "";
        name = "";
        type = 0;
        description = "";
    }

    public FlavorItem(String newImgName, String newName, int newType, String newDescription) {
        imgName = newImgName;
        name = newName;
        type = newType;
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

    public String getImgName() {
        return imgName;
    }
    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public String getTypeString() {
        switch (type) {
            case GELATO: return "Gelato";
            case SORBET: return "Sorbet";
            default: return "Ice Cream";
        }
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAvailable() {
        return available;
    }
    public void setAvailability(boolean newAvail) {
        available = newAvail;
    }

    // ---------------------------------------------------------------------------------------------
    // make a deep copy of the this FlavorItem
    // ---------------------------------------------------------------------------------------------
    public FlavorItem clone() {
        FlavorItem copy = new FlavorItem(imgName, name, type, description);
        copy.oldName = name;
        copy.setAvailability(this.available);
        return copy;
    }
    // ---------------------------------------------------------------------------------------------
    // read a flavor corresponding with the given name from the given File as a JSONObject,
    // returning true when successful and false when unsuccessful
    // ---------------------------------------------------------------------------------------------
    public boolean readFromJSONFile(File flavorFile, String flavorName) {
        // read flavorFile into a JSONObject
        JSONObject jsonAllFlavors = JSONFileHandler.readJsonObjectFromFile(flavorFile);

        // if this flavor name does not exist, show error message and return false
        if (!jsonAllFlavors.has(flavorName)) {
            Log.e("FlavorItem", "JSON file does not contain a flavor called" + flavorName);
            return false;
        }

        // get the associated JSONObject from jsonAllFlavors and fill this FlavorItem's info
        // from that object
        JSONObject jsonFlavor;
        try {
            jsonFlavor = jsonAllFlavors.getJSONObject(flavorName);
            if (MainActivity.TESTING && MainActivity.VERBOSE)
                Log.d("FlavorItem",
                        "Read in flavor \"" + flavorName + "\": "
                                + jsonFlavor.toString(2)
                );

            fillFromJSONObject(jsonFlavor);
        } catch (JSONException e) {
            Log.e("FlavorItem", "Error getting from JSON object.");
            e.printStackTrace();
            return false;
        }
        if (MainActivity.TESTING && MainActivity.VERBOSE)
            Log.d("FlavorItem", "readFromJSONFile oldName = " + oldName);
        return true;
    }

    // ---------------------------------------------------------------------------------------------
    // write this FlavorItem to the given File as a JSONObject, returning SUCCESSFUL, UNSUCCESSFUL,
    // or DUPLICATE
    // ---------------------------------------------------------------------------------------------
    public int writeToJSONFile(File flavorFile) {
        // read flavorFile into a JSONObject
        JSONObject jsonAllFlavors = JSONFileHandler.readJsonObjectFromFile(flavorFile);

        // if there is an old flavor (i.e. we are in editing mode), clear that entry
        // so we can replace it, then reset the old name
        Log.d("FlavorItem", "writeToJSONFile oldName = " + oldName);
        if (!oldName.equals("")) {
            jsonAllFlavors.remove(oldName);
            Log.d("FlavorItem", "writeToJSONFile removed item " + oldName);
        }

        // if this flavor name has already been used, return with DUPLICATE status
        if (jsonAllFlavors.has(name)) {
            Log.d("FlavorItem", "writeToJSONFile duplicate item " + name);
            return DUPLICATE;
        }

        // create JSONObject of this new Flavor and add it to the jsonAllFlavors object,
        // using the flavor name as the object name
        JSONObject jsonFlavor = this.toJSONObject();
        try {
            jsonAllFlavors.put(name, jsonFlavor);
        } catch (JSONException e) {
            Log.d("FlavorItem", "Error putting to JSON object.");
            e.printStackTrace();
            return UNSUCCESSFUL;
        }

        // write the updated jsonAllFlavors to "flavors.json"
        JSONFileHandler.writeJsonObjectToFile(jsonAllFlavors, flavorFile);
        return SUCCESSFUL;
    }

    // ---------------------------------------------------------------------------------------------
    // delete the flavor corresponding to the given name from the given File, returning true if
    // successful and false if no such flavor exists
    // ---------------------------------------------------------------------------------------------
    public static boolean deleteFromJSONFile(File flavorFile, String name) {
        // read flavorFile into a JSONObject
        JSONObject jsonAllFlavors = JSONFileHandler.readJsonObjectFromFile(flavorFile);

        Log.d("FlavorItem", "deleteFromJSONFile name = " + name);
        // if jsonAllFlavors does not have the specified flavor, return false
        if (!jsonAllFlavors.has(name)) {
            Log.d("FlavorItem", "deleteFromJSONFile could not delete item \'" + name + "\' because it does not exist.");
            return false;
        }

        // remove the specified item from jsonAllFlavors and write the updated object
        // to the given file
        jsonAllFlavors.remove(name);
        JSONFileHandler.writeJsonObjectToFile(jsonAllFlavors, flavorFile);
        Log.d("FlavorItem", "deleteFromJSONFile successfully deleted item \'" + name + "\'");

        return true;
    }

    // ---------------------------------------------------------------------------------------------
    // converts the FlavorItem into a JSONObject
    // ---------------------------------------------------------------------------------------------
    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("IMG", imgName);
            jsonObject.put("NAME", name);
            jsonObject.put("TYPE", type);
            jsonObject.put("DESC", description);
            jsonObject.put("AVAIL", available);
        } catch (JSONException e) {
            Log.e("FlavorItem", "Error putting to JSON object.");
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }

    // ---------------------------------------------------------------------------------------------
    // populates this FlavorItem with info from the given JSONObject
    // ---------------------------------------------------------------------------------------------
    public void fillFromJSONObject(JSONObject jsonFlavor) {
        try {
            imgName = jsonFlavor.getString("IMG");
            name = jsonFlavor.getString("NAME");
            oldName = name;
            type = jsonFlavor.getInt("TYPE");
            description = jsonFlavor.getString("DESC");
            available = jsonFlavor.getBoolean("AVAIL");
        } catch (JSONException e) {
            Log.e("FlavorItem", "Error getting from JSON object: " + jsonFlavor.toString());
            e.printStackTrace();
        }
    }
}
