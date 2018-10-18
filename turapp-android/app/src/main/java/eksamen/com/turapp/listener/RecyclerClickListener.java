package eksamen.com.turapp.listener;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import eksamen.com.turapp.adapter.RecyclerAdapter;


/**
 * Custom Click Listener for Recycler Views.
 * Kunne også implementert ClickListener i hver eneste Adapter, men får en mer universell løsning
 * her.
 * <p>
 *
 * @author Kandidatnummer 9
 * @link http://sapandiwakar.in/recycler-view-item-click-handler/
 */
public class RecyclerClickListener implements RecyclerView.OnItemTouchListener {

    //Interface for click listener
    public interface ElementKlikketListener {
        void elementKlikket(RecyclerAdapter adapter, int posisjon);

    }

    // Listener-interfacet
    private ElementKlikketListener listener;

    private GestureDetector gestureDetector;

    /**
     * Konstruktør, tar inn RecyclerView listener skal settes til
     *
     * @param c        kontekst
     * @param listener Custom listener som er definert ovenfor.
     */
    public RecyclerClickListener(Context c, ElementKlikketListener listener) {

        this.listener = listener;
        gestureDetector = new GestureDetector(c, new GestureDetector.SimpleOnGestureListener() {

            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && listener != null && gestureDetector.onTouchEvent(e)) {
            listener.elementKlikket((RecyclerAdapter) view.getAdapter(),
                    view.getChildAdapterPosition(childView));
            //listener.elementKlikket(childView, view.getChildAdapterPosition(childView));
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
}
