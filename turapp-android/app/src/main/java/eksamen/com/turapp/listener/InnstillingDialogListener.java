package eksamen.com.turapp.listener;


import eksamen.com.turapp.fragment.InnstillingDialogFragment;
import eksamen.com.turapp.model.Person;

/**
 * Interface som muliggjør kommunikasjon mellom dialog og aktivitet.
 * Interfacet benyttes i InnstillingDialogFragment.
 *
 * @author Kandidatnummer 9
 * @see InnstillingDialogFragment
 */
public interface InnstillingDialogListener {

    void dialogPositivKlikk(Person person);

}
