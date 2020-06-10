package com.peterson.icecreammenu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


// =================================================================================================
// activity to manage adding new flavors or editing existing flavors
// =================================================================================================
public class AddEditFlavorActivity extends AppCompatActivity {
    EditText txtFlavorName;
    EditText txtFlavorDesc;
    EditText txtFlavorCaseNo;
    Spinner flavorType;
    int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_flavor);

        //determine whether the activity is meant to add or edit a flavor
        // and set the title appropriately
        mode = getIntent().getIntExtra("MODE", MainActivity.ADD_MODE);
        Toolbar toolbar = findViewById(R.id.toolbar2);
        if (mode == MainActivity.ADD_MODE) {
            toolbar.setTitle(R.string.add_flavor_header);
        } else {
            toolbar.setTitle(R.string.edit_flavor_header);
        }

        //set up other view elements
        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                save();
            }
        });

        ImageButton buttonCancel = findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cancel();
            }
        });

        txtFlavorName = findViewById(R.id.txtFlavorName);
        txtFlavorDesc = findViewById(R.id.txtFlavorDesc);
        txtFlavorCaseNo = findViewById(R.id.txtFlavorCaseNo);

        //set the content of spinnerFlavorType from R.array.flavor_types_array
        flavorType = findViewById(R.id.spinnerFlavorType);
        ArrayAdapter<CharSequence> mAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.flavor_types_array,
                android.R.layout.simple_spinner_item
        );
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        flavorType.setAdapter(mAdapter);
    }

    // ---------------------------------------------------------------------------------------------
    // returns to MainActivity after saving any data entered/modified
    // ---------------------------------------------------------------------------------------------
    private void save() {
        //if no name has been specified, act as if pressing "cancel"
        if (txtFlavorName.getText().toString().equals("")) {
            cancel();
        }

        // require a flavor type to be specified
        else if (flavorType.getSelectedItemPosition() == 0) {
            Toast.makeText(
                    getApplicationContext(),
                    R.string.flavor_type_error_msg,
                    Toast.LENGTH_SHORT
            ).show();
        }

        // convert the provided data into a JSONObject and save to file
        else {
            // read "flavors.json" into a JSONObject
            // -- if file does not exist, create a new object from scratch
            JSONObject jsonAllFlavors = null;
            try {
                jsonAllFlavors = readJsonObject();
            } catch (FileNotFoundException e) {
                jsonAllFlavors = new JSONObject();
            } catch (IOException e) {
                Log.d("File IO", "Error reading JSON object from file.");
                e.printStackTrace();
            }

            // create JSONObject for this new Flavor and add it to the jsonAllFlavors object,
            // using the flavor name as the object name
            //TODO -- Modifying in Edit mode
            JSONObject jsonFlavor = new JSONObject();
            try {
                jsonFlavor.put("Name", txtFlavorName.getText().toString());
                jsonFlavor.put("Desc", txtFlavorDesc.getText().toString());
                jsonFlavor.put("Type", flavorType.getSelectedItem().toString());
                jsonFlavor.put("Case", txtFlavorCaseNo.getText().toString());
                jsonAllFlavors.put(txtFlavorName.getText().toString(), jsonFlavor);
            } catch (JSONException e) {
                Log.d("File IO", "Error putting to JSON object.");
                e.printStackTrace();
            }

            // write the updated jsonAllFlavors to "flavors.json"
            try {
                writeJsonObject(jsonAllFlavors);
            } catch (IOException e) {
                Log.d("File IO", "Error writing json file.");
                e.printStackTrace();
            }

            // return to MainActivity
            Intent intent = new Intent();
            setResult(mode, intent);
            finishAfterTransition();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // returns to MainActivity without saving any data entered/modified
    // ---------------------------------------------------------------------------------------------
    private void cancel() {
        setResult(0);
        finishAfterTransition();
    }

    // ---------------------------------------------------------------------------------------------
    // attempts to return a JSONObject read from the "flavors.json" file
    // ---------------------------------------------------------------------------------------------
    private JSONObject readJsonObject() throws IOException {
        File f = new File(getApplicationContext().getFilesDir(), "flavors.json");

        // read in file and build JSON format String
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            sb.append(line).append("\n");
            line = br.readLine();
        }
        br.close();
        fr.close();
        String jsonStr = sb.toString();

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
    // attempts to write the given JSONObject to the "flavors.json" file
    // ---------------------------------------------------------------------------------------------
    private void writeJsonObject(JSONObject obj) throws IOException {
        // convert to a JSON format String
        String objStr = obj.toString();

        // write to the file
        File f = new File(getApplicationContext().getFilesDir(), "flavors.json");
        FileWriter fw = new FileWriter(f);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(objStr);
        bw.close();
        fw.close();
    }
}
