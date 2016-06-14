package harjoitus.petteri.kalasaasovellus;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastActivityFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    public static ArrayAdapter<String> paivaAdapter;

    public ForecastActivityFragment() {
    }

    private void updateWeather() {
        FetchWeekTask fetchWeatherTask = new FetchWeekTask();

        //Alustetaan intent koordinaattitietojen noutoa varten
        Intent intent = getActivity().getIntent();

        //Haetaan paikan tiedot intentistä indekseistä 1 ja 2 (0 on nimi)
        String paikkaLat = intent.getStringArrayExtra(Intent.EXTRA_TEXT)[1];
        String paikkaLon = intent.getStringArrayExtra(Intent.EXTRA_TEXT)[2];

        //Viedään koordinaatit haun suorittavaan aliluokkaan
        fetchWeatherTask.execute(paikkaLat, paikkaLon);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Haetaan säätiedot näkymän luonnin yhteydessä
        updateWeather();

        //Alustetaan intent paikan nimen noutoa varten ja noudetaan se
        Intent intent = getActivity().getIntent();
        String paikkaNimi = intent.getStringArrayExtra(Intent.EXTRA_TEXT)[0];



        //Alustetaan näkymälle muuttuja
        View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);

        //Alustetaan adapteri tietojen listaamista ja näyttöä varten
        List<String> viikkotiedotFake = new ArrayList<>();
        paivaAdapter = new ArrayAdapter<>(
                getContext(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                viikkotiedotFake
        );

        //Luodaan ListView hakemalla se oikealla id:llä
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        //Asetetaan ListViewiin adapteri
        listView.setAdapter(paivaAdapter);

        return rootView;
    }

    /**
     * Aliluokka säätietojen noutamiseen MainActivitysta valitun kalapaikan koordinaateilla.
     */
    public class FetchWeekTask extends AsyncTask<String, Void, String[]> {

        private final String TAG = this.getClass().getSimpleName();

        /**
         * Hakee säätiedot seuraavalle kahdelle viikolle päivätasolla.
         *
         * @param params Sisään tuodaan pääluokan updateWeather-metodista leveys- ja
         *               korkeuskoordinaatit.
         * @return Palauttaa String-taulukon, joka sisältää adapterin tarvitsemat säätiedot.
         */
        @Override
        protected String[] doInBackground(String... params) {
            //Alustetaan yhteyden ottoon tarvittavat oliot
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;

            //UriBuilderin tarvitsemat arvot
            String format = "json";
            String units = "metric";
            int numDays = 14;
            String language = "fi";
            String apiKey = "f2151ae038ac59e107c56040bad4179d";

            //Yritetään rakentaa palautettava taulukko
            try {

                //HTTP-kysely rakennetaan käyttämällä OpenWeatherMap-palvelun API-rajapinnan
                //määritteitä päiväkohtaisten säätietojen hakuun. Lisätietoa osoitteesta:
                //http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2" +
                        ".5/forecast/daily?";
                final String LAT_PARAM = "lat";
                final String LON_PARAM = "lon";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String LANG_PARAM = "lang";
                final String APPID_PARAM = "APPID";

                //Rakennetaan URI
                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(LAT_PARAM, params[0])
                        .appendQueryParameter(LON_PARAM, params[1])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(LANG_PARAM, language)
                        .appendQueryParameter(APPID_PARAM, apiKey)
                        .build();

                //Tehdään URIsta URL
                URL url = new URL(builtUri.toString());
                Log.i(TAG, "Rakennettu URI=" + builtUri.toString());

                //Avataan yhteys muodostettuun osoitteeseen, hakutapana GET
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                //Luetaan vaste forecastJsonStr-muuttujaan
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    //Mikäli tyhjä, ei tehdä mitään
                    Log.d(TAG, "Vaste tyhjä");
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.i(TAG, "Forecast JSON String: " + forecastJsonStr);
            } catch (IOException e) {
                Log.e(TAG, "Error ", e);
                return null;
            } finally {
                //Lopuksi suljetaan yhteys ja lukija
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return getWeatherDataFromJson(forecastJsonStr, numDays);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Suoritetaan suoraan doInBackground-metodin jälkeen ottaen sisään sen palauttaman arvon.
         *
         * @param strings Taulukko, joka pitää sisällään sääpalvelusta haetut ja parsitut tiedot
         *                omina riveinään.
         */
        @Override
        protected void onPostExecute(String[] strings) {
            if (strings != null) {
                ForecastActivityFragment.paivaAdapter.clear();
                for (String day : strings) {
                    ForecastActivityFragment.paivaAdapter.add(day);
                }
            }
        }

        /**
         * Ottaa vastaan haun palauttaman JSON-vasteen ja noutaa sen sisältä päivän maksimi- ja
         * minimilämpötilan sekä säätilan selkokielisenä.
         *
         * @param forecastJsonStr JSON-muotoinen haun palauttama vaste
         * @param numDays
         * @return
         * @throws JSONException
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "description";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];

            //Data is in metric form by default

            for (int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String lowAndHigh;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay + i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = formatToTitleCase(weatherObject.getString(OWM_DESCRIPTION));


                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                lowAndHigh = formatHighLows(high, low);
                resultStrs[i] = day +"  "+ lowAndHigh+"\n" + description;
            }

            for (String s : resultStrs) {
                Log.i(TAG, "Forecast entry: " + s);
            }
            return resultStrs;

        }

        /**
         * Muokataan lämpötila käyttäjäystävälliseen muotoon
         */
        private String formatHighLows(double high, double low) {

            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String lowHighStr = roundedLow + "°C / " + roundedHigh + "°C";
            return lowHighStr;
        }

        /**
         * Muokataan aika käyttäjäystävälliseen ja luettavaan muotoon.
         *
         * @param time Vasteen palauttama aikatieto
         * @return Palauttaa ajan käyttäjän luettavassa muodossa.
         */
        private String getReadableDateString(long time) {
            Locale fi = new Locale("fi");
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEEE d'.' MMMM", fi);
            String date = formatToTitleCase(shortenedDateFormat.format(time));
            return date;
        }

        /**
         * Muuttaa sisään tuodun Stringin jokaisen sanan ensimmäisen kirjaimen isoksi kirjaimeksi.
         */
        private String formatToTitleCase(String input) {
            String[] words = input.split(" ");
            StringBuilder sb = new StringBuilder();
            if (words[0].length() > 0) {
                sb.append(Character.toUpperCase(words[0].charAt(0)) + words[0].subSequence(1,
                        words[0].length()).toString().toLowerCase());
                for (int i = 1; i < words.length; i++) {
                    sb.append(" ");
                    sb.append(words[i]);
                }
            }
            String titleCaseValue = sb.toString();
            return titleCaseValue;
        }
    }


}
