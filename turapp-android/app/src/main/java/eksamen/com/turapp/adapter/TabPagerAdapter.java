package eksamen.com.turapp.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * PagerAdapter som håndterer Tabs i en Activity.
 * Tabs'ene er fragmenter.
 *
 * @author Kandidatnummer 9
 */
public class TabPagerAdapter extends FragmentStatePagerAdapter {

    // Antall tabs
    private int antallTabs;

    // Tabs
    private Fragment[] tabs;

    /**
     * Konstruktør for MainPagerAdapter. Tar inn et FragmentManager-objekt,
     * samt antall tabs som skal håndteres.
     *
     * @param manager fragmentmanager
     */
    public TabPagerAdapter(FragmentManager manager, Fragment... tabs) {
        super(manager);
        this.antallTabs = tabs.length;
        this.tabs = tabs;
    }

    /**
     * Metode som returnerer et fragment, basert på hvilken tab bruker har trykket på.
     *
     * @param position Posisjon til fragmentet
     * @return fragment i gitt posisjon, eller null.
     */
    @Override
    public Fragment getItem(int position) {
        if (position > tabs.length - 1)
            throw new IllegalArgumentException("Ugyldig posisjon!");

        return tabs[position];
    }

    /**
     * @return antall tabs
     */
    @Override
    public int getCount() {
        return antallTabs;
    }
}
