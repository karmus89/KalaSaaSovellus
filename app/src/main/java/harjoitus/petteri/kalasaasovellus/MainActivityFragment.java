package harjoitus.petteri.kalasaasovellus;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * The content view for the MainActivity-class.
 *
 * @author Petteri Nevavuori
 * @version 11.6.2016
 */
public class MainActivityFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    private PaikkaAdapter paikkaAdapter;
    private Context context;
    private List<Paikka> paikkaList;

    public MainActivityFragment() {
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        paikkaList = readFromDB();

        paikkaAdapter = new PaikkaAdapter(
                context,
                R.layout.list_item_main,
                R.id.list_item_main_textview,
                paikkaList);

        //Luodaan ListView hakemalla se oikealla id:llä
        ListView listView = (ListView) rootView.findViewById(R.id.listview_main);
        //Asetetaan ListViewiin adapteri
        listView.setAdapter(paikkaAdapter);

        //Tehdään jokaiselle listan elementille oma painalluksen kuuntelijansa
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Haetaan tiedot valitusta Paikka-oliosta String-taulukkoon
                String[] paikkaTiedot = {paikkaAdapter.getItem(position).getNimi(),
                        paikkaAdapter.getItem(position).getLat(),
                        paikkaAdapter.getItem(position).getLon()};

                //Näytetään Toast painalluksesta
                String toast = "Avataan: " + paikkaTiedot[0] + "\n" + paikkaTiedot[1] + ", " +
                        paikkaTiedot[2];
                Toast.makeText(getActivity(), toast, Toast.LENGTH_SHORT).show();

                //Käynnistetään ForecastActivity, johon viedään valitun paikan koordinaatit
                //String-taulukkona
                Intent intent = new Intent(getActivity(), ForecastActivity.class).putExtra
                        (Intent.EXTRA_TEXT, paikkaTiedot);
                startActivity(intent);
            }
        });

        return rootView;
    }

    private List<Paikka> readFromDB() {
        Log.d(TAG, "Reading from DB..");
        List<Paikka> rowList = new ArrayList<>();

        String[] projection = {
                PaikkaDB.PaikkaEntry.COLUMN_NAME_TITLE,
                PaikkaDB.PaikkaEntry.COLUMN_NAME_LAT,
                PaikkaDB.PaikkaEntry.COLUMN_NAME_LON,
        };

        PaikkaDB.PaikkaDBHelper dbHelper = new PaikkaDB.PaikkaDBHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                PaikkaDB.PaikkaEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String nimi = cursor.getString(cursor.getColumnIndexOrThrow(PaikkaDB.PaikkaEntry
                    .COLUMN_NAME_TITLE));
            String lat = cursor.getString(cursor.getColumnIndexOrThrow(PaikkaDB.PaikkaEntry
                    .COLUMN_NAME_LAT));
            String lon = cursor.getString(cursor.getColumnIndexOrThrow(PaikkaDB.PaikkaEntry
                    .COLUMN_NAME_LON));
            Paikka paikka = new Paikka(nimi, lat, lon);
            Log.d(TAG, "Paikka " + paikka.getNimi() + " " + paikka.getLat() + " " + paikka
                    .getLon());
            rowList.add(paikka);
            cursor.moveToNext();
        }
        Log.d(TAG, ".. entries returned:" + rowList.size());

        cursor.close();
        db.close();
        dbHelper.close();

        return rowList;
    }
}

