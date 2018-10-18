package eksamen.com.turapp.loader;

import com.google.android.gms.maps.model.LatLng;

import org.joda.time.LocalDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import eksamen.com.turapp.model.Person;
import eksamen.com.turapp.model.Sted;
import eksamen.com.turapp.model.Tur;

import static eksamen.com.turapp.loader.JsonKonstanter.JSON_AVREISE_TID;
import static eksamen.com.turapp.loader.JsonKonstanter.JSON_EPOST;
import static eksamen.com.turapp.loader.JsonKonstanter.JSON_FRA_DESTINASJON;
import static eksamen.com.turapp.loader.JsonKonstanter.JSON_FRA_LATITUDE;
import static eksamen.com.turapp.loader.JsonKonstanter.JSON_FRA_LONGITUDE;
import static eksamen.com.turapp.loader.JsonKonstanter.JSON_ID;
import static eksamen.com.turapp.loader.JsonKonstanter.JSON_LEDIGE_PLASSER;
import static eksamen.com.turapp.loader.JsonKonstanter.JSON_MOBIL;
import static eksamen.com.turapp.loader.JsonKonstanter.JSON_NAVN;
import static eksamen.com.turapp.loader.JsonKonstanter.JSON_TIL_DESTINASJON;
import static eksamen.com.turapp.loader.JsonKonstanter.JSON_TIL_LATITUDE;
import static eksamen.com.turapp.loader.JsonKonstanter.JSON_TIL_LONGITUDE;
import static eksamen.com.turapp.loader.JsonKonstanter.JSON_TUR_EIER;

/**
 * Klasse med ansvar for å lese JSON, og returnere passende objekter.
 * <p>
 * Package-private, ApiLaster som benytter denne klassen.
 *
 * @author Kandidatnummer 9
 * @see ApiLaster
 * @see JsonListener
 */
class JsonLeser {

    /**
     * Listener som avfyres ved Json feil
     */
    private JsonListener listener;

    /**
     * Flagg for avreise / destinasjon
     */
    private final static int AVREISE_KODE = 0;
    private final static int DESTINASJON_KODE = 1;

    /**
     * Konstruktør. Tilordner listener.
     *
     * @param listener JsonListener
     * @see JsonListener
     */
    JsonLeser(JsonListener listener) {
        this.listener = listener;
    }

    /**
     * Leser JSON og genererer et person-objekt.
     *
     * @param json json
     * @return personobjekt
     */
    Person getPerson(JSONObject json) {
        try {
            int id = json.getInt(JSON_ID);
            String navn = json.getString(JSON_NAVN);
            String epost = json.getString(JSON_EPOST);
            String mobil = json.getString(JSON_MOBIL);

            return new Person(id, navn, epost, mobil);

        } catch (JSONException err) {
            listener.onError(err.toString());
            return null;
        }
    }

    /**
     * Returnerer en liste med person-objekter fra API'et
     *
     * @param jsonArr array med personer
     * @return liste med personer
     */
    ArrayList<Person> getPersonListe(JSONArray jsonArr) {

        ArrayList<Person> personer = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject jsonObject = jsonArr.getJSONObject(i);

                Person person = getPerson(jsonObject);

                if (person == null)
                    throw new JSONException("Feil ved lesing av tur-objekter");

                personer.add(person);
            }
            return personer;
        } catch (JSONException err) {
            listener.onError(err.toString());
            return null;
        }
    }

    /**
     * Leser JSON og genererer et tur-objekt
     * <p>
     * String-objekt som leses fra avreiseTid-feltet må konverteres til en localdatetime.
     * Benytter DateTimeFormatter fra kallende klasser til dette.
     *
     * @param json json
     * @return turobjekt
     */
    Tur getTur(JSONObject json) {
        try {
            // Henter felter.
            Person sjafor = getPerson(json.getJSONObject(JSON_TUR_EIER));

            LocalDateTime tidsPunkt = LocalDateTime.parse(json.getString(JSON_AVREISE_TID));

            int id = json.getInt(JSON_ID);
            Sted fra = getSted(json, AVREISE_KODE);
            Sted til = getSted(json, DESTINASJON_KODE);
            int ledigeSeter = json.getInt(JSON_LEDIGE_PLASSER);

            if (fra == null || til == null) {
                return null;
            }

            // Bygger tur-objekt
            Tur tur = new Tur.TurBuilder()
                    .id(id)
                    .fra(fra)
                    .til(til)
                    .sjafor(sjafor)
                    .ledigePlasser(ledigeSeter)
                    .avreiseTid(tidsPunkt)
                    .build();

            return tur;
        } catch (JSONException err) {
            listener.onError(err.toString());
            return null;
        }
    }

    /**
     * Returnerer en liste med tur-objekter fra API'et
     *
     * @param jsonArr array med turer
     * @return liste med turer
     */
    ArrayList<Tur> getTurListe(JSONArray jsonArr) {

        ArrayList<Tur> turer = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject jsonObject = jsonArr.getJSONObject(i);

                Tur tur = getTur(jsonObject);

                if (tur == null)
                    throw new JSONException("Feil ved lesing av tur-objekter");

                turer.add(tur);
            }
            return turer;
        } catch (JSONException err) {
            listener.onError(err.toString());
            return null;
        }
    }

    /**
     * Genererer et Sted-objekt fra JsonObject.
     * <p>
     * Kode definerer verdier i Json-parameterne.
     *
     * @param json json
     * @param kode AVREISE_KODE / DESTINASJON_KODE
     * @return Sted
     */
    private Sted getSted(JSONObject json, int kode) {

        String jsonParamAdresse =
                (kode == AVREISE_KODE) ? JSON_FRA_DESTINASJON : JSON_TIL_DESTINASJON;
        String jsonParamLongitude =
                (kode == AVREISE_KODE) ? JSON_FRA_LONGITUDE : JSON_TIL_LONGITUDE;
        String jsonParamLatitude =
                (kode == AVREISE_KODE) ? JSON_FRA_LATITUDE : JSON_TIL_LATITUDE;

        try {
            String adresse = json.getString(jsonParamAdresse);
            double longitude = json.getDouble(jsonParamLongitude);
            double latitude = json.getDouble(jsonParamLatitude);

            return new Sted(new LatLng(latitude, longitude), adresse);
        } catch (JSONException err) {
            listener.onError(err.toString());
            return null;
        }
    }
}
