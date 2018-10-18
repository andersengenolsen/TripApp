package eksamen.com.turapp.loader;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import eksamen.com.turapp.R;
import eksamen.com.turapp.model.Person;
import eksamen.com.turapp.model.Tur;

import static eksamen.com.turapp.loader.JsonKonstanter.JSON_ADDRESSE;
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
 * Klasse med ansvar for å laste data fra API'et.
 * Mer om API'et i rapporten.
 * <p>
 *
 * @link http://54.38.157.214:8080/eksamen-api/
 * @see JsonLeser
 * @see JsonListener
 */

public class ApiLaster implements JsonListener {

    /**
     * Singleton
     */
    private static ApiLaster apiLaster;

    /**
     * Url til API-et.
     */
    private final static String API_URL = "http://54.38.157.214:8080/eksamen-turapp/api";

    /**
     * Tillegg til URL
     */
    private final static String BRUKER_BASE_URL = API_URL + "/user";
    private final static String TUR_BASE_URL = API_URL + "/trip";
    private final static String OPPDATER_URL = "/update";
    private final static String PASSASJER_URL = "/passengertrips";
    private final static String SJAFOR_URL = "/drivertrips";
    private final static String ALLE_PASSASJERER_URL = "/passengers";
    private final static String I_OMRADET_URL = "nearby";

    /**
     * RequestQueue fra Volley-API
     */
    private RequestQueue queue;

    /**
     * Kontekst
     */
    private Context context;

    /**
     * Parser
     */
    private JsonLeser jsonLeser;

    /**
     * Benyttes til å formatere LocalDateTime
     */
    private final DateTimeFormatter formatter = ISODateTimeFormat.dateHourMinute();

    /**
     * Private konstruktør
     *
     * @param context kontekst
     */
    private ApiLaster(Context context) {
        this.context = context;
        // Ekstern bibliotek som benyttes da LocalDateTime ikke er tilgjengelig.
        JodaTimeAndroid.init(context);
        queue = Volley.newRequestQueue(context);
        jsonLeser = new JsonLeser(this);
    }

    /**
     * Returnerer et ApiLaster-objekt.
     *
     * @param context Activity-context
     * @return Singleton-objekt
     */
    public static synchronized ApiLaster getInstance(Context context) {
        if (apiLaster == null)
            apiLaster = new ApiLaster(context);

        return apiLaster;
    }

    /**
     * Avfyres fra JsonLeser dersom en feil inntreffer JSON-leses.
     *
     * @param err JSONException
     */
    @Override
    public void onError(String err) {
        Toast.makeText(context, R.string.feil_lesing, Toast.LENGTH_SHORT).show();
    }

    /**
     * Legger til en bruker i databasen.
     *
     * @param person
     * @param listener
     */
    public void leggTilBruker(Person person, final ApiListener<Person> listener) {
        JSONObject json = lagJsonPersonObjekt(person, false);
        if (json == null) {
            listener.feil();
            return;
        }
        getBrukerObjekt(BRUKER_BASE_URL, json, listener, Request.Method.POST);
    }

    /**
     * Oppdaterer en bruker i databasen
     *
     * @param person   bruker som skal oppdateres
     * @param listener avfyres når kall er avsluttet
     */
    public void oppdaterBruker(final Person person, final ApiListener<Person> listener) {
        String url = BRUKER_BASE_URL + "/" + person.getId() + "/" + OPPDATER_URL;
        JSONObject json = lagJsonPersonObjekt(person, true);
        if (json == null) {
            listener.feil();
            return;
        }
        getBrukerObjekt(url, json, listener, Request.Method.POST);
    }

    /**
     * Henter en bruker fra databasen.
     * JsonLeser omgjør fra JsonObjekt til Personobjekt.
     *
     * @param personId
     * @param listener
     * @see JsonLeser#getPerson(JSONObject)
     */
    public void hentBruker(int personId, final ApiListener<Person> listener) {
        String url = BRUKER_BASE_URL + "/" + personId;
        getBrukerObjekt(url, null, listener, Request.Method.GET);
    }

    /**
     * Registrerer en ny tur i databasen
     *
     * @param tur      tur som skal registreres
     * @param listener avfyres når kall er ferdig
     */
    public void registrerNyTur(Tur tur, final ApiListener<Tur> listener) {
        JSONObject json = lagJsonTurObjekt(tur, false);

        if (json == null) {
            listener.feil();
            return;
        }

        getTurObjekt(TUR_BASE_URL, json, listener, Request.Method.POST);
    }


    /**
     * Henter turer hvor bruker er registrert som passasjer
     *
     * @param personId bruker id
     * @param listener avfyres når kall ferdig.
     */
    public void hentPassasjerTurer(int personId, final ApiListener<ArrayList<Tur>> listener) {
        String url = BRUKER_BASE_URL + "/" + personId + PASSASJER_URL;
        getReturnTurListe(url, listener);
    }

    /**
     * Henter turer hvor bruker er registrert som sjåfør.
     *
     * @param personId bruker id
     * @param listener avfyres når kall ferdig.
     */
    public void hentSjaforTurer(int personId, final ApiListener<ArrayList<Tur>> listener) {
        String url = BRUKER_BASE_URL + "/" + personId + SJAFOR_URL;
        getReturnTurListe(url, listener);
    }

    /**
     * Henter passasjerliste for en tur
     *
     * @param turId    turens id
     * @param listener interface
     */
    public void hentPassasjerListe(int turId, final ApiListener<ArrayList<Person>> listener) {
        String url = TUR_BASE_URL + "/" + turId + "/" + ALLE_PASSASJERER_URL;
        getReturnPersonListe(url, listener);
    }

    /**
     * Sletter tur fra databasen
     *
     * @param turId    id
     * @param listener interface
     */
    public void slettTur(int turId, final ApiListener<Tur> listener) {
        String url = TUR_BASE_URL + "/" + turId;
        getTurObjekt(url, null, listener, Request.Method.DELETE);
    }

    /**
     * Sletter en bruker fra en tur (fjernes fra passasjerlisten).
     *
     * @param brukerId
     * @param tur
     * @param listener
     */
    public void slettBrukerFraTur(int brukerId, Tur tur, final ApiListener<Tur> listener) {
        String url = BRUKER_BASE_URL + "/" + brukerId + PASSASJER_URL + "/" + tur.getId();


        getTurObjekt(url, null, listener, Request.Method.DELETE);
    }

    /**
     * Henter en liste med turer i området.
     * Turene som returneres er på samme poststed, og har samme avreise-dato!
     *
     * @param adresse  adresse
     * @param tid      tidspunkt
     * @param listener avfyres når kall er utført
     */
    public void hentTurerIOmradet(String adresse, LocalDateTime tid,
                                  final ApiListener<ArrayList<Tur>> listener) {
        String url = TUR_BASE_URL + "/" + I_OMRADET_URL;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(JSON_ADDRESSE, adresse);
            jsonObject.put(JSON_AVREISE_TID, formatter.print(tid));
        } catch (JSONException err) {
            listener.feil();
        }

        postReturnTurListe(url, jsonObject, listener);
    }

    /**
     * Melder bruker på en tur.
     */
    public void meldBrukerPaTur(Tur tur, Person person, final ApiListener<Person> listener) {
        String url = TUR_BASE_URL + "/" + tur.getId();

        JSONObject json = lagJsonPersonObjekt(person, true);
        if (json == null) {
            listener.feil();
            return;
        }

        getBrukerObjekt(url, json, listener, Request.Method.POST);
    }

    /**
     * Henter et bruker-objekt fra databasen.
     *
     * @param url      API url
     * @param json     evnt sendes med
     * @param listener interface
     * @param type     request-type
     */
    private void getBrukerObjekt(String url, JSONObject json, final ApiListener<Person> listener,
                                 int type) {
        JsonObjectRequest postRequest = new JsonObjectRequest(type,
                url, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Person p = jsonLeser.getPerson(response);
                if (p == null) {
                    listener.feil();
                    return;
                }
                listener.vellykket(p);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.feil();
            }
        });

        queue.add(postRequest);
    }

    /**
     * Henter ut en liste av Person-objekter fra et GET-kall
     *
     * @param url      API url
     * @param listener avfyres når kall ferdig
     * @see JsonLeser#getPersonListe(JSONArray)
     */
    private void getReturnPersonListe(String url, final ApiListener<ArrayList<Person>> listener) {
        // Volley-kall
        final JsonArrayRequest json = new JsonArrayRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                ArrayList<Person> personer = jsonLeser.getPersonListe(response);
                if (personer == null) {
                    listener.feil();
                    return;
                }

                listener.vellykket(personer);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.feil();
            }
        }
        );

        // Legger i kø
        queue.add(json);
    }

    /**
     * Henter turobjekt fra databasen.
     *
     * @param url      API-url
     * @param json     json som evnt skal postes
     * @param listener avfyres når utført
     * @param type     Request-Type
     * @see JsonLeser#getTur(JSONObject)
     */
    private void getTurObjekt(String url, JSONObject json, final ApiListener<Tur> listener,
                              int type) {

        JsonObjectRequest request = new JsonObjectRequest(type,
                url, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Tur tur = jsonLeser.getTur(response);
                if (tur == null) {
                    listener.feil();
                    return;
                }
                listener.vellykket(tur);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.feil();
            }
        });

        queue.add(request);
    }

    /**
     * Sender GET-request til API'et, og får returnert en liste med turer.
     *
     * @param url      url til API'et
     * @param listener listener som avfyres når kall ferdig
     * @see JsonLeser#getTurListe(JSONArray)
     */
    private void getReturnTurListe(String url, final ApiListener<ArrayList<Tur>> listener) {
        // Volley-kall
        final JsonArrayRequest json = new JsonArrayRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                ArrayList<Tur> turer = jsonLeser.getTurListe(response);

                if (turer == null) {
                    listener.feil();
                    return;
                }

                listener.vellykket(turer);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.feil();
            }
        }
        );

        // Legger i kø
        queue.add(json);
    }

    /**
     * Sender GET-request til API'et, og får returnert en liste med turer.
     *
     * @param url      url til API'et
     * @param listener listener som avfyres når kall ferdig
     * @see JsonLeser#getTurListe(JSONArray)
     * @see CustomJsonArrayRequest (kopiert fra nett!)
     */
    private void postReturnTurListe(String url, JSONObject jsonObject,
                                    final ApiListener<ArrayList<Tur>> listener) {
        // Volley-kall
        final CustomJsonArrayRequest json = new CustomJsonArrayRequest(
                Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        ArrayList<Tur> turer = jsonLeser.getTurListe(response);

                        if (turer == null) {
                            listener.feil();
                            return;
                        }

                        listener.vellykket(turer);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.feil();
            }
        }
        );

        // Legger i kø
        queue.add(json);
    }

    /**
     * Lager et person-objekt som json, uten id
     *
     * @param medId  Om brukerens id også skal legges med i JSON.
     * @param person person-objekt
     * @return json-representasjon av person
     */
    private JSONObject lagJsonPersonObjekt(Person person, boolean medId) {
        // Bygger json object som skal sendes til API'et
        JSONObject json = new JSONObject();
        try {
            if (medId)
                json.put(JSON_ID, person.getId());
            json.put(JSON_NAVN, person.getNavn());
            json.put(JSON_EPOST, person.getEpost());
            json.put(JSON_MOBIL, String.valueOf(person.getTlf()));
            return json;

        } catch (JSONException err) {
            return null;
        }
    }

    /**
     * Lager et JSON-objekt for en tur.
     *
     * @param tur tur objekt
     * @return jsonobjekt
     */
    private JSONObject lagJsonTurObjekt(Tur tur, boolean medId) {
        // Bygger json object som skal sendes til API'et
        JSONObject json = new JSONObject();
        try {
            if (medId)
                json.put(JSON_ID, tur.getId());
            json.put(JSON_FRA_DESTINASJON, tur.getFra().getGateAdresse());
            json.put(JSON_TIL_DESTINASJON, tur.getTil().getGateAdresse());
            json.put(JSON_TUR_EIER, tur.getSjaforId());
            json.put(JSON_LEDIGE_PLASSER, tur.getLedigePlasser());
            json.put(JSON_FRA_LATITUDE, tur.getFra().getPosisjon().latitude);
            json.put(JSON_FRA_LONGITUDE, tur.getFra().getPosisjon().longitude);
            json.put(JSON_TIL_LATITUDE, tur.getTil().getPosisjon().latitude);
            json.put(JSON_TIL_LONGITUDE, tur.getTil().getPosisjon().longitude);
            json.put(JSON_AVREISE_TID, tur.getAvreiseTidString());

            return json;
        } catch (JSONException err) {
            return null;
        }
    }
}
