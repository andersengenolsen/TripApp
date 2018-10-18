package eksamen.com.turapp.listener;

import eksamen.com.turapp.model.Tur;

/**
 * Interface for kommunikasjon mellom TurMerInfoFragment og kallende aktiviteter.
 *
 * @author Kandidatnummer 9
 * @see eksamen.com.turapp.fragment.TurMerInfoFragment
 */
public interface TurMerInfoListener {

    void avlysTur(int turId);

    void meldAvTur(Tur tur);

    void meldPåTur(Tur tur);

}
