package com.peterson.icecreammenu;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class EditFlavorAvailabilityActivity extends AppCompatActivity {

    // TODO -- probably make this an editing activity, move all editing functionality to this
    //  activity. This activity should:
    //  - Allow adding/editing individual flavors with AddEditFlavorActivity
    //  - Allow marking available flavors (probably using a checkbox beside the name)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_flavor_list);
    }
}
