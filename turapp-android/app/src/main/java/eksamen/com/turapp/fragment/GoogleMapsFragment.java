package eksamen.com.turapp.fragment;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import eksamen.com.turapp.R;
import eksamen.com.turapp.listener.GoogleMapsListener;

/**
 * Fragment som inneholder en MapView.
 * Google Maps er benyttet, deler av koden er hentet fra ekstern kilde.
 * <p>
 * Brukeren kan kun velge ETT sted på kartet, som da vil være favoritt avreise / destinasjon
 * <p>
 * Callback til kallende aktivitetet gjøres med GoogleMapsListener.
 *
 * @author Kandidatnummer 9
 * @link https://google-developer-training.gitbooks.io/android-developer-advanced-course-practicals/unit-4-add-geo-features-to-your-apps/lesson-9-mapping/9-1-p-add-a-google-map-to-your-app/9-1-p-add-a-google-map-to-your-app.html
 * @see eksamen.com.turapp.listener.GoogleMapsListener
 */
public class GoogleMapsFragment extends Fragment implements OnMapReadyCallback {

    /**
     * Kart i layout
     */
    private MapView kart;
    /**
     * Google maps
     */
    private GoogleMap gKart;

    /**
     * Latitude og longitude posisjon.
     */
    private LatLng posisjon;
    /**
     * Posisjon til USN i bø
     */
    private final double USN_POS_LONG = 59.409202, USN_POS_LAT = 9.059619;

    /**
     * Definerer hvor mye det skal zoomes.
     * 1: Verden
     * 5: Kontinent
     * 10: Byer
     * 15: Gater
     * 20: Bygningsmasse
     */
    private float zoom = 15;

    /**
     * Interface-callback
     */
    private GoogleMapsListener listener;

    /**
     * Type kart (avreise / destinasjon)
     */
    private String type;

    /**
     * Flagg for om det er en markør på kartet eller ei
     */
    private boolean harFavoritt = false;

    /**
     * Sjekker at aktivitet som benytter fragmentet har implementert LoginDialogListener.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            listener = (GoogleMapsListener) getActivity();
        } catch (ClassCastException err) {
            throw new ClassCastException(getActivity().getClass().getSimpleName()
                    + " må implementere GoogleMapsListener!");
        }
    }


    /**
     * Initierer kartet, og laster det asynkront
     *
     * @see #onMapReady(GoogleMap)
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rot = inflater.inflate(R.layout.fragment_google_maps, container, false);
        kart = rot.findViewById(R.id.kart_view);
        kart.onCreate(savedInstanceState);
        kart.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        kart.getMapAsync(this);

        return rot;
    }

    /**
     * Klargjør kartet, når ferdig lastet.
     * Deler av metoden er autogenerert, laget en ny Maps Activitiy.
     * <p>
     * Metoden henter brukerens nåværende posisjon.
     * Dersom denne ikke er tilgjengelig, settes posisjon til USN i Bø.
     * <p>
     * Favorittsteder som tidligere er lagt inn vil også vises. Vises altså dersom
     * kallende aktivitet har kalt #setPosisjonListe.
     *
     * @see #setPosisjon(LatLng)
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gKart = googleMap;
        gKart.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (posisjon == null)
            posisjon = new LatLng(USN_POS_LAT, USN_POS_LONG);
        setPosisjon(posisjon);

        // Setter listeners
        settMarkorListener(gKart);
        setMapClick(gKart);
        setPoiListener(gKart);

        if (harFavoritt)
            gKart.addMarker(new MarkerOptions().position(posisjon));
    }

    /**
     * Flagg for om brukeren har en favoritt eller ei.
     *
     * @param harFavoritt
     */
    public void setHarFavoritt(boolean harFavoritt) {
        this.harFavoritt = harFavoritt;
    }

    public boolean harFavoritt() {
        return harFavoritt;
    }

    /**
     * Flytter kamera til gitt posisjon.
     *
     * @param posisjon
     */
    public void setPosisjon(LatLng posisjon) {
        this.posisjon = posisjon;
        if (gKart != null) {
            gKart.moveCamera(CameraUpdateFactory.newLatLngZoom(posisjon, zoom));
        }
    }

    /**
     * Type kart, og tilhørende shared prefs.
     * Enten SHARED_PREF_AVREISE eller SHARED_PREF_DESTINASJON
     *
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Setter markør basert på hvor bruker klikker på kartet.
     * Hentet fra kilde nevnt i klasse-kommentar. (Endret til klikk, istedenfor long-click)
     * <p>
     * NB! MarkerListener må settes på nytt, da kartet er forandret.
     *
     * @param map kartet
     * @see #settMarkorListener(GoogleMap)
     */
    private void setMapClick(final GoogleMap map) {
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (harFavoritt) {
                    Toast.makeText(getActivity(), R.string.kun_ett_sted,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                map.addMarker(new MarkerOptions().position(latLng));
                listener.leggTilMarkor(latLng, hentUtAdresse(latLng), type);
                settMarkorListener(map);
                harFavoritt = true;
            }
        });
    }

    /**
     * Henter ut en adresse fra LatLng
     * <p>
     * KILDE: https://stackoverflow.com/questions/27244763/how-to-get-address-from-latitude-and-longitude-in-android
     *
     * @param latLng
     * @return
     */
    private String hentUtAdresse(LatLng latLng) {
        Geocoder geocoder;
        List<Address> adresser;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            adresser = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (adresser.size() > 0) {
                return adresser.get(0).getAddressLine(0);
            }
        } catch (IOException err) {
            return "Ukjent adresse";
        }
        return "Ukjent adresse";
    }

    /**
     * Viser "point of interest" etter bruker har markert ett punkt på kartet.
     * Hentet fra kilde nevnt i klasse-kommentar.
     *
     * @param map kartet
     */
    private void setPoiListener(final GoogleMap map) {
        map.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest poi) {
                Marker poiMarker = gKart.addMarker(new MarkerOptions()
                        .position(poi.latLng)
                        .title(poi.name));
                poiMarker.showInfoWindow();
            }
        });
    }

    /**
     * Fjerner markør dersom den trykkes på.
     * Markøren slettes både fra kart og liste.
     *
     * @param map kartet
     */
    private void settMarkorListener(final GoogleMap map) {
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Sletter markør fra liste og kart.
                listener.slettMarkor(type);
                marker.remove();
                harFavoritt = false;
                return true;
            }
        });
    }
}
