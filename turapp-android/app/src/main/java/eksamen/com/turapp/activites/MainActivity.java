package eksamen.com.turapp.activites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

import eksamen.com.turapp.R;

/**
 * Aktiviteten inneholder 4 Buttons, som starter nye aktiviteter.
 * Dette er launcher activity, altså første aktivitet som vises når app starter.
 * <p>
 * Benytter leggTilLayoutView for å sette layout, som er implementert i superklassen BaseActivity.
 * <p>
 * Ingen av knappene kan trykkes på før bruker har sett info i innstillinger.
 *
 * @author Kandidatnummer 9
 * @see BaseActivity#leggTilLayoutView(int)
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Definert i super
        leggTilLayoutView(R.layout.activity_main);

        settKlikkListener();
    }

    @Override
    public void onClick(View v) {
        if (hentBrukerId() == -1) {
            visToast(getString(R.string.trenger_id));
            return;
        }
        switch (v.getId()) {
            case R.id.sok_turer_btn:
                startSokTurerActivity();
                break;
            case R.id.mine_turer_btn:
                startMineTurerActivity();
                break;
            case R.id.mine_steder_btn:
                startMineStederActivity();
                break;
            case R.id.ny_tur_btn:
                startNyTurActivity();
                break;
        }
    }

    /**
     * Finner alle knapper i layout, og setter click-listener til knappene
     */
    private void settKlikkListener() {
        ArrayList<View> knapper = (findViewById(R.id.main_container)).getTouchables();

        for (View v : knapper)
            v.setOnClickListener(this);
    }

    /**
     * @see SokTurerActivity
     */
    private void startSokTurerActivity() {
        Intent intent = new Intent(this, SokTurerActivity.class);
        startActivity(intent);
    }

    /**
     * @see MineTurerActivity
     */
    private void startMineTurerActivity() {
        Intent intent = new Intent(this, MineTurerActivity.class);
        startActivity(intent);
    }

    /**
     * @see MineStederActivity
     */
    private void startMineStederActivity() {
        Intent intent = new Intent(this, MineStederActivity.class);
        startActivity(intent);
    }

    /**
     * @see NyTurActivity
     */
    private void startNyTurActivity() {
        Intent intent = new Intent(this, NyTurActivity.class);
        startActivity(intent);
    }
}
