package eksamen.com.turapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import eksamen.com.turapp.R;
import eksamen.com.turapp.model.Tur;

/**
 * Custom adapter for RecyclerView, med indre klasse for ViewHolder i Adapteren.
 * <p>
 * RecyclerView'en blir benyttet i når det skal vises en liste med turer.
 * <p>
 * KILDE: https://developer.android.com/guide/topics/ui/layout/recyclerview#java
 *
 * @author Kandidatnummer 9
 */
public class RecyclerAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * Kontekst fra aktivitet
     */
    private Context context;
    /**
     * Liste med turer.
     */
    private List<Tur> content;

    /**
     * Konstruktør, tar inn kontekst samt liste med turer.
     *
     * @param context
     * @param content
     */
    public RecyclerAdapter(Context context, List<Tur> content) {
        this.context = context;
        this.content = content;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflater layout for recycler-view
        View view = inflater.inflate(R.layout.recycler_list_turer, parent, false);

        // Returnerer holder.
        return new RecyclerListHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        RecyclerListHolder recyclerListHolder = (RecyclerListHolder) holder;

        // Henter ut tur-objekt
        Tur tur = content.get(position);

        // Setter verdier
        recyclerListHolder.avreiseSted.setText(tur.getFra().getGateAdresse());
        recyclerListHolder.avreiseTid.setText(tur.getAvreiseTidString());
        recyclerListHolder.destinasjon.setText(tur.getTil().getGateAdresse());
        recyclerListHolder.ledigePlasser.setText(String.valueOf(tur.getLedigePlasser()));
    }

    /**
     * Returnerer totalt antall objekter i adapteren.
     *
     * @return int antall objekter i adapter.
     */
    @Override
    public int getItemCount() {
        return (null != content ? content.size() : 0);
    }

    /**
     * Returnerer turobjekt i gitt posisjon
     *
     * @param pos posisjon i adapter
     * @return Tur, null hvis posisjon ikke i array
     */
    public Tur getElement(int pos) {
        return (null != content ? content.get(pos) : null);
    }

    /**
     * Setter turobjekter
     *
     * @param content turobjekt som skal vises
     */
    public void settListe(List<Tur> content) {
        this.content = content;
    }

    /**
     * Indre klasse, ViewHolder for elementene i RecyclerView'en
     */
    private class RecyclerListHolder extends RecyclerView.ViewHolder {

        private TextView avreiseSted;
        private TextView avreiseTid;
        private TextView destinasjon;
        private TextView ledigePlasser;

        /**
         * Konstruktør, initierer Views i Holderen.
         *
         * @param view Root
         */
        private RecyclerListHolder(View view) {
            super(view);

            // Initierer views.
            avreiseSted = view.findViewById(R.id.avreise_adr);
            avreiseTid = view.findViewById(R.id.avreise_tid);
            destinasjon = view.findViewById(R.id.destinasjon_adr);
            ledigePlasser = view.findViewById(R.id.ledige_plasser);
        }
    }
}
