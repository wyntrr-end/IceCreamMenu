package com.peterson.icecreammenu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


// =================================================================================================
// activity to manage adding new flavors or editing existing flavors
// =================================================================================================
public class AddEditFlavorActivity extends AppCompatActivity {
    EditText txtFlavorName;
    Spinner flavorType;
    EditText txtFlavorDesc;

    EditText txtFlavorCaseNo;
    int slotNo = 0;
    int mode;
    String oldName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = this;
        setContentView(R.layout.activity_add_edit_flavor);
        Intent intent = getIntent();

        txtFlavorName = findViewById(R.id.txtFlavorName);
        txtFlavorDesc = findViewById(R.id.txtFlavorDesc);
        txtFlavorCaseNo = findViewById(R.id.txtFlavorCaseNo);
        flavorType = findViewById(R.id.spinnerFlavorType);

        // set up view elements
        ImageButton buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hideKeyboard(activity);
                save();
            }
        });

        ImageButton buttonCancel = findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cancel();
            }
        });

        // set the content of spinnerFlavorType from R.array.flavor_types_array
        ArrayAdapter<CharSequence> mAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.flavor_types_array,
                android.R.layout.simple_spinner_item
        );
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        flavorType.setAdapter(mAdapter);

        final LinearLayout panelSlot = findViewById(R.id.panelSlot);
        panelSlot.setVisibility(View.INVISIBLE);

        txtFlavorCaseNo.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // if no case # specified, don't show the slot panel, otherwise show it
                if (s.toString().equals("")) {
                    panelSlot.setVisibility(View.INVISIBLE);
                } else {
                    panelSlot.setVisibility(View.VISIBLE);
                }
            }
        });

        final RadioGroup slotButtons1 = findViewById(R.id.radioSlotRow1);
        final RadioGroup slotButtons2 = findViewById(R.id.radioSlotRow2);
        slotButtons1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                // TODO -- make this work
                if (slotButtons2.getCheckedRadioButtonId() != -1 && slotButtons1.getCheckedRadioButtonId() != -1) {
                    Log.d("Check", "clearing checks in slotbuttons2...");
                    slotButtons2.clearCheck();
                }
                Log.d("Check", "slotButtons1 i = " + i);
                slotNo = i;
            }
        });
        slotButtons2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (slotButtons1.getCheckedRadioButtonId() != -1 && slotButtons2.getCheckedRadioButtonId() != -1) {
                    Log.d("Check", "clearing checks in slotbuttons1...");
                    slotButtons1.clearCheck();
                }
                Log.d("Check", "slotButtons2 i = " + i);
                slotNo = i;
            }
        });

        // determine whether the activity is meant to add or edit a flavor
        // and set the title and text boxes appropriately
        mode = intent.getIntExtra("MODE", MainActivity.ADD_MODE);
        Toolbar toolbar = findViewById(R.id.toolbar2);
        if (mode == MainActivity.ADD_MODE) {
            toolbar.setTitle(R.string.add_flavor_header);
        } else {
            oldName = intent.getStringExtra("OLD_NAME");
            toolbar.setTitle(getString(R.string.edit_flavor_header) + ": " + oldName);
            populateTextBoxes(oldName);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // populate the text boxes with info from a given flavor (read from "flavors.json")
    // ---------------------------------------------------------------------------------------------
    private void populateTextBoxes(String name) {
        // read "flavors.json" into a JSONObject
        File flavorFile = new File(getApplicationContext().getFilesDir(), "flavors.json");
        JSONObject jsonAllFlavors = JSONFileHandler.readJsonObjectFromFile(flavorFile);

        try {
            JSONObject jsonOldFlavor = jsonAllFlavors.getJSONObject(name);

            txtFlavorName.setText(name);
            txtFlavorDesc.setText(jsonOldFlavor.getString("DESC"));
            switch (jsonOldFlavor.getString("TYPE")) {
                case "Gelato" : flavorType.setSelection(2); break;
                case  "Sorbet" : flavorType.setSelection(3); break;
                default: flavorType.setSelection(1);
            }
            txtFlavorCaseNo.setText(jsonOldFlavor.getString("CASE"));

        } catch (org.json.JSONException e) {
            Log.d("JSON", "Error getting from JSON object.");
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // returns to MainActivity after saving any data entered/modified
    // ---------------------------------------------------------------------------------------------
    private void save() {
        String newName = txtFlavorName.getText().toString();

        // if no name has been specified, act as if pressing "cancel"
        if (newName.equals("")) {
            cancel();
            return;
        }

        // require a flavor type to be specified
        if (flavorType.getSelectedItemPosition() == 0) {
            Toast.makeText(
                    getApplicationContext(),
                    R.string.flavor_type_error_msg,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // if required fields are good, convert the provided data into a JSONObject and save to file

        // read "flavors.json" into a JSONObject
        File flavorFile = new File(getApplicationContext().getFilesDir(), "flavors.json");
        JSONObject jsonAllFlavors = JSONFileHandler.readJsonObjectFromFile(flavorFile);
        //JSONArray jsonNames = jsonFlavors.names();

        // if we're in edit mode, remove the old version
        if (mode == MainActivity.EDIT_MODE) {
            jsonAllFlavors.remove(oldName);
        }

        // if this flavor name has already been used, show error message and don't save
        if (jsonAllFlavors.has(newName)) {
            Toast.makeText(
                    getApplicationContext(),
                    R.string.flavor_duplicate_error_msg,
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        // create JSONObject for this new Flavor and add it to the jsonAllFlavors object,
        // using the flavor name as the object name
        JSONObject jsonFlavor = new JSONObject();
        try {
            jsonFlavor.put("NAME", newName);
            jsonFlavor.put("DESC", txtFlavorDesc.getText().toString());
            jsonFlavor.put("TYPE", flavorType.getSelectedItem().toString());
            jsonFlavor.put("CASE", txtFlavorCaseNo.getText().toString());
            Log.d("JSON", jsonFlavor.toString(2));

            jsonAllFlavors.put(txtFlavorName.getText().toString(), jsonFlavor);
            //Log.d("JSON", jsonAllFlavors.toString(2));
        } catch (JSONException e) {
            Log.d("JSON", "Error putting to JSON object.");
            e.printStackTrace();
        }

        // write the updated jsonAllFlavors to "flavors.json"
        JSONFileHandler.writeJsonObjectToFile(jsonAllFlavors, flavorFile);

        // return to MainActivity
        Intent intent = new Intent();
        setResult(mode, intent);
        finishAfterTransition();
    }

    // ---------------------------------------------------------------------------------------------
    // returns to MainActivity without saving any data entered/modified
    // ---------------------------------------------------------------------------------------------
    private void cancel() {
        setResult(0);
        finishAfterTransition();
    }

    // ---------------------------------------------------------------------------------------------
    // hides the onscreen keyboard (clipped from stackoverflow)
    // ---------------------------------------------------------------------------------------------
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        // Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        // If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
