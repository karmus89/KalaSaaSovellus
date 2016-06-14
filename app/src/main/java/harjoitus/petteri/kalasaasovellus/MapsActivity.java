package harjoitus.petteri.kalasaasovellus;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * A Google Maps activity that lets the user save new locations to Main activity's list with
 * draggable marker. The activity reads the location from the settings and centers the position
 * accordingly. The coordinate is saved to separate storage from which the Main activity then
 * uses it.
 *
 * @author Petteri Nevavuori
 * @version 10.6.2016
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap
        .OnMarkerDragListener {
    private final String TAG = this.getClass().getSimpleName();
    final Context context = this;

    private SharedPreferences sharedPreferences;
    private GoogleMap mMap;
    private double mLatitude;
    private double mLongitude;
    private String paikkaNimi = null;
    private String paikkaLat = null;
    private String paikkaLon = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //Get preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences
                (MapsActivity.this);

        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Initialize and set floating save-button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_maps);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Save-button pressed");

                //Initialize EditText field
                final EditText textInput = new EditText(context);
                textInput.setInputType(InputType.TYPE_CLASS_TEXT);

                //Initialize AlertDialog
                AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                        .setTitle(getString(R.string.dialog_maps_save))
                        .setView(textInput)
                        .setPositiveButton(getString(R.string.dialog_maps_button_yes), new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Log.d(TAG, "PositiveButton pressed");
                                        paikkaNimi = textInput.getText().toString();
                                        Log.d(TAG, "Updated paikkaNimi " + paikkaNimi);
                                        saveToDB();
                                        closeActivity();
                                    }
                                })
                        .setNegativeButton(getString(R.string.dialog_maps_button_no),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Log.d(TAG, "NegativeButton pressed");
                                    }
                                });

                //Set AlertDialog visible
                dialog.show();
            }
        });
    }

    /**
     * Centers the map to position defined in settings as "Oletussijainti" and adds a draggable
     * marker for place selection purposes.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Assign initialized map to class' map variable
        mMap = googleMap;

        //Initialize coordinates to location defined in settings
        useHomeLocation();

        //Initialize a LatLng-variable for map positioning
        LatLng locationLatLng = new LatLng(mLatitude, mLongitude);

        //Initialize position and adequate zoom (0 is whole world, ~20 is building level) for the
        //marker
        CameraPosition chooserPos = new CameraPosition.Builder().target(locationLatLng).zoom(10)
                .build();

        //Move the camera
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(chooserPos));

        //Add marker drag listener
        mMap.setOnMarkerDragListener(this);

        //Add a draggable marker
        mMap.addMarker(new MarkerOptions().position(locationLatLng).title("Valitsin").draggable
                (true).snippet("Raahaa minut haluamasi pisteeseen"));


        //Show infotext
        Toast toast = Toast.makeText(getApplicationContext(), "Raahaa merkki haluamaasi " +
                "pisteeseen", Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Uses Geocoder to transform location string to latitude and longitude values. Saves the
     * values to classes own properties.
     */
    private void useHomeLocation() {
        Geocoder geocoder;
        List<Address> addressList;

        //Get location from preferences
        String location = sharedPreferences.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        Log.d(TAG, "Location: " + location);

        //Try getting coordinates using location name
        try {
            geocoder = new Geocoder(MapsActivity.this);
            addressList = geocoder.getFromLocationName(location, 1);
            if (addressList.size() > 0) {
                //Get latitude and longitude
                mLatitude = addressList.get(0).getLatitude();
                mLongitude = addressList.get(0).getLongitude();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the name and coordinates for a location to the database.
     */
    private void saveToDB() {
        Log.d(TAG, "Saving to DB..");
        PaikkaDB.PaikkaDBHelper dbHelper = new PaikkaDB.PaikkaDBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Init values for db input
        ContentValues values = new ContentValues();
        //Marker wasn't moved, use home location
        if (paikkaLat == null || paikkaLon == null) {
            Log.d(TAG, "..using home location..");
            values.put(PaikkaDB.PaikkaEntry.COLUMN_NAME_LAT, String.valueOf(mLatitude));
            values.put(PaikkaDB.PaikkaEntry.COLUMN_NAME_LON, String.valueOf(mLongitude));
        }
        //Marker was moved, use new location
        else {
            Log.d(TAG, "..using marker location..");
            values.put(PaikkaDB.PaikkaEntry.COLUMN_NAME_LAT, paikkaLat);
            values.put(PaikkaDB.PaikkaEntry.COLUMN_NAME_LON, paikkaLon);
        }

        values.put(PaikkaDB.PaikkaEntry.COLUMN_NAME_TITLE, paikkaNimi);

        long newRowId = db.insert(
                PaikkaDB.PaikkaEntry.TABLE_NAME,
                null,
                values
        );
        Log.d(TAG, "..created new row " + newRowId);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        Log.d(TAG, "Drag start: " + marker.getTitle());
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    /**
     * Reads the position of the marker after drag is finished.
     */
    @Override
    public void onMarkerDragEnd(Marker marker) {
        Log.d(TAG, "Drag end: " + marker.getTitle());
        paikkaLat = String.valueOf(marker.getPosition().latitude);
        paikkaLon = String.valueOf(marker.getPosition().longitude);
        Log.d(TAG, "Updated paikkaLat " + paikkaLat + ", paikkaLon " + paikkaLon);
    }

    /**
     * Closes the whole MapsActivity.
     */
    private void closeActivity() {
        this.finish();
    }
}
