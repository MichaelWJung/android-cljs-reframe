package dev.michaeljung.clojurereframe;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.liquidplayer.service.MicroService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

public class Application extends android.app.Application {

    private static final String LOG_TAG = Application.class.getSimpleName();
    private static final String DB_FILE_NAME = "db";

    private MicroService clojure;
    private boolean clojure_ready;

    private ArrayList<Subscription> unregistered_subscriptions;
    private ArrayList<Runnable> waiting_ready_listeners;

    private class Subscription {
        private final String id;
        private final JSONArray query;
        private final EventListener listener;

        private Subscription(String id, JSONArray query, EventListener listener) {
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

        final MicroService.EventListener readDbListener = new MicroService.EventListener() {
            @Override
            public void onEvent(MicroService service, String event, JSONObject payload) {
                initializeDbFromFile();
            }
        };
        final MicroService.EventListener storeListener = new MicroService.EventListener() {
            @Override
            public void onEvent(MicroService service, String event, JSONObject payload) {
                saveDbToFile(payload);
            }
        };
        final MicroService.EventListener readyListener = new MicroService.EventListener() {
            @Override
            public void onEvent(MicroService service, String event, JSONObject payload) {
                carryOutReadyActions();
            }
        };

        final MicroService.ServiceStartListener startListener = new MicroService.ServiceStartListener() {
            @Override
            public void onStart(MicroService service) {
                clojure.addEventListener("waiting-for-db", readDbListener);
                clojure.addEventListener("store", storeListener);
                clojure.addEventListener("ready", readyListener);
            }
        };

        startClojureMicroService(startListener);
    }

    synchronized void doWhenReady(final Runnable runnable) {
        if (clojure_ready) {
            Log.v(LOG_TAG, "Running runnable right away");
            runnable.run();
        } else {
            Log.v(LOG_TAG, "Putting runnable on waiting ready listeners list");
            waiting_ready_listeners.add(runnable);
        }
    }

    void dispatch(JSONArray event) {
        // TODO: clojure might not be ready
        clojure.emit("dispatch", event);
    }

    synchronized void subscribe(String id, JSONArray query, EventListener listener) {
        if (clojure_ready) {
            doSubscribe(id, query, listener);
        } else {
            Log.v(LOG_TAG, "Add subscription to list of pending subscriptions");
            unregistered_subscriptions.add(new Subscription(id, query, listener));
        }
    }

    synchronized void unsubscribe(String id) {
        if (clojure_ready) {
            clojure.emit("deregister", id);
        } else {
            // TODO: remove from unregistered_subscriptions
        }
    }

    private void startClojureMicroService(MicroService.ServiceStartListener startListener) {
        URI uri = getCompiledClojureScriptCodeUri();
        clojure = new MicroService(this, uri, startListener);
        Log.d(LOG_TAG, "Starting micro service");
        clojure.start();
    }

    private URI getCompiledClojureScriptCodeUri() {
        try {
            return new URI("android.resource://dev.michaeljung.clojurereframe/raw/app");
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error loading ClojureScript code");
        }
    }

    private synchronized void carryOutReadyActions() {
        Application.this.clojure_ready = true;
        runWaitingReadyListeners();
        registerPendingSubscriptions();
    }

    private void registerPendingSubscriptions() {
        Log.v(LOG_TAG, "Registering pending subscriptions");
        for (Subscription s : unregistered_subscriptions) {
            doSubscribe(s.id, s.query, s.listener);
        }
        unregistered_subscriptions.clear();
    }

    private void runWaitingReadyListeners() {
        Log.v(LOG_TAG, "Running waiting runnables on main thread");
        for (Runnable r : waiting_ready_listeners) {
            new Handler(Looper.getMainLooper()).post(r);
        }
        waiting_ready_listeners.clear();
    }

    private void doSubscribe(String id, JSONArray query, final EventListener listener) {
        Log.v(LOG_TAG, "Do subscription for: " + id + ". Query: " + query);

        final MicroService.EventListener liquidcore_listener = new MicroService.EventListener() {
            @Override
            public void onEvent(MicroService service, final String event, final JSONObject payload) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onEvent(payload);
                    }
                });
            }
        };
        clojure.addEventListener(id, liquidcore_listener);

        JSONObject payload = new JSONObject();
        try {
            payload.put("id", id);
            payload.put("query", query);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        clojure.emit("register", payload);
    }

    private void initializeDbFromFile() {
        String content = readDbFile();
        if (content == null) {
            content = "{}";
        }
        clojure.emit("initialize", content);
    }

    private void saveDbToFile(JSONObject db) {
        try {
            writeDbFile(db.getString("value"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String readDbFile() {
        File file = new File(this.getFilesDir(), DB_FILE_NAME);
        try {
            byte[] encoded = Files.readAllBytes(file.toPath());
            String content = new String(encoded, StandardCharsets.UTF_8);
            Log.v(LOG_TAG, "File db successfully loaded. Content: " + content);
            return content;
        } catch (IOException e) {
            return null;
        }
    }

    private void writeDbFile(String content) {
        try {
            FileOutputStream fos = Application.this.openFileOutput(DB_FILE_NAME, Context.MODE_PRIVATE);
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            Log.v(LOG_TAG, "Saved todos to file. Content: " + content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
