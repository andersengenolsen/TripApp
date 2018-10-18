package eksamen.com.turapp.loader;

/**
 * Interface som avfyres dersom en feil har inntruffet ved lesing av JSON.
 */
interface JsonListener {
    void onError(String err);
}
