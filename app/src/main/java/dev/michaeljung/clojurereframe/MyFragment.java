package dev.michaeljung.clojurereframe;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.UUID;

public abstract class MyFragment extends Fragment {
    private ArrayList<String> listenerIds;

    protected MyFragment() {
        listenerIds = new ArrayList<>();
    }

    protected void doWhenReady(Runnable runnable) {
        Application app = (Application) getActivity().getApplicationContext();
        app.doWhenReady(runnable);
    }

    protected String subscribe(String query, EventListener listener) {
        String listenerId = UUID.randomUUID().toString();
        Application app = (Application) getActivity().getApplicationContext();
        app.subscribe(listenerId, query, listener);
        listenerIds.add(listenerId);
        return listenerId;
    }

    protected void dispatch(String event) {
        Application app = (Application) getActivity().getApplicationContext();
        app.dispatch(event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for (String id : listenerIds) {
            Application app = (Application) getActivity().getApplicationContext();
            app.unsubscribe(id);
        }
    }
}
