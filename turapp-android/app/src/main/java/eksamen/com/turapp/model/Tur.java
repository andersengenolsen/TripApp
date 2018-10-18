package eksamen.com.turapp.model;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;

/**
 * POJO-klasse som representerer en tur.
 * <p>
 * Bygget etter Builder-pattern, da det er mange felter (og noen kan mangle).
 *
 * @author Kandidatnummer 9
 */
public class Tur {

    /**
     * Benyttet til å formatere output for LocalDateTime
     * format: yyyy-mm-ddThh:mm
     */
    private final DateTimeFormatter formatter = ISODateTimeFormat.dateHourMinute();


    private int id;
    private Person sjafor;
    private Sted fra;
    private Sted til;
    private LocalDateTime avreiseTid;
    private int ledigePlasser;
    private ArrayList<Person> passasjerer;

    private Tur(TurBuilder builder) {
        this.id = builder.id;
        this.fra = builder.fra;
        this.til = builder.til;
        this.avreiseTid = builder.avreiseTid;
        this.ledigePlasser = builder.ledigePlasser;
        this.passasjerer = builder.passasjerer;
        this.sjafor = builder.sjafor;
    }

    /**
     * Returnerer string på format yyyy-mm-ddThh:mm
     */
    public String getAvreiseTidString() {
        return formatter.print(avreiseTid);
    }

    /* -- GETTERS OG SETTERS -- */

    public int getId() {
        return id;
    }

    public Sted getTil() {
        return til;
    }

    public Sted getFra() {
        return fra;
    }

    public int getLedigePlasser() {
        return ledigePlasser;
    }

    public ArrayList<Person> getPassasjerer() {
        if (passasjerer == null)
            passasjerer = new ArrayList<>();
        return passasjerer;
    }

    public Person getSjafor() {
        return sjafor;
    }

    public int getSjaforId() {
        return sjafor.getId();
    }

    public LocalDateTime getAvreiseTid() {
        return avreiseTid;
    }

    public void setAvreiseTid(LocalDateTime avreiseTid) {
        this.avreiseTid = avreiseTid;
    }

    public void setPassasjerer(ArrayList<Person> passasjerer) {
        this.passasjerer = passasjerer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        Tur tur = (Tur) obj;

        return tur.getId() == getId();
    }

    /**
     * Builder for tur-objekter.
     * Benyttes da det blir en uendelig lang konstruktør uten denne,
     * og ikke alle elementer er påkrevd.
     */
    public static class TurBuilder {

        private int id;
        private Person sjafor;
        private Sted fra;
        private Sted til;
        private LocalDateTime avreiseTid;
        private int ledigePlasser;
        private ArrayList<Person> passasjerer;

        public TurBuilder id(int id) {
            this.id = id;
            return this;
        }

        public TurBuilder fra(Sted fra) {
            this.fra = fra;
            return this;
        }

        public TurBuilder til(Sted til) {
            this.til = til;
            return this;
        }

        public TurBuilder avreiseTid(LocalDateTime avreiseTid) {
            this.avreiseTid = avreiseTid;
            return this;
        }

        public TurBuilder ledigePlasser(int ledigePlasser) {
            this.ledigePlasser = ledigePlasser;
            return this;
        }

        public TurBuilder passasjerer(ArrayList<Person> passasjerer) {
            this.passasjerer = passasjerer;
            return this;
        }

        public TurBuilder sjafor(Person sjafor) {
            this.sjafor = sjafor;
            return this;
        }

        /**
         * Initierer en Tur.
         *
         * @return Tur
         */
        public Tur build() {
            return new Tur(this);
        }
    }
}
