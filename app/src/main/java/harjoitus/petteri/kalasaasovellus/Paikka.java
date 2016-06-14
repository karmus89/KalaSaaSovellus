package harjoitus.petteri.kalasaasovellus;

/**
 * Luokkaa käytetään kalapaikkojen nimi- ja sijaintitietoja yhdessä oliossa.
 *
 * @author Petteri Nevavuori
 */
public class Paikka {
    private String nimi;
    private String lat;
    private String lon;

    public Paikka(String nimi, String lat, String lon) {
        this.nimi = nimi;
        this.lat = lat;
        this.lon = lon;
    }

    public String getNimi() {
        return nimi;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }
}
