package eksamen.com.turapp.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Representerer et Sted, med bredde- og lengdegrad.
 * Kan også ha tilhørende adresse.
 * <p>
 *
 * @author Kandidatnummer 9
 */
public class Sted {

    private LatLng posisjon;
    private String gateAdresse;

    public Sted() {

    }

    public Sted(LatLng posisjon, String gateAdresse) {
        this.posisjon = posisjon;
        this.gateAdresse = gateAdresse;
    }

    public LatLng getPosisjon() {
        return posisjon;
    }

    public void setPosisjon(LatLng posisjon) {
        this.posisjon = posisjon;
    }

    public String getGateAdresse() {
        return gateAdresse;
    }

    public void setGateAdresse(String gateAdresse) {
        this.gateAdresse = gateAdresse;
    }
}
