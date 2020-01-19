package dev.michaeljung.clojurereframe;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.liquidplayer.service.MicroService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Application extends android.app.Application {

    private static final String LOG_TAG = Application.class.getSimpleName();

    private MicroService clojure;
    private boolean clojure_ready;

    private ArrayList<Subscription> unregistered_subscriptions;
    private ArrayList<Runnable> waiting_ready_listeners;

    private class Subscription {
        private final String id;
        private final String query;
        private final EventListener listener;

        private Subscription(String id, String query, EventListener listener) {
            this.id = id;
            this.query = query;
            this.listener = listener;
        }
    }

    @Override
    public synchronized void onCreate() {
        super.onCreate();

        unregistered_subscriptions = new ArrayList<>();
        waiting_ready_listeners = new ArrayList<>();

        final MicroService.EventListener readyListener = new MicroService.EventListener() {
            @Override
            public void onEvent(MicroService service, String event, JSONObject payload) {
                synchronized (Application.this) {
                    Application.this.clojure_ready = true;

                    Log.v(LOG_TAG, "Running waiting runnables on main thread");
                    for (Runnable r : waiting_ready_listeners) {
                        new Handler(Looper.getMainLooper()).post(r);
                    }
                    waiting_ready_listeners.clear();

                    Log.v(LOG_TAG, "Registering pending subscriptions");
                    for (Subscription s : unregistered_subscriptions) {
                        doSubscribe(s.id, s.query, s.listener);
                    }
                    unregistered_subscriptions.clear();
                }
            }
        };

        final MicroService.ServiceStartListener startListener = new MicroService.ServiceStartListener() {
            @Override
            public void onStart(MicroService service) {
                clojure.addEventListener("ready", readyListener);
            }
        };

        URI uri = null;
        try {
            uri = new URI("android.resource://dev.michaeljung.clojurereframe/raw/app");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        clojure = new MicroService(this, uri, startListener);
        Log.d(LOG_TAG, "Starting micro service");
        clojure.start();
    }

    void doWhenReady(final Runnable runnable) {
        if (clojure_ready) {
            Log.v(LOG_TAG, "Running runnable right away");
            runnable.run();
        } else synchronized (this) {
            Log.v(LOG_TAG, "Putting runnable on waiting ready listeners list");
            waiting_ready_listeners.add(runnable);
        }

    }

    void dispatch(String event) {
        JSONArray array = new JSONArray();
        array.put(event);
        dispatch(array);
    }

    void dispatch(JSONArray event) {
        clojure.emit("dispatch", event);
    }

    void subscribe(String id, String query, EventListener listener) {
        if (clojure_ready) {
            doSubscribe(id, query, listener);
        } else synchronized (this) {
            Log.v(LOG_TAG, "Add subscription to list of pending subscriptions");
            unregistered_subscriptions.add(new Subscription(id, query, listener));
        }
    }

    void unsubscribe(String id) {
        // TODO: if not ready and still in unregistered_subscriptions: remove from there
        if (clojure_ready) {
            clojure.emit("deregister", id);
        }
    }

    private void doSubscribe(String id, String query, final EventListener listener) {
        Log.v(LOG_TAG, "Do subscription for: " + id + ". Query: " + query);

        final MicroService.EventListener liquidcore_listener = new MicroService.EventListener() {
            @Override
            public void onEvent(MicroService service, final String event, final JSONObject payload) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onEvent(event, payload);
                    }
                });
            }
        };
        clojure.addEventListener(id, liquidcore_listener);

        JSONObject payload = new JSONObject();
        try {
            payload.put("id", id);
            JSONArray array = new JSONArray();
            array.put(query);
            payload.put("query", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        clojure.emit("register", payload);
    }
}
