package eksamen.com.turapp.listener;

import com.google.android.gms.maps.model.LatLng;

/**
 * Interface som avfyres når bruker legger til / sletter steder via GoogleMapsFragment.
 *
 * @author Kandidatnummer 9
 */
public interface GoogleMapsListener {

    /**
     * Slette markør.
     *
     * @param type avreise / destinasjon
     */
    void slettMarkor(String type);

    /**
     * Legger til markør
     *
     * @param pos  posisjon
     * @param type avreise / destinasjon
     */
    void leggTilMarkor(LatLng pos, String adresse, String type);

}
