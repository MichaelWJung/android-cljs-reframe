package dev.michaeljung.clojurereframe;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.UUID;

public abstract class CljsFragment extends Fragment {
    private ArrayList<String> listenerIds;
    private CljsApplication application;

    CljsFragment() {
        listenerIds = new ArrayList<>();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        application = (CljsApplication) context.getApplicationContext();
    }

    @Override
    public void onDestroy() {
        for (String id : listenerIds) {
            application.unsubscribe(id);
        }
        super.onDestroy();
    }

    protected void doWhenReady(Runnable runnable) {
        application.doWhenReady(runnable);
    }

    protected String subscribe(String query, EventListener listener) {
        return subscribe(toJsonArray(query), listener);
    }

    protected String subscribe(JSONArray query, EventListener listener) {
        String listenerId = UUID.randomUUID().toString();
        application.subscribe(listenerId, query, listener);
        listenerIds.add(listenerId);
        return listenerId;
    }

    protected void dispatch(String event) {
        dispatch(toJsonArray(event));
    }

    protected void dispatch(JSONArray event) {
        application.dispatch(event);
    }

    private JSONArray toJsonArray(String query) {
        JSONArray array = new JSONArray();
        array.put(query);
        return array;
    }
}
