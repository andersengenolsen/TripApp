package eksamen.com.turapp.activites;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import org.joda.time.LocalDateTime;

import java.util.Calendar;
import java.util.Date;

import eksamen.com.turapp.R;
import eksamen.com.turapp.loader.ApiListener;
import eksamen.com.turapp.model.Person;
import eksamen.com.turapp.model.Tur;

/**
 * Aktivitet for å registrere en ny tur.
 * Subklasse av TurActivity, mye av logikken håndteres der.
 *
 * @author Kandidatnummer 9
 * @see TurActivity
 */
public class NyTurActivity extends TurActivity {

    /**
     * Til saveinstancestate
     */
    private final static String DATO_EKSTRA = "dato";

    /**
     * Timevelger
     */
    private TimePicker tidsVelger;
    /**
     * Knapper
     */
    private Button avreiseBtn, destinasjonBtn;

    /**
     * Ledige plasser edittext
     */
    private EditText ledigePlasserEt;
    /**
     * Ledige plasser.
     */
    private int ledigePlasser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        leggTilLayoutView(R.layout.activity_ny_tur);

        settOppElementer();

        if (harFavorittSted(SHARED_PREF_AVREISE)) {
            fra = getFavorittSted(SHARED_PREF_AVREISE);
            avreiseBtn.setText(fra.getGateAdresse());
        }

        if (harFavorittSted(SHARED_PREF_DESTINASJON)) {
            til = getFavorittSted(SHARED_PREF_DESTINASJON);
            destinasjonBtn.setText(til.getGateAdresse());
        }

        // Bevarer verdier ved layout-endring
        if (savedInstanceState != null) {

            // Setter dato og tid.
            Date dato = new Date(savedInstanceState.getLong(DATO_EKSTRA));
            Calendar kalender = Calendar.getInstance();
            kalender.setTime(dato);
            datoVelger.updateDate(kalender.get(Calendar.YEAR),
                    kalender.get(Calendar.MONTH),
                    kalender.get(Calendar.DAY_OF_MONTH));
            tidsVelger.setHour(kalender.get(Calendar.HOUR));
            tidsVelger.setMinute(kalender.get(Calendar.MINUTE));

            // Setter verdier i Buttons
            if (fra.getGateAdresse() != null)
                avreiseBtn.setText(fra.getGateAdresse());
            if (til.getGateAdresse() != null)
                destinasjonBtn.setText(fra.getGateAdresse());
        }
    }

    /**
     * Lagrer inntastede verdier.
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        LocalDateTime dato = generererDatoObjekt(datoVelger, tidsVelger);
        outState.putLong(DATO_EKSTRA, dato.toDate().getTime());
    }

    /**
     * Håndterer klikk på "Registrer reise"-knapp.
     * Sjekker at input i alle felter er gyldig, og lagrer i tur-databasen.
     *
     * @param v
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.registrer_reise_btn:
                registrerReise();
                break;
            case R.id.avreise_btn:
                startKartAktivitet(AVREISE_KODE);
                break;
            case R.id.destinasjon_btn:
                startKartAktivitet(DESTINASJON_KODE);
                break;
        }
    }

    /**
     * Sjekker at en reise er gyldig (frem i tid, antall passasjer),
     * og lagrer deretter reisen i databasen.
     *
     * @see #lovligInput(LocalDateTime)
     */
    private void registrerReise() {
        final LocalDateTime avreise = generererDatoObjekt(datoVelger, tidsVelger);

        if (!lovligInput(avreise)) {
            visToast(getString(R.string.ugyldige_verdier));
            return;
        }

        // Sjekker at brukeren har en bruker id. Vis ikke må denne settes via innstillinger.
        int id = hentBrukerId();
        if (id == -1) {
            visToast(getString(R.string.trenger_id));
            return;
        }

        // Henter all bruker-info, setter nåværende bruker som sjåfør.
        apiLaster.hentBruker(id, new ApiListener<Person>() {
            @Override
            public void vellykket(Person result) {
                Tur tur = new Tur.TurBuilder()
                        .sjafor(result)
                        .fra(fra)
                        .til(til)
                        .ledigePlasser(ledigePlasser)
                        .avreiseTid(avreise)
                        .build();

                skrivTurTilDatabase(tur);
            }

            @Override
            public void feil() {
                visToast(getString(R.string.fant_ikke_bruker));
            }
        });
    }

    /**
     * Skriver tur-objekt til database.
     * Kalles kun fra vellykket for i registrerReise. Dette tilsier at brukeren er funnet i
     * databasen.
     *
     * @param tur turobjekt
     */
    private void skrivTurTilDatabase(Tur tur) {
        apiLaster.registrerNyTur(tur, new ApiListener<Tur>() {
            @Override
            public void vellykket(Tur result) {
                visToast(getString(R.string.ny_tur));
                lagreIKalender(result);
            }

            @Override
            public void feil() {
                visToast(getString(R.string.feil));
            }
        });

        finish();
    }

    /**
     * Oppdaterer verdier i knappene.
     * Se super-metoden i TurActivity!
     *
     * @param requestCode
     * @param resultCode
     * @param data
     * @see super#onActivityResult(int, int, Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == AVREISE_KODE)
                avreiseBtn.setText(fra.getGateAdresse());
            else if (requestCode == DESTINASJON_KODE)
                destinasjonBtn.setText(til.getGateAdresse());
        }
    }

    /**
     * Sjekker at input i alle felter er gyldig.
     * Gyldig tilsvarer at ledige plasser er >0 og <= 10,
     * samt at avreisedato og tidspunkt er frem i tid.
     *
     * @return true hvis gyldig
     * @see super#lovligInput()
     */
    private boolean lovligInput(LocalDateTime valgtTid) {

        try {
            ledigePlasser = Integer.parseInt(ledigePlasserEt.getText().toString());
            if (ledigePlasser <= 0 || ledigePlasser > 10)
                return false;
        } catch (NumberFormatException err) {
            return false;
        }

        return NA_TID.compareTo(valgtTid) < 0 && super.lovligInput();
    }

    /**
     * Initierer layout-elementer
     */
    private void settOppElementer() {
        ledigePlasserEt = findViewById(R.id.ledige_plasser);
        tidsVelger = findViewById(R.id.tid_velger);
        tidsVelger.setIs24HourView(true);
        datoVelger = findViewById(R.id.dato_velger);

        kunFremITid(datoVelger);

        findViewById(R.id.registrer_reise_btn).setOnClickListener(this);
        avreiseBtn = findViewById(R.id.avreise_btn);
        avreiseBtn.setOnClickListener(this);
        destinasjonBtn = findViewById(R.id.destinasjon_btn);
        destinasjonBtn.setOnClickListener(this);
    }

    /**
     * Genererer et Date-objekt fra verdier i DatePicker og TimePicker.
     * <p>
     * Kaller super-metode for å få dato, og legger til tid selv.
     *
     * @param dato DatePicker
     * @param tid  TimePicker
     * @return Date-objekt
     */
    private LocalDateTime generererDatoObjekt(DatePicker dato, TimePicker tid) {

        Calendar kalender = Calendar.getInstance();
        kalender.setTime(super.generererDateObjekt(dato));
        kalender.add(Calendar.HOUR, tid.getHour());
        kalender.add(Calendar.MINUTE, tid.getMinute());

        LocalDateTime ldt = LocalDateTime.fromCalendarFields(kalender);

        return formatter.parseLocalDateTime(formatter.print(ldt));
    }

}
