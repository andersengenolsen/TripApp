package eksamen.com.turapp.loader;

/**
 * Interface som avfyres når lasting / skriving til api er ferdig utført.
 * <p>
 * Muliggjør at kallene kan utføres asynkront. Callback-metodene legges altså i aktivitetene.
 *
 * @author Kandidatnummer 9
 */
public interface ApiListener<AnyType> {

    /**
     * Returnerer resultat fra kall til API.
     * Bør benyttes ved vellykket kall.
     *
     * @param result resultat fra kall
     */
    void vellykket(AnyType result);

    /**
     * Returnerer resultat fra kall til API.
     * Bør benyttes ved feil.
     */
    void feil();
}
