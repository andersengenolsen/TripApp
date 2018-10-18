package eksamen.com.turapp.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import eksamen.com.turapp.R;
import eksamen.com.turapp.adapter.RecyclerAdapter;
import eksamen.com.turapp.listener.RecyclerClickListener;
import eksamen.com.turapp.model.Tur;

/**
 * RecyclerList som viser en liste av turer.
 *
 * @author Kandidatnummer 9
 */
public class RecyclerListeTurer extends Fragment
        implements RecyclerClickListener.ElementKlikketListener {

    /**
     * Interface som må implementeres i aktiviteter. Avfyres når et element i listen klikkes.
     */
    public interface RecyclerListeListener {
        void listeElementKlikket(Tur tur);
    }

    /**
     * Interface
     */
    private RecyclerListeListener listener;

    /**
     * Adapter
     */
    private RecyclerAdapter adapter;

    /**
     * Listen med turer
     */
    private ArrayList<Tur> turer;

    /**
     * I tillegg til å kalle super, sjekkes det at aktiviteten som "eier" fragmentet implementerer
     * interfacet OnItemClickedListener.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sjekk av implementasjon
        try {
            listener = (RecyclerListeListener) getActivity();
        } catch (ClassCastException err) {
            throw new ClassCastException("Kallende aktivitet må" +
                    " implementere RecyclerListeListener!");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rot = inflater.inflate(R.layout.fragment_liste, container, false);
        if (turer == null)
            turer = new ArrayList<>();
        lagRecyclers(rot);

        return rot;
    }

    /**
     * Implementasjon av interface RecyclerClickListener.ElementKlikketListener.
     *
     * @param adapter  RecyclerAdapter
     * @param posisjon posisjon i liste
     * @see RecyclerClickListener.ElementKlikketListener
     */
    @Override
    public void elementKlikket(RecyclerAdapter adapter, int posisjon) {
        listener.listeElementKlikket(adapter.getElement(posisjon));
    }

    /**
     * Setter turer som skal vises i listen.
     *
     * @param turer
     */
    public void setTurer(List<Tur> turer) {
        this.turer = (ArrayList<Tur>) turer;

        if (adapter != null) {
            adapter.settListe(turer);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Fjerner en tur fra listen
     */
    public void fjernTur(Tur tur) {
        turer.remove(tur);

        adapter.settListe(turer);
        adapter.notifyDataSetChanged();
    }

    /**
     * Initierer RecyclerViews
     *
     * @param v rot
     */
    private void lagRecyclers(View v) {
        adapter = new RecyclerAdapter(getContext(), turer);

        RecyclerView rView = v.findViewById(R.id.recycler_list);

        rView.addOnItemTouchListener(new RecyclerClickListener(getActivity(), this));

        rView.setAdapter(adapter);

        // Setter LayoutManager
        rView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }
}
