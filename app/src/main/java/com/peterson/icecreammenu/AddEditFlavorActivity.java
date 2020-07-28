package com.peterson.icecreammenu;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

// =================================================================================================
// activity to manage adding new flavors or editing existing flavors
// =================================================================================================
public class AddEditFlavorActivity extends AppCompatActivity {
    public static final int ADD_MODE = 1;
    public static final int EDIT_MODE = 2;
    public static final int DELETED = -1;
    public static final int CANCELLED = 0;
    public static final int PICK_IMAGE = 1;

    ImageButton btnAddImage;
    EditText txtFlavorName;
    Spinner flavorType;
    EditText txtFlavorDesc;

    int mode;
    String tmpImg = "tmp.jpg";
    boolean imgChanged = false;
    FlavorItem flavor;
    FlavorItem oldFlavor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = this;
        setContentView(R.layout.activity_add_edit_flavor);
        Intent intent = getIntent();

        // determine whether the activity is meant to add or edit a flavor
        mode = intent.getIntExtra("MODE", ADD_MODE);

        btnAddImage = findViewById(R.id.btnAddImage);
        txtFlavorName = findViewById(R.id.txtFlavorName);
        flavorType = findViewById(R.id.spinnerFlavorType);
        txtFlavorDesc = findViewById(R.id.txtFlavorDesc);

        // set up view elements
        ImageButton btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hideKeyboard(activity);
                save();
            }
        });

        ImageButton btnCancel = findViewById(R.id.btnBack2);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cancel();
            }
        });

        Button btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteDialog();
            }
        });
        if (mode == ADD_MODE) {
            btnDelete.setVisibility(View.GONE);
        }

        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Image", "btnAddImage has been clicked!");
                pickImage();
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

        // set the title and text boxes appropriately
        Toolbar toolbar = findViewById(R.id.toolbarAddEditFlavor);
        if (mode == ADD_MODE) {
            toolbar.setTitle(R.string.add_flavor_header);
            oldFlavor = new FlavorItem();
        } else {
            String oldName = intent.getStringExtra("OLD_NAME");
            toolbar.setTitle(getString(R.string.edit_flavor_header) + ": " + oldName);
            populateFields(oldName);
        }

        flavor = oldFlavor.clone();
    }

    // ---------------------------------------------------------------------------------------------
    // populate the text boxes with info from a given flavor (read from "flavors.json")
    // ---------------------------------------------------------------------------------------------
    private void populateFields(String name) {

        // read "flavors.json" into a JSONObject
        File flavorFile = new File(getApplicationContext().getFilesDir(), "flavors.json");
        JSONObject jsonAllFlavors = JSONFileHandler.readJsonObjectFromFile(flavorFile);

        oldFlavor = new FlavorItem();
        if (!oldFlavor.readFromJSONFile(flavorFile, name)) {
            Log.e("JSON", "Unable to read flavor.");
            return;
        }

        // if an image name was provided previously, load and display that image, otherwise
        // the placeholder icon will be shown by default
        String oldName = oldFlavor.getImgName();
        if (!oldName.equals("")) {
            File flavorImgFile = new File(getApplicationContext().getFilesDir(), oldName);
            if (flavorImgFile.exists()) {
                btnAddImage.setImageURI(Uri.fromFile(flavorImgFile));
            }
        }

        // set the name, description, and flavor type fields
        txtFlavorName.setText(name);
        flavorType.setSelection(oldFlavor.getType());
        txtFlavorDesc.setText(oldFlavor.getDescription());

        return;
    }

    // ---------------------------------------------------------------------------------------------
    // provide the user with a selection of locations from which to obtain a new flavor image
    // ---------------------------------------------------------------------------------------------
    private void pickImage() {
        // intent which allows choosing image content from the file explorer
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        // intent which allows choosing image content from "photos"
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        // intent which wraps the others into a single chooser
        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");

        // if the device has a camera, also include the ability to take a photo
        if (MainActivity.hasCamera) {
            // intent which allows using the camera to capture a new photo
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent, cameraIntent});
        }
        else {
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
        }

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    // ---------------------------------------------------------------------------------------------
    // save any image the user might choose to a new file
    // ---------------------------------------------------------------------------------------------
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if an image was picked, save it
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            Log.d("AddEditActivity", "Image has been picked.");
            if (data == null) {
                Log.e("AddEditActivity", "Image picker returned null.");
                return;
            }

            // new image file name is generated based on the current time
            tmpImg = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg";
            File tmpFlavorImgFile = new File(getApplicationContext().getFilesDir(), tmpImg);
            try {
                Bitmap bitmap;
                // if we can call getData() then this is probably an existing image, so
                // convert it to a bitmap
                if (data.getData() != null) {
                    Log.d("AddEditActivity", "data.getDataString() = " + data.getDataString());
                    InputStream in = getApplicationContext().getContentResolver().openInputStream(data.getData());
                    bitmap = BitmapFactory.decodeStream(in);
                    assert in != null : "InputStream is null";
                    in.close();
                }
                // if we can't call getData(), then this is probably a new photo, so convert
                // it to a bitmap (different method than for an existing image)
                else {
                    bitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                    // TODO -- this currently saves a low-quality image, needs more work to save
                    //  full-res version
                }

                // save the generated bitmap to the new file as a jpg
                OutputStream out = new FileOutputStream(tmpFlavorImgFile);
                assert bitmap != null : "Bitmap is null";
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.close();
                Log.d("AddEditActivity", "Wrote image to " + tmpFlavorImgFile);

                // make sure the ImageButton is updated with the new image
                btnAddImage.setImageDrawable(getDrawable(R.drawable.ic_icecream_vector_purple));
                btnAddImage.setImageURI(Uri.fromFile(tmpFlavorImgFile));

                imgChanged = true;
            } catch (IOException e) {
                Log.e("AddEditActivity", "Image file error.");
                e.printStackTrace();
            }
        }
        // if no image was picked, do nothing
    }

    // ---------------------------------------------------------------------------------------------
    // returns to MainActivity after saving any data entered/modified
    // ---------------------------------------------------------------------------------------------
    private void save() {
        if (imgChanged) {
            flavor.setImgName(tmpImg);
        }
        flavor.setName(txtFlavorName.getText().toString());
        flavor.setType(flavorType.getSelectedItemPosition());
        flavor.setDescription(txtFlavorDesc.getText().toString());

        // if no name has been specified, act as if pressing "cancel"
        if (flavor.getName().equals("")) {
            cancel();
            return;
        }

        // require a flavor type to be specified
        if (flavor.getType() == 0) {
            Snackbar.make(
                    Objects.requireNonNull(getCurrentFocus()),
                    R.string.flavor_type_error_msg,
                    Snackbar.LENGTH_LONG
            ).show();
            return;
        }

        // if required fields are good save the new/edited flavor to file
        File flavorFile = new File(getApplicationContext().getFilesDir(), "flavors.json");
        switch (flavor.writeToJSONFile(flavorFile)) {
            case FlavorItem.DUPLICATE: {
                // if this flavor name has already been used, show an error message and don't save
                Snackbar.make(
                        Objects.requireNonNull(getCurrentFocus()),
                        R.string.flavor_duplicate_error_msg,
                        Snackbar.LENGTH_LONG
                ).show();
                return;
            }
            case FlavorItem.SUCCESSFUL: break;
            default: return; // some other error has occurred
        };

        // if in edit mode and the image has been changed, remove the old image file
        if (mode == EDIT_MODE && imgChanged) {
            boolean deleted = new File(getApplicationContext().getFilesDir(), oldFlavor.getImgName()).delete();
            Log.d("AddEditActivity", "Old image deleted = " + deleted);
        }

        // return to MainActivity
        Intent intent = new Intent();
        setResult(mode, intent);
        finishAfterTransition();
    }

    // ---------------------------------------------------------------------------------------------
    // warn the user about deleting the current flavor
    // ---------------------------------------------------------------------------------------------
    private void showDeleteDialog() {
        new AlertDialog.Builder(AddEditFlavorActivity.this, R.style.Theme_MaterialComponents_Light_Dialog)
                .setTitle(R.string.delete_alert_title)
                .setMessage(R.string.delete_alert_message)
                .setNegativeButton(R.string.delete_alert_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d("AddEditActivity", "AlertDialog: Negative button clicked.");
                    }
                })
                .setPositiveButton(R.string.delete_alert_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d("AddEditActivity", "AlertDialog: Positive button clicked.");
                        delete();
                    }
                })
                .create()
                .show();
    }

    // ---------------------------------------------------------------------------------------------
    // deletes the current flavor and returns to MainActivity
    // ---------------------------------------------------------------------------------------------
    private void delete() {
        // make sure to delete any image files
        boolean deleted = new File(getApplicationContext().getFilesDir(), tmpImg).delete();
        Log.d("AddEditActivity", "Temp image deleted = " + deleted);
        deleted = new File(getApplicationContext().getFilesDir(), oldFlavor.getImgName()).delete();
        Log.d("AddEditActivity", "Old image deleted = " + deleted);

        // remove the flavor from "flavors.json"
        File flavorFile = new File(getApplicationContext().getFilesDir(), "flavors.json");
        FlavorItem.deleteFromJSONFile(flavorFile, oldFlavor.getName());

        setResult(DELETED);
        finishAfterTransition();
    }

    // ---------------------------------------------------------------------------------------------
    // returns to MainActivity without saving any data entered/modified
    // ---------------------------------------------------------------------------------------------
    private void cancel() {
        // make sure to delete any image files created before cancelling
        boolean deleted = new File(getApplicationContext().getFilesDir(), tmpImg).delete();
        Log.d("AddEditActivity", "Temp image deleted = " + deleted);

        setResult(CANCELLED);
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
