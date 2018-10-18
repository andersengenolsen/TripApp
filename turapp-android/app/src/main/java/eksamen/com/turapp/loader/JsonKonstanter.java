package eksamen.com.turapp.loader;

/**
 * Instansløs klasse.
 * JSON-parametere samlet på ett sted.
 * Package-private, trengs kun i loader-klasser.
 *
 * @author Kandidatnummer 9
 */

class JsonKonstanter {

    private JsonKonstanter() {

    }

    /**
     * JSON-parametere for bruker
     */
    final static String JSON_ID = "id";
    final static String JSON_NAVN = "name";
    final static String JSON_MOBIL = "phone";
    final static String JSON_EPOST = "email";

    /**
     * JSON-parametere for tur
     */
    final static String JSON_FRA_DESTINASJON = "fromDestination";
    final static String JSON_TIL_DESTINASJON = "toDestination";
    final static String JSON_TUR_EIER = "owner";
    final static String JSON_FRA_LONGITUDE = "longitudeFrom";
    final static String JSON_FRA_LATITUDE = "latitudeFrom";
    final static String JSON_TIL_LONGITUDE = "longitudeTo";
    final static String JSON_TIL_LATITUDE = "latitudeTo";
    final static String JSON_LEDIGE_PLASSER = "availableSeats";
    final static String JSON_AVREISE_TID = "departureTime";
    final static String JSON_ADDRESSE = "address";
}
