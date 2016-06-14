package harjoitus.petteri.kalasaasovellus;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Database-class for location data persisting and retrieval.
 *
 * @author Petteri Nevavuori
 * @version 11.6.2016
 */
public class PaikkaDB {
    private final String TAG = this.getClass().getSimpleName();

    public PaikkaDB() {
    }

    /**
     * Model for the database.
     */
    public static abstract class PaikkaEntry implements BaseColumns {
        public static final String TABLE_NAME = "Paikka";
        public static final String COLUMN_NAME_ENTRY_ID = "PaikkaID";
        public static final String COLUMN_NAME_TITLE = "Nimi";
        public static final String COLUMN_NAME_LAT = "Lat";
        public static final String COLUMN_NAME_LON = "Lon";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PaikkaEntry.TABLE_NAME + " (" +
                    PaikkaEntry._ID + " INTEGER PRIMARY KEY," +
                    PaikkaEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    PaikkaEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    PaikkaEntry.COLUMN_NAME_LAT + TEXT_TYPE + COMMA_SEP +
                    PaikkaEntry.COLUMN_NAME_LON + TEXT_TYPE +
                    " )";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PaikkaEntry.TABLE_NAME;

    /**
     * Helper class for accessing the database.
     */
    public static class PaikkaDBHelper extends SQLiteOpenHelper {
        private final String TAG = this.getClass().getSimpleName();

        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "PaikkaDB.db";

        public PaikkaDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            Log.d(TAG, "PaikkaDBHelper constructor");
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            Log.d(TAG, "onCreate");
            sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
            onCreate(sqLiteDatabase);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }


    }
}
