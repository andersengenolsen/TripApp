package eksamen.com.turapp.model;

/**
 * POJO-klasse som representerer en Person (Passasjer eller Sjåfør)
 */
public class Person {

    private int id;
    private String navn;
    private String epost;
    private String tlf;

    /**
     * Konstruktør uten id.
     */
    public Person(String navn, String epost, String tlf) {
        this.navn = navn;
        this.epost = epost;
        this.tlf = tlf;
    }

    /**
     * Konstruktør med id
     */
    public Person(int id, String navn, String epost, String tlf) {
        this(navn, epost, tlf);
        this.id = id;
    }

    /* --- GETTER OG SETTER -- */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public String getEpost() {
        return epost;
    }

    public void setEpost(String epost) {
        this.epost = epost;
    }

    public String getTlf() {
        return tlf;
    }

    public void setTlf(String tlf) {
        this.tlf = tlf;
    }
}
