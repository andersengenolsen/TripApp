package eksamen.com.turapp.activites;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.TimeZone;

import eksamen.com.turapp.R;
import eksamen.com.turapp.fragment.InnstillingDialogFragment;
import eksamen.com.turapp.listener.InnstillingDialogListener;
import eksamen.com.turapp.loader.ApiLaster;
import eksamen.com.turapp.loader.ApiListener;
import eksamen.com.turapp.model.Person;
import eksamen.com.turapp.model.Sted;
import eksamen.com.turapp.model.Tur;

/**
 * BaseActivity, alle aktiviter som skal inneholde Toolbar med Options menu må extende denne.
 * Aktiviteten definerer hva som skal gjøres når elementer i optionsmenyen blir valgt.
 * <p>
 * Layout til aktiviteten er activity_base.xml, som inneholder toolbar og en framelayout.
 * Framelayout'en blir benyttet i subaktiviteter til å vise innhold.
 * <p>
 * Subaktiviteter må kalle leggTilLayoutView.
 *
 * @author Kandidatnr 9
 */

public abstract class BaseActivity extends AppCompatActivity implements InnstillingDialogListener {

    /**
     * Shared prefs
     */
    public static final String TUR_APP = "tur_app";
    public static final String SHARED_PREF_ID = "shared_pref_id";
    public static final String SHARED_PREF_AVREISE = "shared_pref_avreise";
    public static final String SHARED_PREF_DESTINASJON = "shared_pref_destinasjon";
    public static final String FAVORITT_LONGITUDE = "favoritt_longitude";
    public static final String FAVORITT_LATITUDE = "favoritt_latitude";
    public static final String FAVORITT_ADRESSE = "favoritt_adresse";

    /**
     * Keys
     */
    public static final String PERSON_EPOST_KEY = "epost";
    public static final String PERSON_NAVN_KEY = "navn";
    public static final String PERSON_MOBIL_KEY = "mobil";


    /**
     * Layout-stub til subaktiviteter.
     */
    protected FrameLayout frameLayout;

    /**
     * Kall til API'et.
     */
    protected ApiLaster apiLaster;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiLaster = ApiLaster.getInstance(this);

        // Setter "super"-layout
        setContentView(R.layout.activity_base);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hjem pil i alle acitivites unntatt MainActivity
        if (!this.getClass().equals(MainActivity.class))
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        frameLayout = findViewById(R.id.frame);
    }

    /**
     * Kalles dersom bruker lagrer verdier fra InnstillingDialogFragment.
     * <p>
     * Lagrer eller oppdaterer verdier i databasen via API'et.
     * <p>
     * Hvis brukeren ikke har en lagret shared-pref for brukerid, legges en ny bruker i databasen.
     * Dersom bruker har en brukerid lagret, oppdateres verdier i databasen.
     * Skriver personens bruker-id til shared prefs.
     *
     * @param person
     * @see ApiLaster
     * @see Person
     */
    @Override
    public void dialogPositivKlikk(Person person) {
        int id = hentBrukerId();

        // Ikke allerede lagret i SharedPrefs
        if (id == -1) {
            apiLaster.leggTilBruker(person, new ApiListener<Person>() {

                @Override
                public void vellykket(Person result) {
                    visToast(getString(R.string.lagret_verdier));
                    // Lagret i database, skriver til SharedPref
                    SharedPreferences.Editor editor = hentSharedPrefsEditor(TUR_APP);
                    editor.putInt(SHARED_PREF_ID, result.getId());
                    editor.apply();
                }

                @Override
                public void feil() {
                    visToast(getString(R.string.feil));
                }
            });
        } else {
            // Må huske å sette id som er hentet fra shared prefs!
            // Benyttes til identifisering i databasen.
            person.setId(id);
            // Oppdaterer bruker i databasen
            apiLaster.oppdaterBruker(person, new ApiListener<Person>() {
                @Override
                public void vellykket(Person result) {
                    visToast(getString(R.string.lagret_verdier));
                }

                @Override
                public void feil() {
                    visToast(getString(R.string.feil));
                }
            });
        }
    }

    /**
     * Laster "sub-layout" inn i framelayout.
     * Metoden kalles fra sub aktiviteter.
     * Subaktiviteter må altså kalle leggTilLayoutView.
     *
     * @param layoutId layout som skal benyttes i subaktivitet
     */
    protected void leggTilLayoutView(int layoutId) {
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layoutId, null, false);
        frameLayout.addView(view, 0);
    }

    /**
     * Håndterer klikk på elementer i optionsmenu
     *
     * @param item
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_settings:
                visSettingsDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Setter options menu, som er layout menu_main.xml
     *
     * @param menu meny
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Hjelpemetode for å vise Toast
     *
     * @param msg Toast-melding
     */
    protected void visToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Henter brukerens id fra shared prefs.
     * -1 returneres dersom brukeren ikke har noen id.
     *
     * @return -1 hvis ikke id.
     */
    protected final int hentBrukerId() {
        SharedPreferences prefs = getSharedPreferences(TUR_APP, Context.MODE_PRIVATE);
        return prefs.getInt(SHARED_PREF_ID, -1);
    }

    /**
     * Returnerer SharedPrefs editor for hele appen
     *
     * @return SharedPreferences.Editor
     */
    protected final SharedPreferences.Editor hentSharedPrefsEditor(String type) {
        return getSharedPreferences(type, Context.MODE_PRIVATE).edit();
    }

    /**
     * Henter en brukers favorittsted fra shared prefs
     *
     * @param type SHARED_PREF_AVREISE eller SHARED_PREF_DESTINASJON
     * @return Sted hvis funnet, null hvis ikke funnet
     */
    protected final Sted getFavorittSted(String type) {
        SharedPreferences preferences = getSharedPreferences(type, MODE_PRIVATE);

        String adresse = preferences.getString(FAVORITT_ADRESSE, null);
        double latitude = Double.longBitsToDouble(
                preferences.getLong(FAVORITT_LATITUDE, -1));
        double longitude = Double.longBitsToDouble(
                preferences.getLong(FAVORITT_LONGITUDE, -1));

        if (adresse == null || latitude < 0 || longitude < 0)
            return null;

        return new Sted(new LatLng(latitude, longitude), adresse);
    }

    /**
     * Sjekker om en bruker har et favorittsted lagret
     *
     * @param type SHARED_PREF_AVREISE eller SHARED_PREF_DESTINASJON
     * @return
     */
    protected final boolean harFavorittSted(String type) {
        return getFavorittSted(type) != null;
    }

    /**
     * Endrer en brukers favorittsted fra shared prefs
     *
     * @param type SHARED_PREF_AVREISE eller SHARED_PREF_DESTINASJON
     */
    protected final void endreFavorittSted(String type, Sted sted) {
        SharedPreferences.Editor editor = hentSharedPrefsEditor(type);

        editor.putString(FAVORITT_ADRESSE, sted.getGateAdresse());
        editor.putLong(FAVORITT_LATITUDE,
                Double.doubleToLongBits(sted.getPosisjon().latitude));
        editor.putLong(FAVORITT_LONGITUDE,
                Double.doubleToLongBits(sted.getPosisjon().longitude));

        editor.apply();

    }

    /**
     * Sletter favoritt-sted fra shared prefs.
     *
     * @param type
     */
    protected final void slettFavorittSted(String type) {
        SharedPreferences.Editor editor = hentSharedPrefsEditor(type);

        editor.remove(FAVORITT_ADRESSE);
        editor.remove(FAVORITT_LONGITUDE);
        editor.remove(FAVORITT_LATITUDE);

        editor.apply();
    }

    /**
     * Lagrer tur i kalendar
     * <p>
     * KILDE: https://stackoverflow.com/questions/10412074/adding-events-date-and-time-in-android-calendar
     */
    protected void lagreIKalender(Tur tur) {

        Long tid = tur.getAvreiseTid().toDate(TimeZone.getDefault()).getTime();
        String tittel = "Kjøretur";
        String fra = tur.getFra().getGateAdresse();
        String til = tur.getTil().getGateAdresse();
        String beskrivelse = "Kjøretur fra " + fra + " til " + til;


        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra(CalendarContract.Events.TITLE, tittel);
        intent.putExtra(CalendarContract.Events.DESCRIPTION, beskrivelse);
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, fra);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, tid);
        intent.putExtra(CalendarContract.Events.HAS_ALARM, 1);
        startActivity(intent);
    }

    /**
     * Laster settings-vinduet.
     * Hvis brukeren har en lagret shared pref id, kan det hentes informasjon fra databasen, som
     * sendes videre til fragmentet.
     * <p>
     * Henter her bruker-informasjon fra databasen ETTER at fragmentet er vist.
     * Om fragmentet først hadde blitt vist i vellykket / feil ville UI-tråden ha fryst
     * mens kallet ble utført.
     * Bruker altså ikke Bundle, men heller set-metode.
     *
     * @see InnstillingDialogFragment
     */
    private void visSettingsDialog() {
        final InnstillingDialogFragment fragment = new InnstillingDialogFragment();
        fragment.show(getFragmentManager(), "InnstillingDialogListener");

        if (hentBrukerId() != -1) {
            apiLaster.hentBruker(hentBrukerId(), new ApiListener<Person>() {
                @Override
                public void vellykket(Person result) {
                    fragment.setPerson(result);
                }

                @Override
                public void feil() {
                    visToast(getString(R.string.feil));
                }
            });
        }
    }
}
