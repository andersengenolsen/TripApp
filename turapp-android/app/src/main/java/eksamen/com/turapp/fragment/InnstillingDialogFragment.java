package eksamen.com.turapp.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import eksamen.com.turapp.R;
import eksamen.com.turapp.activites.BaseActivity;
import eksamen.com.turapp.listener.InnstillingDialogListener;
import eksamen.com.turapp.model.Person;

/**
 * Dialog som vises dersom brukeren trykker på settings-ikonet i Toolbar (OptionsMenu).
 * <p>
 * Dialogen har en custom layout, denne er definert i dialog_innstillinger.xml
 * <p>
 * Aktiviteter som skal benytte dette fragmentet, må implementere InnstillingDialogListener.
 * <p>
 * Verdien i feltene kan være forhådnsutfylte, dersom det er tilsendt informasjon via Bundle.
 *
 * @author Kandidatnummer 9
 */
public class InnstillingDialogFragment extends DialogFragment implements View.OnClickListener {

    /**
     * Callback-interface
     */
    private InnstillingDialogListener listener;

    /**
     * Layout-elementer
     */
    private EditText etNavn, etNummer, etEpost;

    /**
     * Sjekker at aktivitet som benytter fragmentet har implementert InnstillingDialogListener.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            listener = (InnstillingDialogListener) getActivity();
        } catch (ClassCastException err) {
            throw new ClassCastException(getActivity().getClass().getSimpleName()
                    + " må implementere InnstillingDialogListener!");
        }
    }

    /**
     * Setter opp layout, og setter onClickListener til knapper.
     *
     * @param savedInstanceState
     * @return
     * @see #onClick(View)
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Inflater layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_innstillinger, null);

        // EditText
        etNavn = layout.findViewById(R.id.navn);
        etNummer = layout.findViewById(R.id.mobil_nummer);
        etEpost = layout.findViewById(R.id.e_post);

        // Mottar bundle, hvis tilsendt.
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            String navn = bundle.getString(BaseActivity.PERSON_NAVN_KEY);
            String epost = bundle.getString(BaseActivity.PERSON_EPOST_KEY);
            String tlf = bundle.getString(BaseActivity.PERSON_MOBIL_KEY);

            etNavn.setText(navn);
            etEpost.setText(epost);
            etNummer.setText(tlf);
        }

        // Buttons
        Button positivBtn = layout.findViewById(R.id.ok_btn);
        Button negativBtn = layout.findViewById(R.id.avbryt_btn);

        // Bruker DialogBuilder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        positivBtn.setOnClickListener(this);
        negativBtn.setOnClickListener(this);

        builder.setView(layout);

        return builder.create();
    }

    /**
     * Fyrer av interface, sender oppdaterte verdier tilbake til kallende aktivitet.
     *
     * @param v
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.ok_btn:

                if (sjekkVerdier()) {
                    Person person = new Person(etNavn.getText().toString(),
                            etEpost.getText().toString(), etNummer.getText().toString());
                    listener.dialogPositivKlikk(person);
                    dismiss();
                } else
                    Toast.makeText(getActivity(), R.string.ugyldige_verdier,
                            Toast.LENGTH_SHORT).show();

                break;
            case R.id.avbryt_btn:
                dismiss();
                break;
        }
    }

    /**
     * Setter verdier fra person-objekt i edittexts, slik at de er forhåndsutfylte.
     *
     * @param person personobjekt
     */
    public void setPerson(Person person) {
        etNavn.setText(person.getNavn());
        etNummer.setText(person.getTlf());
        etEpost.setText(person.getEpost());
    }

    /**
     * Sjekker at gyldige verdier er tastet inn i "navn" og "mobilnummer" edittext.
     */
    private boolean sjekkVerdier() {
        String navn = etNavn.getText().toString().trim();
        String nummer = etNummer.getText().toString().trim();
        String ePost = etEpost.getText().toString().trim();

        return nummer.length() == 8 && navn.length() >= 2 && ePost.length() >= 5;
    }
}
