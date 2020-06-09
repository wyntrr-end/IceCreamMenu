package com.peterson.icecreammenu;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

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

public class AddFlavorActivity extends AppCompatActivity {
    EditText txtFlavorName;
    EditText txtFlavorDesc;
    Spinner flavorType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_flavor);

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

        flavorType = findViewById(R.id.spinnerFlavorType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.flavor_types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        flavorType.setAdapter(adapter);
    }

    private void save() {
        if (txtFlavorName.getText().toString().equals("")) {
            cancel();
        } else if (flavorType.getSelectedItemPosition() == 0) {
            Toast.makeText(
                    getApplicationContext(),
                    R.string.flavor_type_error_msg,
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Intent intent = new Intent();
            intent.putExtra("TYPE", flavorType.getSelectedItem().toString());
            intent.putExtra("NAME", txtFlavorName.getText().toString());
            intent.putExtra("DESC", txtFlavorDesc.getText().toString());
            setResult(1, intent);
            finishAfterTransition();
        }
    }

    private void cancel() {
        setResult(0);
        finishAfterTransition();
    }
}
