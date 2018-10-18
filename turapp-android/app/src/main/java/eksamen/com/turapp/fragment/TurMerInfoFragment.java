package eksamen.com.turapp.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import eksamen.com.turapp.R;
import eksamen.com.turapp.listener.TurMerInfoListener;
import eksamen.com.turapp.model.Person;
import eksamen.com.turapp.model.Tur;

/**
 * Fragment som viser mer informasjon om en tur, altså passasjer-liste i tillegg.
 * Vises når det trykkes på et element i tur-liste (RecyclerListeTurer).
 * <p>
 * Kommunikasjon til aktivitet gjøres med TurMerInfoListener.
 * <p>
 * OBS! #setTur og #setEier må kalles før Dialogen skal vises!
 *
 * @author Kandidatnummer 9
 * @see RecyclerListeTurer
 * @see TurMerInfoListener
 */
public class TurMerInfoFragment extends DialogFragment implements View.OnClickListener {

    /**
     * Callback
     */
    private TurMerInfoListener listener;

    /**
     * Turen det skal vises informasjon om.
     */
    private Tur tur;

    /**
     * Flagg, om man er sjåfør for turen eller ei.
     * Er man sjafør kan man kansellere hele turen, er man passasjer melder man seg av.
     */
    private Boolean erEier = false;

    /**
     * Flagg, om man er passasjer eller ei.
     */
    private Boolean erPassasjer = false;

    /**
     * Sjekker at aktivitet som benytter fragmentet har implementert TurMerInfoListener.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            listener = (TurMerInfoListener) getActivity();
        } catch (ClassCastException err) {
            throw new ClassCastException(getActivity().getClass().getSimpleName()
                    + " må implementere TurMerInfoListener!");
        }
    }

    /**
     * Setter tur det skal vises mer informasjon om
     *
     * @param tur tur
     */
    public void setTur(Tur tur) {
        this.tur = tur;
    }

    /**
     * Setter om man er sjåfør eller ei
     *
     * @param erEier true hvis sjåfør
     */
    public void setErEier(boolean erEier) {
        this.erEier = erEier;
    }

    /**
     * Setter om man er passasjer eller ei
     *
     * @param erPassasjer true hvis passasjer
     */
    public void erPassasjer(boolean erPassasjer) {
        this.erPassasjer = erPassasjer;
    }

    /**
     * Lager dialog med layout-elementene.
     * <p>
     * Tekst i button avgjøres av om man er sjåfør eller ei.
     *
     * @param savedInstanceState
     * @return dialog
     * @throws RuntimeException Dersom tur eller erEier ikke er satt
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Inflater layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_tur_mer_info, null);

        // Views
        Button knapp = layout.findViewById(R.id.tur_knapp);
        TextView sjafor = layout.findViewById(R.id.sjafor_person);
        TextView passasjerer = layout.findViewById(R.id.passaser_liste);
        knapp.setOnClickListener(this);

        // Tur / eier må være satt!
        if (tur == null)
            throw new RuntimeException("Har du husket å kalle #setTur(Tur)?");

        // Sjekker om bruker er eier av turen. Setter tekst deretter.
        if (erEier)
            knapp.setText(R.string.avlys);
        else if (erPassasjer)
            knapp.setText(R.string.meld_av);
        else
            knapp.setText(R.string.meld_på);

        sjafor.setText(tur.getSjafor().getNavn());

        // Passasjerliste
        StringBuilder bygger = new StringBuilder();
        ArrayList<Person> passasjerListe = tur.getPassasjerer();
        for (Person p : passasjerListe)
            bygger.append(p.getNavn()).append("\n");
        passasjerer.setText(bygger.toString());

        // Bruker DialogBuilder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(layout);
        return builder.create();
    }

    /**
     * Håndterer klikk på knappen. Enten avlyses tur, eller så melder man seg av.
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tur_knapp:
                if (erEier)
                    listener.avlysTur(tur.getId());
                else if (erPassasjer)
                    listener.meldAvTur(tur);
                else
                    listener.meldPåTur(tur);
                dismiss();
                break;
        }
    }
}
