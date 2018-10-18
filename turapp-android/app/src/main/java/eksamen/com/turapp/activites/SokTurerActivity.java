package eksamen.com.turapp.activites;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Calendar;

import eksamen.com.turapp.R;
import eksamen.com.turapp.fragment.RecyclerListeTurer;
import eksamen.com.turapp.fragment.TurMerInfoFragment;
import eksamen.com.turapp.listener.TurMerInfoListener;
import eksamen.com.turapp.loader.ApiListener;
import eksamen.com.turapp.model.Person;
import eksamen.com.turapp.model.Tur;

/**
 * Aktivitet for å søke etter turer.
 * Subklasse av TurActivity, mye av logikken håndteres der.
 *
 * @author Kandidatnummer 9
 * @see TurActivity
 */
public class SokTurerActivity extends TurActivity
        implements RecyclerListeTurer.RecyclerListeListener, TurMerInfoListener {

    /**
     * Avreise og destinasjon adresse
     */
    private TextView destinasjonAdr;

    /**
     * Liste med turer i samme by, og med samme dato
     */
    private RecyclerListeTurer turListeFragment;

    /**
     * Liste med turer som skal vises i fragmentet.
     */
    private ArrayList<Tur> turListe;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        leggTilLayoutView(R.layout.activity_sok_tur);

        settOppElementer();

        if (til.getGateAdresse() != null)
            destinasjonAdr.setText(til.getGateAdresse());

        lastFavoritter();
    }

    /**
     * Starter TurMerInfoFragment.
     *
     * @param tur
     * @see TurMerInfoFragment
     */
    @Override
    public void listeElementKlikket(final Tur tur) {
        apiLaster.hentPassasjerListe(tur.getId(), new ApiListener<ArrayList<Person>>() {
            @Override
            public void vellykket(ArrayList<Person> result) {
                TurMerInfoFragment fragment = new TurMerInfoFragment();
                tur.setPassasjerer(result);
                fragment.setTur(tur);
                // Sjekker om brukeren er eier/passasjer av turen.
                if (hentBrukerId() == tur.getSjaforId())
                    fragment.setErEier(true);
                else {
                    for (Person p : result)
                        if (p.getId() == hentBrukerId())
                            fragment.erPassasjer(true);
                }
                fragment.show(getFragmentManager(), "TurMerInfoFragment");
            }

            @Override
            public void feil() {
                visToast(getString(R.string.passasjerliste_feil));
            }
        });
    }

    /**
     * Avlyser en tur
     *
     * @param turId
     */
    @Override
    public void avlysTur(int turId) {
        apiLaster.slettTur(turId, new ApiListener<Tur>() {
            @Override
            public void vellykket(Tur result) {

                // TODO: SENDE SMS

                visToast(getString(R.string.slettet_tur));
                turListe.remove(result);
                turListeFragment.setTurer(turListe);
            }

            @Override
            public void feil() {
                visToast(getString(R.string.feil));
            }
        });
    }


    /**
     * Melder bruker av en tur
     *
     * @param tur
     */
    @Override
    public void meldAvTur(Tur tur) {
        apiLaster.slettBrukerFraTur(hentBrukerId(), tur, new ApiListener<Tur>() {
            @Override
            public void vellykket(Tur result) {
                visToast(getString(R.string.slettet_fra_tur));
                turListe.remove(result);
                turListeFragment.setTurer(turListe);
                //TODO: SLETT FRA KALENDER
            }

            @Override
            public void feil() {
                visToast(getString(R.string.feil));
            }
        });
    }

    /**
     * Melder bruker på tur
     *
     * @param tur
     * @see #meldBrukerPåTur(Tur, Person)
     */
    @Override
    public void meldPåTur(final Tur tur) {
        int id = hentBrukerId();

        if (id == -1) {
            visToast(getString(R.string.trenger_id));
            return;
        }

        apiLaster.hentBruker(id, new ApiListener<Person>() {
            @Override
            public void vellykket(Person result) {
                meldBrukerPåTur(tur, result);
                lagreIKalender(tur);
            }

            @Override
            public void feil() {
                visToast(getString(R.string.fant_ikke_bruker));
            }
        });
    }

    /**
     * Melder bruker på tur
     *
     * @param tur
     * @param person
     */
    private void meldBrukerPåTur(final Tur tur, Person person) {
        apiLaster.meldBrukerPaTur(tur, person, new ApiListener<Person>() {
            @Override
            public void vellykket(Person result) {
                visToast(getString(R.string.meldt_pa));
                turListe.remove(tur);
                turListeFragment.setTurer(turListe);
            }

            @Override
            public void feil() {
                visToast(getString(R.string.feil));
            }
        });
    }

    /**
     * Henter eventuelle favorittsteder fra shared prefs.
     * Overser varsel om nullpointer, initialiseres alltid i supers onCreate.
     */
    private void lastFavoritter() {
        if (harFavorittSted(SHARED_PREF_DESTINASJON)) {
            til = getFavorittSted(SHARED_PREF_DESTINASJON);
            destinasjonAdr.setText(til.getGateAdresse());
        }
    }

    /**
     * Håndterer resultat fra PlacePicker.
     * Mye av logikken håndteres i superklassen.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == DESTINASJON_KODE)
                destinasjonAdr.setText(til.getGateAdresse());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.destinasjon_kart_btn:
                startKartAktivitet(DESTINASJON_KODE);
                break;
            case R.id.sok_turer_btn:
                sokTurer();
                break;
        }
    }

    /**
     * Initierer layout-elementer
     *
     * @see super#kunFremITid(DatePicker)
     */
    private void settOppElementer() {
        destinasjonAdr = findViewById(R.id.destinasjon_adr_tv);

        datoVelger = findViewById(R.id.dato_velger);
        kunFremITid(datoVelger);

        ArrayList<View> knapper = (findViewById(R.id.main_container)).getTouchables();
        for (View v : knapper) {
            if (v instanceof Button)
                v.setOnClickListener(this);
        }
    }

    /**
     * Foreløpig søkes det kun med fra-adresse.
     * Må derfor override super-metode.
     *
     * @return true hvis lovlig input
     */
    @Override
    protected boolean lovligInput() {
        return til.getGateAdresse() != null;
    }

    /**
     * Søker etter turer.
     * Turene lastes inn i et fragment som vises rett under "Søk-knappen".
     * <p>
     * Dersom listen er tom (det ikke er noen tur), "vises" allikevel fragment. Dette gjøres
     * da det er mulig at bruker har søkt tidligere, og de turene som evnt ligger i listen må
     * fjernes ved nytt søk.
     * <p>
     * Listen viser for øyeblikket turer som bruker er sjåfør / passasjer på også. Dette burde
     * selvfølgelig være annerledes. Rakk ikke annet. Har i det minste lagt inn slik at bruker kan
     * "melde av" og "avlyse" om det blir trykket på turen i listen.
     */
    private void sokTurer() {
        if (lovligInput()) {
            LocalDateTime tidspunkt = generererLocalDateTime(datoVelger);

            apiLaster.hentTurerIOmradet(til.getGateAdresse(), tidspunkt,
                    new ApiListener<ArrayList<Tur>>() {
                        @Override
                        public void vellykket(ArrayList<Tur> result) {

                            if (result.isEmpty())
                                visToast(getString(R.string.fant_ingen_turer));

                            turListeFragment = new RecyclerListeTurer();
                            turListe = result;
                            turListeFragment.setTurer(turListe);

                            getSupportFragmentManager()
                                    .beginTransaction().
                                    replace(R.id.fragment_kontainer, turListeFragment)
                                    .commit();
                        }

                        @Override
                        public void feil() {
                            visToast(getString(R.string.feil));
                        }
                    });
        } else
            visToast(getString(R.string.ugyldige_verdier));
    }

    /**
     * Konverterer dato-verdier i datovelger til LocalDateTime.
     *
     * @param datoVelger
     * @return LocalDateTime (joda)
     */
    private LocalDateTime generererLocalDateTime(DatePicker datoVelger) {
        Calendar kalender = Calendar.getInstance();
        kalender.setTime(super.generererDateObjekt(datoVelger));

        LocalDateTime ldt = LocalDateTime.fromCalendarFields(kalender);

        return formatter.parseLocalDateTime(formatter.print(ldt));
    }
}
