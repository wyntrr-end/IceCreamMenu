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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
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
    RadioButton rb1;
    RadioButton rb2;
    RadioButton rb3;
    RadioButton rb4;
    RadioButton rb5;
    RadioButton rb6;
    RadioButton rb7;
    RadioButton rb8;
    int slotNo = 0;
    int mode;
    String oldName = "";
    String oldCase = "";
    int oldSlot = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = this;
        setContentView(R.layout.activity_add_edit_flavor);
        Intent intent = getIntent();

        txtFlavorName = findViewById(R.id.txtFlavorName);
        flavorType = findViewById(R.id.spinnerFlavorType);
        txtFlavorDesc = findViewById(R.id.txtFlavorDesc);
        txtFlavorCaseNo = findViewById(R.id.txtFlavorCaseNo);
        final TextView lblSlotDesc = findViewById(R.id.lblSlotDesc);

        rb1 = findViewById(R.id.rb1);
        rb2 = findViewById(R.id.rb2);
        rb3 = findViewById(R.id.rb3);
        rb4 = findViewById(R.id.rb4);
        rb5 = findViewById(R.id.rb5);
        rb6 = findViewById(R.id.rb6);
        rb7 = findViewById(R.id.rb7);
        rb8 = findViewById(R.id.rb8);

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
        //panelSlot.setVisibility(View.INVISIBLE); TODO -- uncomment this when done testing

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
                    uncheckAll();
                } else {
                    panelSlot.setVisibility(View.VISIBLE);
                    lblSlotDesc.setText(getString(R.string.add_flavor_slot_desc) +
                            " " + s.toString() + ":");

                }
            }
        });

        // set up radio buttons to act as a custom RadioGroup
        CompoundButton.OnCheckedChangeListener radioGroupListener =
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    checkButton(compoundButton);
                }
            }
        };
        rb1.setOnCheckedChangeListener(radioGroupListener);
        rb2.setOnCheckedChangeListener(radioGroupListener);
        rb3.setOnCheckedChangeListener(radioGroupListener);
        rb4.setOnCheckedChangeListener(radioGroupListener);
        rb5.setOnCheckedChangeListener(radioGroupListener);
        rb6.setOnCheckedChangeListener(radioGroupListener);
        rb7.setOnCheckedChangeListener(radioGroupListener);
        rb8.setOnCheckedChangeListener(radioGroupListener);

        // determine whether the activity is meant to add or edit a flavor
        // and set the title and text boxes appropriately
        mode = intent.getIntExtra("MODE", MainActivity.ADD_MODE);
        Toolbar toolbar = findViewById(R.id.toolbar2);
        if (mode == MainActivity.ADD_MODE) {
            toolbar.setTitle(R.string.add_flavor_header);
        } else {
            oldName = intent.getStringExtra("OLD_NAME");
            toolbar.setTitle(getString(R.string.edit_flavor_header) + ": " + oldName);
            populateFields(oldName);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // populate the text boxes with info from a given flavor (read from "flavors.json")
    // ---------------------------------------------------------------------------------------------
    private void populateFields(String name) {
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
            oldCase = jsonOldFlavor.getString("CASE");
            txtFlavorCaseNo.setText(oldCase);
            if (!oldCase.equals("")) {
                checkButton(jsonOldFlavor.getInt("SLOT"));
                oldSlot = slotNo;
            }

        } catch (org.json.JSONException e) {
            Log.d("JSON", "Error getting from JSON object.");
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // methods to operate custom radio button group
    // ---------------------------------------------------------------------------------------------
    private void uncheckAll() {
        rb1.setChecked(false);
        rb2.setChecked(false);
        rb3.setChecked(false);
        rb4.setChecked(false);
        rb5.setChecked(false);
        rb6.setChecked(false);
        rb7.setChecked(false);
        rb8.setChecked(false);
        slotNo = 0;
    }
    private void checkButton(CompoundButton compoundButton) {
        uncheckAll();
        compoundButton.setChecked(true);
        switch (compoundButton.getId()) {
            case R.id.rb1 : slotNo = 1; break;
            case R.id.rb2 : slotNo = 2; break;
            case R.id.rb3 : slotNo = 3; break;
            case R.id.rb4 : slotNo = 4; break;
            case R.id.rb5 : slotNo = 5; break;
            case R.id.rb6 : slotNo = 6; break;
            case R.id.rb7 : slotNo = 7; break;
            case R.id.rb8 : slotNo = 8; break;
            default: slotNo = 0;
        }
        Log.d("Check", "slotNo = " + slotNo);
    }
    private void checkButton(int buttonNo) {
        uncheckAll();
        slotNo = buttonNo;
        switch (buttonNo) {
            case 1 : rb1.setChecked(true); break;
            case 2 : rb2.setChecked(true); break;
            case 3 : rb3.setChecked(true); break;
            case 4 : rb4.setChecked(true); break;
            case 5 : rb5.setChecked(true); break;
            case 6 : rb6.setChecked(true); break;
            case 7 : rb7.setChecked(true); break;
            case 8 : rb8.setChecked(true); break;
            default: break;
        }
        Log.d("Check", "slotNo = " + slotNo);
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
//        File caseFile = new File(getApplicationContext().getFilesDir(), "cases.json");
//        JSONObject jsonCases = JSONFileHandler.readJsonObjectFromFile(caseFile);

        // if we're in edit mode, remove the old flavor and clear the old slot
        if (mode == MainActivity.EDIT_MODE) {
            jsonAllFlavors.remove(oldName);
//            if (!oldCase.equals("")) {
//                try {
//                    jsonCases.getJSONObject(txtFlavorCaseNo.toString()).put(
//                            Integer.toString(slotNo),
//                            ""
//                    );
//                } catch (JSONException e) {
//                    Log.d("JSON", "Error putting to JSON object.");
//                    e.printStackTrace();
//                }
//            }
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
            jsonFlavor.put("SLOT", slotNo);
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
