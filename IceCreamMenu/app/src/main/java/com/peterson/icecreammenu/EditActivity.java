package com.peterson.icecreammenu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class EditActivity extends AppCompatActivity {

    // TODO -- probably make this an editing activity, move all editing functionality to this
    //  activity. This activity should:
    //  - Allow adding/editing individual flavors with AddEditFlavorActivity
    //  - Allow marking available flavors (probably using a checkbox beside the name)

    public static int ADD_MODE = 1;
    public static int EDIT_MODE = 2;

    FloatingActionButton btnAddNewFlavor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // if for some reason the user got here without admin privileges, return to MainActivity
        if (!MainActivity.isAdmin) {
            setResult(0);
            finishAfterTransition();
        }

        // set up view elements
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                back();
            }
        });

        // allow access to the edit activity if the user is Admin
        btnAddNewFlavor = findViewById(R.id.btnAddNewFlavor);
        btnAddNewFlavor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAddFlavorActivity(v);
            }
        });
    }

    // ---------------------------------------------------------------------------------------------
    // launches AddEditFlavorActivity in Add mode
    // ---------------------------------------------------------------------------------------------
    private void launchAddFlavorActivity(View v) {
        Intent intent = new Intent(this, AddEditFlavorActivity.class);
        intent.putExtra("MODE", ADD_MODE);
        startActivityForResult(intent, 1);
    }

    // ---------------------------------------------------------------------------------------------
    // launches AddEditFlavorActivity in Edit mode, passing the name of the flavor to edit
    // ---------------------------------------------------------------------------------------------
    public void launchEditFlavorActivity(View v, String flavorName) {
        Intent intent = new Intent(this, AddEditFlavorActivity.class);
        intent.putExtra("MODE", EDIT_MODE);
        intent.putExtra("OLD_NAME", flavorName);
        startActivityForResult(intent, 1);
    }

    // ---------------------------------------------------------------------------------------------
    // deals with the result of AddEditFlavorActivity, adding/modifying the corresponding flavor
    // or doing nothing if the activity was cancelled
    // ---------------------------------------------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // if something has been changed, reload the content
        if (resultCode == ADD_MODE || resultCode == EDIT_MODE) {
            reloadContent();
        }

        // otherwise do nothing
    }

    private void reloadContent() {
        // TODO
    }

    private void back() {
        setResult(0);
        finishAfterTransition();
    }

    // TODO -- use the following block to edit flavors
//    // add an onClickListener to launch an appropriate instance of AddEditFlavorActivity
//    // in Edit mode if the user is Admin
//        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            if (MainActivity.isAdmin) {
//                Log.d(
//                        "Adapter",
//                        "launchEditFlavorActivity for flavor " + holder.nameTextView.getText()
//                );
//                mMainActivity.launchEditFlavorActivity(
//                        mMainActivity.getCurrentFocus(),
//                        holder.nameTextView.getText().toString()
//                );
//            }
//        }
//    });
}
