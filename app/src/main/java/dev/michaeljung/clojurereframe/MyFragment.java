package dev.michaeljung.clojurereframe;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.liquidplayer.javascript.JSON;

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
        return subscribe(toJsonArray(query), listener);
    }

    private JSONArray toJsonArray(String query) {
        JSONArray array = new JSONArray();
        array.put(query);
        return array;
    }

    protected String subscribe(JSONArray query, EventListener listener) {
        String listenerId = UUID.randomUUID().toString();
        Application app = (Application) getActivity().getApplicationContext();
        app.subscribe(listenerId, query, listener);
        listenerIds.add(listenerId);
        return listenerId;
    }

    protected void dispatch(String event) {
        dispatch(toJsonArray(event));
    }

    protected void dispatch(JSONArray event) {
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
