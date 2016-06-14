package harjoitus.petteri.kalasaasovellus;

import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;

/**
 * Application's preference activity. Offers preferences for default location used by
 * application's map and deletion of all saved locations.
 *
 * @author Petteri Nevavuori
 * @version 14.6.2016
 */
public class SettingsActivity extends PreferenceActivity implements Preference
        .OnPreferenceChangeListener {

    private final String TAG = this.getClass().getSimpleName();
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.preferences);

        //Add preference change listener to lcoation preference
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));

        //Add click listener to data erase preference
        Preference erase = findPreference(getString(R.string.pref_erase_key));
        erase.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                        .setTitle(getString(R.string.dialog_pref_erase))
                        .setPositiveButton(R.string.dialog_pref_erase_button_yes, new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Log.d(TAG, "PositiveButton pressed");
                                        eraseDB();
                                    }
                                })
                        .setNegativeButton(R.string.dialog_pref_erase_button_no, new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Log.d(TAG, "NegativeButton pressed");

                                    }
                                });
                dialog.show();
                return true;
            }
        });
    }


    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);
        // Trigger the listener immediately with the preference's
        // current value.

        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * Listener for preference change, that only applies new value to only editable
     * EditText-preference.
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        Log.d(TAG, "preference: " + preference.getClass().getSimpleName() + ", key: " +
                preference.getKey() + ", new value: " + value.toString());
        String stringValue = value.toString();
        preference.setSummary(stringValue);
        return true;
    }

    /**
     * Erases all the records in the PaikkaDB.db.
     */
    private void eraseDB() {
        Log.d(TAG, "Erasing all DB entries..");
        PaikkaDB.PaikkaDBHelper dbHelper = new PaikkaDB.PaikkaDBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(
                PaikkaDB.PaikkaEntry.TABLE_NAME,
                null,
                null
        );
        db.close();
        Log.d(TAG, ".. ok");
    }
}
