package dev.michaeljung.clojurereframe;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.UUID;

public abstract class CljsFragment extends Fragment {
    private ArrayList<String> listenerIds;

    CljsFragment() {
        listenerIds = new ArrayList<>();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (String id : listenerIds) {
            CljsApplication app = (CljsApplication) getActivity().getApplicationContext();
            app.unsubscribe(id);
        }
    }

    protected void doWhenReady(Runnable runnable) {
        CljsApplication app = (CljsApplication) getActivity().getApplicationContext();
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
        CljsApplication app = (CljsApplication) getActivity().getApplicationContext();
        app.subscribe(listenerId, query, listener);
        listenerIds.add(listenerId);
        return listenerId;
    }

    protected void dispatch(String event) {
        dispatch(toJsonArray(event));
    }

    protected void dispatch(JSONArray event) {
        CljsApplication app = (CljsApplication) getActivity().getApplicationContext();
        app.dispatch(event);
    }
}
