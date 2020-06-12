package com.peterson.icecreammenu;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JSONFileHandler {

    public JSONFileHandler() {

    }

    // ---------------------------------------------------------------------------------------------
    // attempts to return a JSONObject read from the given file
    // returns a new, blank JSONObject if no such file is found
    // ---------------------------------------------------------------------------------------------
    public static JSONObject readJsonObjectFromFile(File file) {
        String jsonStr = "";

        try {
            // read in file and build JSON format String
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line).append("\n");
                line = br.readLine();
            }
            br.close();
            fr.close();
            jsonStr = sb.toString();
        }
        // if no such file is found, return a new JSONObject
        catch (FileNotFoundException e) {
            return new JSONObject();
        }
        catch (IOException e) {
            Log.d("JSON", "Error reading JSON object from file.");
            e.printStackTrace();
        }

        // convert JSON format String to JSONObject
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonStr);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    // ---------------------------------------------------------------------------------------------
    // attempts to write the given JSONObject to the given file
    // ---------------------------------------------------------------------------------------------
    public static void writeJsonObjectToFile(JSONObject obj, File file) {
        // convert to a JSON format String
        String objStr = obj.toString();

        // write to the file
        try {
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(objStr);
            bw.close();
            fw.close();
        }
        catch (IOException e){
            Log.d("JSON", "Error writing JSON object to file.");
            e.printStackTrace();
        }
    }
}
