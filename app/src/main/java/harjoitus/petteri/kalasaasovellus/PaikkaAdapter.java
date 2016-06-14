package harjoitus.petteri.kalasaasovellus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Paikka-luokan oma ArrayAdapter-laajennos.
 *
 * @author Petteri Nevavuori
 */
public class PaikkaAdapter extends ArrayAdapter<Paikka> {
    private final String TAG = this.getClass().getSimpleName();

    public PaikkaAdapter(Context context, int resource, int textViewResourceId, List<Paikka>
            objects) {
        super(context, resource, textViewResourceId, objects);
    }

    private static class ViewHolder {
        private TextView textView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        //Tarkistetaanko onko vanha näkymä olemassa uudelleenkäyttöä varten
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.list_item_main,
                    parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) convertView.findViewById(R.id.list_item_main_textview);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //Haetaan paikka-olio
        Paikka paikka = getItem(position);

        //Jos ei tyhjä, otetaan olion nimi ja asetetaan näyttönimeksi
        if (paikka != null) {
            viewHolder.textView.setText(paikka.getNimi());
        }

        return convertView;
    }
}
