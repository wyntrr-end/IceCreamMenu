package com.peterson.icecreammenu;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// =================================================================================================
// custom implementation of ArrayList to hold FlavorItems with type constraints
// =================================================================================================
public class FlavorList extends ArrayList {
    List<FlavorItem> flavors;
    public static final int ANY_TYPE = 0;
    public static final int OTHER_TYPE = -1;
    int type = ANY_TYPE;

    // ---------------------------------------------------------------------------------------------
    // generic constructor for making a FlavorList that can contain any kind of flavor
    // ---------------------------------------------------------------------------------------------
    public FlavorList() {
        flavors = new ArrayList<>();
        type = ANY_TYPE;
    }

    // ---------------------------------------------------------------------------------------------
    // constructor for making a FlavorList that can contain only the specified kind of flavor
    // ---------------------------------------------------------------------------------------------
    public FlavorList(int flavorType) {
        flavors = new ArrayList<>();
        type = flavorType;
    }

    // ---------------------------------------------------------------------------------------------
    // if the given flavor matches the FlavorList's type, add it and re-sort the list
    // ---------------------------------------------------------------------------------------------
    public void addFlavor(FlavorItem flavor) {
        if (!isTypeMatch(flavor)) {
            Log.e("FlavorList", "Error: type mismatch. Cannot add item.");
            return;
        }
        flavors.add(flavor);
        Collections.sort(flavors);
    }

    // ---------------------------------------------------------------------------------------------
    // replace this FlavorList with all compatible flavors from the given JSON flavor file
    // ---------------------------------------------------------------------------------------------
    public void reloadFlavorsFromJSON(File flavorFile) {
        Log.i("FlavorList", "Reloading flavor list type=" + type + " ...");

        // read in the "flavors.json" file and get an array of the contained flavor names
        JSONObject jsonFlavors = JSONFileHandler.readJsonObjectFromFile(flavorFile);
        JSONArray jsonNames = jsonFlavors.names();

        // make sure there are names in the jsonFlavors object
        if (jsonNames == null) {
            Log.e("FlavorList", "Error: no names in jsonFlavors object");
            return;
        }
        if (MainActivity.TESTING && MainActivity.VERBOSE) {
            try {
                Log.d("JSON", jsonFlavors.toString(2));
                Log.d("JSON", jsonNames.toString(2));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // save the old list before clearing it, just in case something fails
        List<FlavorItem> oldFlavors = new ArrayList<>(flavors);
        flavors.clear();

        // iteratively add each flavor to the flavor lists
        for (int i = 0; i < jsonNames.length(); i++) {
            try {
                // get the name at this index from the array of names
                String name = jsonNames.getString(i);

                // get the flavor corresponding with that name from the flavors object
                FlavorItem flavor = new FlavorItem();
                if (flavor.readFromJSONFile(flavorFile, name)) {
                    // add the flavor to the list if it is the right type
                    if (isTypeMatch(flavor)) {
                        addFlavor(flavor);
                    }
                } else {
                    Log.e(
                            "FlavorList",
                            "Cannot read flavor \'" + name + "\' from file"
                    );
                }
            }
            // if stuff fails, reload the old list and exit
            catch (org.json.JSONException e) {
                Log.e("FlavorList", "Error reloading flavors from json file");
                flavors = new ArrayList<>(oldFlavors);
                e.printStackTrace();
                break;
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // returns whether the given flavor is of a compatible type with this FlavorList
    // ---------------------------------------------------------------------------------------------
    private boolean isTypeMatch(FlavorItem flavor) {
        int flavorType = flavor.getType();
        return (type == ANY_TYPE
                || flavorType == type
                || (type == OTHER_TYPE && flavorType != FlavorItem.ICE_CREAM));
    }

    // ---------------------------------------------------------------------------------------------
    // returns a clone of this FlavorList
    // ---------------------------------------------------------------------------------------------
    public FlavorList clone() {
        final FlavorList newList = new FlavorList(type);
        for (int i = 0; i < flavors.size(); i++) {
            newList.addFlavor(flavors.get(i));
            Log.d("FlavorList", "Adding Flavor at i=" + i);
        }
        return newList;
    }

    public void clear() {
        flavors.clear();
    }

    public FlavorItem get(int index) {
        return flavors.get(index);
    }

    public int getType() {
        return type;
    }

    @Override
    public int size() {
        return flavors.size();
    }
}
