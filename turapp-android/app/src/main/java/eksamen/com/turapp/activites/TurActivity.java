package eksamen.com.turapp.activites;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.DatePicker;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Calendar;
import java.util.Date;

import eksamen.com.turapp.R;
import eksamen.com.turapp.model.Sted;

/**
 * Abstrakt super-klasse for aktivitetene som registrerer ny tur, og hvor man kan søke etter tur.
 * BRUKT org.jodaTime da java.util.LocalDateTime ikke var tilgjengelig
 *
 * @author Kandidatnummer 9
 */
public abstract class TurActivity extends BaseActivity implements View.OnClickListener {

    /**
     * Til savedinstancestate
     */
    private final static String AVREISE_POS_EKSTRA = "avreise_pos";
    private final static String DESTINASJON_POS_EKSTRA = "destinasjon_pos";
    private final static String AVREISE_ADR_EKSTRA = "avreise";
    private final static String DESTINASJON_ADR_EKSTRA = "destinasjon";

    /**
     * Til intent som sendes til PlacePicker
     */
    protected final static int AVREISE_KODE = 1;
    protected final static int DESTINASJON_KODE = 2;

    /**
     * Datovelger
     */
    protected DatePicker datoVelger;

    /**
     * Fra og til
     */
    protected Sted fra, til;

    /**
     * Variabel for dette tidspunktet. Brukt for å hindre at bruker kan legge til / søke etter
     * turer tilbake i tid.
     */
    protected final static LocalDateTime NA_TID = LocalDateTime.now();
    /**
     * Format på LocalDateTime. yyyy-MM-dd'T'hh:mm
     */
    protected final static DateTimeFormatter formatter = ISODateTimeFormat.dateHourMinute();

    /**
     * Gjenoppretter layout.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fra = new Sted();
        til = new Sted();

        if (savedInstanceState != null) {

            // Henter posisjoner dersom lagret.
            if (savedInstanceState.getParcelable(AVREISE_POS_EKSTRA) != null)
                fra.setPosisjon((LatLng) savedInstanceState.getParcelable(AVREISE_POS_EKSTRA));

            if (savedInstanceState.getParcelable(DESTINASJON_POS_EKSTRA) != null)
                til.setPosisjon((LatLng) savedInstanceState.getParcelable(DESTINASJON_POS_EKSTRA));

            // Henter adresse dersom lagret
            if (savedInstanceState.getString(AVREISE_ADR_EKSTRA) != null)
                fra.setGateAdresse(savedInstanceState.getString(AVREISE_ADR_EKSTRA));

            if (savedInstanceState.getString(DESTINASJON_ADR_EKSTRA) != null)
                til.setGateAdresse(savedInstanceState.getString(DESTINASJON_ADR_EKSTRA));

        }
    }

    /**
     * Mottar avreise / destinasjon fra PlacePicker.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == AVREISE_KODE) {
                Place avreiseSted = PlacePicker.getPlace(this, data);

                if (avreiseSted != null) {
                    if (avreiseSted.getAddress() != null)
                        fra.setGateAdresse(avreiseSted.getAddress().toString());
                    fra.setPosisjon(avreiseSted.getLatLng());
                }

            } else if (requestCode == DESTINASJON_KODE) {
                Place destinasjon = PlacePicker.getPlace(this, data);

                if (destinasjon != null) {
                    if (destinasjon.getAddress() != null)
                        til.setGateAdresse(destinasjon.getAddress().toString());
                    til.setPosisjon(destinasjon.getLatLng());
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Lagrer verdier.
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (fra.getPosisjon() != null)
            outState.putParcelable(AVREISE_POS_EKSTRA, fra.getPosisjon());
        if (til.getPosisjon() != null)
            outState.putParcelable(DESTINASJON_POS_EKSTRA, til.getPosisjon());
        if (fra.getGateAdresse() != null)
            outState.putString(AVREISE_ADR_EKSTRA, fra.getGateAdresse());
        if (til.getGateAdresse() != null)
            outState.putString(DESTINASJON_ADR_EKSTRA, til.getGateAdresse());
    }

    /**
     * Starter PlacePicker. Destinasjon som returneres håndteres i onActivityResult.
     *
     * @param kode request-kode
     */
    protected void startKartAktivitet(int kode) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), kode);
        } catch (GooglePlayServicesNotAvailableException |
                GooglePlayServicesRepairableException err) {
            visToast(getString(R.string.kart_feil));
        }
    }

    /**
     * Genererer et Date-objekt fra en DatePicker.
     * <p>
     * Tiden er på formatet: yyyy-MM-dd'T'hh:mm
     *
     * @param dato DatePicker
     * @return dato
     */
    protected Date generererDateObjekt(DatePicker dato) {
        Calendar kalender = Calendar.getInstance();
        kalender.set(dato.getYear(), dato.getMonth(), dato.getDayOfMonth(), 0, 0);
        return kalender.getTime();
    }

    /**
     * Sjekker at input er lovlig. Spesialiseres mer i subklassene.
     * Sjekker mer spesifikt at både avreise og destinasjon er satt.
     *
     * @return true hvis gyldig
     */
    protected boolean lovligInput() {
        return fra != null && til != null;
    }

    /**
     * Kalles i subaktiviteter dersom dato kun kan settes frem i tid.
     *
     * @param datoVelger
     */
    protected void kunFremITid(DatePicker datoVelger) {
        datoVelger.setMinDate(System.currentTimeMillis() - 1000);
    }

}
