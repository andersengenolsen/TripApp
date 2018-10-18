package eksamen.com.turapp.activites;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;

import com.google.android.gms.maps.model.LatLng;

import eksamen.com.turapp.R;
import eksamen.com.turapp.adapter.TabPagerAdapter;
import eksamen.com.turapp.fragment.GoogleMapsFragment;
import eksamen.com.turapp.listener.GoogleMapsListener;
import eksamen.com.turapp.model.Sted;

/**
 * Aktivitet hvor man kan velge favoritt for både avreise og destinasjon.
 * <p>
 * Aktiviteten inneholder 2 tabs, hvor hver av tabs'ene inneholder et GoogleMapsFragment.
 * Kommunikasjon mellom fragmentene og aktiviteten gjøres via GoogleMapsListener.
 * <p>
 * Favoritter lastes fra database i #lastFavoritter().
 * Favoritter skrives til database når aktivitet pauses, i #onPause().
 *
 *
 * @author Kandidatnummer 9
 * @see GoogleMapsFragment
 * @see GoogleMapsListener
 */
public class MineStederActivity extends BaseActivity implements GoogleMapsListener {

    /**
     * Kode for tillatelser
     */
    private final static int TILLATELSER_KODE = 1;
    /**
     * Posisjon til USN i bø
     */
    private final double USN_POS_LONG = 59.409202, USN_POS_LAT = 9.059619;

    /**
     * Tillatelser som trengs
     */
    private final String[] TILLATELSER_ARR = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};

    /**
     * Lokasjon
     */
    private LocationManager lokasjonManager;

    /**
     * Flagg om bruker godtar eller ei.
     */
    private boolean posisjonTillat;

    /**
     * Tabs
     */
    private GoogleMapsFragment avreiseFragment, destinasjonFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        leggTilLayoutView(R.layout.activity_tab_layout);

        avreiseFragment = new GoogleMapsFragment();
        destinasjonFragment = new GoogleMapsFragment();

        // Markerer til fragmentene dersom bruker har favoritt lagret i shared prefs
        if (harFavorittSted(SHARED_PREF_DESTINASJON)) {
            destinasjonFragment.setHarFavoritt(true);
        }
        if (harFavorittSted(SHARED_PREF_AVREISE)) {
            avreiseFragment.setHarFavoritt(true);
        }

        avreiseFragment.setType(SHARED_PREF_AVREISE);
        destinasjonFragment.setType(SHARED_PREF_DESTINASJON);

        lagTabLayout();

        lokasjonManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        // Up navigation, pil tilbake til forrige Aktivitet.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Tillatelser
        // Kart blir uansett vist, kalles evnt i onRequestPermissionsResult.
        posisjonTillat = hasPermissions(this, TILLATELSER_ARR);
        if (!posisjonTillat) {
            ActivityCompat.requestPermissions(this, TILLATELSER_ARR, TILLATELSER_KODE);
        } else {
            visKart(SHARED_PREF_AVREISE, avreiseFragment);
            visKart(SHARED_PREF_DESTINASJON, destinasjonFragment);
        }
    }

    /**
     * Sletter adresseinformasjon fra rett shared prefs.
     *
     * @param type avreise / destinasjon
     */
    @Override
    public void slettMarkor(String type) {
        if (!type.equals(SHARED_PREF_AVREISE) && !type.equals(SHARED_PREF_DESTINASJON))
            throw new IllegalArgumentException("Type må være SHARED_PREF_AVREISE" +
                    "eller SHARED_PREF_DESTINASJON");

        slettFavorittSted(type);
        visToast(getString(R.string.slettet_sted));
    }

    /**
     * Legger til markør i rett liste.
     *
     * @param pos  posisjonAvreise
     * @param type avreise / destinasjon
     */
    @Override
    public void leggTilMarkor(LatLng pos, String adresse, String type) {
        if (!type.equals(SHARED_PREF_AVREISE) && !type.equals(SHARED_PREF_DESTINASJON))
            throw new IllegalArgumentException("Type må være SHARED_PREF_AVREISE" +
                    "eller SHARED_PREF_DESTINASJON");

        endreFavorittSted(type, new Sted(pos, adresse));
        visToast(getString(R.string.nytt_sted) + adresse);
    }

    /**
     * Mottar resultat etter forespørsel om tillatelser.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == TILLATELSER_KODE) {
            posisjonTillat = hasPermissions(this, TILLATELSER_ARR);
            visKart(SHARED_PREF_DESTINASJON, destinasjonFragment);
            visKart(SHARED_PREF_AVREISE, avreiseFragment);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Viser kart med gjeldende posisjonAvreise dersom tillatelser er satt,
     * og bruker ikke har lagret noe i SharedPref
     * Om ikke, settes posisjonAvreise til Usn i Bø.
     */
    private void visKart(String type, GoogleMapsFragment fragment) {
        LatLng posisjon;

        if (fragment.harFavoritt()) {
            posisjon = getFavorittSted(type).getPosisjon();
        } else if (posisjonTillat)
            posisjon = finnPosisjon();
        else
            posisjon = new LatLng(USN_POS_LONG, USN_POS_LAT);

        fragment.setPosisjon(posisjon);
    }

    /**
     * Finner siste kjente posisjonAvreise.
     *
     * @return LatLng
     */
    private LatLng finnPosisjon() {
        boolean gps = lokasjonManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean nettverk = lokasjonManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Location location = null;
        if (!(gps || nettverk))
            visToast(getString(R.string.fant_ikke_pos));
        else {
            try {
                if (nettverk) {
                    location = lokasjonManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                if (gps) {
                    location = lokasjonManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            } catch (SecurityException err) {
                ActivityCompat.requestPermissions(this, TILLATELSER_ARR, 1);
            }
        }

        if (location != null)
            return new LatLng(location.getLatitude(), location.getLongitude());
        return null;
    }

    /**
     * KILDE: https://stackoverflow.com/questions/34342816/android-6-0-multiple-permissions
     *
     * @param context
     * @param permissions
     * @return
     */
    private boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Setter opp TabLayout for subklassene.
     * Tab-layout er felles for beggge fragmentene
     */
    private void lagTabLayout() {
        // TabLayout-objekt, hentes fra layouten.
        final TabLayout tabLayout = findViewById(R.id.tablayout);

        // Navngir alle tabs.
        tabLayout.addTab(tabLayout.newTab().setText(R.string.avreise));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.destinasjon));

        // Alle tabs skal fylle hele skjermen
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Benytter Adapter til å administrere fragmentene (tabs'ene).
        TabPagerAdapter adapter = new TabPagerAdapter(getSupportFragmentManager(),
                avreiseFragment, destinasjonFragment);

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
