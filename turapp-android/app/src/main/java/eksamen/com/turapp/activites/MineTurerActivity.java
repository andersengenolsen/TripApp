package eksamen.com.turapp.activites;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;

import eksamen.com.turapp.R;
import eksamen.com.turapp.adapter.TabPagerAdapter;
import eksamen.com.turapp.fragment.RecyclerListeTurer;
import eksamen.com.turapp.fragment.TurMerInfoFragment;
import eksamen.com.turapp.listener.TurMerInfoListener;
import eksamen.com.turapp.loader.ApiListener;
import eksamen.com.turapp.model.Person;
import eksamen.com.turapp.model.Tur;

/**
 * Aktivitet som viser oversikt over brukerens turer, både frem og tilbake i tid.
 * Turene presenteres i en RecyclerList.
 * Ved trykk på et element kommer kjøreruten opp i et nytt vindu.
 * <p>
 * Trykk på kanseller-knapp i RecyclerList kan føre til 2 situasjoner:
 * - Dersom brukeren har rolle "Sjåfør", avlyses hele turen. Turen slettes fra databasen,
 * og varsel sendes til alle passasjerer på SMS (dersom frem i tid).
 * - Dersom brukeren har rolle "Passasjer, slettes bruker fra passasjer-liste, og antall ledige
 * plasser økes med 1.
 *
 * @author Kandidatnummer 9
 */
public class MineTurerActivity extends BaseActivity implements
        RecyclerListeTurer.RecyclerListeListener, TurMerInfoListener {

    /**
     * Tabs
     */
    private RecyclerListeTurer passasjerFragment;
    private RecyclerListeTurer sjaforFragment;

    /**
     * Turer brukeren er involvert i.
     */
    private ArrayList<Tur> passasjerTurer, sjaforTurer;

    /**
     * Avlys-knappen er blitt trykt av turens sjåfør.
     * Turen slettes fra databasen.
     *
     * @param turId id på tur som skal slettes fra databasen.
     */
    @Override
    public void avlysTur(int turId) {
        apiLaster.slettTur(turId, new ApiListener<Tur>() {
            @Override
            public void vellykket(Tur result) {
                //TODO: SEND UT SMS
                visToast(getString(R.string.slettet_tur));
                sjaforTurer.remove(result);
                sjaforFragment.fjernTur(result);
            }

            @Override
            public void feil() {
                visToast(getString(R.string.feil));
            }
        });
    }

    /**
     * Melder brukeren AV en tur.
     *
     * @param tur turen brukeren skal meldes av fra.
     */
    @Override
    public void meldAvTur(Tur tur) {
        apiLaster.slettBrukerFraTur(hentBrukerId(), tur, new ApiListener<Tur>() {
            @Override
            public void vellykket(Tur result) {
                visToast(getString(R.string.slettet_fra_tur));
                //TODO: SLETT FRA KALENDER
                passasjerTurer.remove(result);
                passasjerFragment.fjernTur(result);
            }

            @Override
            public void feil() {
                visToast(getString(R.string.feil));
            }
        });
    }

    /**
     * Ingen implementasjon her, bruker allerede meldt på.
     *
     * @param tur
     */
    @Override
    public void meldPåTur(Tur tur) {
    }

    /**
     * Kaller api'et etter passasjerliste for en tur.
     * <p>
     * Dersom kallet er vellykket vises TurMerInfoFragment.
     *
     * @param tur
     * @see TurMerInfoFragment
     * @see eksamen.com.turapp.loader.ApiLaster#hentPassasjerListe(int, ApiListener)
     */
    @Override
    public void listeElementKlikket(final Tur tur) {
        apiLaster.hentPassasjerListe(tur.getId(), new ApiListener<ArrayList<Person>>() {
            @Override
            public void vellykket(ArrayList<Person> result) {
                TurMerInfoFragment fragment = new TurMerInfoFragment();
                tur.setPassasjerer(result);
                fragment.setTur(tur);
                // Sjekker om brukeren er eier av turen.
                if (hentBrukerId() == tur.getSjaforId())
                    fragment.setErEier(true);
                else
                    fragment.erPassasjer(true);

                fragment.show(getFragmentManager(), "TurMerInfoFragment");
            }

            @Override
            public void feil() {
                visToast(getString(R.string.passasjerliste_feil));
            }
        });
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        leggTilLayoutView(R.layout.activity_tab_layout);

        passasjerTurer = new ArrayList<>();
        sjaforTurer = new ArrayList<>();

        passasjerFragment = new RecyclerListeTurer();
        sjaforFragment = new RecyclerListeTurer();

        lagTabLayout();

        lastTurer();
    }

    /**
     * Laster brukerens turer, både passasjer og sjåfør.
     * Listene settes i hvert sitt fragment.
     * <p>
     * Sjekker først at bruker har en id lagret i shared_pref.
     *
     * @see eksamen.com.turapp.loader.ApiLaster#hentPassasjerTurer(int, ApiListener)
     * @see eksamen.com.turapp.loader.ApiLaster#hentSjaforTurer(int, ApiListener)
     */
    private void lastTurer() {

        int brukerId = hentBrukerId();

        if (brukerId == -1) {
            visToast(getString(R.string.trenger_id));
            return;
        }

        apiLaster.hentPassasjerTurer(brukerId, new ApiListener<ArrayList<Tur>>() {
            @Override
            public void vellykket(ArrayList<Tur> result) {
                passasjerTurer = result;
                passasjerFragment.setTurer(passasjerTurer);
            }

            @Override
            public void feil() {
                visToast(getString(R.string.feil));
            }
        });

        apiLaster.hentSjaforTurer(brukerId, new ApiListener<ArrayList<Tur>>() {
            @Override
            public void vellykket(ArrayList<Tur> result) {
                sjaforTurer = result;
                sjaforFragment.setTurer(sjaforTurer);
            }

            @Override
            public void feil() {
                visToast(getString(R.string.feil));
            }
        });
    }

    /**
     * Setter opp TabLayout for subklassene.
     * Tab-layout er felles for beggge fragmentene
     */
    private void lagTabLayout() {
        // TabLayout-objekt, hentes fra layouten.
        final TabLayout tabLayout = findViewById(R.id.tablayout);

        // Navngir alle tabs.
        tabLayout.addTab(tabLayout.newTab().setText(R.string.sjafor));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.passasjer));

        // Alle tabs skal fylle hele skjermen
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Benytter Adapter til å administrere fragmentene (tabs'ene).
        TabPagerAdapter adapter = new TabPagerAdapter(getSupportFragmentManager(),
                sjaforFragment, passasjerFragment);

        // Pager som tar inn adapteren
        final ViewPager viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(adapter);

        // Setter EventListeners
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // Håndterer hva som skal gjøres når en Tab velges av bruker.
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            /**
             * Metoden setter rett fragment ifht. hvilken tab som er valgt.
             *
             * @param tab valgt tab
             */
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Setter rett fragment ifht. hvilken tab som er valgt.
                viewPager.setCurrentItem(tab.getPosition());
            }


            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }

        });
    }
}
